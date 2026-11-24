package com.leochat.features.ai.data.models

import com.leochat.core.network.array
import com.leochat.core.network.bool
import com.leochat.core.network.firstString
import com.leochat.core.network.long
import com.leochat.core.network.objects
import com.leochat.core.network.string
import com.leochat.features.ai.domain.entities.AiMessage
import com.leochat.features.ai.domain.entities.AiMessageRole
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

/** AI message wire + cache model. Server payloads reuse the chat `MessageDto` shape. */
@Serializable
data class AiMessageDto(
    val id: String,
    val conversationId: String = "",
    val seq: Long = 0,
    val clientMessageId: String = "",
    val content: String? = null,
    val createdAt: String? = null,
    val aiGenerated: Boolean = false,
    /** Set for cached entries; server payloads infer the role from [aiGenerated]. */
    val role: String? = null,
    val modelId: String? = null,
    val thinkingContent: String? = null,
    val thinkingDurationSecs: Double? = null,
) {
    /** History mapping: AI-generated rows are assistant turns, everything else is the user. */
    fun toDomain(
        roleOverride: AiMessageRole? = null,
        modelIdOverride: String? = null,
        now: () -> Instant = { Clock.System.now() },
    ): AiMessage = AiMessage(
        id = id,
        role = roleOverride
            ?: AiMessageRole.entries.firstOrNull { it.name == role }
            ?: if (aiGenerated) AiMessageRole.Assistant else AiMessageRole.User,
        content = content.orEmpty(),
        createdAt = createdAt?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: now(),
        modelId = modelIdOverride ?: modelId,
        thinkingContent = thinkingContent,
        thinkingDurationSecs = thinkingDurationSecs,
        seq = seq,
    )

    companion object {
        fun fromJson(json: JsonObject) = AiMessageDto(
            id = json.string("id").orEmpty(),
            conversationId = json.string("conversationId").orEmpty(),
            seq = json.long("seq") ?: 0,
            clientMessageId = json.string("clientMessageId").orEmpty(),
            content = json.string("content"),
            createdAt = json.string("createdAt"),
            aiGenerated = json.bool("aiGenerated") ?: false,
            thinkingContent = json.firstString("thinkingContent", "thinking", "reasoning"),
            thinkingDurationSecs = (json["thinkingDurationSecs"] as? JsonPrimitive)?.doubleOrNull,
        )

        /** Accepts `{items: [...]}` or Spring-style `{content: [...]}` pages. */
        fun pageFromJson(json: JsonObject): List<AiMessageDto> =
            (json.array("items") ?: json.array("content")).objects().map(::fromJson)

        fun fromDomain(m: AiMessage) = AiMessageDto(
            id = m.id,
            seq = m.seq,
            content = m.content,
            createdAt = m.createdAt.toString(),
            aiGenerated = !m.isUser,
            role = m.role.name,
            modelId = m.modelId,
            thinkingContent = m.thinkingContent,
            thinkingDurationSecs = m.thinkingDurationSecs,
        )
    }
}
