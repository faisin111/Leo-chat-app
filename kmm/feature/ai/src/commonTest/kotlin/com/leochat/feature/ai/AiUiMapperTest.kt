package com.leochat.feature.ai

import com.leochat.core.common.constants.AiStrings
import com.leochat.core.designsystem.component.ai.model.AiModelIconUi
import com.leochat.core.designsystem.component.ai.model.AiModelUi
import com.leochat.feature.ai.domain.model.AiMessage
import com.leochat.feature.ai.domain.model.AiMessageRole
import com.leochat.feature.ai.domain.model.AiModelIcon
import com.leochat.feature.ai.domain.model.AiModelInfo
import com.leochat.feature.ai.presentation.mapper.findById
import com.leochat.feature.ai.presentation.mapper.toUi
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AiUiMapperTest {
    private fun message(
        role: AiMessageRole,
        modelId: String? = null,
        thinking: String? = null,
        secs: Double? = null,
    ) = AiMessage(
        id = "m1",
        role = role,
        content = "Hello",
        createdAt = Instant.fromEpochMilliseconds(0),
        modelId = modelId,
        thinkingContent = thinking,
        thinkingDurationSecs = secs,
    )

    @Test
    fun modelInfoMapsEveryDisplayFieldAndIcon() {
        val coder = AiModelInfo.models.first { it.icon == AiModelIcon.Code }
        assertEquals(
            AiModelUi(coder.id, coder.name, coder.tagline, coder.badge, AiModelIconUi.Code),
            coder.toUi(),
        )
        assertEquals(
            listOf(AiModelIconUi.Sparkles, AiModelIconUi.Brain, AiModelIconUi.Code, AiModelIconUi.Zap),
            AiModelIcon.entries.map { it.toUi() },
        )
    }

    @Test
    fun pickedUiModelResolvesBackToTheDomainModel() {
        assertEquals(AiModelInfo.models[1], AiModelInfo.models.findById(AiModelInfo.models[1].toUi().id))
        assertNull(AiModelInfo.models.findById("missing"))
    }

    @Test
    fun userMessageMapsRoleAndContent() {
        val ui = message(AiMessageRole.User).toUi()
        assertEquals(true, ui.isUser)
        assertEquals("Hello", ui.content)
        assertEquals("m1", ui.id)
    }

    @Test
    fun assistantMessageFallsBackToDefaultModelLabel() {
        assertEquals(AiStrings.defaultModelId, message(AiMessageRole.Assistant).toUi().modelLabel)
        assertEquals("leo-coder", message(AiMessageRole.Assistant, modelId = "leo-coder").toUi().modelLabel)
    }

    @Test
    fun thinkingContentIsKeptWithFormattedDurationAndBlankThinkingIsHidden() {
        val ui = message(AiMessageRole.Assistant, thinking = "step 1", secs = 2.35).toUi()
        assertEquals("step 1", ui.thinkingContent)
        assertEquals("2.4s", ui.thinkingDurationText)

        val blank = message(AiMessageRole.Assistant, thinking = "  ").toUi()
        assertNull(blank.thinkingContent)
        assertEquals("", blank.thinkingDurationText)
    }
}
