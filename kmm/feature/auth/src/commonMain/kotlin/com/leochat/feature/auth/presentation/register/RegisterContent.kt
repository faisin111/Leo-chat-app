package com.leochat.feature.auth.presentation.register

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoTypography
import com.leochat.core.designsystem.component.branding.LeoLogo
import com.leochat.core.designsystem.component.buttons.LeoPrimaryButton
import com.leochat.core.designsystem.component.inputs.LeoAuthField
import com.leochat.core.designsystem.component.inputs.PasswordVisibilityToggle
import com.leochat.core.designsystem.component.layout.LeoScreen
import com.leochat.core.designsystem.component.auth.AuthFooterPrompt
import com.leochat.core.designsystem.component.auth.CenteredScrollColumn
import com.leochat.core.designsystem.component.auth.PasswordStrengthHint
import com.leochat.core.designsystem.component.auth.ProfilePhotoPrompt
import com.leochat.core.designsystem.component.auth.TermsAgreementRow
import com.leochat.feature.auth.presentation.RegisterUiState

/** Stateless account-creation layout driven by [state]. */
@Composable
fun RegisterContent(
    state: RegisterUiState,
    onDisplayNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onTermsChange: (Boolean) -> Unit,
    onSubmit: () -> Unit,
    onSignIn: () -> Unit,
) {
    LeoScreen {
        CenteredScrollColumn(
            maxWidth = 480.dp,
            contentPadding = PaddingValues(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 28.dp),
            centerVertically = false,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSignIn, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = AppStrings.backToSignIn,
                        tint = LeoColors.textPrimary,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(Modifier.weight(1f))
                LeoLogo()
            }
            Spacer(Modifier.height(28.dp))
            Text(AppStrings.registerTitle, style = LeoTypography.mainHeading)
            Spacer(Modifier.height(10.dp))
            Text(AppStrings.registerSubtitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = LeoColors.textSecondary)
            Spacer(Modifier.height(26.dp))
            ProfilePhotoPrompt()
            Spacer(Modifier.height(24.dp))
            LeoAuthField(
                label = AppStrings.displayName,
                hint = AppStrings.displayNameHint,
                value = state.displayName,
                onValueChange = onDisplayNameChange,
                icon = Icons.Outlined.PersonOutline,
            )
            Spacer(Modifier.height(18.dp))
            LeoAuthField(
                label = AppStrings.username,
                hint = AppStrings.usernameHint,
                value = state.username,
                onValueChange = onUsernameChange,
                icon = Icons.Rounded.AlternateEmail,
            )
            Spacer(Modifier.height(18.dp))
            LeoAuthField(
                label = AppStrings.emailAddress,
                hint = AppStrings.emailHint,
                value = state.email,
                onValueChange = onEmailChange,
                icon = Icons.Outlined.MailOutline,
                keyboardType = KeyboardType.Email,
            )
            Spacer(Modifier.height(18.dp))
            LeoAuthField(
                label = AppStrings.createPassword,
                hint = AppStrings.passwordHint,
                value = state.password,
                onValueChange = onPasswordChange,
                obscureText = state.obscurePassword,
                suffix = { PasswordVisibilityToggle(state.obscurePassword, onTogglePasswordVisibility) },
            )
            Spacer(Modifier.height(12.dp))
            PasswordStrengthHint()
            Spacer(Modifier.height(16.dp))
            TermsAgreementRow(accepted = state.acceptedTerms, onChange = onTermsChange)
            Spacer(Modifier.height(24.dp))
            LeoPrimaryButton(
                label = AppStrings.createAccountButton,
                onClick = if (state.acceptedTerms) onSubmit else null,
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
            Spacer(Modifier.height(24.dp))
            AuthFooterPrompt(prompt = AppStrings.alreadyConversation, action = AppStrings.login, onAction = onSignIn)
        }
    }
}
