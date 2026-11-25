package com.leochat.android.push

import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.model.CallSignalAction
import com.leochat.feature.calls.domain.model.CallType

sealed interface PushCallEvent {
    data class Incoming(val signal: CallSignal) : PushCallEvent
    data class Cancel(val callId: String, val conversationId: String) : PushCallEvent
}

/**
 * Parses FCM `data` payloads from the backend `INCOMING_CALL` (and call cancel) push notifications,
 * tolerating both camelCase and snake_case keys.
 */
object PushCallPayload {
    fun parse(data: Map<String, String>, notificationTitle: String? = null): PushCallEvent? {
        if (data.isEmpty()) return null
        val kind = (data["type"] ?: data["notificationType"] ?: data["action"])
            ?.trim()
            ?.uppercase()

        val callId = (data["callId"] ?: data["call_id"]).orEmpty().trim()
        val conversationId = (data["conversationId"] ?: data["conversation_id"]).orEmpty().trim()

        if (kind in CANCEL_KINDS) {
            if (callId.isEmpty()) return null
            return PushCallEvent.Cancel(callId = callId, conversationId = conversationId)
        }

        val isIncomingCall = kind in INCOMING_KINDS || (kind == null && callId.isNotEmpty())
        if (!isIncomingCall || callId.isEmpty()) return null

        val fromUserId = (data["fromUserId"] ?: data["callerId"] ?: data["caller_id"] ?: data["senderId"] ?: data["userId"])
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        val fromUsername = (data["fromUsername"] ?: data["callerName"] ?: data["caller_name"] ?: data["senderName"] ?: data["title"] ?: notificationTitle)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        val fromAvatarUrl = (data["fromAvatarUrl"] ?: data["callerAvatarUrl"] ?: data["caller_avatar_url"] ?: data["avatarUrl"])
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        val sdp = (data["sdp"] ?: data["offerSdp"] ?: data["payload"])?.takeIf { it.isNotBlank() }
        val rawCallType = data["callType"] ?: data["call_type"]
        val isVideoFlag = (data["isVideo"] ?: data["is_video"] ?: data["video"])?.trim()?.lowercase()
        val callType = when {
            rawCallType?.trim()?.lowercase() == "video" -> CallType.VIDEO
            rawCallType?.trim()?.lowercase() == "audio" -> CallType.AUDIO
            isVideoFlag == "true" || isVideoFlag == "1" -> CallType.VIDEO
            sdp?.contains("m=video") == true -> CallType.VIDEO
            else -> CallType.AUDIO
        }

        return PushCallEvent.Incoming(
            CallSignal(
                callId = callId,
                conversationId = conversationId,
                fromUserId = fromUserId,
                fromUsername = fromUsername,
                fromAvatarUrl = fromAvatarUrl,
                action = CallSignalAction.Invite,
                callType = callType,
                payload = sdp,
            ),
        )
    }

    private val INCOMING_KINDS = setOf("INCOMING_CALL", "INVITE", "OFFER", "CALL")
    private val CANCEL_KINDS = setOf("CALL_CANCELLED", "CALL_CANCELED", "CALL_ENDED", "MISSED_CALL", "END", "CANCEL", "REJECT")
}
