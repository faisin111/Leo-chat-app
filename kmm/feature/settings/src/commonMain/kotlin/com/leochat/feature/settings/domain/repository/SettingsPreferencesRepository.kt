package com.leochat.feature.settings.domain.repository

import com.leochat.core.common.result.AppResult
import com.leochat.feature.settings.domain.model.SettingsPreferences

interface SettingsPreferencesRepository {
    fun load(): AppResult<SettingsPreferences>
    fun save(preferences: SettingsPreferences): AppResult<Unit>
}
