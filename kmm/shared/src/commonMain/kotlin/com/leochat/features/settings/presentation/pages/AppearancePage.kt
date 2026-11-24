package com.leochat.features.settings.presentation.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.SettingsStrings
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.settings.presentation.controllers.AppearanceController
import com.leochat.features.settings.presentation.widgets.AccentSelector
import com.leochat.features.settings.presentation.widgets.AppearancePreview
import com.leochat.features.settings.presentation.widgets.SettingsAppBar
import com.leochat.features.settings.presentation.widgets.SettingsColors
import com.leochat.features.settings.presentation.widgets.SettingsGroupLabel
import com.leochat.features.settings.presentation.widgets.SettingsTextScope
import com.leochat.features.settings.presentation.widgets.ThemeModeSelector
import org.koin.compose.viewmodel.koinViewModel

/** Theme mode (persisted, applied app-wide by `LeoChatApp`), accent, and live preview. Mirrors `AppearancePage`. */
@Composable
fun AppearancePage(
    onBack: () -> Unit,
    controller: AppearanceController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    val palette = LeoThemeTokens.palette
    val accent = SettingsColors.accents[state.accentIndex.coerceIn(SettingsColors.accents.indices)]

    LeoScreen(background = palette.background) {
        SettingsTextScope {
            Column(Modifier.fillMaxSize()) {
                SettingsAppBar(SettingsStrings.appearance, onBack, background = palette.surface)
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    SettingsGroupLabel(SettingsStrings.themeMode)
                    Spacer(Modifier.height(12.dp))
                    ThemeModeSelector(state.mode, accent, controller::setMode)
                    Spacer(Modifier.height(24.dp))
                    SettingsGroupLabel(SettingsStrings.accentColor)
                    Spacer(Modifier.height(12.dp))
                    AccentSelector(state.accentIndex, controller::selectAccent)
                    Spacer(Modifier.height(24.dp))
                    SettingsGroupLabel(SettingsStrings.livePreview)
                    Spacer(Modifier.height(12.dp))
                    AppearancePreview(accent)
                }
            }
        }
    }
}
