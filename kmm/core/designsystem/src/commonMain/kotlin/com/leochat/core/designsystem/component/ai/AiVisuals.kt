package com.leochat.core.designsystem.component.ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.vector.ImageVector
import com.leochat.core.designsystem.component.ai.model.AiModelIconUi
import com.leochat.core.designsystem.component.buttons.leoPressable

/** Material icon for an AI model glyph. */
fun AiModelIconUi.imageVector(): ImageVector = when (this) {
    AiModelIconUi.Sparkles -> Icons.Filled.AutoAwesome
    AiModelIconUi.Brain -> Icons.Rounded.Psychology
    AiModelIconUi.Code -> Icons.Rounded.Code
    AiModelIconUi.Zap -> Icons.Rounded.Bolt
}

/** LeoPressable equivalent: scale-bounce feedback without a ripple. */
fun Modifier.aiPressable(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    this
        .leoPressable(interaction, enabled = enabled)
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
}
