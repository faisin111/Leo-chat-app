package com.leochat.features.media.domain.entities

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

const val MaxUploadBytes = 10L * 1024 * 1024
