package com.leochat.features.settings.data.datasources

import com.leochat.core.network.LeoJson
import com.leochat.core.storage.KeyValueStorage
import com.leochat.features.settings.data.models.SettingsPreferencesDto

/** Stores settings toggles as one JSON payload in plain preferences (never tokens). */
class SettingsLocalDataSource(private val storage: KeyValueStorage) {
    fun read(): SettingsPreferencesDto? = storage.getString(KEY)?.let {
        runCatching { LeoJson.decodeFromString(SettingsPreferencesDto.serializer(), it) }.getOrNull()
    }

    fun write(dto: SettingsPreferencesDto) {
        storage.putString(KEY, LeoJson.encodeToString(SettingsPreferencesDto.serializer(), dto))
    }

    companion object {
        const val KEY = "leo_settings_preferences"
    }
}
