package com.leochat.feature.settings.presentation.sessions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.settings.presentation.CollectSettingsEffects
import org.koin.compose.viewmodel.koinViewModel

/** Logged-in devices with revoke. Mirrors `SessionsScreen`. */
@Composable
fun SessionsScreen(
    onBack: () -> Unit,
    controller: SessionsViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectSettingsEffects(controller.effects, LocalLeoSnackBar.current)
    SessionsContent(state = state, onBack = onBack, onRevoke = controller::revoke)
}
