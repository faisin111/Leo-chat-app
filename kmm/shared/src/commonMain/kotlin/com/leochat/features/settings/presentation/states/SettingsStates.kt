package com.leochat.features.settings.presentation.states

import com.leochat.core.constants.SettingsStrings
import com.leochat.core.theme.LeoThemeMode
import com.leochat.features.auth.domain.entities.User
import com.leochat.features.settings.domain.entities.SettingsPreferences
import com.leochat.features.users.domain.entities.UserProfile
import com.leochat.features.users.domain.entities.UserSessionItem

/** One-shot UI events emitted by settings controllers. */
sealed interface SettingsEffect {
    data class ShowSuccess(val message: String) : SettingsEffect
    data class ShowError(val message: String) : SettingsEffect
    data object NavigateBack : SettingsEffect
    data object SignedOut : SettingsEffect
}

/** Settings home. Mirrors `SettingsState` plus the profile and auth user it reads. */
data class SettingsState(
    val profile: UserProfile? = null,
    val authUser: User? = null,
    val preferences: SettingsPreferences = SettingsPreferences(),
    val profileVisibility: String = SettingsStrings.profileVisibilityContacts,
    val blockedCount: Int = 0,
    val isSigningOut: Boolean = false,
) {
    val displayName: String
        get() = profile?.effectiveName?.takeIf { it.isNotEmpty() }
            ?: authUser?.displayName?.takeIf { it.isNotEmpty() }
            ?: authUser?.username?.takeIf { it.isNotEmpty() }
            ?: SettingsStrings.yourProfile

    val username: String
        get() = profile?.username ?: authUser?.username ?: SettingsStrings.usernameFallback

    val avatarUrl: String?
        get() = profile?.profilePictureUrl ?: authUser?.avatarUrl

    val isAdmin: Boolean
        get() = profile?.isAdmin == true || authUser?.isAdmin == true
}

data class EditProfileState(
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val isSaving: Boolean = false,
) {
    val avatarName: String get() = displayName.ifEmpty { SettingsStrings.userFallback }
    val bioCounter: String get() = "${bio.length} / ${SettingsStrings.bioMaxLength}"
}

data class SecurityState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val obscureCurrent: Boolean = true,
    val obscureNew: Boolean = true,
    val obscureConfirm: Boolean = true,
    val isUpdating: Boolean = false,
    val isDeactivating: Boolean = false,
)

data class SessionsState(
    val sessions: List<UserSessionItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val revokingId: String? = null,
)

data class AppearanceState(
    val mode: LeoThemeMode = LeoThemeMode.Light,
    val accentIndex: Int = 0,
)
