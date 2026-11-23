package com.leochat.features.chat.data.datasources

import com.leochat.core.network.int
import com.leochat.features.chat.data.models.ConversationDto
import com.leochat.features.chat.data.models.ConversationMemberDto
import com.leochat.features.chat.data.models.MessageDto
import com.leochat.features.chat.data.models.SendMessageRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Chat REST endpoints. Mirrors `ChatRemoteDataSourceImpl`. Throws; repository maps errors. */
interface ChatRemoteDataSource {
    suspend fun getConversations(cursor: Int?, limit: Int, query: String?): List<ConversationDto>
    suspend fun getConversation(conversationId: String): ConversationDto
    suspend fun startDirectMessage(targetUserId: String): ConversationDto
    suspend fun createGroup(title: String, memberIds: List<String>): ConversationDto
    suspend fun updateConversation(conversationId: String, title: String?, avatarUrl: String?): ConversationDto
    suspend fun leaveConversation(conversationId: String)
    suspend fun getMembers(conversationId: String): List<ConversationMemberDto>
    suspend fun addGroupMembers(conversationId: String, memberIds: List<String>)
    suspend fun removeMember(conversationId: String, userId: String)
    suspend fun updateMemberRole(conversationId: String, userId: String, role: String)
    suspend fun muteConversation(conversationId: String, mutedUntil: String)
    suspend fun unmuteConversation(conversationId: String)
    suspend fun pinConversation(conversationId: String)
    suspend fun unpinConversation(conversationId: String)
    suspend fun archiveConversation(conversationId: String)
    suspend fun unarchiveConversation(conversationId: String)
    suspend fun getMessages(conversationId: String, beforeSeq: Long?, afterSeq: Long?, limit: Int): List<MessageDto>
    suspend fun sendMessage(request: SendMessageRequestDto): MessageDto
    suspend fun editMessage(messageId: String, content: String): MessageDto
    suspend fun deleteMessage(messageId: String)
    suspend fun addReaction(messageId: String, emoji: String)
    suspend fun removeReaction(messageId: String, emoji: String)
    suspend fun markConversationRead(conversationId: String, upToSeq: Long)
    suspend fun getUnreadCount(): Int
    suspend fun aiChat(conversationId: String, prompt: String, clientMessageId: String): MessageDto
}

class ChatRemoteDataSourceImpl(private val http: HttpClient) : ChatRemoteDataSource {

    override suspend fun getConversations(cursor: Int?, limit: Int, query: String?): List<ConversationDto> =
        ConversationDto.pageFromJson(
            http.get("conversations") {
                parameter("limit", limit)
                cursor?.let { parameter("cursor", it) }
                query?.trim()?.takeIf { it.isNotEmpty() }?.let { parameter("q", it) }
            }.body<JsonObject>(),
        )

    override suspend fun getConversation(conversationId: String) =
        ConversationDto.fromJson(http.get("conversations/$conversationId").body<JsonObject>())

    override suspend fun startDirectMessage(targetUserId: String) = ConversationDto.fromJson(
        http.post("conversations/direct") { json(buildJsonObject { put("targetUserId", targetUserId) }) }.body<JsonObject>(),
    )

    override suspend fun createGroup(title: String, memberIds: List<String>) = ConversationDto.fromJson(
        http.post("conversations/group") {
            json(buildJsonObject { put("title", title); put("memberIds", ids(memberIds)) })
        }.body<JsonObject>(),
    )

    override suspend fun updateConversation(conversationId: String, title: String?, avatarUrl: String?) = ConversationDto.fromJson(
        http.patch("conversations/$conversationId") {
            json(buildJsonObject { title?.let { put("title", it) }; avatarUrl?.let { put("avatarUrl", it) } })
        }.body<JsonObject>(),
    )

    override suspend fun leaveConversation(conversationId: String) {
        http.delete("conversations/$conversationId")
    }

    override suspend fun getMembers(conversationId: String): List<ConversationMemberDto> {
        val body = http.get("conversations/$conversationId/members").body<JsonElement>()
        return (body as? JsonArray)?.mapNotNull { (it as? JsonObject)?.let(ConversationMemberDto::fromJson) }.orEmpty()
    }

    override suspend fun addGroupMembers(conversationId: String, memberIds: List<String>) {
        http.post("conversations/$conversationId/members") { json(buildJsonObject { put("memberIds", ids(memberIds)) }) }
    }

    override suspend fun removeMember(conversationId: String, userId: String) {
        http.delete("conversations/$conversationId/members/$userId")
    }

    override suspend fun updateMemberRole(conversationId: String, userId: String, role: String) {
        http.patch("conversations/$conversationId/members/$userId/role") { json(buildJsonObject { put("role", role) }) }
    }

    override suspend fun muteConversation(conversationId: String, mutedUntil: String) {
        http.put("conversations/$conversationId/mute") { json(buildJsonObject { put("mutedUntil", mutedUntil) }) }
    }

    override suspend fun unmuteConversation(conversationId: String) {
        http.delete("conversations/$conversationId/mute")
    }

    override suspend fun pinConversation(conversationId: String) {
        http.put("conversations/$conversationId/pin")
    }

    override suspend fun unpinConversation(conversationId: String) {
        http.delete("conversations/$conversationId/pin")
    }

    override suspend fun archiveConversation(conversationId: String) {
        http.put("conversations/$conversationId/archive")
    }

    override suspend fun unarchiveConversation(conversationId: String) {
        http.delete("conversations/$conversationId/archive")
    }

    override suspend fun getMessages(conversationId: String, beforeSeq: Long?, afterSeq: Long?, limit: Int): List<MessageDto> =
        MessageDto.pageFromJson(
            http.get("conversations/$conversationId/messages") {
                parameter("limit", limit)
                beforeSeq?.let { parameter("beforeSeq", it) }
                afterSeq?.let { parameter("afterSeq", it) }
            }.body<JsonObject>(),
        )

    override suspend fun sendMessage(request: SendMessageRequestDto) = MessageDto.fromJson(
        http.post("conversations/${request.conversationId}/messages") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<JsonObject>(),
    )

    override suspend fun editMessage(messageId: String, content: String) = MessageDto.fromJson(
        http.patch("messages/$messageId") { json(buildJsonObject { put("content", content) }) }.body<JsonObject>(),
    )

    override suspend fun deleteMessage(messageId: String) {
        http.delete("messages/$messageId")
    }

    override suspend fun addReaction(messageId: String, emoji: String) {
        http.post("messages/$messageId/reactions") { json(buildJsonObject { put("emoji", emoji) }) }
    }

    override suspend fun removeReaction(messageId: String, emoji: String) {
        http.delete("messages/$messageId/reactions/${emoji.encodeURLPathPart()}")
    }

    override suspend fun markConversationRead(conversationId: String, upToSeq: Long) {
        http.post("conversations/$conversationId/read") { json(buildJsonObject { put("upToSeq", upToSeq) }) }
    }

    override suspend fun getUnreadCount(): Int =
        http.get("conversations/unread-count").body<JsonObject>().int("unreadCount") ?: 0

    override suspend fun aiChat(conversationId: String, prompt: String, clientMessageId: String) = MessageDto.fromJson(
        http.post("ai/chat") {
            json(
                buildJsonObject {
                    put("conversationId", conversationId)
                    put("prompt", prompt)
                    put("clientMessageId", clientMessageId)
                },
            )
        }.body<JsonObject>(),
    )

    private fun ids(values: List<String>) = buildJsonArray { values.forEach { add(kotlinx.serialization.json.JsonPrimitive(it)) } }

    private fun HttpRequestBuilder.json(body: JsonObject) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }
}
