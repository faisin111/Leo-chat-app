package com.leochat.core.designsystem.component.feedback

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import com.leochat.core.designsystem.theme.LeoColors

/**
 * Sweeping specular beam over its content (skeletons, avatars, bubbles).
 * Mirrors `LeoShimmer`: same color stops, 1.4s sweep, src-atop blending.
 */
@Composable
fun LeoShimmer(
    modifier: Modifier = Modifier,
    baseColor: Color = LeoColors.shimmerBase,
    highlightColor: Color = LeoColors.shimmerHighlight,
    lightningColor: Color = Color.White,
    durationMillis: Int = 1400,
    content: @Composable () -> Unit,
) {
    val transition = rememberInfiniteTransition(label = "leoShimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis, easing = LinearEasing)),
        label = "leoShimmerSweep",
    )
    val stops = arrayOf(
        0.0f to baseColor,
        0.28f to baseColor,
        0.42f to highlightColor,
        0.48f to lightningColor,
        0.50f to LeoColors.shimmerCore,
        0.52f to lightningColor,
        0.58f to highlightColor,
        0.72f to baseColor,
        1.0f to baseColor,
    )
    Box(
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawWithContent {
                drawContent()
                val dx = size.width * (progress * 2.5f - 1.25f)
                // Alignment(-1.3,-0.4) -> (1.3,0.4) in Flutter maps to these corners.
                val start = Offset(size.width * (-0.15f) + dx, size.height * 0.3f)
                val end = Offset(size.width * 1.15f + dx, size.height * 0.7f)
                drawRect(Brush.linearGradient(colorStops = stops, start = start, end = end), blendMode = BlendMode.SrcAtop)
            },
    ) { content() }
}
