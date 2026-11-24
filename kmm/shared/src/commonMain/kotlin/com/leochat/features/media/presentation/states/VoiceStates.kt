package com.leochat.features.media.presentation.states

data class VoiceRecordingState(
    val isRecording: Boolean = false,
    val elapsedSeconds: Int = 0,
)

/** Playback speeds cycled by the voice note speed chip: 1x → 1.5x → 2x. */
object VoicePlaybackSpeeds {
    private val speeds = listOf(1f, 1.5f, 2f)

    fun next(current: Float): Float = speeds[(speeds.indexOf(current).coerceAtLeast(0) + 1) % speeds.size]

    fun label(speed: Float): String = if (speed == 1f) "1x" else "${speed}x"
}

/** `m:ss`, like `_formatTime` in `voice_message_player.dart`. */
fun formatVoiceTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
}

/** `mm:ss`, like `_formatDuration` in `chat_composer.dart`. */
fun formatRecordingTime(seconds: Int): String =
    "${(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}"
