package com.leochat.features.settings.domain.repositories

import com.leochat.core.errors.AppResult
import com.leochat.features.settings.domain.entities.SettingsPreferences

interface SettingsPreferencesRepository {
    fun load(): AppResult<SettingsPreferences>
    fun save(preferences: SettingsPreferences): AppResult<Unit>
}
