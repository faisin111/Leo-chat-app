package com.leochat.feature.calls.data.device

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import androidx.core.content.ContextCompat
import com.leochat.feature.calls.data.remote.IceCandidatePayload
import com.leochat.feature.calls.data.remote.SdpPayload
import com.leochat.feature.calls.domain.model.CallIceConfig
import com.leochat.feature.calls.domain.repository.CallMediaEngine
import com.leochat.feature.calls.domain.repository.CallMediaEngineFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.VideoSink
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Process-wide WebRTC factory + shared EGL context; initializing native WebRTC twice crashes. */
internal object WebRtcFactoryHolder {
    @Volatile private var factory: PeerConnectionFactory? = null
    @Volatile private var sharedEglBase: EglBase? = null
    @Volatile private var isInitialized = false

    val eglContext: EglBase.Context?
        get() = synchronized(this) {
            (sharedEglBase ?: runCatching { EglBase.create() }.getOrNull()?.also { sharedEglBase = it })
                ?.eglBaseContext
        }

    fun get(context: Context): PeerConnectionFactory = factory ?: synchronized(this) {
        factory ?: run {
            val app = context.applicationContext
            if (!isInitialized) {
                runCatching {
                    PeerConnectionFactory.initialize(
                        PeerConnectionFactory.InitializationOptions.builder(app).createInitializationOptions()
                    )
                }
                isInitialized = true
            }

            val egl = sharedEglBase ?: runCatching { EglBase.create() }.getOrNull()?.also { sharedEglBase = it }

            val audioModule = runCatching {
                val hwAec = runCatching { JavaAudioDeviceModule.isBuiltInAcousticEchoCancelerSupported() }.getOrDefault(false)
                val hwNs = runCatching { JavaAudioDeviceModule.isBuiltInNoiseSuppressorSupported() }.getOrDefault(false)
                JavaAudioDeviceModule.builder(app)
                    .setUseHardwareAcousticEchoCanceler(hwAec)
                    .setUseHardwareNoiseSuppressor(hwNs)
                    .setAudioRecordErrorCallback(object : JavaAudioDeviceModule.AudioRecordErrorCallback {
                        override fun onWebRtcAudioRecordInitError(errorMessage: String?) {}
                        override fun onWebRtcAudioRecordStartError(errorCode: JavaAudioDeviceModule.AudioRecordStartErrorCode?, errorMessage: String?) {}
                        override fun onWebRtcAudioRecordError(errorMessage: String?) {}
                    })
                    .setAudioTrackErrorCallback(object : JavaAudioDeviceModule.AudioTrackErrorCallback {
                        override fun onWebRtcAudioTrackInitError(errorMessage: String?) {}
                        override fun onWebRtcAudioTrackStartError(errorCode: JavaAudioDeviceModule.AudioTrackStartErrorCode?, errorMessage: String?) {}
                        override fun onWebRtcAudioTrackError(errorMessage: String?) {}
                    })
                    .createAudioDeviceModule()
            }.getOrNull()

            val builder = PeerConnectionFactory.builder()
            if (audioModule != null) {
                builder.setAudioDeviceModule(audioModule)
            }
            val eglCtx = egl?.eglBaseContext
            if (eglCtx != null) {
                runCatching {
                    builder.setVideoEncoderFactory(DefaultVideoEncoderFactory(eglCtx, true, true))
                    builder.setVideoDecoderFactory(DefaultVideoDecoderFactory(eglCtx))
                }
            } else {
                runCatching {
                    builder.setVideoEncoderFactory(DefaultVideoEncoderFactory(null, false, false))
                    builder.setVideoDecoderFactory(DefaultVideoDecoderFactory(null))
                }
            }
            builder.createPeerConnectionFactory().also { factory = it }
        }
    }
}

/**
 * Android WebRTC peer for audio and video calls (Unified Plan, trickle ICE).
 * Shares [WebRtcFactoryHolder.eglContext] across codecs, camera capture, and [SurfaceViewRenderer]s.
 */
