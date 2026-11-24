package com.leochat.feature.ai

import com.leochat.core.common.serialization.LeoJson
import com.leochat.feature.ai.data.remote.AiActionDto
import com.leochat.feature.ai.data.remote.AiConversationDto
import com.leochat.feature.ai.data.remote.AiMessageDto
import com.leochat.feature.ai.domain.model.AiActionRequest
import com.leochat.feature.ai.domain.model.AiMessageRole
import com.leochat.feature.ai.presentation.mapper.formatThinkingDuration
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiDtoTest {
    private fun obj(raw: String): JsonObject = LeoJson.parseToJsonElement(raw).jsonObject

    @Test
    fun parsesMessageAndInfersRoleFromAiGenerated() {
        val dto = AiMessageDto.fromJson(
            obj("""{"id":"m1","conversationId":"c1","seq":4,"content":"hello","createdAt":"2026-11-23T10:00:00Z","aiGenerated":true,"thinkingContent":"step 1","thinkingDurationSecs":2.35}"""),
        )
        val message = dto.toDomain()
        assertEquals("m1", message.id)
        assertEquals(AiMessageRole.Assistant, message.role)
        assertEquals("hello", message.content)
        assertEquals(4L, message.seq)
        assertEquals(Instant.parse("2026-11-23T10:00:00Z"), message.createdAt)
        assertTrue(message.hasThinking)
        assertEquals(2.35, message.thinkingDurationSecs)
    }

    @Test
    fun userRowsMapToUserRoleAndMissingFieldsDefault() {
        val fallback = Instant.parse("2026-01-01T00:00:00Z")
        val message = AiMessageDto.fromJson(obj("""{"id":"u1"}""")).toDomain(now = { fallback })
        assertEquals(AiMessageRole.User, message.role)
        assertEquals("", message.content)
        assertEquals(fallback, message.createdAt)
        assertFalse(message.hasThinking)
    }

    @Test
    fun overridesWinOverWireRole() {
        val message = AiMessageDto.fromJson(obj("""{"id":"r1","content":"x"}"""))
            .toDomain(roleOverride = AiMessageRole.Assistant, modelIdOverride = "leo-coder")
        assertEquals(AiMessageRole.Assistant, message.role)
        assertEquals("leo-coder", message.modelId)
    }

    @Test
    fun parsesItemsAndContentPages() {
        assertEquals(listOf("a", "b"), AiMessageDto.pageFromJson(obj("""{"items":[{"id":"a"},{"id":"b"}]}""")).map { it.id })
        assertEquals(listOf("c"), AiMessageDto.pageFromJson(obj("""{"content":[{"id":"c"}]}""")).map { it.id })
        assertTrue(AiMessageDto.pageFromJson(obj("{}")).isEmpty())
    }

    @Test
    fun cacheRoundTripKeepsRoleAndModel() {
        val original = AiMessageDto.fromJson(obj("""{"id":"m","content":"hi","createdAt":"2026-11-23T10:00:00Z"}"""))
            .toDomain(roleOverride = AiMessageRole.Assistant, modelIdOverride = "leo-fast")
        val encoded = LeoJson.encodeToString(AiMessageDto.serializer(), AiMessageDto.fromDomain(original))
        val restored = LeoJson.decodeFromString(AiMessageDto.serializer(), encoded).toDomain()
        assertEquals(original, restored)
    }

    @Test
    fun parsesConversation() {
        val conversation = AiConversationDto.fromJson(obj("""{"id":"ai-1","title":"Leo AI","type":"AI"}""")).toDomain()
        assertEquals("ai-1", conversation.id)
        assertEquals("Leo AI", conversation.title)
    }

    @Test
    fun encodesActionRequestAndParsesResult() {
        val json = AiActionDto.toJson(AiActionRequest("SEND_MESSAGE", mapOf("to" to "maya"), confirmed = true))
        assertEquals("SEND_MESSAGE", json["action"]?.jsonPrimitive?.content)
        assertEquals("maya", json["to"]?.jsonPrimitive?.content)
        assertEquals("true", json["confirmed"]?.jsonPrimitive?.content)
        assertNull(AiActionDto.toJson(AiActionRequest("X"))["confirmed"])

        val result = AiActionDto.resultFromJson(obj("""{"action":"START_CALL","message":"Ringing","data":{"callId":"k"},"confirmationRequired":false}"""))
        assertEquals("START_CALL", result.action)
        assertEquals("Ringing", result.message)
        assertEquals("""{"callId":"k"}""", result.data)
        assertFalse(result.confirmationRequired)
        assertTrue(AiActionDto.resultFromJson(obj("""{"confirmationRequired":true}""")).confirmationRequired)
        assertNull(AiActionDto.resultFromJson(obj("""{"data":null}""")).data)
    }

    @Test
    fun formatsThinkingDurationLikeToStringAsFixed() {
        assertEquals("", formatThinkingDuration(null))
        assertEquals("2.4s", formatThinkingDuration(2.35))
        assertEquals("0.0s", formatThinkingDuration(0.0))
        assertEquals("12.0s", formatThinkingDuration(12.0))
    }
}
