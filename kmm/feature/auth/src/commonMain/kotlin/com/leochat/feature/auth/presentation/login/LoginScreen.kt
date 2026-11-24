package com.leochat.feature.auth.presentation.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.auth.presentation.CollectAuthEffects
import org.koin.compose.viewmodel.koinViewModel

/** Sign-in screen. Mirrors `LoginScreen`. */
@Composable
fun LoginScreen(
    onNavigateHome: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    controller: LoginViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectAuthEffects(controller.effects, LocalLeoSnackBar.current, onNavigateHome = onNavigateHome)
    LoginContent(
        state = state,
        onUsernameChange = controller::onUsernameChange,
        onPasswordChange = controller::onPasswordChange,
        onTogglePasswordVisibility = controller::togglePasswordVisibility,
        onSubmit = controller::submit,
        onForgotPassword = onForgotPassword,
        onRegister = onRegister,
    )
}
