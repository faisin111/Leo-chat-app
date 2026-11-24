package com.leochat.core.designsystem.component.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.designsystem.component.dialog.LeoConfirmationDialog
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens

/** Green "two-factor" informational card on the security page. */
@Composable
fun TwoFactorCard() {
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

/** Destructive-bordered card with a "Deactivate" text action. */
@Composable
fun DeactivateAccountCard(enabled: Boolean, onDeactivate: () -> Unit) {
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

/** Confirmation dialog before deactivating the account. */
@Composable
fun DeactivateAccountDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    LeoConfirmationDialog(
        title = SettingsStrings.deactivateDialogTitle,
        message = SettingsStrings.deactivateDialogBody,
        confirmText = SettingsStrings.deactivate,
        cancelText = SettingsStrings.cancel,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
