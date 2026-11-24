package com.leochat.feature.chat.data.remote

import com.leochat.core.network.objects
import com.leochat.core.common.util.parseInstant
import com.leochat.core.network.array
import com.leochat.core.network.bool
import com.leochat.core.network.int
import com.leochat.core.network.long
import com.leochat.core.network.obj
import com.leochat.core.network.string
import com.leochat.feature.chat.domain.model.CurrentIdentity
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.model.MessageReaction
import com.leochat.feature.chat.domain.model.MessageReply
import com.leochat.feature.chat.domain.model.MessageSendStatus
import com.leochat.feature.chat.domain.model.ReactionUser
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class MessageReplyDto(
    val id: String,
    val senderId: String? = null,
    val senderUsername: String? = null,
    val senderDisplayName: String? = null,
    val content: String? = null,
    val deleted: Boolean = false,
) {
    fun toDomain() = MessageReply(id, senderId, senderUsername, senderDisplayName, content, deleted)

    companion object {
        fun fromJson(json: JsonObject) = MessageReplyDto(
            id = json.string("id").orEmpty(),
            senderId = json.string("senderId"),
            senderUsername = json.string("senderUsername"),
            senderDisplayName = json.string("senderDisplayName"),
            content = json.string("content"),
            deleted = json.bool("deleted") ?: false,
        )
    }
}

@Serializable
data class ReactionUserDto(val id: String, val username: String, val displayName: String? = null)

@Serializable
data class MessageReactionDto(val emoji: String, val count: Int, val users: List<ReactionUserDto> = emptyList()) {
    fun toDomain() = MessageReaction(emoji, count, users.map { ReactionUser(it.id, it.username, it.displayName) })

    companion object {
        fun fromJson(json: JsonObject) = MessageReactionDto(
            emoji = json.string("emoji").orEmpty(),
            count = json.int("count") ?: 0,
            users = json.array("users").objects().map {
                ReactionUserDto(it.string("id").orEmpty(), it.string("username").orEmpty(), it.string("displayName"))
            },
        )
    }
}

/** Message wire + cache model. Mirrors Flutter `MessageDto`. */
@Serializable
data class MessageDto(
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
    val replyTo: MessageReplyDto? = null,
    val reactions: List<MessageReactionDto> = emptyList(),
    val createdAt: String? = null,
    val editedAt: String? = null,
    val deletedAt: String? = null,
    val deleted: Boolean = false,
    val aiGenerated: Boolean = false,
    val sendStatus: String = "Sent",
) {
    /** `isOwn` matches by id first, then username. */
    fun toDomain(identity: CurrentIdentity): Message {
        val isOwn = (identity.userId.isNotEmpty() && senderId == identity.userId) ||
            (identity.username.isNotEmpty() && senderUsername == identity.username)
        return Message(
            id = id,
            conversationId = conversationId,
            senderId = senderId,
            senderUsername = senderUsername,
            senderDisplayName = senderDisplayName,
            seq = seq,
            clientMessageId = clientMessageId,
            type = type,
            content = content,
            replyToId = replyToId,
            replyTo = replyTo?.toDomain(),
            reactions = reactions.map { it.toDomain() },
            createdAt = parseInstant(createdAt),
            editedAt = parseInstant(editedAt),
            deletedAt = parseInstant(deletedAt),
            deleted = deleted,
            isOwn = isOwn,
            sendStatus = MessageSendStatus.entries.firstOrNull { it.name == sendStatus } ?: MessageSendStatus.Sent,
            aiGenerated = aiGenerated,
        )
    }

    companion object {
        fun fromJson(json: JsonObject) = MessageDto(
            id = json.string("id").orEmpty(),
            conversationId = json.string("conversationId").orEmpty(),
            senderId = json.string("senderId").orEmpty(),
            senderUsername = json.string("senderUsername").orEmpty(),
            senderDisplayName = json.string("senderDisplayName"),
            seq = json.long("seq") ?: 0,
            clientMessageId = json.string("clientMessageId").orEmpty(),
            type = json.string("type") ?: "TEXT",
            content = json.string("content"),
            replyToId = json.string("replyToId"),
            replyTo = json.obj("replyTo")?.let(MessageReplyDto::fromJson),
            reactions = json.array("reactions").objects().map(MessageReactionDto::fromJson),
            createdAt = json.string("createdAt"),
            editedAt = json.string("editedAt"),
            deletedAt = json.string("deletedAt"),
            deleted = json.bool("deleted") ?: false,
            aiGenerated = json.bool("aiGenerated") ?: false,
        )

        fun pageFromJson(json: JsonObject): List<MessageDto> =
            (json.array("items") ?: json.array("content")).objects().map(::fromJson)

        /** Cache keeps local send state so failed messages survive restarts. */
        fun fromDomain(m: Message) = MessageDto(
            id = m.id,
            conversationId = m.conversationId,
            senderId = m.senderId,
            senderUsername = m.senderUsername,
            senderDisplayName = m.senderDisplayName,
            seq = m.seq,
            clientMessageId = m.clientMessageId,
            type = m.type,
            content = m.content,
            replyToId = m.replyToId,
            replyTo = m.replyTo?.let { MessageReplyDto(it.id, it.senderId, it.senderUsername, it.senderDisplayName, it.content, it.deleted) },
            reactions = m.reactions.map { r -> MessageReactionDto(r.emoji, r.count, r.users.map { ReactionUserDto(it.id, it.username, it.displayName) }) },
            createdAt = m.createdAt?.toString(),
            editedAt = m.editedAt?.toString(),
            deletedAt = m.deletedAt?.toString(),
            deleted = m.deleted,
            aiGenerated = m.aiGenerated,
            sendStatus = m.sendStatus.name,
        )
    }
}

@Serializable
data class SendMessageRequestDto(
    val conversationId: String,
    val clientMessageId: String,
    val content: String,
    val type: String = "TEXT",
    val replyToId: String? = null,
)
