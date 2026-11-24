package com.leochat.features.admin.presentation.providers

import com.leochat.features.admin.data.datasources.AdminRemoteDataSource
import com.leochat.features.admin.data.datasources.AdminRemoteDataSourceImpl
import com.leochat.features.admin.data.repositories.AdminRepositoryImpl
import com.leochat.features.admin.domain.repositories.AdminRepository
import com.leochat.features.admin.domain.usecases.CheckAdminAccessUseCase
import com.leochat.features.admin.domain.usecases.DeleteReportedMessageUseCase
import com.leochat.features.admin.domain.usecases.DeleteUserUseCase
import com.leochat.features.admin.domain.usecases.ForceLogoutUserUseCase
import com.leochat.features.admin.domain.usecases.GetAdminReportsUseCase
import com.leochat.features.admin.domain.usecases.GetAdminStatsUseCase
import com.leochat.features.admin.domain.usecases.GetAdminUsersUseCase
import com.leochat.features.admin.domain.usecases.ResolveReportUseCase
import com.leochat.features.admin.domain.usecases.UpdateUserStatusUseCase
import com.leochat.features.admin.presentation.controllers.AdminDashboardController
import com.leochat.features.admin.presentation.controllers.AdminReportsController
import com.leochat.features.admin.presentation.controllers.AdminUsersController
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Admin console graph: data source → repository → use cases → controllers.
 * Depends on `usersModule` for `GetMyProfileUseCase` (admin access gate).
 */
val adminModule = module {
    singleOf(::AdminRemoteDataSourceImpl) bind AdminRemoteDataSource::class
    singleOf(::AdminRepositoryImpl) bind AdminRepository::class

    factoryOf(::CheckAdminAccessUseCase)
    factoryOf(::GetAdminStatsUseCase)
    factoryOf(::GetAdminUsersUseCase)
    factoryOf(::UpdateUserStatusUseCase)
    factoryOf(::ForceLogoutUserUseCase)
    factoryOf(::DeleteUserUseCase)
    factoryOf(::GetAdminReportsUseCase)
    factoryOf(::ResolveReportUseCase)
    factoryOf(::DeleteReportedMessageUseCase)

    viewModelOf(::AdminDashboardController)
    viewModelOf(::AdminUsersController)
    viewModelOf(::AdminReportsController)
}
