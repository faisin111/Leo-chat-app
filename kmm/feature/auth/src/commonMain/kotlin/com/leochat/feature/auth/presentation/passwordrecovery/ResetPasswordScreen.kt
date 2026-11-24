package com.leochat.feature.auth.presentation.passwordrecovery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.auth.presentation.CollectAuthEffects
import org.koin.compose.viewmodel.koinViewModel

/** Set a new password with a recovery token. Mirrors `ResetPasswordScreen`. */
@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
    onNavigateLogin: () -> Unit,
    controller: ResetPasswordViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectAuthEffects(controller.effects, LocalLeoSnackBar.current, onNavigateLogin = onNavigateLogin)
    ResetPasswordContent(
        state = state,
        onTokenChange = controller::onTokenChange,
        onNewPasswordChange = controller::onNewPasswordChange,
        onConfirmPasswordChange = controller::onConfirmPasswordChange,
        onToggleNewVisibility = controller::toggleNewVisibility,
        onToggleConfirmVisibility = controller::toggleConfirmVisibility,
        onSubmit = controller::submit,
        onBack = onBack,
    )
}
