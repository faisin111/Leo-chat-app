package com.leochat.features.chat.presentation.controllers

import androidx.lifecycle.viewModelScope
import com.leochat.core.constants.ContactsStrings
import com.leochat.features.chat.domain.usecases.CreateGroupUseCase
import com.leochat.features.chat.domain.usecases.MergeConversationIntoCacheUseCase
import com.leochat.features.chat.presentation.states.ContactsEffect
import com.leochat.features.chat.presentation.states.NewGroupState
import com.leochat.features.users.domain.entities.UserSearchItem
import com.leochat.features.users.domain.usecases.SearchUsersUseCase
import kotlinx.coroutines.launch

/** Group title, member selection, and creation. Mirrors `NewGroupPage`. */
class NewGroupController(
    private val createGroup: CreateGroupUseCase,
    private val mergeIntoCache: MergeConversationIntoCacheUseCase,
    searchUsers: SearchUsersUseCase,
) : ContactsEffectController<NewGroupState>(NewGroupState()) {

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
