package com.leochat.feature.ai.domain.usecase

import com.leochat.core.common.result.AppResult
import com.leochat.feature.ai.domain.model.AiActionRequest
import com.leochat.feature.ai.domain.model.AiActionResult
import com.leochat.feature.ai.domain.model.AiConversation
import com.leochat.feature.ai.domain.model.AiMessage
import com.leochat.feature.ai.domain.repository.AiRepository

class GetOrCreateAiConversationUseCase(private val repository: AiRepository) {
    suspend operator fun invoke(): AppResult<AiConversation> = repository.getOrCreateConversation()
}

class DeleteAiConversationUseCase(private val repository: AiRepository) {
    suspend operator fun invoke(): AppResult<Unit> = repository.deleteConversation()
}

class GetAiHistoryUseCase(private val repository: AiRepository) {
    suspend operator fun invoke(conversationId: String, limit: Int = 50): AppResult<List<AiMessage>> =
        repository.getHistory(conversationId, limit)
}

class GetCachedAiHistoryUseCase(private val repository: AiRepository) {
    suspend operator fun invoke(): List<AiMessage> = repository.getCachedHistory()
}

class SaveAiHistoryCacheUseCase(private val repository: AiRepository) {
    suspend operator fun invoke(messages: List<AiMessage>) = repository.saveCachedHistory(messages)
}

class SendAiChatUseCase(private val repository: AiRepository) {
    suspend operator fun invoke(conversationId: String, prompt: String, clientMessageId: String?): AppResult<AiMessage> =
        repository.chat(conversationId, prompt.trim(), clientMessageId)
}

class ExecuteAiActionUseCase(private val repository: AiRepository) {
    suspend operator fun invoke(request: AiActionRequest): AppResult<AiActionResult> = repository.executeAction(request)
}

/** The use cases the AI controller coordinates, injected as one unit. */
class AiUseCaseBundle(
    val getOrCreateConversation: GetOrCreateAiConversationUseCase,
    val deleteConversation: DeleteAiConversationUseCase,
    val getHistory: GetAiHistoryUseCase,
    val getCachedHistory: GetCachedAiHistoryUseCase,
    val saveHistory: SaveAiHistoryCacheUseCase,
    val chat: SendAiChatUseCase,
    val executeAction: ExecuteAiActionUseCase,
) {
    companion object {
        fun from(repository: AiRepository) = AiUseCaseBundle(
            GetOrCreateAiConversationUseCase(repository),
            DeleteAiConversationUseCase(repository),
            GetAiHistoryUseCase(repository),
            GetCachedAiHistoryUseCase(repository),
            SaveAiHistoryCacheUseCase(repository),
            SendAiChatUseCase(repository),
            ExecuteAiActionUseCase(repository),
        )
    }
}
