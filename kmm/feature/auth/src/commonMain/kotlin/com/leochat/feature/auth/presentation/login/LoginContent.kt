package com.leochat.feature.auth.presentation.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.component.auth.AuthFooterPrompt
import com.leochat.core.designsystem.component.auth.CenteredScrollColumn
import com.leochat.core.designsystem.component.auth.WelcomeCard
import com.leochat.core.designsystem.component.branding.LeoLogo
import com.leochat.core.designsystem.component.buttons.LeoPrimaryButton
import com.leochat.core.designsystem.component.buttons.LeoTextLink
import com.leochat.core.designsystem.component.inputs.LeoAuthField
import com.leochat.core.designsystem.component.inputs.PasswordVisibilityToggle
import com.leochat.core.designsystem.component.layout.LeoScreen
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.theme.LeoTypography
import com.leochat.feature.auth.presentation.LoginUiState

/** Stateless sign-in layout driven by [state]. */
@Composable
fun LoginContent(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    LeoScreen {
        CenteredScrollColumn(
            maxWidth = 480.dp,
            contentPadding = PaddingValues(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 28.dp),
            centerVertically = false,
        ) {
            LeoLogo()
            Spacer(Modifier.height(24.dp))
            WelcomeCard()
            Spacer(Modifier.height(28.dp))
            Text(AppStrings.loginTitle, style = LeoTypography.mainHeading)
            Spacer(Modifier.height(10.dp))
            Text(
                AppStrings.loginSubtitle,
                fontSize = 15.sp,
                lineHeight = (15 * 1.45).sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textSecondary,
            )
            Spacer(Modifier.height(26.dp))
            LeoAuthField(
                label = AppStrings.username,
                hint = AppStrings.usernameHint,
                value = state.username,
                onValueChange = onUsernameChange,
                icon = Icons.Outlined.PersonOutline,
            )
            Spacer(Modifier.height(18.dp))
            LeoAuthField(
                label = AppStrings.password,
                hint = AppStrings.passwordHint,
                value = state.password,
                onValueChange = onPasswordChange,
                obscureText = state.obscurePassword,
                suffix = { PasswordVisibilityToggle(state.obscurePassword, onTogglePasswordVisibility) },
            )
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                LeoTextLink(label = AppStrings.forgotPassword, onClick = onForgotPassword)
            }
            Spacer(Modifier.height(20.dp))
            LeoPrimaryButton(
                label = AppStrings.login,
                onClick = onSubmit,
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
            Spacer(Modifier.height(26.dp))
            AuthFooterPrompt(prompt = AppStrings.newToLeoChat, action = AppStrings.createAccount, onAction = onRegister)
        }
    }
}