class AndroidCallMediaEngine(
    private val context: Context,
    private val config: CallIceConfig,
) : CallMediaEngine {
    private val factory = WebRtcFactoryHolder.get(context)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val candidates = Channel<String>(Channel.UNLIMITED)
    private val pendingRemoteCandidates = mutableListOf<IceCandidate>()
    private val isClosed = AtomicBoolean(false)
    private val sinkLock = Any()

    private val mutableHasLocalVideo = MutableStateFlow(false)
    private val mutableHasRemoteVideo = MutableStateFlow(false)

    private var connection: PeerConnection? = null
    private var audioSource: AudioSource? = null
    private var audioTrack: AudioTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var videoSource: VideoSource? = null
    private var videoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var localSink: VideoSink? = null
    private var remoteSink: VideoSink? = null
    private var tracksAttachedToPeer = false
    private var remoteDescriptionSet = false
    private var previousAudioMode = AudioManager.MODE_NORMAL
    private var isVideoCall = false

    override val localIceCandidates: Flow<String> = candidates.receiveAsFlow()
    override val hasLocalVideo: StateFlow<Boolean> = mutableHasLocalVideo.asStateFlow()
    override val hasRemoteVideo: StateFlow<Boolean> = mutableHasRemoteVideo.asStateFlow()

    override suspend fun start(video: Boolean) {
        if (isClosed.get() || connection != null) return
        isVideoCall = video
        runCatching {
            previousAudioMode = audioManager.mode
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            if (video) {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = true
            }
        }
        val servers = config.servers.flatMap { server ->
            val validUrls = server.urls.filter { it.isNotBlank() }
            if (validUrls.isEmpty()) emptyList()
            else listOf(
                PeerConnection.IceServer.builder(validUrls).apply {
                    server.username?.let(::setUsername)
                    server.credential?.let(::setPassword)
                }.createIceServer()
            )
        }.ifEmpty {
            listOf(
                PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
                PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
            )
        }
        val rtcConfig = PeerConnection.RTCConfiguration(servers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
        val peer = factory.createPeerConnection(rtcConfig, Observer()) ?: error("PeerConnection unavailable")
        val hasMicPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (hasMicPermission) {
            runCatching {
                val source = factory.createAudioSource(MediaConstraints())
                val track = factory.createAudioTrack("leochat-audio", source)
                audioSource = source
                audioTrack = track
            }
        }
        if (video) {
            startLocalVideoCapture()
        }
        connection = peer
    }

    private fun startLocalVideoCapture() {
        val hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val eglCtx = WebRtcFactoryHolder.eglContext
        if (!hasCameraPermission || eglCtx == null) return

        runCatching {
            val enumerator: CameraEnumerator = if (Camera2Enumerator.isSupported(context)) {
                Camera2Enumerator(context)
            } else {
                Camera1Enumerator(false)
            }
            val names = enumerator.deviceNames
            val selectedName = names.firstOrNull { enumerator.isFrontFacing(it) } ?: names.firstOrNull() ?: return
            val capturer = enumerator.createCapturer(selectedName, null) ?: return
            val helper = SurfaceTextureHelper.create("LeoChatCaptureThread", eglCtx) ?: run {
                runCatching { capturer.dispose() }
                return
            }
            val source = factory.createVideoSource(capturer.isScreencast)
            capturer.initialize(helper, context.applicationContext, source.capturerObserver)
            val started = runCatching {
                capturer.startCapture(720, 1280, 30)
                true
            }.recoverCatching {
                capturer.startCapture(640, 480, 30)
                true
            }.getOrDefault(false)

            if (!started) {
                runCatching { capturer.dispose() }
                runCatching { helper.dispose() }
                runCatching { source.dispose() }
                return
            }

            val track = factory.createVideoTrack("leochat-video", source)
            track.setEnabled(true)
            synchronized(sinkLock) {
                videoCapturer = capturer
                surfaceTextureHelper = helper
                videoSource = source
                videoTrack = track
                localSink?.let { sink -> runCatching { track.addSink(sink) } }
            }
            mutableHasLocalVideo.value = true
        }
    }

    private fun attachLocalTracksIfNeeded(peer: PeerConnection, includeVideoTrack: Boolean) {
        if (tracksAttachedToPeer || isClosed.get()) return
        tracksAttachedToPeer = true
        val streamIds = listOf("leochat")
        audioTrack?.let { runCatching { peer.addTrack(it, streamIds) } }
        if (includeVideoTrack) {
            videoTrack?.let { runCatching { peer.addTrack(it, streamIds) } }
        }
    }

    override suspend fun createOffer(): String? {
        if (isClosed.get()) return null
        val peer = connection ?: return null
        attachLocalTracksIfNeeded(peer, includeVideoTrack = isVideoCall)
        val offer = peer.awaitCreate(isOffer = true, includeVideo = isVideoCall)
        if (isClosed.get()) return null
        peer.awaitSet(local = true, offer)
        return SdpPayload("offer", offer.description).encode()
    }

    override suspend fun acceptOffer(offer: String): String? {
        if (isClosed.get()) return null
        val peer = connection ?: return null
        val sdp = SdpPayload.decode(offer)?.sdp ?: offer.takeIf { it.isNotBlank() } ?: return null
        // Apply remote offer before adding local tracks so Unified Plan binds to matching transceivers cleanly.
        peer.awaitSet(local = false, SessionDescription(SessionDescription.Type.OFFER, sdp))
        if (isClosed.get()) return null
        onRemoteDescriptionSet(peer)
        val offerHasVideo = sdp.contains("m=video")
        attachLocalTracksIfNeeded(peer, includeVideoTrack = isVideoCall && offerHasVideo)
        val answer = peer.awaitCreate(isOffer = false, includeVideo = isVideoCall && offerHasVideo)
        if (isClosed.get()) return null
        peer.awaitSet(local = true, answer)
        return SdpPayload("answer", answer.description).encode()
    }

    override suspend fun applyAnswer(answer: String) {
        if (isClosed.get()) return
        val peer = connection ?: return
        val sdp = SdpPayload.decode(answer)?.sdp ?: answer.takeIf { it.isNotBlank() } ?: return
        peer.awaitSet(local = false, SessionDescription(SessionDescription.Type.ANSWER, sdp))
        onRemoteDescriptionSet(peer)
    }

    override suspend fun addRemoteIceCandidate(candidate: String) {
        if (isClosed.get()) return
        val payload = IceCandidatePayload.decode(candidate) ?: return
        val ice = IceCandidate(payload.sdpMid, payload.sdpMLineIndex, payload.candidate)
        val peer = connection
        if (peer == null || !remoteDescriptionSet) pendingRemoteCandidates += ice else peer.addIceCandidate(ice)
    }

    override fun setMuted(muted: Boolean) {
        runCatching { audioTrack?.setEnabled(!muted) }
    }

    @Suppress("DEPRECATION")
    override fun setSpeakerOn(enabled: Boolean) {
        runCatching { audioManager.isSpeakerphoneOn = enabled }
    }

    override fun setCameraEnabled(enabled: Boolean) {
        val track = videoTrack
        runCatching { track?.setEnabled(enabled) }
        mutableHasLocalVideo.value = enabled && track != null && !isClosed.get()
    }

    override fun switchCamera() {
        runCatching { videoCapturer?.switchCamera(null) }
    }

    fun attachLocalSink(sink: VideoSink) {
        synchronized(sinkLock) {
            if (isClosed.get()) return
            if (localSink !== sink) {
                localSink?.let { old -> runCatching { videoTrack?.removeSink(old) } }
                localSink = sink
                runCatching { videoTrack?.addSink(sink) }
            }
        }
    }

    fun detachLocalSink(sink: VideoSink) {
        synchronized(sinkLock) {
            runCatching { videoTrack?.removeSink(sink) }
            if (localSink === sink) localSink = null
        }
    }

    fun attachRemoteSink(sink: VideoSink) {
        synchronized(sinkLock) {
            if (isClosed.get()) return
            if (remoteSink !== sink) {
                remoteSink?.let { old -> runCatching { remoteVideoTrack?.removeSink(old) } }
                remoteSink = sink
                runCatching { remoteVideoTrack?.addSink(sink) }
            }
        }
    }

    fun detachRemoteSink(sink: VideoSink) {
        synchronized(sinkLock) {
            runCatching { remoteVideoTrack?.removeSink(sink) }
            if (remoteSink === sink) remoteSink = null
        }
    }

    @Suppress("DEPRECATION")
    override fun close() {
        if (!isClosed.compareAndSet(false, true)) return
        candidates.close()
        pendingRemoteCandidates.clear()
        mutableHasLocalVideo.value = false
        mutableHasRemoteVideo.value = false

        val capturer: CameraVideoCapturer?
        val helper: SurfaceTextureHelper?
        val vTrack: VideoTrack?
        val vSource: VideoSource?
        val rTrack: VideoTrack?
        val lSink: VideoSink?
        val rSink: VideoSink?
        synchronized(sinkLock) {
            capturer = videoCapturer
            helper = surfaceTextureHelper
            vTrack = videoTrack
            vSource = videoSource
            rTrack = remoteVideoTrack
            lSink = localSink
            rSink = remoteSink
            videoCapturer = null
            surfaceTextureHelper = null
            videoTrack = null
            videoSource = null
            remoteVideoTrack = null
            localSink = null
            remoteSink = null
        }

        lSink?.let { sink -> runCatching { vTrack?.removeSink(sink) } }
        rSink?.let { sink -> runCatching { rTrack?.removeSink(sink) } }
        runCatching { capturer?.stopCapture() }
        runCatching { capturer?.dispose() }
        runCatching { vTrack?.setEnabled(false) }
        runCatching { vTrack?.dispose() }
        runCatching { vSource?.dispose() }
        runCatching { helper?.dispose() }

        runCatching { audioTrack?.setEnabled(false) }
        runCatching { audioTrack?.dispose() }
        audioTrack = null
        runCatching { audioSource?.dispose() }
        audioSource = null
        runCatching { connection?.close() }
        runCatching { connection?.dispose() }
        connection = null
        runCatching {
            audioManager.isSpeakerphoneOn = false
            audioManager.mode = previousAudioMode
        }
    }

    private fun onRemoteDescriptionSet(peer: PeerConnection) {
        remoteDescriptionSet = true
        pendingRemoteCandidates.forEach { runCatching { peer.addIceCandidate(it) } }
        pendingRemoteCandidates.clear()
    }

    private inner class Observer : PeerConnection.Observer {
        override fun onIceCandidate(candidate: IceCandidate) {
            if (isClosed.get()) return
            candidates.trySend(IceCandidatePayload(candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex).encode())
        }
        override fun onSignalingChange(state: PeerConnection.SignalingState) = Unit
        override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) = Unit
        override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
        override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) = Unit
        override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) = Unit
        override fun onAddStream(stream: MediaStream) {
            if (isClosed.get()) return
            val track = stream.videoTracks?.firstOrNull() ?: return
            bindRemoteVideoTrack(track)
        }
        override fun onRemoveStream(stream: MediaStream) {
            synchronized(sinkLock) {
                remoteVideoTrack?.let { old -> remoteSink?.let { sink -> runCatching { old.removeSink(sink) } } }
                remoteVideoTrack = null
            }
            mutableHasRemoteVideo.value = false
        }
        override fun onDataChannel(channel: DataChannel) = Unit
        override fun onRenegotiationNeeded() = Unit
        override fun onAddTrack(receiver: RtpReceiver, mediaStreams: Array<out MediaStream>) {
            if (isClosed.get()) return
            val track = receiver.track() as? VideoTrack ?: return
            bindRemoteVideoTrack(track)
        }
    }

    private fun bindRemoteVideoTrack(track: VideoTrack) {
        synchronized(sinkLock) {
            if (isClosed.get()) return
            if (remoteVideoTrack !== track) {
                remoteVideoTrack?.let { old -> remoteSink?.let { sink -> runCatching { old.removeSink(sink) } } }
                remoteVideoTrack = track
                runCatching { track.setEnabled(true) }
                remoteSink?.let { sink -> runCatching { track.addSink(sink) } }
            }
        }
        mutableHasRemoteVideo.value = true
    }
}

