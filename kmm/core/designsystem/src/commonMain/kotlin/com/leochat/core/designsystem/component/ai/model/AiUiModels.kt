package com.leochat.core.designsystem.component.ai.model

/** Glyph shown beside an AI model name. */
enum class AiModelIconUi { Sparkles, Brain, Code, Zap }

/** Render-ready AI model for the top-bar pill and the model picker sheet. */
data class AiModelUi(
    val id: String,
    val name: String,
    val tagline: String,
    val badge: String,
    val icon: AiModelIconUi,
)

/** Render-ready Leo AI conversation turn for `AiMessageItem`. */
data class AiMessageUi(
    val id: String,
    val isUser: Boolean,
    val content: String,
    /** Model chip text on assistant cards. */
    val modelLabel: String = "",
    /** Reasoning trace; `null` hides the thinking box. */
    val thinkingContent: String? = null,
    /** Formatted reasoning duration, for example `2.4s`, or empty when unknown. */
    val thinkingDurationText: String = "",
)
