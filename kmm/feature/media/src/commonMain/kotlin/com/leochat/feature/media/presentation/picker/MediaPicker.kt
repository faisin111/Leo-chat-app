package com.leochat.feature.media.presentation.picker

import androidx.compose.runtime.Composable
import com.leochat.feature.media.domain.model.PickedAttachment

/** Cross-platform launcher for picking a photo or any document/file. */
interface AttachmentPickerController {
    fun pickImage()
    fun pickFile()
}

@Composable
expect fun rememberAttachmentPicker(
    onPicked: (PickedAttachment) -> Unit,
    onError: (String) -> Unit,
): AttachmentPickerController
