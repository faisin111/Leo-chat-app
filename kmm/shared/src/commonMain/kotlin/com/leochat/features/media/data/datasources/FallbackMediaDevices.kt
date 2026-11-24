package com.leochat.features.media.data.datasources

import com.leochat.features.media.domain.entities.VoicePlaybackState
import com.leochat.features.media.domain.repositories.VoicePlayer
import com.leochat.features.media.domain.repositories.VoicePlayerFactory
import com.leochat.features.media.domain.repositories.VoiceRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Used when no platform recorder is bound: recording never starts. */
object UnavailableVoiceRecorder : VoiceRecorder {
    override fun start(): Boolean = false
    override fun stop(): String? = null
    override fun cancel() = Unit
}

/** Player that only tracks seek/speed state; used where no audio backend is bound. */
class SilentVoicePlayer : VoicePlayer {
    private val mutableState = MutableStateFlow(VoicePlaybackState())
    override val state: StateFlow<VoicePlaybackState> = mutableState.asStateFlow()

    override fun play(source: String) = mutableState.update { it.copy(isPlaying = true) }
    override fun pause() = mutableState.update { it.copy(isPlaying = false) }
    override fun seekTo(fraction: Float) =
        mutableState.update { it.copy(positionMs = (it.durationMs * fraction.coerceIn(0f, 1f)).toLong()) }
    override fun setSpeed(speed: Float) = Unit
    override fun release() = mutableState.update { VoicePlaybackState() }
}

val SilentVoicePlayerFactory = VoicePlayerFactory { SilentVoicePlayer() }
