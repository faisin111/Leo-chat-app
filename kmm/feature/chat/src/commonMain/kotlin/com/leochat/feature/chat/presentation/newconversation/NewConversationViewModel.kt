package com.leochat.feature.chat.presentation.newconversation

import com.leochat.feature.chat.presentation.ContactsEffectViewModel
import com.leochat.feature.chat.presentation.UserSearchRunner
import androidx.lifecycle.viewModelScope
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.usecase.ChatUseCaseBundle
import com.leochat.feature.chat.domain.usecase.MergeConversationIntoCacheUseCase
import com.leochat.feature.chat.domain.usecase.StartDirectMessageUseCase
import com.leochat.feature.chat.presentation.ContactsEffect
import com.leochat.feature.chat.presentation.NewConversationUiState
import com.leochat.feature.users.domain.model.UserSearchItem
import com.leochat.feature.users.domain.usecase.SearchUsersUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Contacts tab / new conversation: people search, recent direct chats (cache-first with
 * background refresh), and starting a direct message. Mirrors `NewConversationScreen`.
 */
class NewConversationViewModel(
    private val chat: ChatUseCaseBundle,
    private val startDirectMessage: StartDirectMessageUseCase,
    private val mergeIntoCache: MergeConversationIntoCacheUseCase,
    searchUsers: SearchUsersUseCase,
) : ContactsEffectViewModel<NewConversationUiState>(NewConversationUiState()) {

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
