package com.leochat.features.chat.presentation.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.branding.LeoAvatar
import com.leochat.core.widgets.common.branding.LeoLogo
import com.leochat.core.widgets.common.buttons.LeoOutlineButton
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.chat.presentation.controllers.ChatShellController
import org.koin.compose.viewmodel.koinViewModel

/**
 * Placeholder for `/app` until the conversation list, chat, and navigation
 * rail are ported. Shows the signed-in user and allows signing out.
 */
@Composable
fun ChatShellPage(
    onSignedOut: () -> Unit,
    controller: ChatShellController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.signedOut) { if (state.signedOut) onSignedOut() }

    LeoScreen {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LeoLogo()
            Spacer(Modifier.height(32.dp))
            LeoAvatar(radius = 36.dp, name = state.user?.effectiveName, imageUrl = state.user?.avatarUrl)
            Spacer(Modifier.height(12.dp))
            Text(
                state.user?.effectiveName.orEmpty(),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LeoColors.textPrimary,
            )
            Spacer(Modifier.height(24.dp))
            Text(AppStrings.comingSoonTitle, fontSize = 22.sp, fontWeight = FontWeight.Black, color = LeoColors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text(
                AppStrings.comingSoonBody,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = LeoColors.textSecondary,
                modifier = Modifier.widthIn(max = 360.dp),
            )
            Spacer(Modifier.height(28.dp))
            LeoOutlineButton(
                label = AppStrings.signOut,
                onClick = if (state.isSigningOut) null else controller::signOut,
            )
        }
    }
}
