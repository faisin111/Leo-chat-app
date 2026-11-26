package com.leochat.feature.notifications.di

import com.leochat.feature.notifications.data.local.NotificationLocalDataSource
import com.leochat.feature.notifications.data.remote.NotificationRemoteDataSource
import com.leochat.feature.notifications.data.remote.NotificationRemoteDataSourceImpl
import com.leochat.feature.notifications.data.repository.NotificationRepositoryImpl
import com.leochat.feature.notifications.domain.repository.NotificationRepository
import com.leochat.feature.notifications.domain.usecase.DeleteNotificationUseCase
import com.leochat.feature.notifications.domain.usecase.GetNotificationUnreadCountUseCase
import com.leochat.feature.notifications.domain.usecase.GetNotificationsPageUseCase
import com.leochat.feature.notifications.domain.usecase.MarkAllNotificationsReadUseCase
import com.leochat.feature.notifications.domain.usecase.MarkNotificationReadUseCase
import com.leochat.feature.notifications.domain.usecase.NotificationUseCaseBundle
import com.leochat.feature.notifications.presentation.NotificationsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val notificationsModule = module {
    single<NotificationRemoteDataSource> { NotificationRemoteDataSourceImpl(get()) }
    single { NotificationLocalDataSource(get(), get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get(), get(), get()) }
    factory { GetNotificationsPageUseCase(get()) }
    factory { GetNotificationUnreadCountUseCase(get()) }
    factory { MarkNotificationReadUseCase(get()) }
    factory { MarkAllNotificationsReadUseCase(get()) }
    factory { DeleteNotificationUseCase(get()) }
    factory { NotificationUseCaseBundle(get(), get(), get(), get(), get()) }
    viewModel { NotificationsViewModel(get(), get()) }
}
