package com.leochat.features.chat.domain.entities

import kotlinx.datetime.Instant

enum class MessageSendStatus { Sending, Sent, Failed }

data class MessageReply(
    val id: String,
    val senderId: String? = null,
    val senderUsername: String? = null,
    val senderDisplayName: String? = null,
    val content: String? = null,
    val deleted: Boolean = false,
)

data class ReactionUser(val id: String, val username: String, val displayName: String? = null)

data class MessageReaction(val emoji: String, val count: Int, val users: List<ReactionUser> = emptyList())

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String = "",
    val senderUsername: String = "",
    val senderDisplayName: String? = null,
    val seq: Long = 0,
    val clientMessageId: String = "",
    val type: String = "TEXT",
    val content: String? = null,
    val replyToId: String? = null,
    val replyTo: MessageReply? = null,
    val reactions: List<MessageReaction> = emptyList(),
    val createdAt: Instant? = null,
    val editedAt: Instant? = null,
    val deletedAt: Instant? = null,
    val deleted: Boolean = false,
    val sendStatus: MessageSendStatus = MessageSendStatus.Sent,
    val isOwn: Boolean = false,
    val isRead: Boolean = false,
    val aiGenerated: Boolean = false,
) {
    val effectiveSenderName: String
        get() = senderDisplayName?.takeIf { it.isNotEmpty() } ?: senderUsername.ifEmpty { "User" }

    val isVoice: Boolean
        get() = type.trim().uppercase().let { it == "VOICE" || it == "AUDIO" }

    val isImage: Boolean
        get() = type.trim().uppercase().let { it == "IMAGE" || it == "PHOTO" || it == "IMAGE_MESSAGE" } &&
            !content.isNullOrBlank()

    fun hasUserReacted(emoji: String): Boolean = reactions.any { r ->
        r.emoji == emoji && (r.users.any { it.id == senderId } || (isOwn && r.count > 0))
    }
}

/** Merge REST and realtime messages by id / clientMessageId, then sort by `seq`. */
fun mergeMessages(existing: List<Message>, incoming: List<Message>): List<Message> {
    val result = existing.toMutableList()
    for (message in incoming) {
        val index = result.indexOfFirst {
            it.id == message.id || (message.clientMessageId.isNotEmpty() && it.clientMessageId == message.clientMessageId)
        }
        if (index >= 0) result[index] = message else result += message
    }
    return result.sortedBy { it.seq }
}
