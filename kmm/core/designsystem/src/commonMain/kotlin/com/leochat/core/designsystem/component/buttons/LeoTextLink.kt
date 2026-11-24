package com.leochat.core.designsystem.component.buttons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.designsystem.theme.LeoColors

/**
 * Brand-colored inline text action. `compact` matches Flutter's
 * `TextButton(padding: zero, minimumSize: zero, shrinkWrap)` links.
 */
@Composable
fun LeoTextLink(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = true,
    fontWeight: FontWeight = FontWeight.ExtraBold,
) {
    if (!compact) {
        TextButton(onClick = onClick, modifier = modifier) {
            Text(label, color = LeoColors.primary, fontSize = 14.sp, fontWeight = fontWeight)
        }
        return
    }
    val interaction = remember { MutableInteractionSource() }
    Text(
        text = label,
        color = LeoColors.primary,
        fontSize = 14.sp,
        fontWeight = fontWeight,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(interactionSource = interaction, indication = ripple(), role = Role.Button, onClick = onClick),
    )
}
