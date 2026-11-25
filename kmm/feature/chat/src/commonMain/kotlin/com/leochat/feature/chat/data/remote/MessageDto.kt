package com.leochat.feature.chat.data.remote

import com.leochat.core.network.objects
import com.leochat.core.common.util.parseInstant
import com.leochat.core.network.array
import com.leochat.core.network.bool
import com.leochat.core.network.firstString
import com.leochat.core.network.int
import com.leochat.core.network.long
import com.leochat.core.network.obj
import com.leochat.core.network.string
import com.leochat.feature.chat.domain.model.CurrentIdentity
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.model.MessageAttachment
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

@Serializable
data class MessageAttachmentDto(
    val id: String = "",
    val storageKey: String,
    val fileName: String? = null,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
) {
    fun toDomain() = MessageAttachment(
        id = id,
        storageKey = storageKey,
        fileName = fileName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
    )

    companion object {
        fun fromJson(json: JsonObject): MessageAttachmentDto? {
            val storageKey = json.string("storageKey")?.takeIf { it.isNotBlank() } ?: return null
            return MessageAttachmentDto(
                id = json.string("id").orEmpty(),
                storageKey = storageKey,
                fileName = json.string("fileName"),
                mimeType = json.string("mimeType"),
                sizeBytes = json.long("sizeBytes"),
            )
        }

        fun fromDomain(a: MessageAttachment) = MessageAttachmentDto(
            id = a.id,
            storageKey = a.storageKey,
            fileName = a.fileName,
            mimeType = a.mimeType,
            sizeBytes = a.sizeBytes,
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
    val attachments: List<MessageAttachmentDto> = emptyList(),
    val reactions: List<MessageReactionDto> = emptyList(),
    val createdAt: String? = null,
    val editedAt: String? = null,
    val deletedAt: String? = null,
    val deleted: Boolean = false,
    val aiGenerated: Boolean = false,
    val sendStatus: String = "Sent",
) {
    /** `isOwn` matches by id first, then username. AI messages are never own. */
    fun toDomain(identity: CurrentIdentity): Message {
        val isAi = aiGenerated || senderUsername.equals("ai", ignoreCase = true) || senderUsername.equals("LeoChat AI", ignoreCase = true)
        val isOwn = !isAi && ((identity.userId.isNotEmpty() && senderId == identity.userId) ||
            (identity.username.isNotEmpty() && senderUsername == identity.username))
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
            attachments = attachments.map { it.toDomain() },
            reactions = reactions.map { it.toDomain() },
            createdAt = parseInstant(createdAt),
            editedAt = parseInstant(editedAt),
            deletedAt = parseInstant(deletedAt),
            deleted = deleted,
            isOwn = isOwn,
            sendStatus = MessageSendStatus.entries.firstOrNull { it.name == sendStatus } ?: MessageSendStatus.Sent,
            aiGenerated = isAi,
        )
    }

    companion object {
        fun fromJson(json: JsonObject): MessageDto {
            val nestedSender = (json["sender"] ?: json["user"]) as? JsonObject
            val senderId = json.string("senderId") ?: nestedSender?.string("id").orEmpty()
            val senderUsername = json.string("senderUsername") ?: nestedSender?.firstString("username", "userName").orEmpty()
            val senderDisplayName = json.string("senderDisplayName") ?: nestedSender?.firstString("displayName", "name")
            val isAi = json.bool("aiGenerated") == true ||
                senderUsername.equals("ai", ignoreCase = true) ||
                senderUsername.equals("LeoChat AI", ignoreCase = true)

            val parsedAttachments = json.array("attachments").objects().mapNotNull(MessageAttachmentDto::fromJson)
                .ifEmpty {
                    json.string("storageKey")?.takeIf { it.isNotBlank() }?.let { key ->
                        listOf(
                            MessageAttachmentDto(
                                storageKey = key,
                                fileName = json.string("fileName"),
                                mimeType = json.string("mimeType"),
                                sizeBytes = json.long("sizeBytes"),
                            ),
                        )
                    }.orEmpty()
                }

            return MessageDto(
                id = json.string("id").orEmpty(),
                conversationId = json.string("conversationId").orEmpty(),
                senderId = senderId,
                senderUsername = senderUsername,
                senderDisplayName = senderDisplayName,
                seq = json.long("seq") ?: 0,
                clientMessageId = json.string("clientMessageId").orEmpty(),
                type = json.string("type") ?: "TEXT",
                content = json.string("content"),
                replyToId = json.string("replyToId"),
                replyTo = json.obj("replyTo")?.let(MessageReplyDto::fromJson),
                attachments = parsedAttachments,
                reactions = json.array("reactions").objects().map(MessageReactionDto::fromJson),
                createdAt = json.string("createdAt"),
                editedAt = json.string("editedAt"),
                deletedAt = json.string("deletedAt"),
                deleted = json.bool("deleted") ?: false,
                aiGenerated = isAi,
            )
        }

        fun listFromJson(element: kotlinx.serialization.json.JsonElement): List<MessageDto> = when (element) {
            is kotlinx.serialization.json.JsonArray -> element.objects().map(::fromJson)
            is JsonObject -> (element.array("items") ?: element.array("content") ?: element.array("data") ?: element.array("messages")).objects().map(::fromJson)
            else -> emptyList()
        }

        fun pageFromJson(json: JsonObject): List<MessageDto> = listFromJson(json)

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
            attachments = m.attachments.map(MessageAttachmentDto::fromDomain),
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
    val storageKey: String? = null,
    val fileName: String? = null,
    val mimeType: String? = null,
    val sizeBytes: Long? = null,
)
