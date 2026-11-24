package com.leochat.features.ai.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AiStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LocalLeoPalette

/** Horizontally scrolling prompt suggestions shown while the history is short. */
@Composable
fun AiSuggestionChips(
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    prompts: List<String> = AiStrings.suggestionPrompts,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth().height(38.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(prompts) { prompt -> AiSuggestionChip(prompt, onClick = { onSelect(prompt) }) }
    }
}

@Composable
fun AiSuggestionChip(prompt: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isDark = LocalLeoPalette.current.isDark
    val shape = RoundedCornerShape(20.dp)
    Text(
        prompt,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        color = if (isDark) LeoColors.darkTextSecondary else AiTokens.slate600,
        modifier = modifier
            .aiPressable(onClick = onClick)
            .background(if (isDark) LeoColors.darkSurface else Color.White, shape)
            .border(1.dp, if (isDark) LeoColors.darkBorder else AiTokens.slate200, shape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}
