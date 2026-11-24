package com.leochat.feature.settings.data.local

import com.leochat.core.common.serialization.LeoJson
import com.leochat.core.platform.storage.KeyValueStorage
import com.leochat.feature.settings.data.remote.SettingsPreferencesDto

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
