package com.leochat.feature.media.di

import com.leochat.feature.media.domain.repository.VoicePlayerFactory
import com.leochat.feature.media.domain.repository.VoiceRecorder
import com.leochat.feature.media.data.remote.MediaFileReader
import com.leochat.feature.media.data.remote.MediaRemoteDataSource
import com.leochat.feature.media.data.remote.MediaRemoteDataSourceImpl
import com.leochat.feature.media.data.remote.SystemMediaFileReader
import com.leochat.feature.media.data.repository.MediaRepositoryImpl
import com.leochat.feature.media.domain.repository.MediaRepository
import com.leochat.feature.media.domain.usecase.GetMediaDownloadUrlUseCase
import com.leochat.feature.media.domain.usecase.SendVoiceMessageUseCase
import com.leochat.feature.media.domain.usecase.UploadAttachmentUseCase
import com.leochat.feature.media.domain.usecase.UploadVoiceUseCase
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
    single<MediaRepository> { MediaRepositoryImpl(get(), getOrNull<MediaFileReader>() ?: SystemMediaFileReader()) }
    factoryOf(::UploadVoiceUseCase)
    factoryOf(::SendVoiceMessageUseCase)
    factoryOf(::UploadAttachmentUseCase)
    factoryOf(::GetMediaDownloadUrlUseCase)
}

/** Binds platform `VoiceRecorder` and `VoicePlayerFactory`. */
expect val mediaPlatformModule: Module
