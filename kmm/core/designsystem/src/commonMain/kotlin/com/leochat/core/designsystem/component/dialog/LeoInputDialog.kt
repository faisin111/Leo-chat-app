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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.theme.poppinsFontFamily

/**
 * Modern, beautifully styled input dialog for editing messages and short text entries.
 */
@Composable
fun LeoInputDialog(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = AppStrings.save,
    cancelText: String = AppStrings.cancel,
    placeholder: String = AppStrings.messageHint,
    icon: ImageVector = Icons.Rounded.Edit,
    maxLines: Int = 4,
) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(26.dp)
    val cardBackground = if (palette.isDark) Color(0xFF101014) else Color.White
    val cardBorder = if (palette.isDark) Color(0xFF1E1E24) else palette.border.copy(alpha = 0.5f)
    val inputBackground = if (palette.isDark) Color(0xFF16161B) else palette.input.copy(alpha = 0.6f)

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
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .shadow(16.dp, shape, spotColor = Color.Black.copy(alpha = 0.25f))
                    .clip(shape)
                    .background(cardBackground)
                    .border(1.dp, cardBorder, shape)
                    .padding(22.dp),
            ) {
                // Header with icon badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(LeoColors.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = LeoColors.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Input field
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = maxLines,
                    textStyle = TextStyle(
                        fontFamily = poppinsFontFamily(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.textPrimary,
                    ),
                    placeholder = {
                        Text(
                            text = placeholder,
                            fontSize = 14.sp,
                            color = palette.textMuted,
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = inputBackground,
                        unfocusedContainerColor = inputBackground,
                        focusedBorderColor = LeoColors.primary,
                        unfocusedBorderColor = if (palette.isDark) Color(0xFF24242C) else palette.border,
                        cursorColor = LeoColors.primary,
                    ),
                )

                Spacer(Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Cancel button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(13.dp))
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
                    val isEnabled = value.trim().isNotEmpty()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                if (isEnabled) LeoColors.primary else LeoColors.primary.copy(alpha = 0.4f),
                            )
                            .clickable(enabled = isEnabled, onClick = onConfirm),
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
