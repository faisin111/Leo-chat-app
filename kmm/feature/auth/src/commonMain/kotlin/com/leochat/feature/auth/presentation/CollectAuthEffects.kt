package com.leochat.feature.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.leochat.core.designsystem.component.feedback.LeoSnackBarController
import kotlinx.coroutines.flow.Flow

/** Routes auth controller effects to the app snack bar and navigation callbacks. */
@Composable
internal fun CollectAuthEffects(
    effects: Flow<AuthEffect>,
    snackBar: LeoSnackBarController,
    onNavigateHome: () -> Unit = {},
    onNavigateLogin: () -> Unit = {},
) {
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is AuthEffect.ShowError -> snackBar.showError(effect.message)
                is AuthEffect.ShowSuccess -> snackBar.showSuccess(effect.message)
                AuthEffect.NavigateHome -> onNavigateHome()
                AuthEffect.NavigateLogin -> onNavigateLogin()
            }
        }
    }
}
