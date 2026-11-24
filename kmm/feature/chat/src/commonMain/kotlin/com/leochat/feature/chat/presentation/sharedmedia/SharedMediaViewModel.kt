package com.leochat.feature.chat.presentation.sharedmedia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.feature.chat.domain.usecase.ChatUseCaseBundle
import com.leochat.feature.chat.domain.usecase.ResolveConversationUseCase
import com.leochat.feature.chat.presentation.SharedMediaFilter
import com.leochat.feature.chat.presentation.SharedMediaUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Media, files, and links of one conversation. Cache-first message page with a background
 * refresh; refresh errors keep the cached page. Mirrors `SharedMediaScreen`.
 */
class SharedMediaViewModel(
    private val chat: ChatUseCaseBundle,
    private val resolveConversation: ResolveConversationUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(SharedMediaUiState())
    val state: StateFlow<SharedMediaUiState> = mutableState.asStateFlow()
    private var loadedId: String? = null

    fun load(conversationId: String) {
        if (conversationId.isEmpty() || loadedId == conversationId) return
        loadedId = conversationId
        mutableState.value = SharedMediaUiState()
        viewModelScope.launch {
            resolveConversation(conversationId).onRight { c -> mutableState.update { it.copy(conversation = c) } }
            val cached = chat.getCachedMessages(conversationId)
            mutableState.update { it.copy(messages = cached, isLoading = cached.isEmpty()) }
            chat.getMessages(conversationId).fold(
                { failure -> mutableState.update { it.copy(isLoading = false, errorMessage = failure.message) } },
                { messages -> mutableState.update { it.copy(isLoading = false, messages = messages, errorMessage = null) } },
            )
        }
    }

    fun setFilter(filter: SharedMediaFilter) = mutableState.update { it.copy(filter = filter) }
}
