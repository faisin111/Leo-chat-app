package com.leochat.core.designsystem.component.buttons

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Filled brand button with optional leading icon and loading state. Mirrors `LeoPrimaryButton`. */
@Composable
fun LeoPrimaryButton(
    label: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isLoading: Boolean = false,
) {
    val enabled = onClick != null && !isLoading
    val interaction = remember { MutableInteractionSource() }
    Button(
        onClick = { onClick?.invoke() },
        enabled = enabled,
        interactionSource = interaction,
        contentPadding = if (icon != null && !isLoading) ButtonDefaults.ButtonWithIconContentPadding else ButtonDefaults.ContentPadding,
        modifier = modifier.leoPressable(interaction, enabled),
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = Color.White,
            )
            else -> {
                if (icon != null) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                }
                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
