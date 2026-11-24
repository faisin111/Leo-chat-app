package com.leochat.core.designsystem.component.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.component.buttons.LeoOutlineButton
import com.leochat.core.designsystem.component.buttons.LeoPrimaryButton
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoTypography

/**
 * "Check your email" confirmation shown after a reset link was requested.
 * Emits its children directly into the caller's column (no own container).
 */
@Composable
fun ResetLinkSentPanel(
    email: String,
    onEnterResetCode: () -> Unit,
    onBackToLogin: () -> Unit,
) {
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
