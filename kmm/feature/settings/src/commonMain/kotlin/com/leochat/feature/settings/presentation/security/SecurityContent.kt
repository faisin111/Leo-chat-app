package com.leochat.feature.settings.presentation.security

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.designsystem.component.buttons.LeoPrimaryButton
import com.leochat.core.designsystem.component.inputs.LeoAuthField
import com.leochat.core.designsystem.component.inputs.PasswordVisibilityToggle
import com.leochat.core.designsystem.component.layout.LeoScreen
import com.leochat.core.designsystem.component.settings.DeactivateAccountCard
import com.leochat.core.designsystem.component.settings.DeactivateAccountDialog
import com.leochat.core.designsystem.component.settings.SettingsAppBar
import com.leochat.core.designsystem.component.settings.SettingsCard
import com.leochat.core.designsystem.component.settings.SettingsIconBadge
import com.leochat.core.designsystem.component.settings.SettingsTextScope
import com.leochat.core.designsystem.component.settings.TwoFactorCard
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.feature.settings.presentation.settings.SecurityUiState

/**
 * Stateless security layout driven by [state]. The deactivate confirmation is transient UI state;
 * [onDeactivate] fires only after the user confirms the dialog.
 */
@Composable
fun SecurityContent(
    state: SecurityUiState,
    onBack: () -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleCurrentVisibility: () -> Unit,
    onToggleNewVisibility: () -> Unit,
    onToggleConfirmVisibility: () -> Unit,
    onSubmitPasswordChange: () -> Unit,
    onDeactivate: () -> Unit,
) {
    var confirmDeactivate by rememberSaveable { mutableStateOf(false) }
    val palette = LeoThemeTokens.palette

    LeoScreen(background = palette.background) {
        SettingsTextScope {
            Column(Modifier.fillMaxSize()) {
                SettingsAppBar(SettingsStrings.security, onBack, background = palette.surface)
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                ) {
                    ChangePasswordCard(
                        state = state,
                        onCurrentPasswordChange = onCurrentPasswordChange,
                        onNewPasswordChange = onNewPasswordChange,
                        onConfirmPasswordChange = onConfirmPasswordChange,
                        onToggleCurrentVisibility = onToggleCurrentVisibility,
                        onToggleNewVisibility = onToggleNewVisibility,
                        onToggleConfirmVisibility = onToggleConfirmVisibility,
                        onSubmit = onSubmitPasswordChange,
                    )
                    Spacer(Modifier.height(20.dp))
                    TwoFactorCard()
                    Spacer(Modifier.height(20.dp))
                    DeactivateAccountCard(enabled = !state.isDeactivating, onDeactivate = { confirmDeactivate = true })
                }
            }
        }
    }

    if (confirmDeactivate) {
        DeactivateAccountDialog(
            onDismiss = { confirmDeactivate = false },
            onConfirm = {
                confirmDeactivate = false
                onDeactivate()
            },
        )
    }
}

/** Change-password form section bound to [SecurityUiState]. */
@Composable
private fun ChangePasswordCard(
    state: SecurityUiState,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleCurrentVisibility: () -> Unit,
    onToggleNewVisibility: () -> Unit,
    onToggleConfirmVisibility: () -> Unit,
    onSubmit: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    SettingsCard(padding = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingsIconBadge(Icons.Rounded.Key, LeoColors.primary, palette.primarySoft)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    SettingsStrings.changePasswordTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary,
                )
                Text(SettingsStrings.changePasswordBody, fontSize = 12.sp, color = palette.textSecondary)
            }
        }
        Spacer(Modifier.height(20.dp))
        LeoAuthField(
            label = SettingsStrings.currentPassword,
            hint = SettingsStrings.currentPasswordHint,
            value = state.currentPassword,
            onValueChange = onCurrentPasswordChange,
            obscureText = state.obscureCurrent,
            suffix = { PasswordVisibilityToggle(state.obscureCurrent, onToggleCurrentVisibility) },
        )
        Spacer(Modifier.height(14.dp))
        LeoAuthField(
            label = AppStrings.newPassword,
            hint = AppStrings.strongPassword,
            value = state.newPassword,
            onValueChange = onNewPasswordChange,
            obscureText = state.obscureNew,
            suffix = { PasswordVisibilityToggle(state.obscureNew, onToggleNewVisibility) },
        )
        Spacer(Modifier.height(14.dp))
        LeoAuthField(
            label = AppStrings.confirmPassword,
            hint = SettingsStrings.confirmPasswordHint,
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            obscureText = state.obscureConfirm,
            suffix = { PasswordVisibilityToggle(state.obscureConfirm, onToggleConfirmVisibility) },
        )
        Spacer(Modifier.height(20.dp))
        LeoPrimaryButton(
            label = AppStrings.updatePassword,
            onClick = onSubmit,
            icon = Icons.Rounded.CheckCircleOutline,
            isLoading = state.isUpdating,
            modifier = Modifier.fillMaxWidth().height(50.dp),
        )
    }
}
