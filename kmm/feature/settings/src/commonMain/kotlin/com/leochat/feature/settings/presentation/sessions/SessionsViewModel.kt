package com.leochat.feature.settings.presentation.sessions

import com.leochat.feature.settings.presentation.SettingsEffectViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.feature.settings.presentation.settings.SessionsUiState
import com.leochat.feature.settings.presentation.settings.SettingsEffect
import com.leochat.feature.users.domain.usecase.GetSessionsUseCase
import com.leochat.feature.users.domain.usecase.RevokeSessionUseCase
import kotlinx.coroutines.launch

/** Logged-in devices list. Mirrors `SessionsScreen` + `userSessionsProvider`. */
class SessionsViewModel(
    private val getSessions: GetSessionsUseCase,
    private val revokeSession: RevokeSessionUseCase,
) : SettingsEffectViewModel<SessionsUiState>(SessionsUiState()) {

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
