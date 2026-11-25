package com.leochat.feature.media.presentation.picker

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.leochat.core.common.constants.MediaStrings
import com.leochat.feature.media.domain.model.MaxUploadBytes
import com.leochat.feature.media.domain.model.PickedAttachment
import com.leochat.feature.media.domain.model.mimeTypeForFileName

@Composable
actual fun rememberAttachmentPicker(
    onPicked: (PickedAttachment) -> Unit,
    onError: (String) -> Unit,
): AttachmentPickerController {
    val context = LocalContext.current

    fun handleUri(uri: Uri?, defaultImage: Boolean) {
        if (uri == null) return
        val resolved = resolveUriMetadata(context, uri, defaultImage)
        if (resolved == null) {
            onError(MediaStrings.fileNotFound)
            return
        }
        if (resolved.sizeBytes > MaxUploadBytes) {
            onError(MediaStrings.fileTooLarge)
            return
        }
        onPicked(resolved)
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        handleUri(uri, defaultImage = true)
    }
    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        handleUri(uri, defaultImage = false)
    }

    return remember(imageLauncher, documentLauncher) {
        object : AttachmentPickerController {
            override fun pickImage() {
                runCatching {
                    imageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }.onFailure {
                    // Fallback to system document picker scoped to images if PhotoPicker is unavailable.
                    runCatching { documentLauncher.launch(arrayOf("image/*")) }
                }
            }

            override fun pickFile() {
                documentLauncher.launch(arrayOf("*/*"))
            }
        }
    }
}

private fun resolveUriMetadata(context: Context, uri: Uri, defaultImage: Boolean): PickedAttachment? {
    val resolver = context.contentResolver
    var displayName: String? = null
    var sizeBytes = 0L

    runCatching {
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIdx >= 0) displayName = cursor.getString(nameIdx)
                if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) sizeBytes = cursor.getLong(sizeIdx)
            }
        }
    }

    if (sizeBytes <= 0L) {
        sizeBytes = runCatching {
            resolver.openAssetFileDescriptor(uri, "r")?.use { it.length.takeIf { len -> len >= 0L } }
        }.getOrNull() ?: 0L
    }

    val fallbackName = if (defaultImage) "image_${System.currentTimeMillis()}.jpg" else "attachment_${System.currentTimeMillis()}"
    val fileName = displayName?.takeIf { it.isNotBlank() } ?: uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() } ?: fallbackName
    val detectedMime = resolver.getType(uri)?.takeIf { it.isNotBlank() }
        ?: mimeTypeForFileName(fileName, if (defaultImage) "image/jpeg" else "application/octet-stream")

    return PickedAttachment(
        uriOrPath = uri.toString(),
        fileName = fileName,
        mimeType = detectedMime,
        sizeBytes = sizeBytes,
        isImage = detectedMime.startsWith("image/", ignoreCase = true) || defaultImage,
    )
}
