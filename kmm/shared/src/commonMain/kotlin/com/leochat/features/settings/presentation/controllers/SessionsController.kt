package com.leochat.features.settings.presentation.controllers

import androidx.lifecycle.viewModelScope
import com.leochat.core.constants.SettingsStrings
import com.leochat.features.settings.presentation.states.SessionsState
import com.leochat.features.settings.presentation.states.SettingsEffect
import com.leochat.features.users.domain.usecases.GetSessionsUseCase
import com.leochat.features.users.domain.usecases.RevokeSessionUseCase
import kotlinx.coroutines.launch

/** Logged-in devices list. Mirrors `SessionsPage` + `userSessionsProvider`. */
class SessionsController(
    private val getSessions: GetSessionsUseCase,
    private val revokeSession: RevokeSessionUseCase,
) : SettingsEffectController<SessionsState>(SessionsState()) {

    init {
        load()
    }

    /** Keeps the current list on screen while reloading; shows the error only when nothing is loaded. */
    fun load() {
        update { it.copy(isLoading = it.sessions.isEmpty(), error = null) }
        viewModelScope.launch {
            getSessions().fold(
                ifLeft = { failure -> update { it.copy(isLoading = false, error = failure.message) } },
                ifRight = { sessions -> update { it.copy(isLoading = false, sessions = sessions, error = null) } },
            )
        }
    }

    fun revoke(sessionId: String) {
        if (state.value.revokingId != null) return
        update { it.copy(revokingId = sessionId) }
        viewModelScope.launch {
            val result = revokeSession(sessionId)
            update { it.copy(revokingId = null) }
            result.fold(
                ifLeft = { emit(SettingsEffect.ShowError(it.message)) },
                ifRight = {
                    emit(SettingsEffect.ShowSuccess(SettingsStrings.sessionRevoked))
                    load()
                },
            )
        }
    }
}
