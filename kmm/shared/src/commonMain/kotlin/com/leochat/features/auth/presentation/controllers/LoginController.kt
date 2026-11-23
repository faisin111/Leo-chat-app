package com.leochat.features.auth.presentation.controllers

import androidx.lifecycle.viewModelScope
import com.leochat.core.constants.AppStrings
import com.leochat.features.auth.domain.usecases.LoginUseCase
import com.leochat.features.auth.presentation.states.AuthEffect
import com.leochat.features.auth.presentation.states.LoginState
import kotlinx.coroutines.launch

class LoginController(private val login: LoginUseCase) : EffectController<LoginState>(LoginState()) {

    fun onUsernameChange(value: String) = update { it.copy(username = value) }
    fun onPasswordChange(value: String) = update { it.copy(password = value) }
    fun togglePasswordVisibility() = update { it.copy(obscurePassword = !it.obscurePassword) }

    fun submit() {
        val current = state.value
        if (current.isLoading) return
        val username = current.username.trim()
        if (username.isEmpty() || current.password.isEmpty()) {
            emit(AuthEffect.ShowError(AppStrings.fillRequiredFields))
            return
        }
        update { it.copy(isLoading = true) }
        viewModelScope.launch {
            login(username, current.password).fold(
                ifLeft = { failure ->
                    update { it.copy(isLoading = false) }
                    emit(AuthEffect.ShowError(failure.message))
                },
                ifRight = {
                    update { it.copy(isLoading = false) }
                    emit(AuthEffect.ShowSuccess(AppStrings.loginSuccess))
                    emit(AuthEffect.NavigateHome)
                },
            )
        }
    }
}
