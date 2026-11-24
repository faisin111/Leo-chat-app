package com.leochat.feature.auth.presentation.passwordrecovery
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoTypography
import com.leochat.core.designsystem.component.buttons.LeoPrimaryButton
import com.leochat.core.designsystem.component.inputs.LeoAuthField
import com.leochat.core.designsystem.component.inputs.PasswordVisibilityToggle
import com.leochat.core.designsystem.component.layout.LeoBackBar
import com.leochat.core.designsystem.component.layout.LeoScreen
import com.leochat.core.designsystem.component.auth.AuthIconBadge
import com.leochat.core.designsystem.component.auth.CenteredScrollColumn
import com.leochat.feature.auth.presentation.ResetPasswordUiState

/** Stateless new-password layout driven by [state]. */
@Composable
fun ResetPasswordContent(
    state: ResetPasswordUiState,
    onTokenChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onToggleNewVisibility: () -> Unit,
    onToggleConfirmVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    LeoScreen {
        Column {
            LeoBackBar(onBack = onBack)
            CenteredScrollColumn(
                maxWidth = 440.dp,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            ) {
                AuthIconBadge(Icons.Rounded.Key)
                Spacer(Modifier.height(24.dp))
                Text(AppStrings.resetPasswordTitle, style = LeoTypography.mainHeading)
                Spacer(Modifier.height(10.dp))
                Text(
                    AppStrings.resetPasswordSubtitle,
                    fontSize = 15.sp,
                    lineHeight = (15 * 1.45).sp,
                    fontWeight = FontWeight.Medium,
                    color = LeoColors.textSecondary,
                )
                Spacer(Modifier.height(28.dp))
                LeoAuthField(
                    label = AppStrings.resetToken,
                    hint = AppStrings.resetTokenHint,
                    value = state.token,
                    onValueChange = onTokenChange,
                    icon = Icons.Outlined.VpnKey,
                )
                Spacer(Modifier.height(18.dp))
                LeoAuthField(
                    label = AppStrings.newPassword,
                    hint = AppStrings.passwordHint,
                    value = state.newPassword,
                    onValueChange = onNewPasswordChange,
                    obscureText = state.obscureNew,
                    suffix = { PasswordVisibilityToggle(state.obscureNew, onToggleNewVisibility) },
                )
                Spacer(Modifier.height(18.dp))
                LeoAuthField(
                    label = AppStrings.confirmPassword,
                    hint = AppStrings.passwordHint,
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    obscureText = state.obscureConfirm,
                    suffix = { PasswordVisibilityToggle(state.obscureConfirm, onToggleConfirmVisibility) },
                )
                Spacer(Modifier.height(26.dp))
                LeoPrimaryButton(
                    label = AppStrings.updatePassword,
                    onClick = onSubmit,
                    icon = Icons.Rounded.CheckCircleOutline,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                )
            }
        }
    }
}
