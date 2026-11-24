package com.leochat.feature.auth.presentation.passwordrecovery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.component.auth.AuthIconBadge
import com.leochat.core.designsystem.component.auth.CenteredScrollColumn
import com.leochat.core.designsystem.component.auth.ResetLinkSentPanel
import com.leochat.core.designsystem.component.buttons.LeoPrimaryButton
import com.leochat.core.designsystem.component.buttons.LeoTextLink
import com.leochat.core.designsystem.component.inputs.LeoAuthField
import com.leochat.core.designsystem.component.layout.LeoBackBar
import com.leochat.core.designsystem.component.layout.LeoScreen
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoTypography
import com.leochat.feature.auth.presentation.ForgotPasswordUiState

/** Stateless password-recovery layout: request form, or the sent confirmation once [ForgotPasswordUiState.submitted]. */
@Composable
fun ForgotPasswordContent(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onBackToLogin: () -> Unit,
    onEnterResetCode: () -> Unit,
) {
    LeoScreen {
        Column {
            LeoBackBar(onBack = onBack)
            CenteredScrollColumn(
                maxWidth = 440.dp,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = if (state.submitted) Alignment.CenterHorizontally else Alignment.Start,
            ) {
                if (state.submitted) {
                    ResetLinkSentPanel(state.email, onEnterResetCode, onBackToLogin)
                } else {
                    ForgotPasswordForm(state, onEmailChange, onSubmit, onBackToLogin)
                }
            }
        }
    }
}

/** Email request form bound to [ForgotPasswordUiState]. */
@Composable
private fun ForgotPasswordForm(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
) {
    AuthIconBadge(Icons.Rounded.LockReset)
    Spacer(Modifier.height(24.dp))
    Text(AppStrings.forgotPasswordTitle, style = LeoTypography.mainHeading)
    Spacer(Modifier.height(10.dp))
    Text(
        AppStrings.forgotPasswordSubtitle,
        fontSize = 15.sp,
        lineHeight = (15 * 1.45).sp,
        fontWeight = FontWeight.Medium,
        color = LeoColors.textSecondary,
    )
    Spacer(Modifier.height(28.dp))
    LeoAuthField(
        label = AppStrings.emailAddress,
        hint = AppStrings.emailHint,
        value = state.email,
        onValueChange = onEmailChange,
        icon = Icons.Outlined.MailOutline,
        keyboardType = KeyboardType.Email,
    )
    Spacer(Modifier.height(24.dp))
    LeoPrimaryButton(
        label = AppStrings.sendResetLink,
        onClick = onSubmit,
        icon = Icons.AutoMirrored.Rounded.Send,
        isLoading = state.isLoading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
    )
    Spacer(Modifier.height(16.dp))
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        LeoTextLink(label = AppStrings.backToLogin, onClick = onBackToLogin, compact = false, fontWeight = FontWeight.Bold)
    }
}
