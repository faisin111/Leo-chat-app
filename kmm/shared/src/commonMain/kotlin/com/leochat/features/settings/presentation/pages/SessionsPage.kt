package com.leochat.features.settings.presentation.pages

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.SettingsStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.settings.presentation.controllers.SessionsController
import com.leochat.features.settings.presentation.states.SessionPresentation
import com.leochat.features.settings.presentation.states.SessionsState
import com.leochat.features.settings.presentation.widgets.CollectSettingsEffects
import com.leochat.features.settings.presentation.widgets.SessionTile
import com.leochat.features.settings.presentation.widgets.SessionsEmptyCard
import com.leochat.features.settings.presentation.widgets.SessionsTipCard
import com.leochat.features.settings.presentation.widgets.SettingsAppBar
import com.leochat.features.settings.presentation.widgets.SettingsGroupLabel
import com.leochat.features.settings.presentation.widgets.SettingsTextScope
import org.koin.compose.viewmodel.koinViewModel

/** Logged-in devices with revoke. Mirrors `SessionsPage`. */
@Composable
fun SessionsPage(
    onBack: () -> Unit,
    controller: SessionsController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectSettingsEffects(controller.effects, LocalLeoSnackBar.current)
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
                    SessionsBody(state, controller::revoke)
                }
            }
        }
    }
}

@Composable
private fun SessionsBody(state: SessionsState, onRevoke: (String) -> Unit) {
    when {
        state.isLoading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LeoColors.primary)
        }
        state.error != null && state.sessions.isEmpty() -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("${SettingsStrings.errorPrefix}${state.error}", color = LeoThemeTokens.palette.textPrimary)
        }
        state.sessions.isEmpty() -> SessionsEmptyCard()
        else -> Column {
            state.sessions.forEachIndexed { index, session ->
                if (index > 0) Spacer(Modifier.height(10.dp))
                SessionTile(
                    title = SessionPresentation.title(session),
                    subtitle = SessionPresentation.subtitle(session),
                    kind = SessionPresentation.deviceKind(session.platform, session.userAgent),
                    isCurrent = SessionPresentation.isCurrent(session, index),
                    revoking = state.revokingId == session.sessionId,
                    onRevoke = { onRevoke(session.sessionId) },
                )
            }
        }
    }
}
