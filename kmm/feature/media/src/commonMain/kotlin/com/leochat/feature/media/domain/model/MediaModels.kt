package com.leochat.feature.media.domain.model

/** Message type the backend uses for voice notes. */
const val VoiceMessageType = "VOICE"

/** A finished recording on local storage. */
data class VoiceRecording(
    val filePath: String,
    val durationSeconds: Int,
    val mimeType: String = mimeTypeForVoice(filePath),
)

/** What chat sends for a voice note: `content` = uploaded URL, `type` = VOICE. Mirrors `ChatNotifier.sendVoice`. */
data class VoiceMessageDraft(
    val content: String,
    val type: String = VoiceMessageType,
    val durationSeconds: Int? = null,
)

/** Picked local image or document ready for preview & upload. */
data class PickedAttachment(
    val uriOrPath: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long = 0L,
    val isImage: Boolean = mimeType.startsWith("image/", ignoreCase = true),
)

/** Confirmed backend attachment metadata returned after presign -> PUT -> confirm. */
data class UploadedAttachment(
    val storageKey: String,
    val url: String,
    val fileName: String,
    val mimeType: String,
    val sizeBytes: Long,
)

/** Presign result from `POST /media/presign`. */
data class MediaUploadTarget(val uploadUrl: String, val storageKey: String, val contentType: String)

/** Playback snapshot of one voice note. */
data class VoicePlaybackState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val hasError: Boolean = false,
) {
    val progress: Float
        get() = if (durationMs <= 0) 0f else (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
}

/** `audio/wav` for `.wav`, otherwise `audio/m4a` (the recorder writes AAC in MPEG-4). */
fun mimeTypeForVoice(path: String): String = if (path.lowercase().endsWith(".wav")) "audio/wav" else "audio/m4a"

/** Resolves MIME type from filename extension (mirrors Web `resolveMimeType`). */
fun mimeTypeForFileName(fileName: String, fallback: String = "application/octet-stream"): String {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "avif" -> "image/avif"
        "pdf" -> "application/pdf"
        "doc" -> "application/msword"
        "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        "xls" -> "application/vnd.ms-excel"
        "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        "ppt" -> "application/vnd.ms-powerpoint"
        "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        "txt" -> "text/plain"
        "csv" -> "text/csv"
        "zip" -> "application/zip"
        "json" -> "application/json"
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "ogg", "oga" -> "audio/ogg"
        "m4a", "aac" -> "audio/aac"
        "mp4" -> "video/mp4"
        "webm" -> "video/webm"
        else -> fallback
    }
}

const val MaxUploadBytes = 10L * 1024 * 1024
