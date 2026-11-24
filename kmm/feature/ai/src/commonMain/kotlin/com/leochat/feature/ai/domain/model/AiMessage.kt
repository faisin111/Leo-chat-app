package com.leochat.feature.ai.domain.model

import kotlinx.datetime.Instant

enum class AiMessageRole { User, Assistant, System }

/** One turn in the Leo AI conversation. Mirrors Flutter `AiMessage`. */
data class AiMessage(
    val id: String,
    val role: AiMessageRole,
    val content: String,
    val createdAt: Instant,
    val modelId: String? = null,
    val thinkingContent: String? = null,
    val thinkingDurationSecs: Double? = null,
    val seq: Long = 0,
) {
    val isUser: Boolean get() = role == AiMessageRole.User
    val hasThinking: Boolean get() = !thinkingContent.isNullOrBlank()
}
