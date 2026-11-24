package com.leochat.feature.chat.di

import com.leochat.feature.chat.data.local.ChatLocalDataSource
import com.leochat.feature.chat.data.remote.ChatRemoteDataSource
import com.leochat.feature.chat.data.remote.ChatRemoteDataSourceImpl
import com.leochat.feature.chat.data.repository.ChatRealtimeRepositoryImpl
import com.leochat.feature.chat.data.repository.ChatRepositoryImpl
import com.leochat.feature.chat.domain.repository.ChatRealtimeRepository
import com.leochat.feature.chat.domain.repository.ChatRepository
import com.leochat.feature.chat.domain.usecase.ChatUseCaseBundle
import com.leochat.feature.chat.domain.usecase.CreateGroupUseCase
import com.leochat.feature.chat.domain.usecase.StartDirectMessageUseCase
import com.leochat.feature.chat.presentation.ChatViewModel
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

    viewModel { ChatViewModel(get(), get(), get(), get(), getOrNull()) }
}
