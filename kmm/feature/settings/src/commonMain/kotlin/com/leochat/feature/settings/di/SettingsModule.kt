package com.leochat.feature.settings.di

import com.leochat.feature.settings.data.local.SettingsLocalDataSource
import com.leochat.feature.settings.data.remote.BlockedUsersRemoteDataSource
import com.leochat.feature.settings.data.repository.BlockedUsersRepositoryImpl
import com.leochat.feature.settings.data.repository.SettingsPreferencesRepositoryImpl
import com.leochat.feature.settings.domain.repository.BlockedUsersRepository
import com.leochat.feature.settings.domain.repository.SettingsPreferencesRepository
import com.leochat.feature.settings.domain.usecase.GetBlockedUsersUseCase
import com.leochat.feature.settings.domain.usecase.GetSettingsPreferencesUseCase
import com.leochat.feature.settings.domain.usecase.SaveSettingsPreferencesUseCase
import com.leochat.feature.settings.presentation.appearance.AppearanceViewModel
import com.leochat.feature.settings.presentation.blocked.BlockedUsersViewModel
import com.leochat.feature.settings.presentation.editprofile.EditProfileViewModel
import com.leochat.feature.settings.presentation.security.SecurityViewModel
import com.leochat.feature.settings.presentation.sessions.SessionsViewModel
import com.leochat.feature.settings.presentation.settings.SettingsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Settings graph. Profile, session, and account use cases come from `usersModule`, `authModule`, and `mediaModule`. */
val settingsModule = module {
    singleOf(::SettingsLocalDataSource)
    singleOf(::SettingsPreferencesRepositoryImpl) bind SettingsPreferencesRepository::class
    singleOf(::BlockedUsersRemoteDataSource)
    singleOf(::BlockedUsersRepositoryImpl) bind BlockedUsersRepository::class
    factoryOf(::GetSettingsPreferencesUseCase)
    factoryOf(::SaveSettingsPreferencesUseCase)
    factoryOf(::GetBlockedUsersUseCase)

    viewModelOf(::SettingsViewModel)
    viewModelOf(::EditProfileViewModel)
    viewModelOf(::SecurityViewModel)
    viewModelOf(::SessionsViewModel)
    viewModelOf(::AppearanceViewModel)
    viewModelOf(::BlockedUsersViewModel)
}
