package com.leochat.features.auth.presentation.pages

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoTypography
import com.leochat.core.widgets.common.buttons.LeoPrimaryButton
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.core.widgets.common.inputs.LeoAuthField
import com.leochat.core.widgets.common.inputs.PasswordVisibilityToggle
import com.leochat.core.widgets.common.layout.LeoBackBar
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.auth.presentation.controllers.ResetPasswordController
import com.leochat.features.auth.presentation.widgets.AuthIconBadge
import com.leochat.features.auth.presentation.widgets.CenteredScrollColumn
import com.leochat.features.auth.presentation.widgets.CollectAuthEffects
import org.koin.compose.viewmodel.koinViewModel

/** Set a new password with a recovery token. Mirrors `ResetPasswordPage`. */
@Composable
fun ResetPasswordPage(
    onBack: () -> Unit,
    onNavigateLogin: () -> Unit,
    controller: ResetPasswordController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectAuthEffects(controller.effects, LocalLeoSnackBar.current, onNavigateLogin = onNavigateLogin)

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
                    onValueChange = controller::onTokenChange,
                    icon = Icons.Outlined.VpnKey,
                )
                Spacer(Modifier.height(18.dp))
                LeoAuthField(
                    label = AppStrings.newPassword,
                    hint = AppStrings.passwordHint,
                    value = state.newPassword,
                    onValueChange = controller::onNewPasswordChange,
                    obscureText = state.obscureNew,
                    suffix = { PasswordVisibilityToggle(state.obscureNew, controller::toggleNewVisibility) },
                )
                Spacer(Modifier.height(18.dp))
                LeoAuthField(
                    label = AppStrings.confirmPassword,
                    hint = AppStrings.passwordHint,
                    value = state.confirmPassword,
                    onValueChange = controller::onConfirmPasswordChange,
                    obscureText = state.obscureConfirm,
                    suffix = { PasswordVisibilityToggle(state.obscureConfirm, controller::toggleConfirmVisibility) },
                )
                Spacer(Modifier.height(26.dp))
                LeoPrimaryButton(
                    label = AppStrings.updatePassword,
                    onClick = controller::submit,
                    icon = Icons.Rounded.CheckCircleOutline,
                    isLoading = state.isLoading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                )
            }
        }
    }
}
