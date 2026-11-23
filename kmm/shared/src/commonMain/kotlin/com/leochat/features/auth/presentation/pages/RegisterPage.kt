package com.leochat.features.auth.presentation.pages

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoTypography
import com.leochat.core.widgets.common.branding.LeoLogo
import com.leochat.core.widgets.common.buttons.LeoPrimaryButton
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.core.widgets.common.inputs.LeoAuthField
import com.leochat.core.widgets.common.inputs.PasswordVisibilityToggle
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.auth.presentation.controllers.RegisterController
import com.leochat.features.auth.presentation.widgets.AuthFooterPrompt
import com.leochat.features.auth.presentation.widgets.CenteredScrollColumn
import com.leochat.features.auth.presentation.widgets.CollectAuthEffects
import com.leochat.features.auth.presentation.widgets.PasswordStrengthHint
import com.leochat.features.auth.presentation.widgets.ProfilePhotoPrompt
import com.leochat.features.auth.presentation.widgets.TermsAgreementRow
import org.koin.compose.viewmodel.koinViewModel

/** Account creation screen. Mirrors `RegisterPage`. */
@Composable
fun RegisterPage(
    onNavigateHome: () -> Unit,
    onSignIn: () -> Unit,
    controller: RegisterController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectAuthEffects(controller.effects, LocalLeoSnackBar.current, onNavigateHome = onNavigateHome)

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
                onValueChange = controller::onDisplayNameChange,
                icon = Icons.Outlined.PersonOutline,
            )
            Spacer(Modifier.height(18.dp))
            LeoAuthField(
                label = AppStrings.username,
                hint = AppStrings.usernameHint,
                value = state.username,
                onValueChange = controller::onUsernameChange,
                icon = Icons.Rounded.AlternateEmail,
            )
            Spacer(Modifier.height(18.dp))
            LeoAuthField(
                label = AppStrings.emailAddress,
                hint = AppStrings.emailHint,
                value = state.email,
                onValueChange = controller::onEmailChange,
                icon = Icons.Outlined.MailOutline,
                keyboardType = KeyboardType.Email,
            )
            Spacer(Modifier.height(18.dp))
            LeoAuthField(
                label = AppStrings.createPassword,
                hint = AppStrings.passwordHint,
                value = state.password,
                onValueChange = controller::onPasswordChange,
                obscureText = state.obscurePassword,
                suffix = { PasswordVisibilityToggle(state.obscurePassword, controller::togglePasswordVisibility) },
            )
            Spacer(Modifier.height(12.dp))
            PasswordStrengthHint()
            Spacer(Modifier.height(16.dp))
            TermsAgreementRow(accepted = state.acceptedTerms, onChange = controller::onTermsChange)
            Spacer(Modifier.height(24.dp))
            LeoPrimaryButton(
                label = AppStrings.createAccountButton,
                onClick = if (state.acceptedTerms) controller::submit else null,
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
            Spacer(Modifier.height(24.dp))
            AuthFooterPrompt(prompt = AppStrings.alreadyConversation, action = AppStrings.login, onAction = onSignIn)
        }
    }
}