private suspend fun PeerConnection.awaitCreate(isOffer: Boolean, includeVideo: Boolean): SessionDescription = suspendCancellableCoroutine { cont ->
    val observer = object : SdpObserver {
        override fun onCreateSuccess(description: SessionDescription) {
            if (cont.isActive) cont.resume(description)
        }
        override fun onCreateFailure(error: String?) {
            if (cont.isActive) cont.resumeWithException(IllegalStateException(error ?: "SDP create failed"))
        }
        override fun onSetSuccess() = Unit
        override fun onSetFailure(error: String?) = Unit
    }
    val constraints = MediaConstraints().apply {
        mandatory += MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")
        mandatory += MediaConstraints.KeyValuePair("OfferToReceiveVideo", includeVideo.toString())
    }
    if (isOffer) createOffer(observer, constraints) else createAnswer(observer, constraints)
}

private suspend fun PeerConnection.awaitSet(local: Boolean, description: SessionDescription): Unit = suspendCancellableCoroutine { cont ->
    val observer = object : SdpObserver {
        override fun onSetSuccess() {
            if (cont.isActive) cont.resume(Unit)
        }
        override fun onSetFailure(error: String?) {
            if (cont.isActive) cont.resumeWithException(IllegalStateException(error ?: "SDP set failed"))
        }
        override fun onCreateSuccess(description: SessionDescription) = Unit
        override fun onCreateFailure(error: String?) = Unit
    }
    if (local) setLocalDescription(observer, description) else setRemoteDescription(observer, description)
}

class AndroidCallMediaEngineFactory(private val context: Context) : CallMediaEngineFactory {
    override fun create(config: CallIceConfig): CallMediaEngine = AndroidCallMediaEngine(context, config)
}
