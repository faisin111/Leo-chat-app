package com.leochat.feature.ai.data.repository

import com.leochat.core.common.result.AppResult
import com.leochat.core.network.safeApiCall
import com.leochat.feature.ai.data.local.AiLocalDataSource
import com.leochat.feature.ai.data.remote.AiRemoteDataSource
import com.leochat.feature.ai.data.remote.AiMessageDto
import com.leochat.feature.ai.domain.model.AiActionRequest
import com.leochat.feature.ai.domain.model.AiActionResult
import com.leochat.feature.ai.domain.model.AiConversation
import com.leochat.feature.ai.domain.model.AiMessage
import com.leochat.feature.ai.domain.model.AiMessageRole
import com.leochat.feature.ai.domain.repository.AiRepository

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
