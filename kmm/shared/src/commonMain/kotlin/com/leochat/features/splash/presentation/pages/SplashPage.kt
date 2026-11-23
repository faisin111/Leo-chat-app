package com.leochat.features.splash.presentation.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.branding.LeoLogo
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.splash.presentation.controllers.SplashController
import com.leochat.features.splash.presentation.states.SplashDestination
import org.koin.compose.viewmodel.koinViewModel

/** Startup screen shown on every launch for three seconds. Mirrors `SplashPage`. */
@Composable
fun SplashPage(
    onNavigateHome: () -> Unit,
    onNavigateOnboarding: () -> Unit,
    controller: SplashController = koinViewModel(),
) {
    val destination by controller.destination.collectAsStateWithLifecycle()
    LaunchedEffect(destination) {
        when (destination) {
            SplashDestination.Home -> onNavigateHome()
            SplashDestination.Onboarding -> onNavigateOnboarding()
            null -> Unit
        }
    }

    LeoScreen {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            LeoLogo()
            Spacer(Modifier.height(14.dp))
            Text(
                AppStrings.yourChatsSubtitle,
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                color = LeoColors.textSecondary,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            LinearProgressIndicator(
                modifier = Modifier.width(56.dp).height(3.dp).clip(RoundedCornerShape(99.dp)),
                color = LeoColors.primary,
                trackColor = LeoColors.border,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Shield, contentDescription = null, tint = LeoColors.textMuted, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(6.dp))
                Text(AppStrings.splashSubtitle, color = LeoColors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
