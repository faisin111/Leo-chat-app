package com.leochat.feature.auth.di

import com.leochat.feature.auth.data.local.AuthLocalDataSource
import com.leochat.feature.auth.data.remote.AuthRemoteDataSource
import com.leochat.feature.auth.data.remote.AuthRemoteDataSourceImpl
import com.leochat.feature.auth.data.repository.AuthRepositoryImpl
import com.leochat.feature.auth.domain.repository.AuthRepository
import com.leochat.feature.auth.domain.usecase.ChangePasswordUseCase
import com.leochat.feature.auth.domain.usecase.ForgotPasswordUseCase
import com.leochat.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.leochat.feature.auth.domain.usecase.HasActiveSessionUseCase
import com.leochat.feature.auth.domain.usecase.LoginUseCase
import com.leochat.feature.auth.domain.usecase.LogoutAllUseCase
import com.leochat.feature.auth.domain.usecase.LogoutUseCase
import com.leochat.feature.auth.domain.usecase.RegisterUseCase
import com.leochat.feature.auth.domain.usecase.ResendVerificationUseCase
import com.leochat.feature.auth.domain.usecase.ResetPasswordUseCase
import com.leochat.feature.auth.domain.usecase.VerifyEmailUseCase
import com.leochat.feature.auth.presentation.passwordrecovery.ForgotPasswordViewModel
import com.leochat.feature.auth.presentation.login.LoginViewModel
import com.leochat.feature.auth.presentation.register.RegisterViewModel
import com.leochat.feature.auth.presentation.passwordrecovery.ResetPasswordViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Auth dependency graph: data sources → repository → use cases → controllers. */
val authModule = module {
    singleOf(::AuthRemoteDataSourceImpl) bind AuthRemoteDataSource::class
    singleOf(::AuthLocalDataSource)
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get(), getOrNull()) }

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

    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
}
