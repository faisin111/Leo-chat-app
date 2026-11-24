package com.leochat.feature.settings.data.repository

import com.leochat.core.common.result.AppFailure
import com.leochat.core.common.result.AppResult
import com.leochat.core.common.result.Either
import com.leochat.feature.settings.data.local.SettingsLocalDataSource
import com.leochat.feature.settings.data.remote.SettingsPreferencesDto
import com.leochat.feature.settings.domain.model.SettingsPreferences
import com.leochat.feature.settings.domain.repository.SettingsPreferencesRepository

class SettingsPreferencesRepositoryImpl(
    private val local: SettingsLocalDataSource,
) : SettingsPreferencesRepository {
    override fun load(): AppResult<SettingsPreferences> =
        Either.Right(local.read()?.toDomain() ?: SettingsPreferences())

    override fun save(preferences: SettingsPreferences): AppResult<Unit> =
        runCatching { local.write(SettingsPreferencesDto.fromDomain(preferences)) }
            .fold(onSuccess = { Either.Right(Unit) }, onFailure = { Either.Left(AppFailure.Unknown()) })
}
