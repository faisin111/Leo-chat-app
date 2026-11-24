package com.leochat.feature.calls.data.device

import com.leochat.feature.calls.domain.model.IceServer
import android.content.Context
import android.media.AudioManager
import com.leochat.feature.calls.data.remote.IceCandidatePayload
import com.leochat.feature.calls.data.remote.SdpPayload
import com.leochat.feature.calls.domain.model.CallIceConfig
import com.leochat.feature.calls.domain.repository.CallMediaEngine
import com.leochat.feature.calls.domain.repository.CallMediaEngineFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.audio.JavaAudioDeviceModule
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Process-wide WebRTC factory; initializing it twice crashes the native library. */
private object WebRtcFactoryHolder {
    @Volatile private var factory: PeerConnectionFactory? = null

    fun get(context: Context): PeerConnectionFactory = factory ?: synchronized(this) {
        factory ?: run {
            val app = context.applicationContext
            PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(app).createInitializationOptions())
            val audioModule = JavaAudioDeviceModule.builder(app)
                .setUseHardwareAcousticEchoCanceler(true)
                .setUseHardwareNoiseSuppressor(true)
                .createAudioDeviceModule()
            PeerConnectionFactory.builder().setAudioDeviceModule(audioModule).createPeerConnectionFactory().also { factory = it }
        }
    }
}

/**
 * Audio WebRTC peer for one call (unified plan, trickle ICE). Remote audio plays
 * through the audio device module. Video capture/rendering is not wired yet; the
 * Flutter app shows a camera placeholder for video calls and so does this port.
 */
class AndroidCallMediaEngine(
    private val context: Context,
    private val config: CallIceConfig,
) : CallMediaEngine {
    private val factory = WebRtcFactoryHolder.get(context)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val candidates = Channel<String>(Channel.UNLIMITED)
    private val pendingRemoteCandidates = mutableListOf<IceCandidate>()
    private var connection: PeerConnection? = null
    private var audioTrack: AudioTrack? = null
    private var remoteDescriptionSet = false
    private var previousAudioMode = AudioManager.MODE_NORMAL

    override val localIceCandidates: Flow<String> = candidates.receiveAsFlow()

    override suspend fun start(video: Boolean) {
        if (connection != null) return
        previousAudioMode = audioManager.mode
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        val servers = config.servers.map { server ->
            PeerConnection.IceServer.builder(server.urls).apply {
                server.username?.let(::setUsername)
                server.credential?.let(::setPassword)
            }.createIceServer()
        }
        val rtcConfig = PeerConnection.RTCConfiguration(servers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
        val peer = factory.createPeerConnection(rtcConfig, Observer()) ?: error("PeerConnection unavailable")
        val source = factory.createAudioSource(MediaConstraints())
        val track = factory.createAudioTrack("leochat-audio", source)
        peer.addTrack(track, listOf("leochat"))
        audioTrack = track
        connection = peer
    }

    override suspend fun createOffer(): String? {
        val peer = connection ?: return null
        val offer = peer.awaitCreate(isOffer = true)
        peer.awaitSet(local = true, offer)
        return SdpPayload("offer", offer.description).encode()
    }

    override suspend fun acceptOffer(offer: String): String? {
        val peer = connection ?: return null
        val remote = SdpPayload.decode(offer) ?: return null
        peer.awaitSet(local = false, SessionDescription(SessionDescription.Type.OFFER, remote.sdp))
        onRemoteDescriptionSet(peer)
        val answer = peer.awaitCreate(isOffer = false)
        peer.awaitSet(local = true, answer)
        return SdpPayload("answer", answer.description).encode()
    }

    override suspend fun applyAnswer(answer: String) {
        val peer = connection ?: return
        val remote = SdpPayload.decode(answer) ?: return
        peer.awaitSet(local = false, SessionDescription(SessionDescription.Type.ANSWER, remote.sdp))
        onRemoteDescriptionSet(peer)
    }

    override suspend fun addRemoteIceCandidate(candidate: String) {
        val payload = IceCandidatePayload.decode(candidate) ?: return
        val ice = IceCandidate(payload.sdpMid, payload.sdpMLineIndex, payload.candidate)
        val peer = connection
        if (peer == null || !remoteDescriptionSet) pendingRemoteCandidates += ice else peer.addIceCandidate(ice)
    }

    override fun setMuted(muted: Boolean) {
        audioTrack?.setEnabled(!muted)
    }

    @Suppress("DEPRECATION")
    override fun setSpeakerOn(enabled: Boolean) {
        audioManager.isSpeakerphoneOn = enabled
    }

    override fun switchCamera() = Unit

    @Suppress("DEPRECATION")
    override fun close() {
        candidates.close()
        pendingRemoteCandidates.clear()
        runCatching { connection?.close() }
        runCatching { connection?.dispose() }
        connection = null
        audioTrack = null
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = previousAudioMode
    }

    private fun onRemoteDescriptionSet(peer: PeerConnection) {
        remoteDescriptionSet = true
        pendingRemoteCandidates.forEach(peer::addIceCandidate)
        pendingRemoteCandidates.clear()
    }

    private inner class Observer : PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate) {
            candidates.trySend(IceCandidatePayload(candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex).encode())
        }
        override fun onSignalingChange(state: PeerConnection.SignalingState) = Unit
        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) = Unit
        override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) = Unit
        override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) = Unit
        override fun onAddStream(stream: MediaStream) = Unit
        override fun onRemoveStream(stream: MediaStream) = Unit
        override fun onDataChannel(channel: DataChannel) = Unit
        override fun onRenegotiationNeeded() = Unit
        override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<out MediaStream>) = Unit
    }
}

private suspend fun PeerConnection.awaitCreate(isOffer: Boolean): SessionDescription = suspendCancellableCoroutine { cont ->
    val observer = object : SdpObserver {
        override fun onCreateSuccess(description: SessionDescription) = cont.resume(description)
        override fun onCreateFailure(error: String?) = cont.resumeWithException(IllegalStateException(error))
        override fun onSetSuccess() = Unit
        override fun onSetFailure(error: String?) = Unit
    }
    val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
    }
    if (isOffer) createOffer(observer, constraints) else createAnswer(observer, constraints)
}

private suspend fun PeerConnection.awaitSet(local: Boolean, description: SessionDescription): Unit = suspendCancellableCoroutine { cont ->
    val observer = object : SdpObserver {
        override fun onCreateSuccess(description: SessionDescription) = Unit
        override fun onCreateFailure(error: String?) = Unit
        override fun onSetSuccess() = cont.resume(Unit)
        override fun onSetFailure(error: String?) = cont.resumeWithException(IllegalStateException(error))
    }
    if (local) setLocalDescription(observer, description) else setRemoteDescription(observer, description)
}

class AndroidCallMediaEngineFactory(private val context: Context) : CallMediaEngineFactory {
    override fun create(config: CallIceConfig): CallMediaEngine = AndroidCallMediaEngine(context, config)
}
