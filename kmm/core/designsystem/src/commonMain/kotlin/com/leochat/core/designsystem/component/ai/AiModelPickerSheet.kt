package com.leochat.core.designsystem.component.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.leochat.core.designsystem.theme.LeoPalette
import com.leochat.core.designsystem.theme.LocalLeoPalette

/** Bottom sheet listing the intelligence models. Mirrors `_showModelPicker`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiModelPickerSheet(
    currentModelId: String,
    models: List<AiModelUi>,
    onSelect: (AiModelUi) -> Unit,
    onDismiss: () -> Unit,
) {
    val palette = LocalLeoPalette.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
    ) {
        Column(Modifier.navigationBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .background(palette.border, RoundedCornerShape(2.dp)),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                AiStrings.selectModelTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = palette.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                AiStrings.selectModelSubtitle,
                fontSize = 13.sp,
                color = palette.textSecondary,
            )
            Spacer(Modifier.height(16.dp))
            models.forEach { model ->
                AiModelOption(model, isSelected = model.id == currentModelId, palette = palette, onClick = { onSelect(model) })
            }
        }
    }
}

@Composable
private fun AiModelOption(model: AiModelUi, isSelected: Boolean, palette: LeoPalette, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    val background = when {
        isSelected -> palette.primarySoft
        palette.isDark -> palette.surfaceElevated
        else -> AiTokens.slate50
    }
    val border = when {
        isSelected -> LeoColors.primary
        else -> palette.border
    }
    val secondary = palette.textSecondary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .aiPressable(onClick = onClick)
            .background(background, shape)
            .border(1.dp, border, shape)
            .padding(12.dp),
    ) {
        Icon(
            model.icon.imageVector(),
            contentDescription = null,
            tint = if (isSelected) LeoColors.primary else secondary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    model.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) LeoColors.primary else palette.textPrimary,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    model.badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) LeoColors.primary else secondary,
                    modifier = Modifier
                        .background(
                            when {
                                isSelected -> LeoColors.primary.copy(alpha = 0.15f)
                                palette.isDark -> palette.border
                                else -> AiTokens.slate200
                            },
                            RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 6.dp, vertical = 1.5.dp),
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                model.tagline,
                fontSize = 12.sp,
                color = palette.textMuted,
            )
        }
    }
}
