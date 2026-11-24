package com.leochat.feature.onboarding.presentation.mapper

import com.leochat.core.designsystem.component.onboarding.model.OnboardingPreviewKind
import com.leochat.core.designsystem.component.onboarding.model.OnboardingSlideUi
import com.leochat.feature.onboarding.presentation.OnboardingPreview
import com.leochat.feature.onboarding.presentation.OnboardingSlide

/** Maps the feature's preview selector onto the design-system preview kind. */
fun OnboardingPreview.toUi(): OnboardingPreviewKind = when (this) {
    OnboardingPreview.Chat -> OnboardingPreviewKind.Chat
    OnboardingPreview.Sync -> OnboardingPreviewKind.Sync
    OnboardingPreview.Expression -> OnboardingPreviewKind.Expression
}

/** Maps one onboarding slide onto its design-system display model. */
fun OnboardingSlide.toUi(): OnboardingSlideUi = OnboardingSlideUi(
    step = step,
    title = title,
    body = body,
    badge = badge,
    preview = preview.toUi(),
)

fun List<OnboardingSlide>.toUi(): List<OnboardingSlideUi> = map { it.toUi() }
