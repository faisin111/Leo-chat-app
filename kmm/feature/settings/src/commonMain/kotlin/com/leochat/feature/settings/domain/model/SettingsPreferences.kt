package com.leochat.feature.settings.domain.model

import com.leochat.core.common.constants.SettingsStrings

/** Local notification and privacy toggles shown on the settings home. Defaults mirror `SettingsUiState`. */
data class SettingsPreferences(
    val messageNotifications: Boolean = true,
    val messagePreviews: Boolean = false,
    val callNotifications: Boolean = true,
    val onlineStatus: Boolean = true,
    val profileVisibility: String = SettingsStrings.profileVisibilityContacts,
)
