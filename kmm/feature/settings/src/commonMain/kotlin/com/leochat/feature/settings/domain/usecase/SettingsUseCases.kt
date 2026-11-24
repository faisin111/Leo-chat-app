package com.leochat.feature.settings.domain.usecase

import com.leochat.feature.settings.presentation.security.SecurityScreen
import com.leochat.core.common.result.AppResult
import com.leochat.feature.settings.domain.model.SettingsPreferences
import com.leochat.feature.settings.domain.repository.SettingsPreferencesRepository

class GetSettingsPreferencesUseCase(private val repository: SettingsPreferencesRepository) {
    operator fun invoke(): AppResult<SettingsPreferences> = repository.load()
}

class SaveSettingsPreferencesUseCase(private val repository: SettingsPreferencesRepository) {
    operator fun invoke(preferences: SettingsPreferences): AppResult<Unit> = repository.save(preferences)
}

/** Change-password form rules. Mirrors the checks in `SecurityScreen._handleChangePassword`. */
object PasswordChangeValidation {
    const val MinPasswordLength = 8

    enum class Error { MissingFields, TooShort, Mismatch }

    fun validate(current: String, newPassword: String, confirm: String): Error? = when {
        current.isEmpty() || newPassword.isEmpty() || confirm.isEmpty() -> Error.MissingFields
        newPassword.length < MinPasswordLength -> Error.TooShort
        newPassword != confirm -> Error.Mismatch
        else -> null
    }
}
