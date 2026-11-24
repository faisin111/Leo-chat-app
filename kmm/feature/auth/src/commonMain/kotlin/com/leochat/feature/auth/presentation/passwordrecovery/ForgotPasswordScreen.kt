package com.leochat.feature.auth.presentation.passwordrecovery

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.auth.presentation.CollectAuthEffects
import org.koin.compose.viewmodel.koinViewModel

/** Password recovery request. Mirrors `ForgotPasswordScreen`. */
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onBackToLogin: () -> Unit,
    onEnterResetCode: () -> Unit,
    controller: ForgotPasswordViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectAuthEffects(controller.effects, LocalLeoSnackBar.current)
    ForgotPasswordContent(
        state = state,
        onEmailChange = controller::onEmailChange,
        onSubmit = controller::submit,
        onBack = onBack,
        onBackToLogin = onBackToLogin,
        onEnterResetCode = onEnterResetCode,
    )
}
