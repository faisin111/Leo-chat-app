package com.leochat.feature.settings.data.remote

import com.leochat.core.common.constants.SettingsStrings
import com.leochat.feature.settings.domain.model.SettingsPreferences
import kotlinx.serialization.Serializable

@Serializable
data class SettingsPreferencesDto(
    val messageNotifications: Boolean = true,
    val messagePreviews: Boolean = false,
    val callNotifications: Boolean = true,
    val onlineStatus: Boolean = true,
    val profileVisibility: String = SettingsStrings.profileVisibilityContacts,
) {
    fun toDomain() = SettingsPreferences(
        messageNotifications = messageNotifications,
        messagePreviews = messagePreviews,
        callNotifications = callNotifications,
        onlineStatus = onlineStatus,
        profileVisibility = profileVisibility,
    )

    companion object {
        fun fromDomain(p: SettingsPreferences) = SettingsPreferencesDto(
            messageNotifications = p.messageNotifications,
            messagePreviews = p.messagePreviews,
            callNotifications = p.callNotifications,
            onlineStatus = p.onlineStatus,
            profileVisibility = p.profileVisibility,
        )
    }
}
