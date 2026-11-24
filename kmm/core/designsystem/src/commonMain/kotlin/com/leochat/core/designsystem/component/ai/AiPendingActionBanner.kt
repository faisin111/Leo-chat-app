package com.leochat.core.designsystem.component.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AiStrings
import com.leochat.core.designsystem.theme.AiTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LocalLeoPalette

/** Confirmation banner for AI actions such as SEND_MESSAGE or START_CALL. */
@Composable
fun AiPendingActionBanner(
    action: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalLeoPalette.current.isDark
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .background(if (isDark) AiTokens.actionSurfaceDark else AiTokens.actionSurfaceLight, shape)
            .border(1.dp, AiTokens.gradientStart.copy(alpha = 0.5f), shape)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = AiTokens.gradientStart, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                AiStrings.actionConfirmationRequired,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else AiTokens.actionTitleLight,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            AiStrings.actionRequested(action),
            fontSize = 12.sp,
            color = if (isDark) LeoColors.darkTextSecondary else AiTokens.actionBodyLight,
        )
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onDismiss) { Text(AiStrings.dismiss, color = LeoColors.primary) }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LeoColors.primary, contentColor = Color.White),
            ) { Text(AiStrings.confirmAndRun) }
        }
    }
}
