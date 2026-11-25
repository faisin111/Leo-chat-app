package com.leochat.feature.media.data.device

import android.content.Context
import android.net.Uri
import com.leochat.feature.media.data.remote.MediaFileReader
import com.leochat.feature.media.data.remote.SystemMediaFileReader

/**
 * Reads bytes for both local filesystem paths (voice recordings) and Android
 * `content://` / `file://` URIs returned by photo/document pickers.
 */
class AndroidMediaFileReader(private val context: Context) : MediaFileReader {
    private val fallback = SystemMediaFileReader()

    override fun read(path: String): ByteArray? {
        if (path.startsWith("content://") || path.startsWith("file://")) {
            return runCatching {
                context.contentResolver.openInputStream(Uri.parse(path))?.use { it.readBytes() }
            }.getOrNull()
        }
        return fallback.read(path)
    }
}
