package com.leochat.feature.onboarding

import com.leochat.core.designsystem.component.onboarding.model.OnboardingPreviewKind
import com.leochat.core.designsystem.component.onboarding.model.OnboardingSlideUi
import com.leochat.feature.onboarding.presentation.OnboardingPreview
import com.leochat.feature.onboarding.presentation.OnboardingSlide
import com.leochat.feature.onboarding.presentation.mapper.toUi
import com.leochat.feature.onboarding.presentation.onboardingSlides
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingSlideMapperTest {
    @Test
    fun mapsEveryPreviewKind() {
        assertEquals(OnboardingPreviewKind.Chat, OnboardingPreview.Chat.toUi())
        assertEquals(OnboardingPreviewKind.Sync, OnboardingPreview.Sync.toUi())
        assertEquals(OnboardingPreviewKind.Expression, OnboardingPreview.Expression.toUi())
    }

    @Test
    fun mapsSlideFieldsUnchanged() {
        val slide = OnboardingSlide("step", "title", "body", "badge", OnboardingPreview.Sync)
        assertEquals(OnboardingSlideUi("step", "title", "body", "badge", OnboardingPreviewKind.Sync), slide.toUi())
    }

    @Test
    fun mapsAllShippedSlidesInOrder() {
        val ui = onboardingSlides.toUi()
        assertEquals(onboardingSlides.size, ui.size)
        assertEquals(
            listOf(OnboardingPreviewKind.Chat, OnboardingPreviewKind.Sync, OnboardingPreviewKind.Expression),
            ui.map { it.preview },
        )
        assertEquals(onboardingSlides.map { it.title }, ui.map { it.title })
    }
}
