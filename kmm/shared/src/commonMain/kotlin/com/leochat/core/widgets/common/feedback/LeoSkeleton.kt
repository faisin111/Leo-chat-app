package com.leochat.core.widgets.common.feedback

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.leochat.core.theme.LeoColors

/** Warm pulsing placeholder block. Mirrors `LeoSkeleton`. */
@Composable
fun LeoSkeleton(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    isCircle: Boolean = false,
) {
    val transition = rememberInfiniteTransition(label = "leoSkeleton")
    val t by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOut), RepeatMode.Reverse),
        label = "leoSkeletonPulse",
    )
    val shape = if (isCircle) CircleShape else RoundedCornerShape(cornerRadius)
    Box(modifier.background(lerp(LeoColors.skeletonFrom, LeoColors.skeletonTo, t), shape))
}

@Composable
fun LeoSkeletonCircle(size: Dp, modifier: Modifier = Modifier) {
    LeoSkeleton(modifier = modifier.size(size), isCircle = true)
}
