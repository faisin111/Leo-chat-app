package com.leochat.features.auth.domain.usecases

import com.leochat.core.errors.AppResult
import com.leochat.features.auth.domain.entities.AuthSession
import com.leochat.features.auth.domain.entities.User
import com.leochat.features.auth.domain.repositories.AuthRepository

/** Application actions keep controllers independent from repository details. */
class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): AppResult<AuthSession> =
        repository.login(username.trim(), password)
}

class RegisterUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, email: String, password: String, displayName: String): AppResult<AuthSession> =
        repository.register(username.trim(), email.trim(), password, displayName.trim())
}

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AppResult<Unit> = repository.logout()
}

class LogoutAllUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AppResult<Unit> = repository.logoutAll()
}

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AppResult<User?> = repository.getCurrentUser()
}

class HasActiveSessionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Boolean = repository.hasActiveSession()
}

class ForgotPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): AppResult<Unit> = repository.forgotPassword(email.trim())
}

class ResetPasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(token: String, newPassword: String): AppResult<Unit> =
        repository.resetPassword(token.trim(), newPassword)
}

class ChangePasswordUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): AppResult<Unit> =
        repository.changePassword(currentPassword, newPassword)
}

class VerifyEmailUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(token: String): AppResult<Unit> = repository.verifyEmail(token.trim())
}

class ResendVerificationUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String? = null): AppResult<Unit> = repository.resendVerification(email?.trim())
}
