package com.leochat.feature.calls.domain.model

enum class CallType {
    AUDIO,
    VIDEO,
    ;

    /** Wire value sent in `callType` (Flutter sends `CallType.name`). */
    val wireName: String get() = name.lowercase()

    companion object {
        fun fromWire(value: String?): CallType = if (value?.trim()?.lowercase() == "video") VIDEO else AUDIO
    }
}

enum class CallStatus { IDLE, OUTGOING, RINGING, INCOMING, CONNECTING, CONNECTED, ENDED }

/** Actions exchanged over `/app/call.signal` and `/user/queue/calls`. Mirrors `CallSignalAction`. */
object CallSignalAction {
    const val Invite = "INVITE"
    const val Offer = "OFFER"
    const val Answer = "ANSWER"
    const val IceCandidate = "ICE_CANDIDATE"
    const val Reject = "REJECT"
    const val Busy = "BUSY"
    const val End = "END"
    const val Delivered = "DELIVERED"
    const val Unavailable = "UNAVAILABLE"
}

/**
 * One signaling frame. [payload] carries WebRTC data: an SDP description for
 * OFFER/ANSWER and an ICE candidate for ICE_CANDIDATE, both JSON-encoded.
 */
data class CallSignal(
    val callId: String,
    val conversationId: String,
    val targetUserId: String? = null,
    val fromUserId: String? = null,
    val fromUsername: String? = null,
    val action: String,
    val callType: CallType? = null,
    val payload: String? = null,
)

data class CallSession(
    val callId: String,
    val conversationId: String,
    val targetUserId: String,
    val targetUserName: String,
    val callType: CallType,
    val status: CallStatus,
    val isCaller: Boolean,
    val targetAvatarUrl: String? = null,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isCameraFront: Boolean = true,
    val connectedSeconds: Int = 0,
) {
    val isConnected: Boolean get() = status == CallStatus.CONNECTED
    val isVideo: Boolean get() = callType == CallType.VIDEO
}

data class IceServer(val urls: List<String>, val username: String? = null, val credential: String? = null)

/** STUN/TURN servers handed to the media engine. Production should add a TURN server. */
data class CallIceConfig(val servers: List<IceServer>) {
    companion object {
        val Default = CallIceConfig(
            listOf(
                IceServer(
                    listOf(
                        "stun:stun.l.google.com:19302",
                        "stun:stun1.l.google.com:19302",
                        "stun:stun2.l.google.com:19302",
                        "stun:stun3.l.google.com:19302",
                        "stun:stun4.l.google.com:19302",
                    ),
                ),
            ),
        )
    }
}
