package com.leochat.android

import android.content.Context
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.PeerConnection
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.IceCandidate

/** Native media factory used by the KMM call controller once signaling is connected. */
class AndroidWebRtcEngine(context: Context) {
    private val factory: PeerConnectionFactory

    init {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context.applicationContext).createInitializationOptions(),
        )
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory()
    }

    fun createMicrophoneTrack(): AudioTrack {
        val source: AudioSource = factory.createAudioSource(MediaConstraints())
        return factory.createAudioTrack("leochat-audio", source)
    }

    fun createPeerConnection(onIceCandidate: (IceCandidate) -> Unit): PeerConnection? {
        val configuration = PeerConnection.RTCConfiguration(
            listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()),
        )
        return factory.createPeerConnection(configuration, object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState) = Unit
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) = Unit
            override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) = Unit
            override fun onIceCandidate(candidate: IceCandidate) { onIceCandidate(candidate) }
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) = Unit
            override fun onAddStream(stream: org.webrtc.MediaStream) = Unit
            override fun onRemoveStream(stream: org.webrtc.MediaStream) = Unit
            override fun onDataChannel(channel: org.webrtc.DataChannel) = Unit
            override fun onRenegotiationNeeded() = Unit
            override fun onAddTrack(receiver: org.webrtc.RtpReceiver, mediaStreams: Array<out org.webrtc.MediaStream>) = Unit
        })
    }

    fun createOffer(connection: PeerConnection, onResult: (SessionDescription) -> Unit, onError: (String) -> Unit) {
        connection.createOffer(object : SdpObserver {
            override fun onCreateSuccess(description: SessionDescription) = onResult(description)
            override fun onSetSuccess() = Unit
            override fun onCreateFailure(error: String) = onError(error)
            override fun onSetFailure(error: String) = onError(error)
        }, MediaConstraints())
    }

    fun dispose() { factory.dispose() }
}
