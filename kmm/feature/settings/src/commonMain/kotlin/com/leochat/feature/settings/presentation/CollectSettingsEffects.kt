package com.leochat.feature.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.leochat.core.designsystem.component.feedback.LeoSnackBarController
import com.leochat.feature.settings.presentation.settings.SettingsEffect
import kotlinx.coroutines.flow.Flow

/** Routes settings controller effects to the snack bar and navigation callbacks. */
@Composable
internal fun CollectSettingsEffects(
    effects: Flow<SettingsEffect>,
    snackBar: LeoSnackBarController,
    onNavigateBack: () -> Unit = {},
    onSignedOut: () -> Unit = {},
) {
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowSuccess -> snackBar.showSuccess(effect.message)
                is SettingsEffect.ShowError -> snackBar.showError(effect.message)
                SettingsEffect.NavigateBack -> onNavigateBack()
                SettingsEffect.SignedOut -> onSignedOut()
            }
        }
    }
}
