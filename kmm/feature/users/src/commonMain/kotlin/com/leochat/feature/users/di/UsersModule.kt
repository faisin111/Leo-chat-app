package com.leochat.feature.users.di

import com.leochat.feature.users.data.remote.UsersRemoteDataSource
import com.leochat.feature.users.data.repository.UsersRepositoryImpl
import com.leochat.feature.users.domain.repository.UsersRepository
import com.leochat.feature.users.domain.usecase.DeactivateAccountUseCase
import com.leochat.feature.users.domain.usecase.DeleteProfilePictureUseCase
import com.leochat.feature.users.domain.usecase.GetMyProfileUseCase
import com.leochat.feature.users.domain.usecase.GetSessionsUseCase
import com.leochat.feature.users.domain.usecase.GetUserProfileUseCase
import com.leochat.feature.users.domain.usecase.GetUsersPresenceUseCase
import com.leochat.feature.users.domain.usecase.RevokeSessionUseCase
import com.leochat.feature.users.domain.usecase.SearchUsersUseCase
import com.leochat.feature.users.domain.usecase.UpdateProfileUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Users graph. Screens that show profiles, search, or sessions depend on these use cases. */
val usersModule = module {
    singleOf(::UsersRemoteDataSource)
    singleOf(::UsersRepositoryImpl) bind UsersRepository::class
    factoryOf(::GetMyProfileUseCase)
    factoryOf(::GetUserProfileUseCase)
    factoryOf(::SearchUsersUseCase)
    factoryOf(::UpdateProfileUseCase)
    factoryOf(::DeleteProfilePictureUseCase)
    factoryOf(::GetSessionsUseCase)
    factoryOf(::RevokeSessionUseCase)
    factoryOf(::DeactivateAccountUseCase)
    factoryOf(::GetUsersPresenceUseCase)
}
