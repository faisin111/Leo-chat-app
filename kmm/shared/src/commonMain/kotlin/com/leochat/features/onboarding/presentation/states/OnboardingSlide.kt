package com.leochat.features.onboarding.presentation.states

import com.leochat.core.constants.AppStrings

enum class OnboardingPreview { Chat, Sync, Expression }

data class OnboardingSlide(
    val step: String,
    val title: String,
    val body: String,
    val badge: String,
    val preview: OnboardingPreview,
)

val onboardingSlides = listOf(
    OnboardingSlide(AppStrings.onboardingStepOne, AppStrings.onboardingTitleOne, AppStrings.onboardingBodyOne, AppStrings.onboardingBadgeOne, OnboardingPreview.Chat),
    OnboardingSlide(AppStrings.onboardingStepTwo, AppStrings.onboardingTitleTwo, AppStrings.onboardingBodyTwo, AppStrings.onboardingBadgeTwo, OnboardingPreview.Sync),
    OnboardingSlide(AppStrings.onboardingStepThree, AppStrings.onboardingTitleThree, AppStrings.onboardingBodyThree, AppStrings.onboardingBadgeThree, OnboardingPreview.Expression),
)
