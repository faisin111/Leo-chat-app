package com.leochat.features.ai.presentation.providers

import com.leochat.features.ai.data.datasources.AiLocalDataSource
import com.leochat.features.ai.data.datasources.AiRemoteDataSource
import com.leochat.features.ai.data.datasources.AiRemoteDataSourceImpl
import com.leochat.features.ai.data.repositories.AiRepositoryImpl
import com.leochat.features.ai.domain.repositories.AiRepository
import com.leochat.features.ai.domain.usecases.AiUseCaseBundle
import com.leochat.features.ai.presentation.controllers.AiController
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
    viewModelOf(::AiController)
}
