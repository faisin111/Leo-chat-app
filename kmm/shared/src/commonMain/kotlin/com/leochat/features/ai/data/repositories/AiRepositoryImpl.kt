package com.leochat.features.ai.data.repositories

import com.leochat.core.errors.AppResult
import com.leochat.core.network.safeApiCall
import com.leochat.features.ai.data.datasources.AiLocalDataSource
import com.leochat.features.ai.data.datasources.AiRemoteDataSource
import com.leochat.features.ai.data.models.AiMessageDto
import com.leochat.features.ai.domain.entities.AiActionRequest
import com.leochat.features.ai.domain.entities.AiActionResult
import com.leochat.features.ai.domain.entities.AiConversation
import com.leochat.features.ai.domain.entities.AiMessage
import com.leochat.features.ai.domain.entities.AiMessageRole
import com.leochat.features.ai.domain.repositories.AiRepository

/** Mirrors `AiRepositoryImpl`; history is cached per user and cleared on delete. */
class AiRepositoryImpl(
    private val remote: AiRemoteDataSource,
    private val local: AiLocalDataSource,
) : AiRepository {

    override suspend fun getOrCreateConversation(): AppResult<AiConversation> =
        safeApiCall { remote.getOrCreateConversation().toDomain() }

    override suspend fun deleteConversation(): AppResult<Unit> {
        val result = safeApiCall { remote.deleteConversation() }
        if (result.isRight) local.clear()
        return result
    }

    override suspend fun getHistory(conversationId: String, limit: Int): AppResult<List<AiMessage>> {
        val result = safeApiCall {
            remote.getMessages(conversationId, limit).sortedBy { it.seq }.map { it.toDomain() }
        }
        result.getOrNull()?.takeIf { it.isNotEmpty() }?.let { saveCachedHistory(it) }
        return result
    }

    override suspend fun getCachedHistory(): List<AiMessage> = local.messages().map { it.toDomain() }

    override suspend fun saveCachedHistory(messages: List<AiMessage>) =
        local.saveMessages(messages.map(AiMessageDto::fromDomain))

    override suspend fun chat(conversationId: String, prompt: String, clientMessageId: String?): AppResult<AiMessage> =
        safeApiCall {
            remote.chat(conversationId, prompt, clientMessageId).toDomain(roleOverride = AiMessageRole.Assistant)
        }

    override suspend fun executeAction(request: AiActionRequest): AppResult<AiActionResult> =
        safeApiCall { remote.executeAction(request) }
}
