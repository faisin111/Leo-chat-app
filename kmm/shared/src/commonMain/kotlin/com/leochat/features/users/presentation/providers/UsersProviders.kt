package com.leochat.features.users.presentation.providers

import com.leochat.features.users.data.datasources.UsersRemoteDataSource
import com.leochat.features.users.data.repositories.UsersRepositoryImpl
import com.leochat.features.users.domain.repositories.UsersRepository
import com.leochat.features.users.domain.usecases.DeactivateAccountUseCase
import com.leochat.features.users.domain.usecases.GetMyProfileUseCase
import com.leochat.features.users.domain.usecases.GetSessionsUseCase
import com.leochat.features.users.domain.usecases.GetUserProfileUseCase
import com.leochat.features.users.domain.usecases.RevokeSessionUseCase
import com.leochat.features.users.domain.usecases.SearchUsersUseCase
import com.leochat.features.users.domain.usecases.UpdateProfileUseCase
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
    factoryOf(::GetSessionsUseCase)
    factoryOf(::RevokeSessionUseCase)
    factoryOf(::DeactivateAccountUseCase)
}
