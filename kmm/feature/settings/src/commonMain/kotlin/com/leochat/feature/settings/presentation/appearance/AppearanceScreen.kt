package com.leochat.feature.settings.presentation.appearance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

/** Theme mode (persisted, applied app-wide by `LeoChatApp`), accent, and live preview. Mirrors `AppearanceScreen`. */
@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    controller: AppearanceViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    AppearanceContent(
        state = state,
        onBack = onBack,
        onModeSelect = controller::setMode,
        onAccentSelect = controller::selectAccent,
    )
}
