package com.leochat.core.designsystem.component.ai

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AiStrings
import com.leochat.core.designsystem.component.feedback.LeoSkeleton
import com.leochat.core.designsystem.component.feedback.LeoShimmer
import com.leochat.core.designsystem.theme.AiTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LocalLeoPalette

/** Three bouncing dots with staggered sine-wave motion for active thinking indication. */
@Composable
fun AiWaveDots(
    modifier: Modifier = Modifier,
    dotSize: Dp = 6.dp,
    dotColor: Color = LeoColors.primary,
) {
    val transition = rememberInfiniteTransition(label = "ai_wave_dots")
    val dot0 by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot0",
    )
    val dot1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, delayMillis = 150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot1",
    )
    val dot2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, delayMillis = 300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot2",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        Box(Modifier.offset(y = dot0.dp).size(dotSize).background(dotColor, CircleShape))
        Box(Modifier.offset(y = dot1.dp).size(dotSize).background(dotColor, CircleShape))
        Box(Modifier.offset(y = dot2.dp).size(dotSize).background(dotColor, CircleShape))
    }
}

/** Shown when the AI conversation has no messages, with breathing halo and quick-starter cards. */
@Composable
fun AiEmptyState(
    modifier: Modifier = Modifier,
    onSelectPrompt: ((String) -> Unit)? = null,
) {
    val palette = LocalLeoPalette.current
    val isDark = palette.isDark
    val transition = rememberInfiniteTransition(label = "ai_empty_animation")
    val floatY by transition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hero_float",
    )
    val haloScale by transition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "halo_scale",
    )
    val haloAlpha by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.32f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "halo_alpha",
    )

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            // Floating hero badge with breathing ambient halo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(y = floatY.dp)
                    .padding(bottom = 6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(haloScale)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    AiTokens.gradientStart.copy(alpha = haloAlpha),
                                    Color.Transparent,
                                ),
                            ),
                            CircleShape,
                        ),
                )
                val shape = RoundedCornerShape(24.dp)
                AiGradientBadge(
                    size = 68,
                    cornerRadius = 22,
                    iconSize = 34,
                    modifier = Modifier.shadow(12.dp, shape, ambientColor = AiTokens.gradientGlowSoft, spotColor = AiTokens.gradientGlowSoft),
                )
            }

            Spacer(Modifier.height(14.dp))

            // Luminous tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(palette.primarySoft, RoundedCornerShape(16.dp))
                    .border(1.dp, LeoColors.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Leo AI Intelligence",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = LeoColors.primary,
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                AiStrings.emptyTitle,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(6.dp))

            Text(
                AiStrings.emptySubtitle,
                textAlign = TextAlign.Center,
                fontSize = 13.5.sp,
                lineHeight = (13.5 * 1.45).sp,
                color = palette.textSecondary,
                modifier = Modifier.fillMaxWidth(0.92f),
            )

            if (onSelectPrompt != null) {
                Spacer(Modifier.height(24.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    EmptyPromptCard(
                        icon = Icons.Rounded.Psychology,
                        title = "Architecture Queries",
                        prompt = "Explain STOMP sequence deduplication in chat architecture",
                        onClick = { onSelectPrompt("Explain STOMP sequence deduplication in chat architecture") },
                        isDark = isDark,
                    )
                    EmptyPromptCard(
                        icon = Icons.Rounded.Code,
                        title = "Code Implementation",
                        prompt = "Write a Flutter widget for smooth real-time voice message player",
                        onClick = { onSelectPrompt("Write a Flutter widget for smooth real-time voice message player") },
                        isDark = isDark,
                    )
                    EmptyPromptCard(
                        icon = Icons.Rounded.Security,
                        title = "System Resilience",
                        prompt = "How does single-flight token refresh interceptor prevent 401 storms?",
                        onClick = { onSelectPrompt("How does single-flight token refresh interceptor prevent 401 storms?") },
                        isDark = isDark,
                    )
                    EmptyPromptCard(
                        icon = Icons.Rounded.Lightbulb,
                        title = "Data Architecture",
                        prompt = "Design an end-to-end database schema for a high scale chat app",
                        onClick = { onSelectPrompt("Design an end-to-end database schema for a high scale chat app") },
                        isDark = isDark,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPromptCard(
    icon: ImageVector,
    title: String,
    prompt: String,
    onClick: () -> Unit,
    isDark: Boolean,
) {
    val palette = LocalLeoPalette.current
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .aiPressable(onClick = onClick)
            .shadow(2.dp, shape, ambientColor = AiTokens.cardShadow, spotColor = AiTokens.cardShadow)
            .background(palette.surface, shape)
            .border(1.dp, palette.border, shape)
            .padding(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .background(palette.primarySoft, RoundedCornerShape(10.dp)),
        ) {
            Icon(icon, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = palette.textPrimary)
            Spacer(Modifier.height(2.dp))
            Text(
                prompt,
                fontSize = 11.5.sp,
                color = palette.textSecondary,
                maxLines = 1,
            )
        }
    }
}

/**
 * Beautiful animated card shown while Leo AI is formulating a response.
 * Features an animated gradient border shimmer, breathing aura badge, wave dots, and streaming skeleton lines.
 */
@Composable
fun AiGeneratingIndicator(modifier: Modifier = Modifier) {
    val palette = LocalLeoPalette.current
    val shape = RoundedCornerShape(20.dp)
    val transition = rememberInfiniteTransition(label = "ai_generating_pulse")
    val scale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "generating_badge_scale",
    )
    val auraAlpha by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "generating_aura_alpha",
    )
    val shimmerOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "generating_border_shimmer",
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            palette.border,
            AiTokens.gradientStart.copy(alpha = 0.65f),
            AiTokens.gradientEnd.copy(alpha = 0.75f),
            palette.border,
        ),
        start = Offset(shimmerOffset - 400f, 0f),
        end = Offset(shimmerOffset + 400f, 400f),
    )

    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = AiTokens.gradientGlowSoft, spotColor = AiTokens.gradientGlowSoft)
            .background(palette.surface, shape)
            .border(1.2.dp, borderBrush, shape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                // Expanding glowing halo
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .scale(scale * 1.15f)
                        .background(AiTokens.gradientStart.copy(alpha = auraAlpha), CircleShape),
                )
                // Breathing badge
                AiGradientBadge(
                    size = 28,
                    cornerRadius = 8,
                    iconSize = 16,
                    modifier = Modifier.scale(scale),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        AiStrings.assistantName,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Thinking",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = LeoColors.primary,
                        modifier = Modifier
                            .background(palette.primarySoft, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    AiStrings.generating,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = palette.textMuted,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(8.dp))
            AiWaveDots(dotColor = LeoColors.primary)
        }
        Spacer(Modifier.height(14.dp))
        // Thought stream shimmer lines simulating incoming reasoning
        LeoShimmer(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LeoSkeleton(modifier = Modifier.fillMaxWidth(0.88f).height(9.dp), cornerRadius = 6.dp)
                LeoSkeleton(modifier = Modifier.fillMaxWidth(0.68f).height(9.dp), cornerRadius = 6.dp)
                LeoSkeleton(modifier = Modifier.fillMaxWidth(0.45f).height(9.dp), cornerRadius = 6.dp)
            }
        }
    }
}
