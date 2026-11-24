package com.leochat.feature.ai.presentation.ai

import com.leochat.feature.ai.domain.model.AiActionRequest
import com.leochat.feature.ai.domain.model.AiMessage
import com.leochat.feature.ai.domain.model.AiModelInfo

/** Immutable Leo AI tab state. Mirrors Flutter `AiUiState`. */
data class AiUiState(
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
