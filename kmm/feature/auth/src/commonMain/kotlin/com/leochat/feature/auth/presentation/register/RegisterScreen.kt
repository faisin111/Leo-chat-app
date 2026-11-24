package com.leochat.feature.auth.presentation.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.auth.presentation.CollectAuthEffects
import org.koin.compose.viewmodel.koinViewModel

/** Account creation screen. Mirrors `RegisterScreen`. */
@Composable
fun RegisterScreen(
    onNavigateHome: () -> Unit,
    onSignIn: () -> Unit,
    controller: RegisterViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectAuthEffects(controller.effects, LocalLeoSnackBar.current, onNavigateHome = onNavigateHome)
    RegisterContent(
        state = state,
        onDisplayNameChange = controller::onDisplayNameChange,
        onUsernameChange = controller::onUsernameChange,
        onEmailChange = controller::onEmailChange,
        onPasswordChange = controller::onPasswordChange,
        onTogglePasswordVisibility = controller::togglePasswordVisibility,
        onTermsChange = controller::onTermsChange,
        onSubmit = controller::submit,
        onSignIn = onSignIn,
    )
}
