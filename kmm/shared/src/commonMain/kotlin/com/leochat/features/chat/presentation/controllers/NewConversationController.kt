package com.leochat.features.chat.presentation.controllers

import androidx.lifecycle.viewModelScope
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.domain.usecases.ChatUseCaseBundle
import com.leochat.features.chat.domain.usecases.MergeConversationIntoCacheUseCase
import com.leochat.features.chat.domain.usecases.StartDirectMessageUseCase
import com.leochat.features.chat.presentation.states.ContactsEffect
import com.leochat.features.chat.presentation.states.NewConversationState
import com.leochat.features.users.domain.entities.UserSearchItem
import com.leochat.features.users.domain.usecases.SearchUsersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Contacts tab / new conversation: people search, recent direct chats (cache-first with
 * background refresh), and starting a direct message. Mirrors `NewConversationPage`.
 */
class NewConversationController(
    private val chat: ChatUseCaseBundle,
    private val startDirectMessage: StartDirectMessageUseCase,
    private val mergeIntoCache: MergeConversationIntoCacheUseCase,
    searchUsers: SearchUsersUseCase,
) : ContactsEffectController<NewConversationState>(NewConversationState()) {

    private val searchRunner = UserSearchRunner(viewModelScope, searchUsers) { transform ->
        update { it.copy(search = transform(it.search)) }
    }

    init {
        loadRecent()
    }

    fun loadRecent(): Job = viewModelScope.launch {
        val cached = chat.getCachedConversations()
        update { it.copy(conversations = cached.ifEmpty { it.conversations }, isLoadingRecent = cached.isEmpty() && it.conversations.isEmpty()) }
        chat.getConversations().fold(
            { update { it.copy(isLoadingRecent = false) } },
            { list -> update { it.copy(conversations = list, isLoadingRecent = false) } },
        )
    }

    fun search(query: String) = searchRunner.search(query)

    fun clearSearch() = searchRunner.clear()

    fun startChat(user: UserSearchItem) {
        if (state.value.isStartingChat) return
        update { it.copy(isStartingChat = true) }
        viewModelScope.launch {
            val result = startDirectMessage(user.id)
            update { it.copy(isStartingChat = false) }
            result.fold(
                { failure -> emit(ContactsEffect.ShowError(failure.message)) },
                { conversation ->
                    mergeIntoCache(conversation)
                    emit(ContactsEffect.ConversationOpened(conversation))
                },
            )
        }
    }

    fun openExisting(conversation: Conversation) = emit(ContactsEffect.ConversationOpened(conversation))
}
