package com.leochat.feature.chat.domain.model

import kotlinx.datetime.Instant

data class Conversation(
    val id: String,
    val title: String = "",
    val type: String = "DIRECT",
    val avatarUrl: String? = null,
    val directKey: String? = null,
    val createdBy: String? = null,
    val lastSeq: Long = 0,
    val lastMessageAt: Instant? = null,
    val createdAt: Instant? = null,
    val status: String = "ACTIVE",
    val targetUserId: String? = null,
    val targetUsername: String? = null,
    val targetDisplayName: String? = null,
    val targetAvatarUrl: String? = null,
    val unreadCount: Int = 0,
    val lastMessageSnippet: String? = null,
    val isOnline: Boolean = false,
    val isTyping: Boolean = false,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
) {
    val isGroup: Boolean get() = type.uppercase() == "GROUP"
    val isAi: Boolean get() = type.uppercase() == "AI"

    val preferredAvatarUrl: String?
        get() = listOf(avatarUrl, targetAvatarUrl).firstOrNull { !it.isNullOrBlank() }

    val effectiveTitle: String
        get() = when {
            isGroup && title.isNotBlank() -> title
            !targetDisplayName.isNullOrBlank() -> targetDisplayName
            !targetUsername.isNullOrBlank() -> targetUsername
            title.isNotBlank() -> title
            else -> "Conversation"
        }

    /**
     * Resolves the peer user ID in a 1-on-1 direct chat using backend fields (`targetUserId`,
     * `directKey`, `createdBy`) while avoiding [currentUserId]. Returns null for group/AI chats.
     */
    fun resolveDirectTargetUserId(currentUserId: String?): String? {
        if (isGroup || isAi) return null
        val self = currentUserId?.trim()?.takeIf { it.isNotEmpty() }
        targetUserId?.trim()?.takeIf { it.isNotEmpty() && (self == null || !it.equals(self, ignoreCase = true)) }?.let { return it }
        extractOtherUserFromDirectKey(directKey, self)?.let { return it }
        if (self != null) {
            createdBy?.trim()?.takeIf { it.isNotEmpty() && !it.equals(self, ignoreCase = true) }?.let { return it }
        }
        return null
    }

    companion object {
        private val uuidRegex = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")

        fun extractOtherUserFromDirectKey(directKey: String?, currentUserId: String?): String? {
            if (directKey.isNullOrBlank()) return null
            val self = currentUserId?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            val uuids = uuidRegex.findAll(directKey).map { it.value }.toList()
            if (uuids.isNotEmpty()) {
                return uuids.firstOrNull { !it.equals(self, ignoreCase = true) }
            }
            val parts = directKey.split(':', '_', '|').map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.size == 2 && parts.any { it.equals(self, ignoreCase = true) }) {
                return parts.firstOrNull { !it.equals(self, ignoreCase = true) }
            }
            return null
        }
    }
}

data class ConversationMember(
    val conversationId: String,
    val userId: String,
    val role: String,
    val lastReadSeq: Long = 0,
    val username: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
) {
    val isOwner: Boolean get() = role == "OWNER"
    val isAdmin: Boolean get() = role == "ADMIN" || isOwner
    val effectiveName: String get() = displayName?.trim()?.takeIf { it.isNotEmpty() } ?: username ?: "Member"
}
