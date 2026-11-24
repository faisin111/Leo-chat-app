package com.leochat.feature.settings.presentation.settings

import com.leochat.feature.settings.presentation.SettingsEffectViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.leochat.feature.auth.domain.usecase.LogoutUseCase
import com.leochat.feature.settings.domain.model.SettingsPreferences
import com.leochat.feature.settings.domain.usecase.GetSettingsPreferencesUseCase
import com.leochat.feature.settings.domain.usecase.SaveSettingsPreferencesUseCase
import com.leochat.feature.settings.presentation.settings.SettingsEffect
import com.leochat.feature.settings.presentation.settings.SettingsUiState
import com.leochat.feature.users.domain.usecase.GetMyProfileUseCase
import kotlinx.coroutines.launch

/** Settings home. Mirrors `SettingsNotifier` plus the profile and sign-out wiring of `SettingsScreen`. */
class SettingsViewModel(
    private val getMyProfile: GetMyProfileUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getPreferences: GetSettingsPreferencesUseCase,
    private val savePreferences: SaveSettingsPreferencesUseCase,
    private val logout: LogoutUseCase,
) : SettingsEffectViewModel<SettingsUiState>(SettingsUiState()) {

    init {
        getPreferences().onRight { prefs -> update { it.copy(preferences = prefs) } }
        refresh()
    }

    private var shownOnce = false

    /** Called each time the page enters composition; refreshes after returning from edit profile. */
    fun onVisible() {
        if (shownOnce) refresh() else shownOnce = true
    }

    /** Keeps the shown profile while refreshing in the background (cache-first). */
    fun refresh() {
        viewModelScope.launch {
            getCurrentUser().onRight { user -> update { it.copy(authUser = user) } }
            getMyProfile().onRight { profile -> update { it.copy(profile = profile) } }
        }
    }

    fun toggleMessageNotifications(value: Boolean) = updatePreferences { it.copy(messageNotifications = value) }
    fun toggleMessagePreviews(value: Boolean) = updatePreferences { it.copy(messagePreviews = value) }
    fun toggleCallNotifications(value: Boolean) = updatePreferences { it.copy(callNotifications = value) }
    fun toggleOnlineStatus(value: Boolean) = updatePreferences { it.copy(onlineStatus = value) }

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
