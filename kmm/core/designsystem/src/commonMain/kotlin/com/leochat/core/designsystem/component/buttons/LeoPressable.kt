package com.leochat.core.designsystem.component.buttons

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Tactile scale-down-and-bounce-back press effect. Mirrors `LeoPressable`:
 * presses ease in/out cubic, releases spring back with ease-out-back.
 */
fun Modifier.leoPressable(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 0.94f,
    durationMillis: Int = 110,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val active = pressed && enabled
    val scale by animateFloatAsState(
        targetValue = if (active) pressedScale else 1f,
        animationSpec = tween(durationMillis, easing = if (active) EaseInOutCubic else EaseOutBack),
        label = "leoPressable",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
