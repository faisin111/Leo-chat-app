package com.leochat.feature.media.data.remote

import com.leochat.feature.media.domain.model.MediaUploadTarget
import kotlinx.serialization.Serializable

@Serializable
data class PresignRequestDto(val mimeType: String, val sizeInBytes: Long)

@Serializable
data class PresignResponseDto(
    val uploadUrl: String? = null,
    val storageKey: String? = null,
    val contentType: String? = null,
) {
    fun toDomain(fallbackContentType: String): MediaUploadTarget? {
        val url = uploadUrl?.takeIf { it.isNotBlank() } ?: return null
        val key = storageKey?.takeIf { it.isNotBlank() } ?: return null
        return MediaUploadTarget(url, key, contentType?.takeIf { it.isNotBlank() } ?: fallbackContentType)
    }
}

@Serializable
data class ConfirmRequestDto(val storageKey: String)

@Serializable
data class ConfirmResponseDto(
    val url: String? = null,
    val storageKey: String? = null,
    val contentType: String? = null,
    val sizeBytes: Long? = null,
)

@Serializable
data class DownloadResponseDto(
    val storageKey: String? = null,
    val url: String? = null,
    val expiresIn: Long = 900,
)
