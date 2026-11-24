package com.leochat.feature.ai.presentation.mapper

import com.leochat.core.common.constants.AiStrings
import com.leochat.core.designsystem.component.ai.model.AiMessageUi
import com.leochat.core.designsystem.component.ai.model.AiModelIconUi
import com.leochat.core.designsystem.component.ai.model.AiModelUi
import com.leochat.feature.ai.domain.model.AiMessage
import com.leochat.feature.ai.domain.model.AiModelIcon
import com.leochat.feature.ai.domain.model.AiModelInfo

fun AiModelIcon.toUi(): AiModelIconUi = when (this) {
    AiModelIcon.Sparkles -> AiModelIconUi.Sparkles
    AiModelIcon.Brain -> AiModelIconUi.Brain
    AiModelIcon.Code -> AiModelIconUi.Code
    AiModelIcon.Zap -> AiModelIconUi.Zap
}

fun AiModelInfo.toUi(): AiModelUi =
    AiModelUi(id = id, name = name, tagline = tagline, badge = badge, icon = icon.toUi())

/** The domain model behind a picked [AiModelUi], or `null` when it is not in [this] list. */
fun List<AiModelInfo>.findById(id: String): AiModelInfo? = firstOrNull { it.id == id }

/** Assistant cards fall back to the default model id; blank reasoning hides the thinking box. */
fun AiMessage.toUi(): AiMessageUi = AiMessageUi(
    id = id,
    isUser = isUser,
    content = content,
    modelLabel = modelId ?: AiStrings.defaultModelId,
    thinkingContent = thinkingContent?.takeIf { hasThinking },
    thinkingDurationText = formatThinkingDuration(thinkingDurationSecs),
)

/** `toStringAsFixed(1)` + `s`, or empty when unknown. */
fun formatThinkingDuration(seconds: Double?): String {
    if (seconds == null) return ""
    // Half-up rounding, as Dart's `toStringAsFixed`.
    val tenths = kotlin.math.floor(kotlin.math.abs(seconds) * 10 + 0.5).toLong()
    return "${tenths / 10}.${tenths % 10}s"
}
