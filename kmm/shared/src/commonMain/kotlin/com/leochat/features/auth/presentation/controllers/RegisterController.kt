package com.leochat.features.auth.presentation.controllers

import androidx.lifecycle.viewModelScope
import com.leochat.core.constants.AppStrings
import com.leochat.features.auth.domain.usecases.AuthValidation
import com.leochat.features.auth.domain.usecases.RegisterUseCase
import com.leochat.features.auth.presentation.states.AuthEffect
import com.leochat.features.auth.presentation.states.RegisterState
import kotlinx.coroutines.launch

class RegisterController(private val register: RegisterUseCase) : EffectController<RegisterState>(RegisterState()) {

    fun onDisplayNameChange(value: String) = update { it.copy(displayName = value) }
    fun onUsernameChange(value: String) = update { it.copy(username = value) }
    fun onEmailChange(value: String) = update { it.copy(email = value) }
    fun onPasswordChange(value: String) = update { it.copy(password = value) }
    fun togglePasswordVisibility() = update { it.copy(obscurePassword = !it.obscurePassword) }
    fun onTermsChange(accepted: Boolean) = update { it.copy(acceptedTerms = accepted) }

    fun submit() {
        val s = state.value
        if (s.isLoading) return
        val displayName = s.displayName.trim()
        val username = s.username.trim()
        val email = s.email.trim()
        val error = when {
            displayName.isEmpty() || username.isEmpty() || email.isEmpty() || s.password.isEmpty() -> AppStrings.fillRequiredFields
            !AuthValidation.isValidEmail(email) -> AppStrings.invalidEmail
            !AuthValidation.isStrongEnough(s.password) -> AppStrings.passwordTooShort
            !s.acceptedTerms -> AppStrings.acceptTermsRequired
            else -> null
        }
        if (error != null) {
            emit(AuthEffect.ShowError(error))
            return
        }
        update { it.copy(isLoading = true) }
        viewModelScope.launch {
            register(username, email, s.password, displayName).fold(
                ifLeft = { failure ->
                    update { it.copy(isLoading = false) }
                    emit(AuthEffect.ShowError(failure.message))
                },
                ifRight = {
                    update { it.copy(isLoading = false) }
                    emit(AuthEffect.ShowSuccess(AppStrings.registerSuccess))
                    emit(AuthEffect.NavigateHome)
                },
            )
        }
    }
}
