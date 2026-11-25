package com.leochat.feature.calls.domain.repository

import com.leochat.feature.calls.domain.model.CallIceConfig
import com.leochat.feature.calls.domain.model.CallSignal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

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

    /** True while local camera video track is active and enabled. */
    val hasLocalVideo: StateFlow<Boolean>

    /** True once a remote video track has been received from the peer. */
    val hasRemoteVideo: StateFlow<Boolean>

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

    fun setCameraEnabled(enabled: Boolean)

    fun switchCamera()

    fun close()
}

fun interface CallMediaEngineFactory {
    fun create(config: CallIceConfig): CallMediaEngine
}

/** Platform audio feedback for calling tick, outgoing ringback, incoming ringtone, and call-end tone. */
interface CallSoundPlayer {
    fun startOutgoingTick()
    fun startOutgoingRing()
    fun startIncomingRing()
    fun stopRing()
    fun playCallEnd()
}

object NoOpCallSoundPlayer : CallSoundPlayer {
    override fun startOutgoingTick() = Unit
    override fun startOutgoingRing() = Unit
    override fun startIncomingRing() = Unit
    override fun stopRing() = Unit
    override fun playCallEnd() = Unit
}
