package com.leochat.features.settings.domain.entities

/** Local notification and privacy toggles shown on the settings home. Defaults mirror `SettingsState`. */
data class SettingsPreferences(
    val messageNotifications: Boolean = true,
    val messagePreviews: Boolean = false,
    val callNotifications: Boolean = true,
    val onlineStatus: Boolean = true,
)
