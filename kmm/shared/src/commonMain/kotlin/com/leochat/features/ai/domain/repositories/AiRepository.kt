package com.leochat.features.ai.domain.repositories

import com.leochat.core.errors.AppResult
import com.leochat.features.ai.domain.entities.AiActionRequest
import com.leochat.features.ai.domain.entities.AiActionResult
import com.leochat.features.ai.domain.entities.AiConversation
import com.leochat.features.ai.domain.entities.AiMessage

/** Leo AI contract. Mirrors Flutter `AiRepository` plus cached history. */
interface AiRepository {
    suspend fun getOrCreateConversation(): AppResult<AiConversation>
    suspend fun deleteConversation(): AppResult<Unit>

    /** Server history, oldest first. Refreshes the local cache on success. */
    suspend fun getHistory(conversationId: String, limit: Int = 50): AppResult<List<AiMessage>>
    suspend fun getCachedHistory(): List<AiMessage>
    suspend fun saveCachedHistory(messages: List<AiMessage>)

    suspend fun chat(conversationId: String, prompt: String, clientMessageId: String?): AppResult<AiMessage>
    suspend fun executeAction(request: AiActionRequest): AppResult<AiActionResult>
}
