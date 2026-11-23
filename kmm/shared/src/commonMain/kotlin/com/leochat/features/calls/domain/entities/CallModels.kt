package com.leochat.features.calls.domain.entities

enum class CallType { AUDIO, VIDEO }

enum class CallStatus { IDLE, OUTGOING, INCOMING, CONNECTED, ENDED }

/** Signal exchanged over `/app/call.signal` and `/user/queue/calls`. */
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
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isCameraFront: Boolean = true,
    val connectedSeconds: Int = 0,
)
