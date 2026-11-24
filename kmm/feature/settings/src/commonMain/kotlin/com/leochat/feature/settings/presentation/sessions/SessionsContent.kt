package com.leochat.feature.settings.presentation.sessions

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.designsystem.component.layout.LeoScreen
import com.leochat.core.designsystem.component.settings.SessionTile
import com.leochat.core.designsystem.component.settings.SessionsEmptyCard
import com.leochat.core.designsystem.component.settings.SessionsTipCard
import com.leochat.core.designsystem.component.settings.SettingsAppBar
import com.leochat.core.designsystem.component.settings.SettingsGroupLabel
import com.leochat.core.designsystem.component.settings.SettingsTextScope
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.feature.settings.presentation.mapper.toSessionRows
import com.leochat.feature.settings.presentation.settings.SessionsUiState

/** Stateless sessions layout driven by [state]; [onRevoke] receives the session id. */
@Composable
fun SessionsContent(
    state: SessionsUiState,
    onBack: () -> Unit,
    onRevoke: (String) -> Unit,
) {
    val palette = LeoThemeTokens.palette

    LeoScreen(background = palette.background) {
        SettingsTextScope {
            Column(Modifier.fillMaxSize()) {
                SettingsAppBar(SettingsStrings.sessionsAndDevices, onBack, background = palette.surface)
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
                    SessionsTipCard()
                    Spacer(Modifier.height(20.dp))
                    SettingsGroupLabel(SettingsStrings.loggedInDevices)
                    Spacer(Modifier.height(12.dp))
                    SessionsBody(state, onRevoke)
                }
            }
        }
    }
}

/** Loading, error, empty, or device list section bound to [SessionsUiState]. */
@Composable
private fun SessionsBody(state: SessionsUiState, onRevoke: (String) -> Unit) {
    when {
        state.isLoading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LeoColors.primary)
        }
        state.error != null && state.sessions.isEmpty() -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("${SettingsStrings.errorPrefix}${state.error}", color = LeoThemeTokens.palette.textPrimary)
        }
        state.sessions.isEmpty() -> SessionsEmptyCard()
        else -> Column {
            state.toSessionRows().forEachIndexed { index, row ->
                if (index > 0) Spacer(Modifier.height(10.dp))
                SessionTile(row = row, onRevoke = { onRevoke(row.id) })
            }
        }
    }
}
