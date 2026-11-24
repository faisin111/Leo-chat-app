package com.leochat.core.designsystem.component.ai

import androidx.compose.runtime.Composable
import com.leochat.core.common.constants.AiStrings
import com.leochat.core.designsystem.component.dialog.LeoConfirmationDialog

/** Confirms permanent deletion of the AI conversation. Mirrors the Leo AI screen dialog. */
@Composable
fun AiDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    LeoConfirmationDialog(
        title = AiStrings.deleteTitle,
        message = AiStrings.deleteBody,
        confirmText = AiStrings.delete,
        cancelText = AiStrings.cancel,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}
