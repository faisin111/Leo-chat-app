package com.leochat.feature.auth.presentation

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val obscurePassword: Boolean = true,
    val isLoading: Boolean = false,
)

data class RegisterUiState(
    val displayName: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val obscurePassword: Boolean = true,
    val acceptedTerms: Boolean = true,
    val isLoading: Boolean = false,
)

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val submitted: Boolean = false,
)

data class ResetPasswordUiState(
    val token: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val obscureNew: Boolean = true,
    val obscureConfirm: Boolean = true,
    val isLoading: Boolean = false,
)

/** One-off outcomes the page turns into snack bars and navigation. */
sealed interface AuthEffect {
    data class ShowError(val message: String) : AuthEffect
    data class ShowSuccess(val message: String) : AuthEffect
    data object NavigateHome : AuthEffect
    data object NavigateLogin : AuthEffect
}
