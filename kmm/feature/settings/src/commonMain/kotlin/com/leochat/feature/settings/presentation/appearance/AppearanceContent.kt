package com.leochat.feature.settings.presentation.appearance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.designsystem.component.layout.LeoScreen
import com.leochat.core.designsystem.component.settings.AccentSelector
import com.leochat.core.designsystem.component.settings.AppearancePreview
import com.leochat.core.designsystem.component.settings.SettingsAppBar
import com.leochat.core.designsystem.component.settings.SettingsGroupLabel
import com.leochat.core.designsystem.component.settings.SettingsTextScope
import com.leochat.core.designsystem.component.settings.ThemeModeSelector
import com.leochat.core.designsystem.theme.LeoThemeMode
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.theme.SettingsTokens
import com.leochat.feature.settings.presentation.settings.AppearanceUiState

/** Stateless appearance layout driven by [state]. */
@Composable
fun AppearanceContent(
    state: AppearanceUiState,
    onBack: () -> Unit,
    onModeSelect: (LeoThemeMode) -> Unit,
    onAccentSelect: (Int) -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val accent = SettingsTokens.accents[state.accentIndex.coerceIn(SettingsTokens.accents.indices)]

    LeoScreen(background = palette.background) {
        SettingsTextScope {
            Column(Modifier.fillMaxSize()) {
                SettingsAppBar(SettingsStrings.appearance, onBack, background = palette.surface)
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    SettingsGroupLabel(SettingsStrings.themeMode)
                    Spacer(Modifier.height(12.dp))
                    ThemeModeSelector(state.mode, accent, onModeSelect)
                    Spacer(Modifier.height(24.dp))
                    SettingsGroupLabel(SettingsStrings.accentColor)
                    Spacer(Modifier.height(12.dp))
                    AccentSelector(state.accentIndex, onAccentSelect)
                    Spacer(Modifier.height(24.dp))
                    SettingsGroupLabel(SettingsStrings.livePreview)
                    Spacer(Modifier.height(12.dp))
                    AppearancePreview(accent)
                }
            }
        }
    }
}
