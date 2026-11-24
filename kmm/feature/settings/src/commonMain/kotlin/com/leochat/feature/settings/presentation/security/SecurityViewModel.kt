package com.leochat.feature.settings.presentation.security

import com.leochat.feature.settings.presentation.SettingsEffectViewModel
import androidx.lifecycle.viewModelScope
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.feature.auth.domain.usecase.ChangePasswordUseCase
import com.leochat.feature.auth.domain.usecase.LogoutUseCase
import com.leochat.feature.settings.domain.usecase.PasswordChangeValidation
import com.leochat.feature.settings.presentation.settings.SecurityUiState
import com.leochat.feature.settings.presentation.settings.SettingsEffect
import com.leochat.feature.users.domain.usecase.DeactivateAccountUseCase
import kotlinx.coroutines.launch

/** Password change and account deactivation. Mirrors `_SecurityPageState`. */
class SecurityViewModel(
    private val changePassword: ChangePasswordUseCase,
    private val deactivateAccount: DeactivateAccountUseCase,
    private val logout: LogoutUseCase,
) : SettingsEffectViewModel<SecurityUiState>(SecurityUiState()) {

    fun onCurrentPasswordChange(value: String) = update { it.copy(currentPassword = value) }
    fun onNewPasswordChange(value: String) = update { it.copy(newPassword = value) }
    fun onConfirmPasswordChange(value: String) = update { it.copy(confirmPassword = value) }
    fun toggleCurrentVisibility() = update { it.copy(obscureCurrent = !it.obscureCurrent) }
    fun toggleNewVisibility() = update { it.copy(obscureNew = !it.obscureNew) }
    fun toggleConfirmVisibility() = update { it.copy(obscureConfirm = !it.obscureConfirm) }

    fun submitPasswordChange() {
        val s = state.value
        if (s.isUpdating) return
        val error = when (PasswordChangeValidation.validate(s.currentPassword, s.newPassword, s.confirmPassword)) {
            PasswordChangeValidation.Error.MissingFields -> AppStrings.fillRequiredFields
            PasswordChangeValidation.Error.TooShort -> AppStrings.passwordTooShort
            PasswordChangeValidation.Error.Mismatch -> AppStrings.passwordsDoNotMatch
            null -> null
        }
        if (error != null) return emit(SettingsEffect.ShowError(error))
        update { it.copy(isUpdating = true) }
        viewModelScope.launch {
            changePassword(s.currentPassword, s.newPassword).fold(
                ifLeft = { failure ->
                    update { it.copy(isUpdating = false) }
                    emit(SettingsEffect.ShowError(failure.message))
                },
                ifRight = {
                    update { it.copy(isUpdating = false, currentPassword = "", newPassword = "", confirmPassword = "") }
                    emit(SettingsEffect.ShowSuccess(SettingsStrings.passwordUpdated))
                },
            )
        }
    }

    /** Called after the user confirms the deactivate dialog. */
    fun deactivate() {
        if (state.value.isDeactivating) return
        update { it.copy(isDeactivating = true) }
        viewModelScope.launch {
            deactivateAccount().fold(
                ifLeft = { failure ->
                    update { it.copy(isDeactivating = false) }
                    emit(SettingsEffect.ShowError(failure.message))
                },
                ifRight = {
                    logout()
                    update { it.copy(isDeactivating = false) }
                    emit(SettingsEffect.SignedOut)
                },
            )
        }
    }
}
