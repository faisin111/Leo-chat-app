package com.leochat.feature.splash

import com.leochat.feature.auth.FakeAuthRepository
import com.leochat.feature.auth.domain.usecase.HasActiveSessionUseCase
import com.leochat.feature.splash.presentation.splash.SplashViewModel
import com.leochat.feature.splash.presentation.SplashDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SplashControllerTest {
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun waitsThreeSecondsThenRoutesToOnboardingWithoutToken() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val controller = SplashViewModel(HasActiveSessionUseCase(FakeAuthRepository(hasSession = false)))
        advanceTimeBy(2_999)
        assertNull(controller.destination.value)
        advanceTimeBy(10)
        assertEquals(SplashDestination.Onboarding, controller.destination.value)
    }

    @Test
    fun routesHomeWithStoredToken() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val controller = SplashViewModel(HasActiveSessionUseCase(FakeAuthRepository(hasSession = true)))
        advanceTimeBy(3_010)
        assertEquals(SplashDestination.Home, controller.destination.value)
    }
}
