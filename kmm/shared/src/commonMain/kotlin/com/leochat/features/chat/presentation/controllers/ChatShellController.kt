package com.leochat.features.chat.presentation.controllers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.features.auth.domain.usecases.GetCurrentUserUseCase
import com.leochat.features.auth.domain.usecases.LogoutUseCase
import com.leochat.features.chat.presentation.states.ChatShellState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Temporary shell controller until the chat feature is ported (phase 2). */
class ChatShellController(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val logout: LogoutUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ChatShellState())
    val state: StateFlow<ChatShellState> = mutableState.asStateFlow()

    init {
        viewModelScope.launch {
            getCurrentUser().onRight { user -> mutableState.update { it.copy(user = user) } }
        }
    }

    fun signOut() {
        if (state.value.isSigningOut) return
        mutableState.update { it.copy(isSigningOut = true) }
        viewModelScope.launch {
            logout()
            mutableState.update { it.copy(isSigningOut = false, signedOut = true) }
        }
    }
}
