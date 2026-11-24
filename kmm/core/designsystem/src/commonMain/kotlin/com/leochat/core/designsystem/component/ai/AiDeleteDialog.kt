package com.leochat.core.designsystem.component.ai

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.leochat.core.common.constants.AiStrings
import com.leochat.core.designsystem.theme.AiTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LocalLeoPalette

/** Confirms permanent deletion of the AI conversation. Mirrors the Leo AI screen dialog. */
@Composable
fun AiDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val isDark = LocalLeoPalette.current.isDark
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDark) LeoColors.darkSurface else Color.White,
        title = {
            Text(AiStrings.deleteTitle, color = if (isDark) LeoColors.darkTextPrimary else LeoColors.textPrimary)
        },
        text = {
            Text(AiStrings.deleteBody, color = if (isDark) LeoColors.darkTextSecondary else LeoColors.textSecondary)
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(AiStrings.cancel, color = LeoColors.primary) }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(AiStrings.delete, color = LeoColors.destructive) }
        },
    )
}
