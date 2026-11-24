package com.leochat.features.settings.presentation.controllers

import androidx.lifecycle.viewModelScope
import com.leochat.features.auth.domain.usecases.GetCurrentUserUseCase
import com.leochat.features.auth.domain.usecases.LogoutUseCase
import com.leochat.features.settings.domain.entities.SettingsPreferences
import com.leochat.features.settings.domain.usecases.GetSettingsPreferencesUseCase
import com.leochat.features.settings.domain.usecases.SaveSettingsPreferencesUseCase
import com.leochat.features.settings.presentation.states.SettingsEffect
import com.leochat.features.settings.presentation.states.SettingsState
import com.leochat.features.users.domain.usecases.GetMyProfileUseCase
import kotlinx.coroutines.launch

/** Settings home. Mirrors `SettingsNotifier` plus the profile and sign-out wiring of `SettingsPage`. */
class SettingsController(
    private val getMyProfile: GetMyProfileUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getPreferences: GetSettingsPreferencesUseCase,
    private val savePreferences: SaveSettingsPreferencesUseCase,
    private val logout: LogoutUseCase,
) : SettingsEffectController<SettingsState>(SettingsState()) {

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
