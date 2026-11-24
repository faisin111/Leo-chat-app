package com.leochat.feature.settings.presentation.security

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.settings.presentation.CollectSettingsEffects
import org.koin.compose.viewmodel.koinViewModel

/**
 * Change password, 2FA note, and account deactivation. Mirrors `SecurityScreen`.
 * [onSignedOut] fires after a confirmed deactivation has logged the user out.
 */
@Composable
fun SecurityScreen(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    controller: SecurityViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectSettingsEffects(controller.effects, LocalLeoSnackBar.current, onSignedOut = onSignedOut)
    SecurityContent(
        state = state,
        onBack = onBack,
        onCurrentPasswordChange = controller::onCurrentPasswordChange,
        onNewPasswordChange = controller::onNewPasswordChange,
        onConfirmPasswordChange = controller::onConfirmPasswordChange,
        onToggleCurrentVisibility = controller::toggleCurrentVisibility,
        onToggleNewVisibility = controller::toggleNewVisibility,
        onToggleConfirmVisibility = controller::toggleConfirmVisibility,
        onSubmitPasswordChange = controller::submitPasswordChange,
        onDeactivate = controller::deactivate,
    )
}
