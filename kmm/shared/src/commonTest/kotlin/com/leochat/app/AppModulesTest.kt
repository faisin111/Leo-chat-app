package com.leochat.app

import com.leochat.core.di.ApplicationScope
import com.leochat.core.di.platformModule
import com.leochat.core.storage.InMemoryKeyValueStorage
import com.leochat.core.storage.InMemorySecureStorage
import com.leochat.core.storage.KeyValueStorage
import com.leochat.core.storage.SecureStorage
import com.leochat.features.admin.presentation.controllers.AdminDashboardController
import com.leochat.features.admin.presentation.controllers.AdminReportsController
import com.leochat.features.admin.presentation.controllers.AdminUsersController
import com.leochat.features.ai.presentation.controllers.AiController
import com.leochat.features.auth.presentation.controllers.ForgotPasswordController
import com.leochat.features.auth.presentation.controllers.LoginController
import com.leochat.features.auth.presentation.controllers.RegisterController
import com.leochat.features.auth.presentation.controllers.ResetPasswordController
import com.leochat.features.calls.presentation.controllers.CallController
import com.leochat.features.calls.presentation.providers.callsPlatformModule
import com.leochat.features.chat.presentation.controllers.ChatController
import com.leochat.features.chat.presentation.controllers.GroupDetailsController
import com.leochat.features.chat.presentation.controllers.NewConversationController
import com.leochat.features.chat.presentation.controllers.NewGroupController
import com.leochat.features.chat.presentation.controllers.SharedMediaController
import com.leochat.features.media.domain.usecases.SendVoiceMessageUseCase
import com.leochat.features.media.presentation.providers.mediaPlatformModule
import com.leochat.features.settings.presentation.controllers.AppearanceController
import com.leochat.features.settings.presentation.controllers.EditProfileController
import com.leochat.features.settings.presentation.controllers.SecurityController
import com.leochat.features.settings.presentation.controllers.SessionsController
import com.leochat.features.settings.presentation.controllers.SettingsController
import com.leochat.features.splash.presentation.controllers.SplashController
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/** Resolves every screen controller from the real graph, so a missing binding fails here and not at runtime. */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModulesTest {
    // Main never runs, so controller init work stays queued; app-scope work is cancelled up front.
    @BeforeTest fun setUp() = Dispatchers.setMain(StandardTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val testPlatform = module {
        single<SecureStorage> { InMemorySecureStorage() }
        single<KeyValueStorage> { InMemoryKeyValueStorage() }
        single<HttpClientEngine> { MockEngine { respondError(HttpStatusCode.ServiceUnavailable) } }
        single<CoroutineScope>(ApplicationScope) { CoroutineScope(Job().apply { cancel() }) }
    }

    private fun koin(): Koin = koinApplication {
        // Android platform modules need a Context; the in-memory module stands in for them.
        modules(appModules - platformModule - mediaPlatformModule - callsPlatformModule + testPlatform)
    }.koin

    @Test
    fun resolvesEveryController() {
        val koin = koin()
        koin.get<SplashController>()
        koin.get<LoginController>()
        koin.get<RegisterController>()
        koin.get<ForgotPasswordController>()
        koin.get<ResetPasswordController>()
        koin.get<ChatController>()
        koin.get<NewConversationController>()
        koin.get<NewGroupController>()
        koin.get<GroupDetailsController>()
        koin.get<SharedMediaController>()
        koin.get<AiController>()
        koin.get<SettingsController>()
        koin.get<EditProfileController>()
        koin.get<SecurityController>()
        koin.get<SessionsController>()
        koin.get<AppearanceController>()
        koin.get<AdminDashboardController>()
        koin.get<AdminUsersController>()
        koin.get<AdminReportsController>()
        koin.get<CallController>()
        koin.get<SendVoiceMessageUseCase>()
        koin.close()
    }
}
