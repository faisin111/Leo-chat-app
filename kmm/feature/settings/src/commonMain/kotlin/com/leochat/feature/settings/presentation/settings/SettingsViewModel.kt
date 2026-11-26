package com.leochat.feature.settings.presentation.settings

import androidx.lifecycle.viewModelScope
import com.leochat.core.platform.system.SystemSettingsHelper
import com.leochat.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.leochat.feature.auth.domain.usecase.LogoutUseCase
import com.leochat.feature.settings.domain.model.SettingsPreferences
import com.leochat.feature.settings.domain.usecase.GetBlockedUsersUseCase
import com.leochat.feature.settings.domain.usecase.GetSettingsPreferencesUseCase
import com.leochat.feature.settings.domain.usecase.SaveSettingsPreferencesUseCase
import com.leochat.feature.settings.presentation.SettingsEffectViewModel
import com.leochat.feature.users.domain.usecase.GetMyProfileUseCase
import kotlinx.coroutines.launch

/** Settings home. Mirrors `SettingsNotifier` plus profile, blocked count, search, visibility, help, and OS permissions. */
class SettingsViewModel(
    private val getMyProfile: GetMyProfileUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getPreferences: GetSettingsPreferencesUseCase,
    private val savePreferences: SaveSettingsPreferencesUseCase,
    private val getBlockedUsers: GetBlockedUsersUseCase,
    private val logout: LogoutUseCase,
    private val systemSettings: SystemSettingsHelper,
) : SettingsEffectViewModel<SettingsUiState>(SettingsUiState()) {

    init {
        getPreferences().onRight { prefs -> update { it.copy(preferences = prefs) } }
        refreshSystemPermissions()
        refresh()
    }

    private var shownOnce = false

    /** Called each time the page enters composition; refreshes profile & blocked count on revisit. */
    fun onVisible() {
        refreshSystemPermissions()
        if (shownOnce) refresh() else shownOnce = true
    }

    fun refreshSystemPermissions() {
        update {
            it.copy(
                supportsOverlayPermission = systemSettings.supportsOverlayPermission,
                overlayPermissionGranted = systemSettings.canDrawOverlays(),
                osNotificationsEnabled = systemSettings.areNotificationsEnabled(),
            )
        }
    }

    /** Keeps the shown profile while refreshing in the background (cache-first). */
    fun refresh() {
        viewModelScope.launch {
            getCurrentUser().onRight { user -> update { it.copy(authUser = user) } }
            getMyProfile().onRight { profile -> update { it.copy(profile = profile) } }
            getBlockedUsers().onRight { blocked -> update { it.copy(blockedCount = blocked.size) } }
        }
    }

    fun toggleMessageNotifications(value: Boolean) = updatePreferences { it.copy(messageNotifications = value) }
    fun toggleMessagePreviews(value: Boolean) = updatePreferences { it.copy(messagePreviews = value) }
    fun toggleCallNotifications(value: Boolean) = updatePreferences { it.copy(callNotifications = value) }
    fun toggleOnlineStatus(value: Boolean) = updatePreferences { it.copy(onlineStatus = value) }

    fun setProfileVisibility(visibility: String) {
        updatePreferences { it.copy(profileVisibility = visibility) }
        update { it.copy(showVisibilitySheet = false) }
    }

    fun toggleSearch() {
        update {
            val nextSearching = !it.isSearching
            it.copy(
                isSearching = nextSearching,
                searchQuery = if (nextSearching) it.searchQuery else "",
            )
        }
    }

    fun onSearchQueryChange(query: String) = update { it.copy(searchQuery = query) }
    fun openVisibilitySheet() = update { it.copy(showVisibilitySheet = true) }
    fun dismissVisibilitySheet() = update { it.copy(showVisibilitySheet = false) }
    fun openHelpSheet() {
        refreshSystemPermissions()
        update { it.copy(showHelpSheet = true) }
    }

    fun dismissHelpSheet() = update { it.copy(showHelpSheet = false) }
    fun openOverlaySettings() = systemSettings.openOverlaySettings()
    fun openNotificationSettings() = systemSettings.openNotificationSettings()

    private fun updatePreferences(transform: (SettingsPreferences) -> SettingsPreferences) {
        update { it.copy(preferences = transform(it.preferences)) }
        savePreferences(state.value.preferences)
    }

    /** Flutter signs out without confirmation, then routes to login. */
    fun signOut() {
        if (state.value.isSigningOut) return
        update { it.copy(isSigningOut = true) }
        viewModelScope.launch {
            logout()
            update { it.copy(isSigningOut = false) }
            emit(SettingsEffect.SignedOut)
        }
    }
}
