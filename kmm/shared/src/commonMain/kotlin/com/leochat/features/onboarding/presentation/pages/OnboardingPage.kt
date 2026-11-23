package com.leochat.features.onboarding.presentation.pages

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.branding.LeoLogo
import com.leochat.core.widgets.common.buttons.LeoOutlineButton
import com.leochat.core.widgets.common.buttons.LeoPrimaryButton
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.onboarding.presentation.states.onboardingSlides
import com.leochat.features.onboarding.presentation.widgets.OnboardingDot
import com.leochat.features.onboarding.presentation.widgets.OnboardingSlideView
import kotlinx.coroutines.launch

/** Three-step intro carousel. Mirrors `OnboardingPage`. */
@Composable
fun OnboardingPage(onFinish: () -> Unit) {
    val pager = rememberPagerState { onboardingSlides.size }
    val scope = rememberCoroutineScope()
    val isLast = pager.currentPage == onboardingSlides.lastIndex

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
            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { index ->
                OnboardingSlideView(onboardingSlides[index])
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row { onboardingSlides.indices.forEach { OnboardingDot(active = it == pager.currentPage) } }
                Spacer(Modifier.weight(1f))
                LeoPrimaryButton(
                    label = if (isLast) AppStrings.startChatting else AppStrings.next,
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = {
                        if (isLast) onFinish()
                        else scope.launch { pager.animateScrollToPage(pager.currentPage + 1, animationSpec = tween(280, easing = EaseOut)) }
                    },
                )
            }
            Text(
                AppStrings.onboardingFooter,
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                color = LeoColors.textMuted,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 10.dp, end = 24.dp, bottom = 16.dp),
            )
        }
    }
}
