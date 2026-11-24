package com.leochat.features.ai.presentation.controllers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.core.constants.AiStrings
import com.leochat.features.ai.domain.entities.AiActionRequest
import com.leochat.features.ai.domain.entities.AiMessage
import com.leochat.features.ai.domain.entities.AiMessageRole
import com.leochat.features.ai.domain.entities.AiModelInfo
import com.leochat.features.ai.domain.usecases.AiUseCaseBundle
import com.leochat.features.ai.presentation.states.AiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

/**
 * Leo AI conversation. Mirrors `AiNotifier`: get-or-create the AI conversation,
 * load history (cache first, then server), send prompts, and run confirmable actions.
 */
class AiController(private val useCases: AiUseCaseBundle) : ViewModel() {
    private val mutableState = MutableStateFlow(AiState())
    val state: StateFlow<AiState> = mutableState.asStateFlow()

    init {
        initConversation()
    }

    fun initConversation(): Job = viewModelScope.launch {
        val cached = useCases.getCachedHistory()
        if (cached.isNotEmpty() && state.value.messages.isEmpty()) {
            mutableState.update { it.copy(messages = cached) }
        }
        useCases.getOrCreateConversation().fold(
            ifLeft = { failure -> mutableState.update { it.copy(errorMessage = failure.message) } },
            ifRight = { conversation ->
                mutableState.update { it.copy(conversationId = conversation.id, errorMessage = null) }
                refreshHistory(conversation.id)
            },
        )
    }

    /** Background refresh; only replaces the list if the user has not sent anything meanwhile. */
    private suspend fun refreshHistory(conversationId: String) {
        val before = state.value.messages
        useCases.getHistory(conversationId).onRight { messages ->
            if (messages.isNotEmpty() && state.value.messages === before && !state.value.isGenerating) {
                mutableState.update { it.copy(messages = messages) }
            }
        }
    }

    fun selectModel(model: AiModelInfo) = mutableState.update { it.copy(selectedModel = model) }

    fun toggleDeepThink() = mutableState.update { it.copy(isDeepThinkEnabled = !it.isDeepThinkEnabled) }

    fun toggleWebSearch() = mutableState.update { it.copy(isWebSearchEnabled = !it.isWebSearchEnabled) }

    fun clearHistory(): Job = deleteConversation()

    fun deleteConversation(): Job = viewModelScope.launch {
        mutableState.update { it.copy(isGenerating = true) }
        useCases.deleteConversation().fold(
            ifLeft = { failure -> mutableState.update { it.copy(isGenerating = false, errorMessage = failure.message) } },
            ifRight = {
                // The server conversation is gone; the next prompt creates a fresh one.
                mutableState.update {
                    it.copy(
                        isGenerating = false,
                        messages = emptyList(),
                        conversationId = null,
                        errorMessage = null,
                        pendingAction = null,
                    )
                }
            },
        )
    }

    fun sendMessage(text: String): Job? {
        val prompt = text.trim()
        if (prompt.isEmpty() || state.value.isGenerating) return null

        val clientMessageId = "ai-client-${nowMillis()}"
        val userMessage = AiMessage(
            id = clientMessageId,
            role = AiMessageRole.User,
            content = prompt,
            createdAt = Clock.System.now(),
        )
        mutableState.update {
            it.copy(messages = it.messages + userMessage, isGenerating = true, errorMessage = null)
        }

        return viewModelScope.launch {
            var conversationId = state.value.conversationId
            if (conversationId.isNullOrEmpty()) {
                useCases.getOrCreateConversation().onRight { conversation ->
                    conversationId = conversation.id
                    mutableState.update { it.copy(conversationId = conversation.id) }
                }
            }
            val target = conversationId
            if (target.isNullOrEmpty()) {
                appendAssistant(errorMessage(AiStrings.conversationUnavailable))
                return@launch
            }

            useCases.chat(target, prompt, clientMessageId).fold(
                ifLeft = { failure ->
                    appendAssistant(errorMessage(AiStrings.serverError(failure.message)), failure.message)
                },
                ifRight = { reply ->
                    appendAssistant(reply.copy(role = AiMessageRole.Assistant, modelId = state.value.selectedModel.id))
                    useCases.saveHistory(state.value.messages)
                },
            )
        }
    }

    fun executeAction(request: AiActionRequest): Job = viewModelScope.launch {
        mutableState.update { it.copy(isGenerating = true) }
        useCases.executeAction(request).fold(
            ifLeft = { failure -> mutableState.update { it.copy(isGenerating = false, errorMessage = failure.message) } },
            ifRight = { result ->
                if (result.confirmationRequired) {
                    mutableState.update { it.copy(isGenerating = false, pendingAction = request) }
                } else {
                    val content = AiStrings.actionCompleted(
                        action = result.action ?: AiStrings.defaultActionName,
                        message = result.message ?: AiStrings.defaultActionMessage,
                        data = result.data.toString(),
                    )
                    val message = AiMessage(
                        id = "act-${nowMillis()}",
                        role = AiMessageRole.Assistant,
                        content = content,
                        createdAt = Clock.System.now(),
                    )
                    mutableState.update {
                        it.copy(isGenerating = false, messages = it.messages + message, pendingAction = null)
                    }
                }
            },
        )
    }

    fun confirmPendingAction(): Job? {
        val pending = state.value.pendingAction ?: return null
        return executeAction(pending.copy(confirmed = true))
    }

    fun cancelPendingAction() = mutableState.update { it.copy(pendingAction = null) }

    private fun appendAssistant(message: AiMessage, error: String? = null) {
        mutableState.update {
            it.copy(
                messages = it.messages + message,
                isGenerating = false,
                errorMessage = error ?: it.errorMessage,
            )
        }
    }

    private fun errorMessage(content: String) = AiMessage(
        id = "err-${nowMillis()}",
        role = AiMessageRole.Assistant,
        content = content,
        createdAt = Clock.System.now(),
    )

    private fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
}
