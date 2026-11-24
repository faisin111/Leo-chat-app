package com.leochat.feature.onboarding.presentation.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.component.branding.LeoLogo
import com.leochat.core.designsystem.component.buttons.LeoOutlineButton
import com.leochat.core.designsystem.component.buttons.LeoPrimaryButton
import com.leochat.core.designsystem.component.layout.LeoScreen
import com.leochat.core.designsystem.component.onboarding.OnboardingDot
import com.leochat.core.designsystem.component.onboarding.OnboardingSlideView
import com.leochat.core.designsystem.component.onboarding.model.OnboardingSlideUi
import com.leochat.core.designsystem.theme.LeoThemeTokens

/**
 * Stateless onboarding layout. [pagerState] is hoisted UI state owned by the screen;
 * the primary button calls [onNext] on every page but the last, and [onFinish] on the last.
 */
@Composable
fun OnboardingContent(
    slides: List<OnboardingSlideUi>,
    pagerState: PagerState,
    onNext: () -> Unit,
    onFinish: () -> Unit,
) {
    val isLast = pagerState.currentPage == slides.lastIndex
    val palette = LeoThemeTokens.palette

    LeoScreen {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LeoLogo(compact = true)
                Spacer(Modifier.weight(1f))
                LeoOutlineButton(label = AppStrings.skip, onClick = onFinish)
            }
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { index ->
                OnboardingSlideView(slides[index])
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row { slides.indices.forEach { OnboardingDot(active = it == pagerState.currentPage) } }
                Spacer(Modifier.weight(1f))
                LeoPrimaryButton(
                    label = if (isLast) AppStrings.startChatting else AppStrings.next,
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = { if (isLast) onFinish() else onNext() },
                )
            }
            Text(
                AppStrings.onboardingFooter,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                color = palette.textMuted,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 10.dp, end = 24.dp, bottom = 16.dp),
            )
        }
    }
}
