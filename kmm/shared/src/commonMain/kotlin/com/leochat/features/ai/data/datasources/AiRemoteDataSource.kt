package com.leochat.features.ai.data.datasources

import com.leochat.features.ai.data.models.AiActionDto
import com.leochat.features.ai.data.models.AiConversationDto
import com.leochat.features.ai.data.models.AiMessageDto
import com.leochat.features.ai.domain.entities.AiActionRequest
import com.leochat.features.ai.domain.entities.AiActionResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Leo AI REST endpoints. Mirrors `AiRemoteDataSourceImpl`. Throws; repository maps errors. */
interface AiRemoteDataSource {
    suspend fun getOrCreateConversation(): AiConversationDto
    suspend fun deleteConversation()
    suspend fun getMessages(conversationId: String, limit: Int): List<AiMessageDto>
    suspend fun chat(conversationId: String, prompt: String, clientMessageId: String?): AiMessageDto
    suspend fun executeAction(request: AiActionRequest): AiActionResult
}

class AiRemoteDataSourceImpl(private val http: HttpClient) : AiRemoteDataSource {

    override suspend fun getOrCreateConversation(): AiConversationDto =
        AiConversationDto.fromJson(http.post("ai/conversation").body<JsonObject>())

    override suspend fun deleteConversation() {
        http.delete("ai/conversation")
    }

    override suspend fun getMessages(conversationId: String, limit: Int): List<AiMessageDto> =
        AiMessageDto.pageFromJson(
            http.get("conversations/$conversationId/messages") { parameter("limit", limit) }.body<JsonObject>(),
        )

    override suspend fun chat(conversationId: String, prompt: String, clientMessageId: String?): AiMessageDto =
        AiMessageDto.fromJson(
            http.post("ai/chat") {
                // Generation can be slow; Flutter allows a 90 second receive timeout.
                timeout {
                    requestTimeoutMillis = CHAT_TIMEOUT_MILLIS
                    socketTimeoutMillis = CHAT_TIMEOUT_MILLIS
                }
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("conversationId", conversationId)
                        put("prompt", prompt)
                        clientMessageId?.let { put("clientMessageId", it) }
                    },
                )
            }.body<JsonObject>(),
        )

    override suspend fun executeAction(request: AiActionRequest): AiActionResult =
        AiActionDto.resultFromJson(
            http.post("ai/actions") {
                contentType(ContentType.Application.Json)
                setBody(AiActionDto.toJson(request))
            }.body<JsonObject>(),
        )

    private companion object {
        const val CHAT_TIMEOUT_MILLIS = 90_000L
    }
}
