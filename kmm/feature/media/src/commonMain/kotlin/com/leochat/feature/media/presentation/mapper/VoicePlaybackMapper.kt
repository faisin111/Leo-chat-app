package com.leochat.feature.media.presentation.mapper

import com.leochat.core.designsystem.component.media.model.VoicePlaybackUi
import com.leochat.feature.media.domain.model.VoicePlaybackState
import com.leochat.feature.media.presentation.VoicePlaybackSpeeds
import com.leochat.feature.media.presentation.formatVoiceTime

/** Elapsed time once playback has started or moved, otherwise the total duration; `m:ss`. */
fun VoicePlaybackState.toUi(speed: Float): VoicePlaybackUi {
    val showElapsed = isPlaying || positionMs > 0
    return VoicePlaybackUi(
        isPlaying = isPlaying,
        isLoading = isLoading,
        progress = progress,
        timeText = formatVoiceTime(if (showElapsed) positionMs else durationMs),
        speedLabel = VoicePlaybackSpeeds.label(speed),
    )
}
