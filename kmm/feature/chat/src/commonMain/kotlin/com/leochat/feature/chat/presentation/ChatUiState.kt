package com.leochat.feature.chat.presentation

import com.leochat.feature.auth.domain.model.User
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.model.RealtimeStatus

enum class ChatFilter { All, Unread, Groups }

/** Immutable chat screen state. Mirrors `ChatUiState` in Flutter. */
data class ChatUiState(
    val currentUser: User? = null,
    val isLoadingConversations: Boolean = true,
    val conversations: List<Conversation> = emptyList(),
    val filter: ChatFilter = ChatFilter.All,
    val searchQuery: String = "",
    val totalUnreadCount: Int = 0,
    val selectedConversation: Conversation? = null,
    val isLoadingMessages: Boolean = false,
    val messages: List<Message> = emptyList(),
    val typingStatusText: String? = null,
    val realtimeStatus: RealtimeStatus = RealtimeStatus.Disconnected,
    val errorMessage: String? = null,
    val isSigningOut: Boolean = false,
    val signedOut: Boolean = false,
) {
    val selectedConversationId: String? get() = selectedConversation?.id

    val isReconnecting: Boolean get() = realtimeStatus == RealtimeStatus.Connecting

    /** List after the filter pill and the local search text. */
    val displayedConversations: List<Conversation>
        get() {
            var list = when (filter) {
                ChatFilter.All -> conversations
                ChatFilter.Unread -> conversations.filter { it.unreadCount > 0 }
                ChatFilter.Groups -> conversations.filter { it.isGroup }
            }
            val q = searchQuery.trim().lowercase()
            if (q.isNotEmpty()) {
                list = list.filter {
                    it.effectiveTitle.lowercase().contains(q) || it.lastMessageSnippet.orEmpty().lowercase().contains(q)
                }
            }
            return list
        }

    val userDisplayName: String
        get() = currentUser?.let { it.displayName.ifBlank { it.username } }?.ifBlank { null } ?: "You"
}
