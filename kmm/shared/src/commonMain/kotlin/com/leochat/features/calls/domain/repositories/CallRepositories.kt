package com.leochat.features.calls.domain.repositories

import com.leochat.features.calls.domain.entities.CallIceConfig
import com.leochat.features.calls.domain.entities.CallSignal
import kotlinx.coroutines.flow.Flow

/** Call signaling over STOMP: `/user/queue/calls` in, `/app/call.signal` out. */
interface CallSignalingRepository {
    fun incomingSignals(): Flow<CallSignal>

    /** Returns false when there is no live realtime connection. */
    suspend fun send(signal: CallSignal): Boolean
}

/**
 * Platform WebRTC peer connection for one call. SDP and ICE values are the
 * JSON strings carried in [CallSignal.payload].
 */
interface CallMediaEngine {
    /** Local ICE candidates, buffered until collected. */
    val localIceCandidates: Flow<String>

    /** Captures local media (microphone, plus camera for video when supported). */
    suspend fun start(video: Boolean)

    /** Creates and applies a local offer. Null when the platform cannot negotiate media. */
    suspend fun createOffer(): String?

    /** Applies a remote offer and returns the local answer. */
    suspend fun acceptOffer(offer: String): String?

    suspend fun applyAnswer(answer: String)

    suspend fun addRemoteIceCandidate(candidate: String)

    fun setMuted(muted: Boolean)

    fun setSpeakerOn(enabled: Boolean)

    fun switchCamera()

    fun close()
}

fun interface CallMediaEngineFactory {
    fun create(config: CallIceConfig): CallMediaEngine
}
