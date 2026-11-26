package com.leochat.feature.settings.presentation.blocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.feature.settings.domain.usecase.GetBlockedUsersUseCase
import com.leochat.feature.settings.presentation.settings.BlockedUsersUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads users returned by `GET_BLOCKED_USERS` and enriches them from `/users/{id}`. */
class BlockedUsersViewModel(
    private val getBlockedUsers: GetBlockedUsersUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(BlockedUsersUiState())
    val state: StateFlow<BlockedUsersUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getBlockedUsers().fold(
                ifLeft = { failure ->
                    _state.update { it.copy(isLoading = false, error = failure.message) }
                },
                ifRight = { items ->
                    _state.update { it.copy(users = items, isLoading = false, error = null) }
                },
            )
        }
    }
}
