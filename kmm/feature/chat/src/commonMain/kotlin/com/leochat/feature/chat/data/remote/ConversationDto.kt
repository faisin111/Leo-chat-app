package com.leochat.feature.chat.data.remote

import com.leochat.core.network.objects
import com.leochat.core.common.util.parseInstant
import com.leochat.core.network.array
import com.leochat.core.network.bool
import com.leochat.core.network.firstString
import com.leochat.core.network.int
import com.leochat.core.network.long
import com.leochat.core.network.string
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.ConversationMember
import com.leochat.feature.chat.domain.model.Message
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/** Conversation wire + cache model. Parsing mirrors the lenient Flutter `ConversationDto.fromJson`. */
@Serializable
data class ConversationDto(
    val id: String,
    val type: String = "DIRECT",
    val title: String = "",
    val avatarUrl: String? = null,
    val directKey: String? = null,
    val createdBy: String? = null,
    val lastSeq: Long = 0,
    val lastMessageAt: String? = null,
    val createdAt: String? = null,
    val status: String = "ACTIVE",
    val targetUserId: String? = null,
    val targetUsername: String? = null,
    val targetDisplayName: String? = null,
    val targetAvatarUrl: String? = null,
    val unreadCount: Int = 0,
    val lastMessageSnippet: String? = null,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
) {
    fun toDomain() = Conversation(
        id = id,
        title = title,
        type = type,
        avatarUrl = avatarUrl,
        directKey = directKey,
        createdBy = createdBy,
        lastSeq = lastSeq,
        lastMessageAt = parseInstant(lastMessageAt),
        createdAt = parseInstant(createdAt),
        status = status,
        targetUserId = targetUserId,
        targetUsername = targetUsername,
        targetDisplayName = targetDisplayName,
        targetAvatarUrl = targetAvatarUrl,
        unreadCount = unreadCount,
        lastMessageSnippet = lastMessageSnippet,
        isPinned = isPinned,
        isMuted = isMuted,
    )

    companion object {
        private val targetKeys = listOf("targetUser", "target", "otherUser", "participant", "user", "contact", "recipient")

        fun fromDomain(c: Conversation) = ConversationDto(
            id = c.id,
            type = c.type,
            title = c.title,
            avatarUrl = c.avatarUrl,
            directKey = c.directKey,
            createdBy = c.createdBy,
            lastSeq = c.lastSeq,
            lastMessageAt = c.lastMessageAt?.toString(),
            createdAt = c.createdAt?.toString(),
            status = c.status,
            targetUserId = c.targetUserId,
            targetUsername = c.targetUsername,
            targetDisplayName = c.targetDisplayName,
            targetAvatarUrl = c.targetAvatarUrl,
            unreadCount = c.unreadCount,
            lastMessageSnippet = c.lastMessageSnippet,
            isPinned = c.isPinned,
            isMuted = c.isMuted,
        )

        fun fromJson(json: JsonObject, currentUserId: String? = null): ConversationDto {
            val selfId = currentUserId?.trim()?.takeIf { it.isNotEmpty() }
            val nestedTarget = targetKeys.firstNotNullOfOrNull { json[it] as? JsonObject }
            val membersArray = (json["members"] ?: json["participants"] ?: json["users"]) as? JsonArray
            // In direct chats, select the participant that is NOT the current logged-in user
            val nestedMember = membersArray?.objects()?.firstOrNull { m ->
                val mId = m.firstString("id", "userId")
                selfId == null || (mId != null && !mId.equals(selfId, ignoreCase = true))
            } ?: membersArray?.objects()?.firstOrNull()?.takeIf { m ->
                val mId = m.firstString("id", "userId")
                selfId == null || mId == null || !mId.equals(selfId, ignoreCase = true)
            }

            val target = nestedTarget ?: nestedMember
            fun t(vararg keys: String) = target?.firstString(*keys)
            val lastMessage = json["lastMessage"] as? JsonObject
            val lastMessageType = lastMessage?.string("type")
            val lastMessageContent = lastMessage?.string("content")
            val lastMessageDeleted = lastMessage?.bool("deleted") == true
            val lastMessageSenderId = lastMessage?.string("senderId")
            val lastMessageCreatedAt = lastMessage?.string("createdAt")
            val type = json.string("type") ?: "DIRECT"
            val directKey = json.string("directKey")
            val createdBy = json.string("createdBy")
            val rawTitle = json.firstString("title", "name")
            val targetDisplayName = json.firstString("targetDisplayName", "displayName") ?: t("displayName", "name")
            val targetUsername = json.firstString("targetUsername", "username") ?: t("username", "userName")
            val rawTargetUserId = json.firstString("targetUserId") ?: t("id", "userId")
            val targetUserId = rawTargetUserId?.takeIf { selfId == null || !it.equals(selfId, ignoreCase = true) }
                ?: if (type.equals("DIRECT", ignoreCase = true)) {
                    Conversation.extractOtherUserFromDirectKey(directKey, selfId)
                        ?: createdBy?.takeIf { selfId != null && !it.equals(selfId, ignoreCase = true) }
                        ?: lastMessageSenderId?.takeIf {
                            selfId != null && !it.equals(selfId, ignoreCase = true) &&
                                !lastMessageType.equals("SYSTEM", ignoreCase = true)
                        }
                } else null
            val targetAvatarUrl = json.firstString("targetAvatarUrl")
                ?: t("avatarUrl", "avatar", "profileImageUrl", "profileImage", "imageUrl", "photoUrl")

            // For DIRECT conversations, prefer the target user's name as the title
            val resolvedTitle = when {
                type.equals("GROUP", ignoreCase = true) -> rawTitle.orEmpty()
                type.equals("AI", ignoreCase = true) -> rawTitle ?: "LeoChat AI"
                !targetDisplayName.isNullOrBlank() -> targetDisplayName
                !targetUsername.isNullOrBlank() -> targetUsername
                !rawTitle.isNullOrBlank() -> rawTitle
                else -> t("displayName", "name", "username").orEmpty()
            }

            val isOwnLastMessage = selfId != null && lastMessageSenderId != null &&
                lastMessageSenderId.equals(selfId, ignoreCase = true)
            val resolvedLastMessageSnippet = json.firstString("lastMessageSnippet", "snippet")
                ?: lastMessage?.let {
                    Message.previewSnippet(lastMessageType, lastMessageContent, lastMessageDeleted, isOwn = isOwnLastMessage)
                }

            return ConversationDto(
                id = json.string("id").orEmpty(),
                type = type,
                title = resolvedTitle,
                avatarUrl = json.firstString("avatarUrl", "avatar", "imageUrl", "profileImageUrl", "profileImage"),
                directKey = directKey,
                createdBy = createdBy,
                lastSeq = json.long("lastSeq") ?: lastMessage?.long("seq") ?: 0,
                lastMessageAt = json.string("lastMessageAt") ?: lastMessageCreatedAt,
                createdAt = json.string("createdAt"),
                status = json.string("status") ?: "ACTIVE",
                targetUserId = targetUserId,
                targetUsername = targetUsername,
                targetDisplayName = targetDisplayName,
                targetAvatarUrl = targetAvatarUrl,
                unreadCount = json.int("unreadCount") ?: 0,
                lastMessageSnippet = resolvedLastMessageSnippet,
                isPinned = json.string("pinnedAt") != null,
                isMuted = json.string("mutedUntil") != null,
            )
        }

        fun listFromJson(element: kotlinx.serialization.json.JsonElement, currentUserId: String? = null): List<ConversationDto> = when (element) {
            is JsonArray -> element.objects().map { fromJson(it, currentUserId) }
            is JsonObject -> (element.array("items") ?: element.array("content") ?: element.array("data")).objects().map { fromJson(it, currentUserId) }
            else -> emptyList()
        }

        /** Accepts `items`, `content`, or `data` page envelopes. */
        fun pageFromJson(json: JsonObject): List<ConversationDto> = listFromJson(json)
    }
}

data class ConversationMemberDto(
    val conversationId: String,
    val userId: String,
    val role: String,
    val lastReadSeq: Long,
    val username: String?,
    val displayName: String?,
    val avatarUrl: String?,
) {
    fun toDomain() = ConversationMember(conversationId, userId, role, lastReadSeq, username, displayName, avatarUrl)

    companion object {
        fun fromJson(json: JsonObject) = ConversationMemberDto(
            conversationId = json.string("conversationId").orEmpty(),
            userId = json.string("userId") ?: json.string("id").orEmpty(),
            role = json.string("role") ?: "MEMBER",
            lastReadSeq = json.long("lastReadSeq") ?: 0,
            username = json.string("username"),
            displayName = json.string("displayName"),
            avatarUrl = json.string("avatarUrl") ?: json.string("profilePictureUrl"),
        )
    }
}
