package com.leochat.feature.ai.di

import com.leochat.feature.ai.data.local.AiLocalDataSource
import com.leochat.feature.ai.data.remote.AiRemoteDataSource
import com.leochat.feature.ai.data.remote.AiRemoteDataSourceImpl
import com.leochat.feature.ai.data.repository.AiRepositoryImpl
import com.leochat.feature.ai.domain.repository.AiRepository
import com.leochat.feature.ai.domain.usecase.AiUseCaseBundle
import com.leochat.feature.ai.presentation.ai.AiViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Leo AI graph: data sources → repository → use cases → controller. */
val aiModule = module {
    singleOf(::AiRemoteDataSourceImpl) bind AiRemoteDataSource::class
    singleOf(::AiLocalDataSource)
    singleOf(::AiRepositoryImpl) bind AiRepository::class
    factory { AiUseCaseBundle.from(get()) }
    viewModelOf(::AiViewModel)
}
