package com.leochat.feature.settings.presentation.settings

import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.designsystem.theme.LeoThemeMode
import com.leochat.feature.auth.domain.model.User
import com.leochat.feature.settings.domain.model.SettingsPreferences
import com.leochat.feature.users.domain.model.UserProfile
import com.leochat.feature.users.domain.model.UserSessionItem

/** One-shot UI events emitted by settings controllers. */
sealed interface SettingsEffect {
    data class ShowSuccess(val message: String) : SettingsEffect
    data class ShowError(val message: String) : SettingsEffect
    data object NavigateBack : SettingsEffect
    data object SignedOut : SettingsEffect
}

/** Settings home. Mirrors `SettingsUiState` plus the profile and auth user it reads. */
data class SettingsUiState(
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

}

data class EditProfileUiState(
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
) {
    val avatarName: String get() = displayName.ifEmpty { SettingsStrings.userFallback }
    val bioCounter: String get() = "${bio.length} / ${SettingsStrings.bioMaxLength}"
}

data class SecurityUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val obscureCurrent: Boolean = true,
    val obscureNew: Boolean = true,
    val obscureConfirm: Boolean = true,
    val isUpdating: Boolean = false,
    val isDeactivating: Boolean = false,
)

data class SessionsUiState(
    val sessions: List<UserSessionItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val revokingId: String? = null,
)

data class AppearanceUiState(
    val mode: LeoThemeMode = LeoThemeMode.Light,
    val accentIndex: Int = 0,
)
