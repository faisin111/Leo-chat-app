package com.leochat.feature.settings.data.remote

import com.leochat.feature.settings.domain.model.SettingsPreferences
import kotlinx.serialization.Serializable

@Serializable
data class SettingsPreferencesDto(
    val messageNotifications: Boolean = true,
    val messagePreviews: Boolean = false,
    val callNotifications: Boolean = true,
    val onlineStatus: Boolean = true,
) {
    fun toDomain() = SettingsPreferences(messageNotifications, messagePreviews, callNotifications, onlineStatus)

    companion object {
        fun fromDomain(p: SettingsPreferences) =
            SettingsPreferencesDto(p.messageNotifications, p.messagePreviews, p.callNotifications, p.onlineStatus)
    }
}
