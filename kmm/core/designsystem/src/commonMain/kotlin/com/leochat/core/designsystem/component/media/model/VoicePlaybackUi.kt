package com.leochat.core.designsystem.component.media.model

/** Render-ready snapshot of one voice note for `VoiceMessagePlayerView`. */
data class VoicePlaybackUi(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    /** Playback progress in `0f..1f`. */
    val progress: Float = 0f,
    /** Elapsed time while playing or paused mid-way, otherwise total duration. */
    val timeText: String = "",
    val speedLabel: String = "",
)
