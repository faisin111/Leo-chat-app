package com.leochat.feature.chat.presentation.groupdetails

import com.leochat.feature.chat.presentation.ContactsEffectViewModel
import com.leochat.feature.chat.presentation.UserSearchRunner
import androidx.lifecycle.viewModelScope
import com.leochat.core.common.constants.ContactsStrings
import com.leochat.feature.chat.domain.model.ConversationMember
import com.leochat.feature.chat.domain.usecase.AddGroupMembersUseCase
import com.leochat.feature.chat.domain.usecase.ChatUseCaseBundle
import com.leochat.feature.chat.domain.usecase.GetGroupMembersUseCase
import com.leochat.feature.chat.domain.usecase.MergeConversationIntoCacheUseCase
import com.leochat.feature.chat.domain.usecase.RemoveGroupMemberUseCase
import com.leochat.feature.chat.domain.usecase.ResolveConversationUseCase
import com.leochat.feature.chat.domain.usecase.SetConversationMutedUseCase
import com.leochat.feature.chat.presentation.ContactsEffect
import com.leochat.feature.chat.presentation.GroupDetailsUiState
import com.leochat.feature.users.domain.model.UserSearchItem
import com.leochat.feature.users.domain.usecase.SearchUsersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Group header, mute, members, add/remove members. Mirrors `GroupDetailsScreen`. */
class GroupDetailsViewModel(
    private val chat: ChatUseCaseBundle,
    private val resolveConversation: ResolveConversationUseCase,
    private val getMembers: GetGroupMembersUseCase,
    private val addMembers: AddGroupMembersUseCase,
    private val removeGroupMember: RemoveGroupMemberUseCase,
    private val setMuted: SetConversationMutedUseCase,
    private val mergeIntoCache: MergeConversationIntoCacheUseCase,
    searchUsers: SearchUsersUseCase,
) : ContactsEffectViewModel<GroupDetailsUiState>(GroupDetailsUiState()) {

    private val addSearchRunner = UserSearchRunner(viewModelScope, searchUsers) { transform ->
        update { it.copy(addSearch = transform(it.addSearch)) }
    }

    /** Loads the group once per id: identity, conversation header, then members. */
    fun load(conversationId: String) {
        if (conversationId.isEmpty() || state.value.conversationId == conversationId) return
        update { GroupDetailsUiState(conversationId = conversationId) }
        viewModelScope.launch {
            val identity = chat.getIdentity()
            update { it.copy(currentUserId = identity.userId) }
            resolveConversation(conversationId).onRight { conversation ->
                update { it.copy(conversation = conversation, isMuted = conversation.isMuted) }
            }
            loadMembers().join()
        }
    }

    fun loadMembers(): Job = viewModelScope.launch {
        val id = state.value.conversationId ?: return@launch
        update { it.copy(isLoadingMembers = true, membersErrorMessage = null) }
        getMembers(id).fold(
            { failure -> update { it.copy(isLoadingMembers = false, membersErrorMessage = failure.message) } },
            { members -> update { it.copy(isLoadingMembers = false, members = members) } },
        )
    }

    fun toggleMute() {
        val id = state.value.conversationId ?: return
        val mute = !state.value.isMuted
        viewModelScope.launch {
            setMuted(id, mute).fold(
                { failure -> emit(ContactsEffect.ShowError(failure.message)) },
                {
                    update { it.copy(isMuted = mute, conversation = it.conversation?.copy(isMuted = mute)) }
                    state.value.conversation?.let { mergeIntoCache(it) }
                    emit(ContactsEffect.ShowSuccess(if (mute) ContactsStrings.notificationsMuted else ContactsStrings.notificationsUnmuted))
                },
            )
        }
    }

    fun removeMember(member: ConversationMember) {
        val id = state.value.conversationId ?: return
        viewModelScope.launch {
            removeGroupMember(id, member.userId).fold(
                { failure -> emit(ContactsEffect.ShowError(failure.message)) },
                {
                    update { s -> s.copy(members = s.members.filterNot { it.userId == member.userId }) }
                    emit(ContactsEffect.ShowSuccess(ContactsStrings.removedFromGroup(member.effectiveName)))
                },
            )
        }
    }

    fun openAddSheet() {
        if (state.value.conversationId == null) return
        update { it.copy(isAddSheetOpen = true) }
    }

    fun closeAddSheet() {
        addSearchRunner.clear()
        update { it.copy(isAddSheetOpen = false) }
    }

    fun searchToAdd(query: String) = addSearchRunner.search(query)

    fun addMember(user: UserSearchItem) {
        val id = state.value.conversationId ?: return
        if (state.value.isAddingMember) return
        update { it.copy(isAddingMember = true) }
        viewModelScope.launch {
            val result = addMembers(id, listOf(user.id))
            update { it.copy(isAddingMember = false) }
            result.fold(
                { failure -> emit(ContactsEffect.ShowError(failure.message)) },
                {
                    closeAddSheet()
                    emit(ContactsEffect.ShowSuccess(ContactsStrings.addedToGroup(user.effectiveName)))
                    loadMembers()
                },
            )
        }
    }
}
