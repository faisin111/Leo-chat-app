package com.leochat.feature.auth.presentation.passwordrecovery

import com.leochat.feature.auth.presentation.EffectViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.core.common.constants.AppStrings
import com.leochat.feature.auth.domain.usecase.AuthValidation
import com.leochat.feature.auth.domain.usecase.ForgotPasswordUseCase
import com.leochat.feature.auth.domain.usecase.ResetPasswordUseCase
import com.leochat.feature.auth.presentation.AuthEffect
import com.leochat.feature.auth.presentation.ForgotPasswordUiState
import com.leochat.feature.auth.presentation.ResetPasswordUiState
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val forgotPassword: ForgotPasswordUseCase,
) : EffectViewModel<ForgotPasswordUiState>(ForgotPasswordUiState()) {

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

class ResetPasswordViewModel(
    private val resetPassword: ResetPasswordUseCase,
) : EffectViewModel<ResetPasswordUiState>(ResetPasswordUiState()) {

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
