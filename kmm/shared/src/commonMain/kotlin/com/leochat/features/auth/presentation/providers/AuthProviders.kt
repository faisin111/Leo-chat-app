package com.leochat.features.auth.presentation.providers

import com.leochat.features.auth.data.datasources.AuthLocalDataSource
import com.leochat.features.auth.data.datasources.AuthRemoteDataSource
import com.leochat.features.auth.data.datasources.AuthRemoteDataSourceImpl
import com.leochat.features.auth.data.repositories.AuthRepositoryImpl
import com.leochat.features.auth.domain.repositories.AuthRepository
import com.leochat.features.auth.domain.usecases.ChangePasswordUseCase
import com.leochat.features.auth.domain.usecases.ForgotPasswordUseCase
import com.leochat.features.auth.domain.usecases.GetCurrentUserUseCase
import com.leochat.features.auth.domain.usecases.HasActiveSessionUseCase
import com.leochat.features.auth.domain.usecases.LoginUseCase
import com.leochat.features.auth.domain.usecases.LogoutAllUseCase
import com.leochat.features.auth.domain.usecases.LogoutUseCase
import com.leochat.features.auth.domain.usecases.RegisterUseCase
import com.leochat.features.auth.domain.usecases.ResendVerificationUseCase
import com.leochat.features.auth.domain.usecases.ResetPasswordUseCase
import com.leochat.features.auth.domain.usecases.VerifyEmailUseCase
import com.leochat.features.auth.presentation.controllers.ForgotPasswordController
import com.leochat.features.auth.presentation.controllers.LoginController
import com.leochat.features.auth.presentation.controllers.RegisterController
import com.leochat.features.auth.presentation.controllers.ResetPasswordController
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Auth dependency graph: data sources → repository → use cases → controllers. */
val authModule = module {
    singleOf(::AuthRemoteDataSourceImpl) bind AuthRemoteDataSource::class
    singleOf(::AuthLocalDataSource)
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class

    factoryOf(::LoginUseCase)
    factoryOf(::RegisterUseCase)
    factoryOf(::LogoutUseCase)
    factoryOf(::LogoutAllUseCase)
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::HasActiveSessionUseCase)
    factoryOf(::ForgotPasswordUseCase)
    factoryOf(::ResetPasswordUseCase)
    factoryOf(::ChangePasswordUseCase)
    factoryOf(::VerifyEmailUseCase)
    factoryOf(::ResendVerificationUseCase)

    viewModelOf(::LoginController)
    viewModelOf(::RegisterController)
    viewModelOf(::ForgotPasswordController)
    viewModelOf(::ResetPasswordController)
}
