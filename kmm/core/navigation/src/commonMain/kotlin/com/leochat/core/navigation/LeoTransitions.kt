package com.leochat.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry

/**
 * Shared route transition: fade + scale from 0.94 with ease-out-cubic,
 * 320 ms forward and 240 ms back. Mirrors `_page` in `app/app.dart`.
 */
object LeoTransitions {
    private const val ForwardMs = 320
    private const val ReverseMs = 240
    private const val InitialScale = 0.94f

    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(ForwardMs, easing = EaseOutCubic)) + scaleIn(tween(ForwardMs, easing = EaseOutCubic), initialScale = InitialScale)
    }

    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(tween(ForwardMs, easing = EaseOutCubic))
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(tween(ReverseMs, easing = EaseOutCubic))
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(tween(ReverseMs, easing = EaseOutCubic)) + scaleOut(tween(ReverseMs, easing = EaseOutCubic), targetScale = InitialScale)
    }
}
