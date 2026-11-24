package com.leochat.features.media.presentation.providers

import com.leochat.features.media.data.datasources.MediaFileReader
import com.leochat.features.media.data.datasources.MediaRemoteDataSource
import com.leochat.features.media.data.datasources.MediaRemoteDataSourceImpl
import com.leochat.features.media.data.datasources.SystemMediaFileReader
import com.leochat.features.media.data.repositories.MediaRepositoryImpl
import com.leochat.features.media.domain.repositories.MediaRepository
import com.leochat.features.media.domain.usecases.SendVoiceMessageUseCase
import com.leochat.features.media.domain.usecases.UploadVoiceUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Bare client for presigned storage uploads: no Bearer, no API base URL. */
val MediaUploadClient = named("mediaUploadClient")

fun createMediaUploadClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    expectSuccess = true
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        requestTimeoutMillis = 60_000
        socketTimeoutMillis = 30_000
    }
}

/** Media graph: presign/upload/confirm and the voice message use case. */
val mediaModule = module {
    single(MediaUploadClient) { createMediaUploadClient(get()) }
    single<MediaRemoteDataSource> { MediaRemoteDataSourceImpl(apiClient = get(), uploadClient = get(MediaUploadClient)) }
    single<MediaFileReader> { SystemMediaFileReader() }
    single<MediaRepository> { MediaRepositoryImpl(get(), get()) }
    factoryOf(::UploadVoiceUseCase)
    factoryOf(::SendVoiceMessageUseCase)
}

/** Binds platform `VoiceRecorder` and `VoicePlayerFactory`. */
expect val mediaPlatformModule: Module
