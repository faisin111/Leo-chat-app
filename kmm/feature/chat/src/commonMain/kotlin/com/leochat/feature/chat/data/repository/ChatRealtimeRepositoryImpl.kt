package com.leochat.feature.chat.data.repository

import com.leochat.core.common.serialization.LeoJson
import com.leochat.core.network.bool
import com.leochat.core.network.firstString
import com.leochat.core.network.long
import com.leochat.core.network.string
import com.leochat.core.network.stomp.StompClient
import com.leochat.core.network.stomp.StompConnectionState
import com.leochat.feature.chat.data.remote.MessageDto
import com.leochat.feature.chat.domain.model.CurrentIdentity
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.model.MessageAck
import com.leochat.feature.chat.domain.model.RealtimeStatus
import com.leochat.feature.chat.domain.model.TypingEvent
import com.leochat.feature.chat.domain.repository.ChatRealtimeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private fun conversationTopic(id: String) = "/topic/conversations.$id"
private fun typingTopic(id: String) = "/topic/conversations.$id.typing"
private const val AcksQueue = "/user/queue/acks"

/** Chat over the shared [StompClient]. Payload parsing mirrors `StompService`. */
class ChatRealtimeRepositoryImpl(private val stomp: StompClient) : ChatRealtimeRepository {

    override val status: Flow<RealtimeStatus> = stomp.state.map {
        when (it) {
            StompConnectionState.Connected -> RealtimeStatus.Connected
            StompConnectionState.Connecting -> RealtimeStatus.Connecting
            StompConnectionState.Disconnected -> RealtimeStatus.Disconnected
        }
    }

    override fun connect() = stomp.connect()
    override fun disconnect() = stomp.disconnect()

    override fun messages(conversationId: String, identity: CurrentIdentity): Flow<Message> =
        stomp.subscribe(conversationTopic(conversationId)).mapNotNull { frame ->
            parse(frame.body)?.let { MessageDto.fromJson(it).toDomain(identity) }
        }

    override fun typing(conversationId: String): Flow<TypingEvent> =
        stomp.subscribe(typingTopic(conversationId)).mapNotNull { frame ->
            parse(frame.body)?.let { json ->
                TypingEvent(
                    conversationId = json.string("conversationId"),
                    userId = json.firstString("userId", "senderId", "fromId"),
                    username = json.firstString("username", "senderUsername", "from", "user"),
                    displayName = json.string("displayName"),
                    isTyping = json.bool("isTyping") == true,
                )
            }
        }

    override fun acks(): Flow<MessageAck> = stomp.subscribe(AcksQueue).mapNotNull { frame ->
        parse(frame.body)?.let { json ->
            MessageAck(
                conversationId = json.string("conversationId").orEmpty(),
                clientMessageId = json.string("clientMessageId").orEmpty(),
                id = json.string("id").orEmpty(),
                seq = json.long("seq") ?: 0,
            )
        }
    }

    override fun unsubscribe(conversationId: String) {
        stomp.unsubscribe(conversationTopic(conversationId))
        stomp.unsubscribe(typingTopic(conversationId))
    }

    override suspend fun sendMessage(conversationId: String, clientMessageId: String, content: String, type: String, replyToId: String?) =
        stomp.send(
            "/app/chat.send",
            buildJsonObject {
                put("conversationId", conversationId)
                put("clientMessageId", clientMessageId)
                put("content", content)
                put("type", type)
                replyToId?.let { put("replyToId", it) }
            }.toString(),
        )

    override suspend fun sendAiChat(conversationId: String, prompt: String, clientMessageId: String) =
        stomp.send(
            "/app/ai.chat",
            buildJsonObject {
                put("conversationId", conversationId)
                put("prompt", prompt)
                put("clientMessageId", clientMessageId)
            }.toString(),
        )

    override suspend fun sendTyping(conversationId: String, isTyping: Boolean) =
        stomp.send("/app/chat.typing", buildJsonObject { put("conversationId", conversationId); put("isTyping", isTyping) }.toString())

    override suspend fun sendRead(conversationId: String, upToSeq: Long) =
        stomp.send("/app/chat.read", buildJsonObject { put("conversationId", conversationId); put("upToSeq", upToSeq) }.toString())

    private fun parse(body: String): JsonObject? = runCatching { LeoJson.parseToJsonElement(body) as? JsonObject }.getOrNull()
}
