package com.leochat.feature.auth

import com.leochat.core.common.constants.AppStrings
import com.leochat.feature.auth.domain.usecase.ForgotPasswordUseCase
import com.leochat.feature.auth.domain.usecase.LoginUseCase
import com.leochat.feature.auth.domain.usecase.RegisterUseCase
import com.leochat.feature.auth.domain.usecase.ResetPasswordUseCase
import com.leochat.feature.auth.presentation.passwordrecovery.ForgotPasswordViewModel
import com.leochat.feature.auth.presentation.login.LoginViewModel
import com.leochat.feature.auth.presentation.register.RegisterViewModel
import com.leochat.feature.auth.presentation.passwordrecovery.ResetPasswordViewModel
import com.leochat.feature.auth.presentation.AuthEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthControllersTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loginRequiresAllFields() = runTest {
        val repo = FakeAuthRepository()
        val controller = LoginViewModel(LoginUseCase(repo))
        controller.onUsernameChange("  ")
        controller.submit()
        assertEquals(AuthEffect.ShowError(AppStrings.fillRequiredFields), controller.effects.first())
        assertTrue(repo.calls.isEmpty())
    }

    @Test
    fun loginSuccessTrimsUsernameAndNavigatesHome() = runTest {
        val repo = FakeAuthRepository()
        val controller = LoginViewModel(LoginUseCase(repo))
        controller.onUsernameChange(" alex ")
        controller.onPasswordChange("secret")
        controller.submit()
        val effects = controller.effects.take(2).toList()
        assertEquals(listOf(AuthEffect.ShowSuccess(AppStrings.loginSuccess), AuthEffect.NavigateHome), effects)
        assertEquals(listOf("login:alex"), repo.calls)
        assertFalse(controller.state.value.isLoading)
    }

    @Test
    fun loginFailureShowsServerMessage() = runTest {
        val repo = FakeAuthRepository(loginResult = FakeAuthRepository.apiFailure)
        val controller = LoginViewModel(LoginUseCase(repo))
        controller.onUsernameChange("alex")
        controller.onPasswordChange("bad")
        controller.submit()
        assertEquals(AuthEffect.ShowError("Invalid credentials"), controller.effects.first())
    }

    @Test
    fun registerValidatesEmailPasswordAndTerms() = runTest {
        val controller = RegisterViewModel(RegisterUseCase(FakeAuthRepository()))
        controller.onDisplayNameChange("Alex")
        controller.onUsernameChange("alex")
        controller.onEmailChange("not-an-email")
        controller.onPasswordChange("12345678")
        controller.submit()
        assertEquals(AuthEffect.ShowError(AppStrings.invalidEmail), controller.effects.first())

        controller.onEmailChange("alex@leo.chat")
        controller.onPasswordChange("short")
        controller.submit()
        assertEquals(AuthEffect.ShowError(AppStrings.passwordTooShort), controller.effects.first())

        controller.onPasswordChange("long-enough")
        controller.onTermsChange(false)
        controller.submit()
        assertEquals(AuthEffect.ShowError(AppStrings.acceptTermsRequired), controller.effects.first())
    }

    @Test
    fun registerSuccessNavigatesHome() = runTest {
        val repo = FakeAuthRepository()
        val controller = RegisterViewModel(RegisterUseCase(repo))
        controller.onDisplayNameChange(" Alex ")
        controller.onUsernameChange("alex")
        controller.onEmailChange("alex@leo.chat")
        controller.onPasswordChange("long-enough")
        controller.submit()
        assertEquals(AuthEffect.NavigateHome, controller.effects.take(2).toList().last())
        assertEquals(listOf("register:alex:alex@leo.chat:Alex"), repo.calls)
    }

    @Test
    fun forgotPasswordMarksSubmitted() = runTest {
        val repo = FakeAuthRepository()
        val controller = ForgotPasswordViewModel(ForgotPasswordUseCase(repo))
        controller.onEmailChange("alex@leo.chat")
        controller.submit()
        assertEquals(AuthEffect.ShowSuccess(AppStrings.resetInstructionsSent), controller.effects.first())
        assertTrue(controller.state.value.submitted)
    }

    @Test
    fun resetPasswordRequiresMatchingPasswords() = runTest {
        val repo = FakeAuthRepository()
        val controller = ResetPasswordViewModel(ResetPasswordUseCase(repo))
        controller.onTokenChange("token")
        controller.onNewPasswordChange("password-1")
        controller.onConfirmPasswordChange("password-2")
        controller.submit()
        assertEquals(AuthEffect.ShowError(AppStrings.passwordsDoNotMatch), controller.effects.first())

        controller.onConfirmPasswordChange("password-1")
        controller.submit()
        assertEquals(AuthEffect.NavigateLogin, controller.effects.take(2).toList().last())
        assertEquals(listOf("reset:token"), repo.calls)
    }
}
