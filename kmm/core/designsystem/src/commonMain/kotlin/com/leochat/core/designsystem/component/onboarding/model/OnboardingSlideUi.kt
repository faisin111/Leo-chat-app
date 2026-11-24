package com.leochat.core.designsystem.component.onboarding.model

/** Which illustrated preview card an onboarding slide shows. */
enum class OnboardingPreviewKind { Chat, Sync, Expression }

/** Immutable display model for one onboarding page. */
data class OnboardingSlideUi(
    val step: String,
    val title: String,
    val body: String,
    val badge: String,
    val preview: OnboardingPreviewKind,
)
