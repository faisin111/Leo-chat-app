package com.leochat.feature.chat.presentation

import com.leochat.feature.chat.presentation.newconversation.NewConversationScreen
import com.leochat.core.common.constants.ContactsStrings
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.ConversationMember
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.usecase.SharedMediaBuckets
import com.leochat.feature.chat.domain.usecase.SharedMediaRules
import com.leochat.feature.users.domain.model.UserSearchItem

/** One-shot UI effects for the contacts screens. */
sealed interface ContactsEffect {
    data class ShowError(val message: String) : ContactsEffect
    data class ShowSuccess(val message: String) : ContactsEffect
    data class ConversationOpened(val conversation: Conversation) : ContactsEffect
}

/** People search: raw [query] as typed, results for its trimmed value. */
data class UserSearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<UserSearchItem> = emptyList(),
    val errorMessage: String? = null,
) {
    val isActive: Boolean get() = query.trim().isNotEmpty()
}

/** Contacts tab / new conversation. Mirrors `NewConversationScreen` local and watched state. */
data class NewConversationUiState(
    val search: UserSearchUiState = UserSearchUiState(),
    val conversations: List<Conversation> = emptyList(),
    val isLoadingRecent: Boolean = true,
    val isStartingChat: Boolean = false,
) {
    /** Direct, non-AI chats shown under RECENT CONTACTS. */
    val recentDirectChats: List<Conversation> get() = conversations.filter { !it.isGroup && !it.isAi }
}

data class NewGroupUiState(
    val title: String = "",
    val search: UserSearchUiState = UserSearchUiState(),
    val selected: List<UserSearchItem> = emptyList(),
    val isCreating: Boolean = false,
) {
    val canCreate: Boolean get() = title.trim().isNotEmpty() && selected.isNotEmpty() && !isCreating

    fun isSelected(user: UserSearchItem): Boolean = selected.any { it.id == user.id }
}

data class GroupDetailsUiState(
    val conversationId: String? = null,
    val conversation: Conversation? = null,
    val currentUserId: String = "",
    val members: List<ConversationMember> = emptyList(),
    val isLoadingMembers: Boolean = false,
    val membersErrorMessage: String? = null,
    val isMuted: Boolean = false,
    val isAddSheetOpen: Boolean = false,
    val addSearch: UserSearchUiState = UserSearchUiState(),
    val isAddingMember: Boolean = false,
) {
    val title: String get() = conversation?.effectiveTitle ?: ContactsStrings.groupDetails
    val description: String get() = conversation?.lastMessageSnippet.orEmpty()

    val isOwnerOrAdmin: Boolean
        get() = members.any { it.userId == currentUserId && it.role.uppercase().let { r -> r == OWNER || r == ADMIN } }

    fun isSelf(member: ConversationMember): Boolean = member.userId == currentUserId

    fun canRemove(member: ConversationMember): Boolean =
        isOwnerOrAdmin && !isSelf(member) && member.role.uppercase() != OWNER

    /** Search hits that are not already members. */
    val eligibleToAdd: List<UserSearchItem>
        get() {
            val ids = members.map { it.userId }.toSet()
            return addSearch.results.filter { it.id !in ids }
        }

    companion object {
        const val OWNER = "OWNER"
        const val ADMIN = "ADMIN"
    }
}

enum class SharedMediaFilter { Media, Files, Links }

data class SharedMediaUiState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val filter: SharedMediaFilter = SharedMediaFilter.Media,
) {
    val title: String get() = conversation?.effectiveTitle ?: ContactsStrings.sharedMediaAndFiles
    val buckets: SharedMediaBuckets get() = SharedMediaRules.classify(messages)
}
