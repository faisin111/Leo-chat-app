package com.leochat.android.push

import com.leochat.core.common.constants.AppStrings
import com.leochat.feature.calls.domain.model.CallSignal
import com.leochat.feature.calls.domain.model.CallSignalAction
import com.leochat.feature.calls.domain.model.CallType

sealed interface PushEvent {
    data class NewMessage(
        val messageId: String,
        val conversationId: String,
        val senderId: String?,
        val senderName: String,
        val senderAvatarUrl: String?,
        val conversationTitle: String?,
        val messageType: String,
        val displayTitle: String,
        val displayBody: String,
    ) : PushEvent

    data class MessageReaction(
        val messageId: String,
        val conversationId: String,
        val senderId: String?,
        val senderName: String,
        val emoji: String?,
        val messageType: String?,
        val displayTitle: String,
        val displayBody: String,
    ) : PushEvent

    data class IncomingCall(val signal: CallSignal) : PushEvent
    data class CancelCall(val callId: String, val conversationId: String) : PushEvent
}

/**
 * Parses all backend FCM `data` notifications:
 * - `NEW_MESSAGE` (`TEXT`, `IMAGE`, `VOICE`, `FILE`, `CALL`, `SYSTEM`)
 * - `MESSAGE_REACTION`
 * - `INCOMING_CALL` (and call cancel events)
 */
object PushNotificationPayload {
    fun parse(
        data: Map<String, String>,
        notificationTitle: String? = null,
        notificationBody: String? = null,
    ): PushEvent? {
        if (data.isEmpty() && notificationTitle.isNullOrBlank() && notificationBody.isNullOrBlank()) return null

        val kind = (data["type"] ?: data["notificationType"] ?: data["eventType"] ?: data["action"])
            ?.trim()
            ?.uppercase()

        return when (kind) {
            "NEW_MESSAGE" -> parseNewMessage(data, notificationTitle, notificationBody)
            "MESSAGE_REACTION" -> parseReaction(data, notificationTitle, notificationBody)
            else -> when (val call = PushCallPayload.parse(data, notificationTitle)) {
                is PushCallEvent.Incoming -> PushEvent.IncomingCall(call.signal)
                is PushCallEvent.Cancel -> PushEvent.CancelCall(call.callId, call.conversationId)
                null -> null
            }
        }
    }

    private fun parseNewMessage(
        data: Map<String, String>,
        notificationTitle: String?,
        notificationBody: String?,
    ): PushEvent.NewMessage? {
        val conversationId = data.firstNonBlank("conversationId", "conversation_id") ?: return null
        val messageId = data.firstNonBlank("messageId", "message_id").orEmpty()
        val senderId = data.firstNonBlank("senderId", "sender_id", "fromUserId", "from_user_id", "actorId", "userId")
        val senderName = data.firstNonBlank(
            "senderDisplayName",
            "sender_display_name",
            "senderName",
            "sender_name",
            "senderUsername",
            "sender_username",
            "fromUsername",
            "from_username",
            "actorName",
            "title",
        ) ?: notificationTitle?.trim()?.takeIf { it.isNotEmpty() } ?: AppStrings.appName

        val senderAvatarUrl = data.firstNonBlank("senderAvatarUrl", "sender_avatar_url", "avatarUrl", "fromAvatarUrl")
        val conversationTitle = data.firstNonBlank("conversationTitle", "conversation_title", "conversationName", "groupName")
        val messageType = (data.firstNonBlank("messageType", "message_type") ?: "TEXT").uppercase()
        val rawContent = data.firstNonBlank("content", "messageContent", "message_content", "preview", "body", "text")
            ?: notificationBody?.trim()?.takeIf { it.isNotEmpty() }

        val preview = when (messageType) {
            "IMAGE" -> rawContent?.takeIf { !it.startsWith("http") } ?: AppStrings.photoPreview
            "VOICE" -> AppStrings.voicePreview
            "FILE" -> rawContent?.takeIf { !it.startsWith("http") }?.let { "📎 $it" } ?: AppStrings.filePreview
            "CALL" -> rawContent ?: AppStrings.callPreview
            else -> rawContent ?: AppStrings.newMessageFallback
        }

        val isGroupHeader = !conversationTitle.isNullOrBlank() && !conversationTitle.equals(senderName, ignoreCase = true)
        val displayTitle = if (isGroupHeader) conversationTitle!! else senderName
        val displayBody = if (isGroupHeader && senderName != AppStrings.appName) "$senderName: $preview" else preview

        return PushEvent.NewMessage(
            messageId = messageId,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            senderAvatarUrl = senderAvatarUrl,
            conversationTitle = conversationTitle,
            messageType = messageType,
            displayTitle = displayTitle,
            displayBody = displayBody,
        )
    }

    private fun parseReaction(
        data: Map<String, String>,
        notificationTitle: String?,
        notificationBody: String?,
    ): PushEvent.MessageReaction? {
        val conversationId = data.firstNonBlank("conversationId", "conversation_id") ?: return null
        val messageId = data.firstNonBlank("messageId", "message_id").orEmpty()
        val senderId = data.firstNonBlank("senderId", "sender_id", "fromUserId", "from_user_id", "actorId", "userId")
        val senderName = data.firstNonBlank(
            "senderDisplayName",
            "sender_display_name",
            "senderName",
            "sender_name",
            "senderUsername",
            "sender_username",
            "fromUsername",
            "actorName",
            "title",
        ) ?: notificationTitle?.trim()?.takeIf { it.isNotEmpty() } ?: AppStrings.appName

        val emoji = data.firstNonBlank("emoji", "reaction", "reactionEmoji", "reaction_emoji")
        val messageType = data.firstNonBlank("messageType", "message_type")?.uppercase()
        val conversationTitle = data.firstNonBlank("conversationTitle", "conversation_title", "conversationName", "groupName")
        val rawBody = data.firstNonBlank("body", "preview", "content", "text")
            ?: notificationBody?.trim()?.takeIf { it.isNotEmpty() }

        val actionText = when {
            !emoji.isNullOrBlank() && !rawBody.isNullOrBlank() && !rawBody.startsWith("http") ->
                "Reacted $emoji to: $rawBody"
            !emoji.isNullOrBlank() -> "Reacted $emoji to your message"
            !rawBody.isNullOrBlank() -> rawBody
            else -> AppStrings.reactedToMessage
        }

        val isGroupHeader = !conversationTitle.isNullOrBlank() && !conversationTitle.equals(senderName, ignoreCase = true)
        val displayTitle = if (isGroupHeader) conversationTitle!! else senderName
        val displayBody = if (isGroupHeader && senderName != AppStrings.appName) "$senderName: $actionText" else actionText

        return PushEvent.MessageReaction(
            messageId = messageId,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            emoji = emoji,
            messageType = messageType,
            displayTitle = displayTitle,
            displayBody = displayBody,
        )
    }

    private fun Map<String, String>.firstNonBlank(vararg keys: String): String? {
        for (key in keys) {
            val value = this[key]?.trim()
            if (!value.isNullOrEmpty()) return value
        }
        return null
    }
}
