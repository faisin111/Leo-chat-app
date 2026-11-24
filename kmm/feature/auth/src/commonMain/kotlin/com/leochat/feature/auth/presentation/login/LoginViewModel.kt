package com.leochat.feature.auth.presentation.login

import com.leochat.feature.auth.presentation.EffectViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.core.common.constants.AppStrings
import com.leochat.feature.auth.domain.usecase.LoginUseCase
import com.leochat.feature.auth.presentation.AuthEffect
import com.leochat.feature.auth.presentation.LoginUiState
import kotlinx.coroutines.launch

class LoginViewModel(private val login: LoginUseCase) : EffectViewModel<LoginUiState>(LoginUiState()) {

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
