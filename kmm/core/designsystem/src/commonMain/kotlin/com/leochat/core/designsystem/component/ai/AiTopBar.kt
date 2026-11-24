package com.leochat.core.designsystem.component.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AiStrings
import com.leochat.core.designsystem.component.ai.model.AiModelUi
import com.leochat.core.designsystem.theme.AiTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LocalLeoPalette

/** App bar: model selector pill and clear-history action. Mirrors the Leo AI screen AppBar. */
@Composable
fun AiTopBar(
    selectedModel: AiModelUi,
    onOpenModelPicker: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalLeoPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(palette.surface)
            .padding(start = 16.dp, end = 4.dp),
    ) {
        ModelPill(selectedModel, palette.primarySoft, onOpenModelPicker)
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onClearHistory) {
            Icon(
                Icons.Outlined.DeleteSweep,
                contentDescription = AiStrings.clearHistory,
                tint = palette.textSecondary,
            )
        }
    }
}

@Composable
private fun ModelPill(model: AiModelUi, background: Color, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .aiPressable(onClick = onClick)
            .background(background, shape)
            .border(1.dp, LeoColors.primary.copy(alpha = 0.3f), shape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Icon(model.icon.imageVector(), contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(model.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LeoColors.primary)
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(18.dp))
    }
}
