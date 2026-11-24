package com.leochat.feature.chat.presentation.newgroup

import com.leochat.feature.chat.presentation.ContactsEffectViewModel
import com.leochat.feature.chat.presentation.UserSearchRunner
import androidx.lifecycle.viewModelScope
import com.leochat.core.common.constants.ContactsStrings
import com.leochat.feature.chat.domain.usecase.CreateGroupUseCase
import com.leochat.feature.chat.domain.usecase.MergeConversationIntoCacheUseCase
import com.leochat.feature.chat.presentation.ContactsEffect
import com.leochat.feature.chat.presentation.NewGroupUiState
import com.leochat.feature.users.domain.model.UserSearchItem
import com.leochat.feature.users.domain.usecase.SearchUsersUseCase
import kotlinx.coroutines.launch

/** Group title, member selection, and creation. Mirrors `NewGroupScreen`. */
class NewGroupViewModel(
    private val createGroup: CreateGroupUseCase,
    private val mergeIntoCache: MergeConversationIntoCacheUseCase,
    searchUsers: SearchUsersUseCase,
) : ContactsEffectViewModel<NewGroupUiState>(NewGroupUiState()) {

    private val searchRunner = UserSearchRunner(viewModelScope, searchUsers) { transform ->
        update { it.copy(search = transform(it.search)) }
    }

    fun setTitle(title: String) = update { it.copy(title = title) }

    fun search(query: String) = searchRunner.search(query)

    fun toggle(user: UserSearchItem) = update { s ->
        s.copy(selected = if (s.isSelected(user)) s.selected.filterNot { it.id == user.id } else s.selected + user)
    }

    fun create() {
        val current = state.value
        if (!current.canCreate) return
        val title = current.title.trim()
        update { it.copy(isCreating = true) }
        viewModelScope.launch {
            val result = createGroup(title, current.selected.map { it.id })
            update { it.copy(isCreating = false) }
            result.fold(
                { failure -> emit(ContactsEffect.ShowError(failure.message)) },
                { conversation ->
                    mergeIntoCache(conversation)
                    emit(ContactsEffect.ShowSuccess(ContactsStrings.groupCreated(title)))
                    emit(ContactsEffect.ConversationOpened(conversation))
                },
            )
        }
    }
}
