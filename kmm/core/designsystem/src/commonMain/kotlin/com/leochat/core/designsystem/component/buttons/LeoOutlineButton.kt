package com.leochat.core.designsystem.component.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.designsystem.theme.LeoColors

/** Neutral outlined button. Mirrors `LeoOutlineButton`. */
@Composable
fun LeoOutlineButton(
    label: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    OutlinedButton(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        interactionSource = interaction,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, LeoColors.border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = LeoColors.textPrimary),
        modifier = modifier.leoPressable(interaction, onClick != null),
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
