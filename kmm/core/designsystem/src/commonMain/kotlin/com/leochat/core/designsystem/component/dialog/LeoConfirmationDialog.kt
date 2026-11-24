package com.leochat.core.designsystem.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens

/**
 * Modern, beautifully styled confirmation dialog matching the LeoChat OLED dark / clean light theme.
 */
@Composable
fun LeoConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = AppStrings.delete,
    cancelText: String = AppStrings.cancel,
    icon: ImageVector = Icons.Rounded.DeleteOutline,
    isDestructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(26.dp)
    val cardBackground = if (palette.isDark) Color(0xFF101014) else Color.White
    val cardBorder = if (palette.isDark) Color(0xFF1E1E24) else palette.border.copy(alpha = 0.5f)
    val iconColor = if (isDestructive) LeoColors.destructive else LeoColors.primary
    val iconBadgeBg = if (isDestructive) LeoColors.destructive.copy(alpha = 0.12f) else LeoColors.primary.copy(alpha = 0.12f)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 340.dp)
                    .fillMaxWidth()
                    .shadow(16.dp, shape, spotColor = Color.Black.copy(alpha = 0.25f))
                    .clip(shape)
                    .background(cardBackground)
                    .border(1.dp, cardBorder, shape)
                    .padding(24.dp),
            ) {
                // Top icon badge
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(iconBadgeBg),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(26.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Title
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = palette.textPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                // Message body
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = palette.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                )

                Spacer(Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Cancel button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (palette.isDark) Color(0xFF1C1C22) else palette.input)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = cancelText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = palette.textPrimary,
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    // Confirm button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDestructive) LeoColors.destructive else LeoColors.primary)
                            .clickable(onClick = onConfirm),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = confirmText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}
