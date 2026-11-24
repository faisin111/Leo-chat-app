package com.leochat.features.settings.domain.usecases

import com.leochat.core.errors.AppResult
import com.leochat.features.settings.domain.entities.SettingsPreferences
import com.leochat.features.settings.domain.repositories.SettingsPreferencesRepository

class GetSettingsPreferencesUseCase(private val repository: SettingsPreferencesRepository) {
    operator fun invoke(): AppResult<SettingsPreferences> = repository.load()
}

class SaveSettingsPreferencesUseCase(private val repository: SettingsPreferencesRepository) {
    operator fun invoke(preferences: SettingsPreferences): AppResult<Unit> = repository.save(preferences)
}

/** Change-password form rules. Mirrors the checks in `SecurityPage._handleChangePassword`. */
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
