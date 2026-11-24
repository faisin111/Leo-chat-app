package com.leochat.features.ai.presentation.states

import com.leochat.features.ai.domain.entities.AiActionRequest
import com.leochat.features.ai.domain.entities.AiMessage
import com.leochat.features.ai.domain.entities.AiModelInfo

/** Immutable Leo AI tab state. Mirrors Flutter `AiState`. */
data class AiState(
    val selectedModel: AiModelInfo = AiModelInfo.defaultModel,
    val messages: List<AiMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val conversationId: String? = null,
    val errorMessage: String? = null,
    val isDeepThinkEnabled: Boolean = false,
    val isWebSearchEnabled: Boolean = true,
    val pendingAction: AiActionRequest? = null,
) {
    /** Suggestion chips show while the history is short and nothing is generating. */
    val showSuggestions: Boolean get() = messages.size <= 2 && !isGenerating
    val isEmpty: Boolean get() = messages.isEmpty()
}
