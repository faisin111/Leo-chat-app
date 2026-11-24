package com.leochat.feature.settings.domain.model

import com.leochat.feature.settings.presentation.settings.SettingsUiState

/** Local notification and privacy toggles shown on the settings home. Defaults mirror `SettingsUiState`. */
data class SettingsPreferences(
    val messageNotifications: Boolean = true,
    val messagePreviews: Boolean = false,
    val callNotifications: Boolean = true,
    val onlineStatus: Boolean = true,
)
