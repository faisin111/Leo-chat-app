package com.leochat.features.auth.presentation.controllers

import androidx.lifecycle.viewModelScope
import com.leochat.core.constants.AppStrings
import com.leochat.features.auth.domain.usecases.AuthValidation
import com.leochat.features.auth.domain.usecases.ForgotPasswordUseCase
import com.leochat.features.auth.domain.usecases.ResetPasswordUseCase
import com.leochat.features.auth.presentation.states.AuthEffect
import com.leochat.features.auth.presentation.states.ForgotPasswordState
import com.leochat.features.auth.presentation.states.ResetPasswordState
import kotlinx.coroutines.launch

class ForgotPasswordController(
    private val forgotPassword: ForgotPasswordUseCase,
) : EffectController<ForgotPasswordState>(ForgotPasswordState()) {

    fun onEmailChange(value: String) = update { it.copy(email = value) }

    fun submit() {
        val s = state.value
        if (s.isLoading) return
        val email = s.email.trim()
        when {
            email.isEmpty() -> return emit(AuthEffect.ShowError(AppStrings.fillRequiredFields))
            !AuthValidation.isValidEmail(email) -> return emit(AuthEffect.ShowError(AppStrings.invalidEmail))
        }
        update { it.copy(isLoading = true) }
        viewModelScope.launch {
            forgotPassword(email).fold(
                ifLeft = { failure ->
                    update { it.copy(isLoading = false) }
                    emit(AuthEffect.ShowError(failure.message))
                },
                ifRight = {
                    update { it.copy(isLoading = false, submitted = true) }
                    emit(AuthEffect.ShowSuccess(AppStrings.resetInstructionsSent))
                },
            )
        }
    }
}

class ResetPasswordController(
    private val resetPassword: ResetPasswordUseCase,
) : EffectController<ResetPasswordState>(ResetPasswordState()) {

    fun onTokenChange(value: String) = update { it.copy(token = value) }
    fun onNewPasswordChange(value: String) = update { it.copy(newPassword = value) }
    fun onConfirmPasswordChange(value: String) = update { it.copy(confirmPassword = value) }
    fun toggleNewVisibility() = update { it.copy(obscureNew = !it.obscureNew) }
    fun toggleConfirmVisibility() = update { it.copy(obscureConfirm = !it.obscureConfirm) }

    fun submit() {
        val s = state.value
        if (s.isLoading) return
        val token = s.token.trim()
        val error = when {
            token.isEmpty() || s.newPassword.isEmpty() || s.confirmPassword.isEmpty() -> AppStrings.fillRequiredFields
            !AuthValidation.isStrongEnough(s.newPassword) -> AppStrings.passwordTooShort
            s.newPassword != s.confirmPassword -> AppStrings.passwordsDoNotMatch
            else -> null
        }
        if (error != null) return emit(AuthEffect.ShowError(error))
        update { it.copy(isLoading = true) }
        viewModelScope.launch {
            resetPassword(token, s.newPassword).fold(
                ifLeft = { failure ->
                    update { it.copy(isLoading = false) }
                    emit(AuthEffect.ShowError(failure.message))
                },
                ifRight = {
                    update { it.copy(isLoading = false) }
                    emit(AuthEffect.ShowSuccess(AppStrings.passwordResetSuccess))
                    emit(AuthEffect.NavigateLogin)
                },
            )
        }
    }
}
