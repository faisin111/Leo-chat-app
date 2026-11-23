package com.leochat.features.auth.presentation.pages

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoTypography
import com.leochat.core.widgets.common.branding.LeoLogo
import com.leochat.core.widgets.common.buttons.LeoPrimaryButton
import com.leochat.core.widgets.common.buttons.LeoTextLink
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.core.widgets.common.inputs.LeoAuthField
import com.leochat.core.widgets.common.inputs.PasswordVisibilityToggle
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.auth.presentation.controllers.LoginController
import com.leochat.features.auth.presentation.widgets.AuthFooterPrompt
import com.leochat.features.auth.presentation.widgets.CenteredScrollColumn
import com.leochat.features.auth.presentation.widgets.CollectAuthEffects
import com.leochat.features.auth.presentation.widgets.WelcomeCard
import org.koin.compose.viewmodel.koinViewModel

/** Sign-in screen. Mirrors `LoginPage`. */
@Composable
fun LoginPage(
    onNavigateHome: () -> Unit,
    onForgotPassword: () -> Unit,
    onRegister: () -> Unit,
    controller: LoginController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectAuthEffects(controller.effects, LocalLeoSnackBar.current, onNavigateHome = onNavigateHome)

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
                color = LeoColors.textSecondary,
            )
            Spacer(Modifier.height(26.dp))
            LeoAuthField(
                label = AppStrings.username,
                hint = AppStrings.usernameHint,
                value = state.username,
                onValueChange = controller::onUsernameChange,
                icon = Icons.Outlined.PersonOutline,
            )
            Spacer(Modifier.height(18.dp))
            LeoAuthField(
                label = AppStrings.password,
                hint = AppStrings.passwordHint,
                value = state.password,
                onValueChange = controller::onPasswordChange,
                obscureText = state.obscurePassword,
                suffix = { PasswordVisibilityToggle(state.obscurePassword, controller::togglePasswordVisibility) },
            )
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                LeoTextLink(label = AppStrings.forgotPassword, onClick = onForgotPassword)
            }
            Spacer(Modifier.height(20.dp))
            LeoPrimaryButton(
                label = AppStrings.login,
                onClick = controller::submit,
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
            Spacer(Modifier.height(26.dp))
            AuthFooterPrompt(prompt = AppStrings.newToLeoChat, action = AppStrings.createAccount, onAction = onRegister)
        }
    }
}
