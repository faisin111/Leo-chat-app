package com.leochat.core.designsystem.component.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.component.onboarding.model.OnboardingPreviewKind
import com.leochat.core.designsystem.component.onboarding.model.OnboardingSlideUi
import com.leochat.core.designsystem.theme.OnboardingTokens

private val PillShape = RoundedCornerShape(999.dp)

/** One onboarding page: preview card, step pill, title, body, capability chip. */
@Composable
fun OnboardingSlideView(slide: OnboardingSlideUi, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        when (slide.preview) {
            OnboardingPreviewKind.Chat -> ChatPreviewCard()
            OnboardingPreviewKind.Sync -> SyncPreviewCard()
            OnboardingPreviewKind.Expression -> ExpressionPreviewCard()
        }
        Spacer(Modifier.height(24.dp))
        Text(
            slide.step,
            color = LeoColors.primary,
            fontSize = 11.sp,
            letterSpacing = 0.6.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.background(LeoColors.primarySoft, PillShape).padding(horizontal = 10.dp, vertical = 4.dp),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            slide.title,
            textAlign = TextAlign.Center,
            fontSize = 28.sp,
            lineHeight = (28 * 1.15).sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.8).sp,
            color = LeoColors.textPrimary,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            slide.body,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            lineHeight = (14 * 1.5).sp,
            fontWeight = FontWeight.SemiBold,
            color = LeoColors.textSecondary,
            modifier = Modifier.widthIn(max = 420.dp),
        )
        Spacer(Modifier.height(14.dp))
        Text(
            slide.badge,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = LeoColors.textPrimary,
            modifier = Modifier
                .shadow(4.dp, PillShape, ambientColor = OnboardingTokens.badgeShadow, spotColor = OnboardingTokens.badgeShadow)
                .background(Color.White, PillShape)
                .border(1.dp, LeoColors.border, PillShape)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

/** Page indicator dot that stretches when active. */
@Composable
fun OnboardingDot(active: Boolean, modifier: Modifier = Modifier) {
    val width by animateDpAsState(if (active) 22.dp else 7.dp, tween(200), label = "dotWidth")
    val color by animateColorAsState(if (active) LeoColors.primary else LeoColors.border, tween(200), label = "dotColor")
    Box(
        modifier = modifier
            .padding(end = 6.dp)
            .width(width)
            .height(7.dp)
            .background(color, RoundedCornerShape(99.dp)),
    )
}
