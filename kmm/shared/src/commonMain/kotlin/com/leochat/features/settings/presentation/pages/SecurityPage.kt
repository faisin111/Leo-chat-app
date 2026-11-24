package com.leochat.features.settings.presentation.pages

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
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.AppStrings
import com.leochat.core.constants.SettingsStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.buttons.LeoPrimaryButton
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.core.widgets.common.inputs.LeoAuthField
import com.leochat.core.widgets.common.inputs.PasswordVisibilityToggle
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.settings.presentation.controllers.SecurityController
import com.leochat.features.settings.presentation.states.SecurityState
import com.leochat.features.settings.presentation.widgets.CollectSettingsEffects
import com.leochat.features.settings.presentation.widgets.SettingsAppBar
import com.leochat.features.settings.presentation.widgets.SettingsCard
import com.leochat.features.settings.presentation.widgets.SettingsIconBadge
import com.leochat.features.settings.presentation.widgets.SettingsTextScope
import org.koin.compose.viewmodel.koinViewModel

/**
 * Change password, 2FA note, and account deactivation. Mirrors `SecurityPage`.
 * [onSignedOut] fires after a confirmed deactivation has logged the user out.
 */
@Composable
fun SecurityPage(
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    controller: SecurityController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectSettingsEffects(controller.effects, LocalLeoSnackBar.current, onSignedOut = onSignedOut)
    var confirmDeactivate by rememberSaveable { mutableStateOf(false) }
    val palette = LeoThemeTokens.palette

    LeoScreen(background = palette.background) {
        SettingsTextScope {
            Column(Modifier.fillMaxSize()) {
                SettingsAppBar(SettingsStrings.security, onBack, background = palette.surface)
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                ) {
                    ChangePasswordCard(state, controller)
                    Spacer(Modifier.height(20.dp))
                    TwoFactorCard()
                    Spacer(Modifier.height(20.dp))
                    DeactivateCard(enabled = !state.isDeactivating, onDeactivate = { confirmDeactivate = true })
                }
            }
        }
    }

    if (confirmDeactivate) {
        DeactivateDialog(
            onDismiss = { confirmDeactivate = false },
            onConfirm = {
                confirmDeactivate = false
                controller.deactivate()
            },
        )
    }
}

@Composable
private fun ChangePasswordCard(state: SecurityState, controller: SecurityController) {
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
            onValueChange = controller::onCurrentPasswordChange,
            obscureText = state.obscureCurrent,
            suffix = { PasswordVisibilityToggle(state.obscureCurrent, controller::toggleCurrentVisibility) },
        )
        Spacer(Modifier.height(14.dp))
        LeoAuthField(
            label = AppStrings.newPassword,
            hint = AppStrings.strongPassword,
            value = state.newPassword,
            onValueChange = controller::onNewPasswordChange,
            obscureText = state.obscureNew,
            suffix = { PasswordVisibilityToggle(state.obscureNew, controller::toggleNewVisibility) },
        )
        Spacer(Modifier.height(14.dp))
        LeoAuthField(
            label = AppStrings.confirmPassword,
            hint = SettingsStrings.confirmPasswordHint,
            value = state.confirmPassword,
            onValueChange = controller::onConfirmPasswordChange,
            obscureText = state.obscureConfirm,
            suffix = { PasswordVisibilityToggle(state.obscureConfirm, controller::toggleConfirmVisibility) },
        )
        Spacer(Modifier.height(20.dp))
        LeoPrimaryButton(
            label = AppStrings.updatePassword,
            onClick = controller::submitPasswordChange,
            icon = Icons.Rounded.CheckCircleOutline,
            isLoading = state.isUpdating,
            modifier = Modifier.fillMaxWidth().height(50.dp),
        )
    }
}

@Composable
private fun TwoFactorCard() {
    val palette = LeoThemeTokens.palette
    SettingsCard(padding = 20.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingsIconBadge(Icons.Rounded.VerifiedUser, LeoColors.success, LeoColors.success.copy(alpha = 0.12f))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(SettingsStrings.twoFactorTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(SettingsStrings.twoFactorBody, fontSize = 12.sp, color = palette.textSecondary)
            }
        }
    }
}

@Composable
private fun DeactivateCard(enabled: Boolean, onDeactivate: () -> Unit) {
    val palette = LeoThemeTokens.palette
    SettingsCard(padding = 20.dp, borderColor = LeoColors.destructive.copy(alpha = 0.3f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingsIconBadge(Icons.Rounded.PersonRemove, LeoColors.destructive, LeoColors.destructive.copy(alpha = 0.1f))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    SettingsStrings.deactivateTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = LeoColors.destructive,
                )
                Spacer(Modifier.height(2.dp))
                Text(SettingsStrings.deactivateBody, fontSize = 12.sp, color = palette.textSecondary)
            }
            TextButton(onClick = onDeactivate, enabled = enabled) {
                Text(SettingsStrings.deactivate, color = LeoColors.destructive, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DeactivateDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(SettingsStrings.deactivateDialogTitle) },
        text = { Text(SettingsStrings.deactivateDialogBody) },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(SettingsStrings.cancel) }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = LeoColors.destructive, contentColor = Color.White),
            ) { Text(SettingsStrings.deactivate) }
        },
    )
}
