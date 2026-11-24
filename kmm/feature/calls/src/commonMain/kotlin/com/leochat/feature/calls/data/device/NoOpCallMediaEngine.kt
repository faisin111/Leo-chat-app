package com.leochat.feature.calls.data.device

import com.leochat.feature.calls.domain.repository.CallMediaEngine
import com.leochat.feature.calls.domain.repository.CallMediaEngineFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Signaling-only engine for platforms without WebRTC (iOS for now) and tests. */
class NoOpCallMediaEngine : CallMediaEngine {
    override val localIceCandidates: Flow<String> = emptyFlow()
    override suspend fun start(video: Boolean) = Unit
    override suspend fun createOffer(): String? = null
    override suspend fun acceptOffer(offer: String): String? = null
    override suspend fun applyAnswer(answer: String) = Unit
    override suspend fun addRemoteIceCandidate(candidate: String) = Unit
    override fun setMuted(muted: Boolean) = Unit
    override fun setSpeakerOn(enabled: Boolean) = Unit
    override fun switchCamera() = Unit
    override fun close() = Unit
}

val NoOpCallMediaEngineFactory = CallMediaEngineFactory { NoOpCallMediaEngine() }
