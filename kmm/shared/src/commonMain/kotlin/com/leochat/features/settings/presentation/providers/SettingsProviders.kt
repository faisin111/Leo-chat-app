package com.leochat.features.settings.presentation.providers

import com.leochat.features.settings.data.datasources.SettingsLocalDataSource
import com.leochat.features.settings.data.repositories.SettingsPreferencesRepositoryImpl
import com.leochat.features.settings.domain.repositories.SettingsPreferencesRepository
import com.leochat.features.settings.domain.usecases.GetSettingsPreferencesUseCase
import com.leochat.features.settings.domain.usecases.SaveSettingsPreferencesUseCase
import com.leochat.features.settings.presentation.controllers.AppearanceController
import com.leochat.features.settings.presentation.controllers.EditProfileController
import com.leochat.features.settings.presentation.controllers.SecurityController
import com.leochat.features.settings.presentation.controllers.SessionsController
import com.leochat.features.settings.presentation.controllers.SettingsController
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Settings graph. Profile, session, and account use cases come from `usersModule` and `authModule`. */
val settingsModule = module {
    singleOf(::SettingsLocalDataSource)
    singleOf(::SettingsPreferencesRepositoryImpl) bind SettingsPreferencesRepository::class
    factoryOf(::GetSettingsPreferencesUseCase)
    factoryOf(::SaveSettingsPreferencesUseCase)

    viewModelOf(::SettingsController)
    viewModelOf(::EditProfileController)
    viewModelOf(::SecurityController)
    viewModelOf(::SessionsController)
    viewModelOf(::AppearanceController)
}
