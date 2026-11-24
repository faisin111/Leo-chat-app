package com.leochat.features.media.presentation.controllers

import com.leochat.features.media.domain.entities.VoiceRecording
import com.leochat.features.media.domain.repositories.VoiceRecorder
import com.leochat.features.media.presentation.states.VoiceRecordingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Tap-to-record flow of the chat composer: start → (timer) → stop & send, or
 * cancel. Mirrors `_startRecording` / `_stopAndSendRecording` / `_cancelRecording`.
 */
class VoiceRecordingController(
    private val recorder: VoiceRecorder,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(VoiceRecordingState())
    val state: StateFlow<VoiceRecordingState> = mutableState.asStateFlow()
    private var timer: Job? = null

    /** Returns false when the recorder could not start. */
    fun start(): Boolean {
        if (mutableState.value.isRecording) return true
        val started = runCatching { recorder.start() }.getOrDefault(false)
        if (!started) return false
        mutableState.value = VoiceRecordingState(isRecording = true)
        timer?.cancel()
        timer = scope.launch {
            while (isActive) {
                delay(1_000)
                mutableState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
        return true
    }

    /** Stops and returns the recording (at least one second long), or null when nothing was captured. */
    fun stop(): VoiceRecording? {
        if (!mutableState.value.isRecording) return null
        val seconds = mutableState.value.elapsedSeconds.coerceAtLeast(1)
        reset()
        val path = runCatching { recorder.stop() }.getOrNull() ?: return null
        return VoiceRecording(filePath = path, durationSeconds = seconds)
    }

    fun cancel() {
        if (!mutableState.value.isRecording) return
        reset()
        runCatching { recorder.cancel() }
    }

    private fun reset() {
        timer?.cancel()
        timer = null
        mutableState.value = VoiceRecordingState()
    }
}
