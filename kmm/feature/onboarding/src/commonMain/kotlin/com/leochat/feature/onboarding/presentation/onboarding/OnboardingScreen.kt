package com.leochat.feature.onboarding.presentation.onboarding

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.leochat.feature.onboarding.presentation.mapper.toUi
import com.leochat.feature.onboarding.presentation.onboardingSlides
import kotlinx.coroutines.launch

/** Three-step intro carousel. Mirrors `OnboardingScreen`. */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val slides = remember { onboardingSlides.toUi() }
    val pager = rememberPagerState { slides.size }
    val scope = rememberCoroutineScope()

    OnboardingContent(
        slides = slides,
        pagerState = pager,
        onNext = {
            scope.launch { pager.animateScrollToPage(pager.currentPage + 1, animationSpec = tween(280, easing = EaseOut)) }
        },
        onFinish = onFinish,
    )
}
