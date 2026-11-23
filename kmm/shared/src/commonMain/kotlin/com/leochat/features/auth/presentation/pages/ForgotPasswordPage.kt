package com.leochat.features.auth.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoTypography
import com.leochat.core.widgets.common.buttons.LeoOutlineButton
import com.leochat.core.widgets.common.buttons.LeoPrimaryButton
import com.leochat.core.widgets.common.buttons.LeoTextLink
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.core.widgets.common.inputs.LeoAuthField
import com.leochat.core.widgets.common.layout.LeoBackBar
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.auth.presentation.controllers.ForgotPasswordController
import com.leochat.features.auth.presentation.states.ForgotPasswordState
import com.leochat.features.auth.presentation.widgets.AuthIconBadge
import com.leochat.features.auth.presentation.widgets.CenteredScrollColumn
import com.leochat.features.auth.presentation.widgets.CollectAuthEffects
import org.koin.compose.viewmodel.koinViewModel

/** Password recovery request. Mirrors `ForgotPasswordPage`. */
@Composable
fun ForgotPasswordPage(
    onBack: () -> Unit,
    onBackToLogin: () -> Unit,
    onEnterResetCode: () -> Unit,
    controller: ForgotPasswordController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectAuthEffects(controller.effects, LocalLeoSnackBar.current)

    LeoScreen {
        Column {
            LeoBackBar(onBack = onBack)
            CenteredScrollColumn(
                maxWidth = 440.dp,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = if (state.submitted) Alignment.CenterHorizontally else Alignment.Start,
            ) {
                if (state.submitted) {
                    ForgotPasswordSuccess(state.email, onEnterResetCode, onBackToLogin)
                } else {
                    ForgotPasswordForm(state, controller, onBackToLogin)
                }
            }
        }
    }
}

@Composable
private fun ForgotPasswordForm(
    state: ForgotPasswordState,
    controller: ForgotPasswordController,
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
        onValueChange = controller::onEmailChange,
        icon = Icons.Outlined.MailOutline,
        keyboardType = KeyboardType.Email,
    )
    Spacer(Modifier.height(24.dp))
    LeoPrimaryButton(
        label = AppStrings.sendResetLink,
        onClick = controller::submit,
        icon = Icons.AutoMirrored.Rounded.Send,
        isLoading = state.isLoading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
    )
    Spacer(Modifier.height(16.dp))
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        LeoTextLink(label = AppStrings.backToLogin, onClick = onBackToLogin, compact = false, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ForgotPasswordSuccess(email: String, onEnterResetCode: () -> Unit, onBackToLogin: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(64.dp).background(LeoColors.success.copy(alpha = 0.12f), CircleShape),
    ) {
        Icon(Icons.Rounded.MarkEmailRead, contentDescription = null, tint = LeoColors.success, modifier = Modifier.size(34.dp))
    }
    Spacer(Modifier.height(20.dp))
    Text(AppStrings.checkYourEmail, style = LeoTypography.mainHeading, textAlign = TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    Text(
        AppStrings.resetInstructionsSentTo(email),
        fontSize = 15.sp,
        lineHeight = (15 * 1.45).sp,
        color = LeoColors.textSecondary,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(28.dp))
    LeoPrimaryButton(
        label = AppStrings.enterResetCode,
        onClick = onEnterResetCode,
        icon = Icons.Rounded.Key,
        modifier = Modifier.fillMaxWidth().height(50.dp),
    )
    Spacer(Modifier.height(12.dp))
    LeoOutlineButton(
        label = AppStrings.backToLogin,
        onClick = onBackToLogin,
        modifier = Modifier.fillMaxWidth().height(50.dp),
    )
}
