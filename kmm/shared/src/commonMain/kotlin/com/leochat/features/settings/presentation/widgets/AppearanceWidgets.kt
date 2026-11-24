package com.leochat.features.settings.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.SettingsStrings
import com.leochat.core.theme.LeoThemeMode
import com.leochat.core.theme.LeoThemeTokens

private data class ThemeOption(val mode: LeoThemeMode, val label: String, val icon: ImageVector)

private val themeOptions = listOf(
    ThemeOption(LeoThemeMode.Light, SettingsStrings.themeLight, Icons.Rounded.LightMode),
    ThemeOption(LeoThemeMode.Dark, SettingsStrings.themeDark, Icons.Rounded.DarkMode),
    ThemeOption(LeoThemeMode.System, SettingsStrings.themeSystem, Icons.Rounded.BrightnessAuto),
)

/** Light / Dark / System tiles, outlined in the selected accent. */
@Composable
internal fun ThemeModeSelector(selected: LeoThemeMode, accent: Color, onSelect: (LeoThemeMode) -> Unit) {
    val palette = LeoThemeTokens.palette
    Row(Modifier.fillMaxWidth()) {
        themeOptions.forEach { option ->
            val isSelected = option.mode == selected
            val shape = RoundedCornerShape(18.dp)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp)
                    .settingsPressable { onSelect(option.mode) }
                    .background(palette.surface, shape)
                    .border(if (isSelected) 2.dp else 1.dp, if (isSelected) accent else palette.border, shape)
                    .padding(vertical = 18.dp),
            ) {
                Icon(
                    option.icon,
                    contentDescription = null,
                    tint = when {
                        isSelected -> accent
                        palette.isDark -> palette.textSecondary
                        else -> SettingsColors.slateIconMuted
                    },
                    modifier = Modifier.size(26.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    option.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accent else palette.textPrimary,
                )
            }
        }
    }
}

/** Five accent swatches; the selected one gets a white ring, glow, and check. */
@Composable
internal fun AccentSelector(selectedIndex: Int, onSelect: (Int) -> Unit) {
    SettingsCard(radius = 20.dp, padding = 16.dp) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            SettingsColors.accents.forEachIndexed { index, color ->
                val isSelected = index == selectedIndex
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .settingsPressable { onSelect(index) }
                        .then(
                            if (isSelected) {
                                Modifier.shadow(10.dp, CircleShape, ambientColor = color.copy(alpha = 0.5f), spotColor = color.copy(alpha = 0.5f))
                            } else {
                                Modifier
                            },
                        )
                        .size(44.dp)
                        .background(color, CircleShape)
                        .then(if (isSelected) Modifier.border(3.dp, Color.White, CircleShape) else Modifier),
                ) {
                    if (isSelected) {
                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

/** Two sample bubbles using the chosen accent. */
@Composable
internal fun AppearancePreview(accent: Color) {
    val palette = LeoThemeTokens.palette
    val bubble = RoundedCornerShape(16.dp)
    SettingsCard(padding = 20.dp) {
        Text(
            SettingsStrings.previewOutgoing,
            color = Color.White,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.End)
                .background(accent, bubble)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            SettingsStrings.previewIncoming,
            color = if (palette.isDark) palette.textPrimary else SettingsColors.slateText,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .background(if (palette.isDark) palette.incomingBubble else SettingsColors.slateSurface, bubble)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}
