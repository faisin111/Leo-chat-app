package com.leochat.features.chat.presentation.providers

import com.leochat.features.chat.data.datasources.ChatLocalDataSource
import com.leochat.features.chat.data.datasources.ChatRemoteDataSource
import com.leochat.features.chat.data.datasources.ChatRemoteDataSourceImpl
import com.leochat.features.chat.data.repositories.ChatRealtimeRepositoryImpl
import com.leochat.features.chat.data.repositories.ChatRepositoryImpl
import com.leochat.features.chat.domain.repositories.ChatRealtimeRepository
import com.leochat.features.chat.domain.repositories.ChatRepository
import com.leochat.features.chat.domain.usecases.ChatUseCaseBundle
import com.leochat.features.chat.domain.usecases.CreateGroupUseCase
import com.leochat.features.chat.domain.usecases.StartDirectMessageUseCase
import com.leochat.features.chat.presentation.controllers.ChatController
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

/** Chat graph: data sources → repositories → use cases → controller. */
val chatModule = module {
    singleOf(::ChatRemoteDataSourceImpl) bind ChatRemoteDataSource::class
    singleOf(::ChatLocalDataSource)
    singleOf(::ChatRepositoryImpl) bind ChatRepository::class
    singleOf(::ChatRealtimeRepositoryImpl) bind ChatRealtimeRepository::class

    factory { ChatUseCaseBundle.from(get()) }
    factoryOf(::StartDirectMessageUseCase)
    factoryOf(::CreateGroupUseCase)

    viewModel { ChatController(get(), get(), get(), get(), getOrNull()) }
}
