package com.leochat.features.settings.data.repositories

import com.leochat.core.errors.AppFailure
import com.leochat.core.errors.AppResult
import com.leochat.core.errors.Either
import com.leochat.features.settings.data.datasources.SettingsLocalDataSource
import com.leochat.features.settings.data.models.SettingsPreferencesDto
import com.leochat.features.settings.domain.entities.SettingsPreferences
import com.leochat.features.settings.domain.repositories.SettingsPreferencesRepository

class SettingsPreferencesRepositoryImpl(
    private val local: SettingsLocalDataSource,
) : SettingsPreferencesRepository {
    override fun load(): AppResult<SettingsPreferences> =
        Either.Right(local.read()?.toDomain() ?: SettingsPreferences())

    override fun save(preferences: SettingsPreferences): AppResult<Unit> =
        runCatching { local.write(SettingsPreferencesDto.fromDomain(preferences)) }
            .fold(onSuccess = { Either.Right(Unit) }, onFailure = { Either.Left(AppFailure.Unknown()) })
}
