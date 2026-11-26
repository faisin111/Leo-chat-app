package com.leochat.app

import com.leochat.app.di.appModules
import com.leochat.core.common.di.ApplicationScope
import com.leochat.core.network.di.httpEngineModule
import com.leochat.core.platform.di.platformModule
import com.leochat.core.platform.storage.InMemoryKeyValueStorage
import com.leochat.core.platform.storage.InMemorySecureStorage
import com.leochat.core.platform.storage.KeyValueStorage
import com.leochat.core.platform.storage.SecureStorage
import com.leochat.feature.ai.presentation.ai.AiViewModel
import com.leochat.feature.auth.presentation.passwordrecovery.ForgotPasswordViewModel
import com.leochat.feature.auth.presentation.login.LoginViewModel
import com.leochat.feature.auth.presentation.register.RegisterViewModel
import com.leochat.feature.auth.presentation.passwordrecovery.ResetPasswordViewModel
import com.leochat.feature.calls.presentation.CallController
import com.leochat.feature.calls.di.callsPlatformModule
import com.leochat.feature.chat.presentation.ChatViewModel
import com.leochat.feature.chat.presentation.groupdetails.GroupDetailsViewModel
import com.leochat.feature.chat.presentation.newconversation.NewConversationViewModel
import com.leochat.feature.chat.presentation.newgroup.NewGroupViewModel
import com.leochat.feature.chat.presentation.sharedmedia.SharedMediaViewModel
import com.leochat.feature.media.domain.usecase.SendVoiceMessageUseCase
import com.leochat.feature.media.di.mediaPlatformModule
import com.leochat.feature.notifications.presentation.NotificationsViewModel
import com.leochat.feature.settings.presentation.appearance.AppearanceViewModel
import com.leochat.feature.settings.presentation.editprofile.EditProfileViewModel
import com.leochat.feature.settings.presentation.security.SecurityViewModel
import com.leochat.feature.settings.presentation.sessions.SessionsViewModel
import com.leochat.feature.settings.presentation.settings.SettingsViewModel
import com.leochat.feature.splash.presentation.splash.SplashViewModel
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
        modules(appModules - platformModule - httpEngineModule - mediaPlatformModule - callsPlatformModule + testPlatform)
    }.koin

    @Test
    fun resolvesEveryController() {
        val koin = koin()
        koin.get<SplashViewModel>()
        koin.get<LoginViewModel>()
        koin.get<RegisterViewModel>()
        koin.get<ForgotPasswordViewModel>()
        koin.get<ResetPasswordViewModel>()
        koin.get<ChatViewModel>()
        koin.get<NewConversationViewModel>()
        koin.get<NewGroupViewModel>()
        koin.get<GroupDetailsViewModel>()
        koin.get<SharedMediaViewModel>()
        koin.get<AiViewModel>()
        koin.get<SettingsViewModel>()
        koin.get<EditProfileViewModel>()
        koin.get<SecurityViewModel>()
        koin.get<SessionsViewModel>()
        koin.get<AppearanceViewModel>()
        koin.get<CallController>()
        koin.get<NotificationsViewModel>()
        koin.get<SendVoiceMessageUseCase>()
        koin.close()
    }
}
