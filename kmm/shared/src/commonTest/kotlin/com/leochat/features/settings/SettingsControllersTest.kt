package com.leochat.features.settings

import com.leochat.core.constants.AppStrings
import com.leochat.core.constants.SettingsStrings
import com.leochat.core.errors.Either
import com.leochat.core.storage.InMemoryKeyValueStorage
import com.leochat.core.theme.LeoThemeMode
import com.leochat.core.theme.ThemeModeStore
import com.leochat.features.auth.domain.entities.User
import com.leochat.features.auth.domain.usecases.ChangePasswordUseCase
import com.leochat.features.auth.domain.usecases.GetCurrentUserUseCase
import com.leochat.features.auth.domain.usecases.LogoutUseCase
import com.leochat.features.settings.data.datasources.SettingsLocalDataSource
import com.leochat.features.settings.data.repositories.SettingsPreferencesRepositoryImpl
import com.leochat.features.settings.domain.usecases.GetSettingsPreferencesUseCase
import com.leochat.features.settings.domain.usecases.SaveSettingsPreferencesUseCase
import com.leochat.features.settings.presentation.controllers.AppearanceController
import com.leochat.features.settings.presentation.controllers.EditProfileController
import com.leochat.features.settings.presentation.controllers.SecurityController
import com.leochat.features.settings.presentation.controllers.SessionsController
import com.leochat.features.settings.presentation.controllers.SettingsController
import com.leochat.features.settings.presentation.states.SettingsEffect
import com.leochat.features.users.domain.usecases.DeactivateAccountUseCase
import com.leochat.features.users.domain.usecases.GetMyProfileUseCase
import com.leochat.features.users.domain.usecases.GetSessionsUseCase
import com.leochat.features.users.domain.usecases.RevokeSessionUseCase
import com.leochat.features.users.domain.usecases.UpdateProfileUseCase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsControllersTest {
    @BeforeTest fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun settingsController(
        users: SettingsFakeUsersRepository = SettingsFakeUsersRepository(),
        auth: SettingsFakeAuthRepository = SettingsFakeAuthRepository(),
        storage: InMemoryKeyValueStorage = InMemoryKeyValueStorage(),
    ): SettingsController {
        val prefs = SettingsPreferencesRepositoryImpl(SettingsLocalDataSource(storage))
        return SettingsController(
            GetMyProfileUseCase(users),
            GetCurrentUserUseCase(auth),
            GetSettingsPreferencesUseCase(prefs),
            SaveSettingsPreferencesUseCase(prefs),
            LogoutUseCase(auth),
        )
    }

    // Settings home

    @Test
    fun settingsShowsProfileNameUsernameAndAvatar() = runTest {
        val state = settingsController().state.value
        assertEquals("Alex Leo", state.displayName)
        assertEquals("alex", state.username)
        assertEquals("https://cdn/alex.png", state.avatarUrl)
        assertFalse(state.isAdmin)
    }

    @Test
    fun settingsFallsBackToAuthUserThenPlaceholder() = runTest {
        val failing = SettingsFakeUsersRepository(meResult = SettingsFakeUsersRepository.apiFailure)
        assertEquals("Alex", settingsController(users = failing).state.value.displayName)

        val none = settingsController(users = failing, auth = SettingsFakeAuthRepository(currentUser = null)).state.value
        assertEquals(SettingsStrings.yourProfile, none.displayName)
        assertEquals(SettingsStrings.usernameFallback, none.username)
    }

    @Test
    fun settingsExposesAdminEntryForAdmins() = runTest {
        val users = SettingsFakeUsersRepository(meResult = Either.Right(SettingsFakeUsersRepository.sampleProfile.copy(role = "ROLE_ADMIN")))
        assertTrue(settingsController(users = users).state.value.isAdmin)

        val authAdmin = SettingsFakeAuthRepository(currentUser = User("1", "root", role = "ADMIN"))
        val failing = SettingsFakeUsersRepository(meResult = SettingsFakeUsersRepository.apiFailure)
        assertTrue(settingsController(users = failing, auth = authAdmin).state.value.isAdmin)
    }

    @Test
    fun settingsTogglesPersistAcrossControllers() = runTest {
        val storage = InMemoryKeyValueStorage()
        val first = settingsController(storage = storage)
        assertTrue(first.state.value.preferences.messageNotifications)
        assertFalse(first.state.value.preferences.messagePreviews)
        first.toggleMessageNotifications(false)
        first.toggleMessagePreviews(true)
        first.toggleCallNotifications(false)
        first.toggleOnlineStatus(false)

        val prefs = settingsController(storage = storage).state.value.preferences
        assertFalse(prefs.messageNotifications)
        assertTrue(prefs.messagePreviews)
        assertFalse(prefs.callNotifications)
        assertFalse(prefs.onlineStatus)
    }

    @Test
    fun settingsRefreshesProfileOnlyWhenRevisited() = runTest {
        val users = SettingsFakeUsersRepository()
        val controller = settingsController(users = users)
        controller.onVisible()
        assertEquals(1, users.calls.count { it == "me" })
        users.meResult = Either.Right(SettingsFakeUsersRepository.sampleProfile.copy(displayName = "Renamed"))
        controller.onVisible()
        assertEquals("Renamed", controller.state.value.displayName)
    }

    @Test
    fun signOutLogsOutAndEmitsSignedOut() = runTest {
        val auth = SettingsFakeAuthRepository()
        val controller = settingsController(auth = auth)
        controller.signOut()
        assertEquals(SettingsEffect.SignedOut, controller.effects.first())
        assertEquals(listOf("logout"), auth.calls)
        assertFalse(controller.state.value.isSigningOut)
    }

    // Edit profile

    private fun editController(users: SettingsFakeUsersRepository, auth: SettingsFakeAuthRepository = SettingsFakeAuthRepository()) =
        EditProfileController(GetMyProfileUseCase(users), GetCurrentUserUseCase(auth), UpdateProfileUseCase(users))

    @Test
    fun editProfilePrefillsFromProfile() = runTest {
        val state = editController(SettingsFakeUsersRepository()).state.value
        assertEquals("Alex Leo", state.displayName)
        assertEquals("alex", state.username)
        assertEquals("alex@leo.chat", state.email)
        assertEquals("Hello there", state.bio)
        assertEquals("https://cdn/alex.png", state.avatarUrl)
        assertEquals("11 / 140", state.bioCounter)
    }

    @Test
    fun editProfileFallsBackToAuthUser() = runTest {
        val state = editController(SettingsFakeUsersRepository(meResult = SettingsFakeUsersRepository.apiFailure)).state.value
        assertEquals("Alex", state.displayName)
        assertEquals("alex@leo.chat", state.email)
        assertEquals("", state.bio)
        assertEquals("Alex", state.avatarName)
    }

    @Test
    fun editProfileBioIsCappedAt140() = runTest {
        val controller = editController(SettingsFakeUsersRepository())
        controller.onBioChange("x".repeat(200))
        assertEquals(140, controller.state.value.bio.length)
    }

    @Test
    fun editProfileSaveSendsTrimmedNameAndBioThenNavigatesBack() = runTest {
        val users = SettingsFakeUsersRepository()
        val controller = editController(users)
        controller.onDisplayNameChange("  New Name ")
        controller.onBioChange("   ")
        controller.save()
        val effects = controller.effects.take(2).toList()
        assertEquals(listOf(SettingsEffect.ShowSuccess(SettingsStrings.profileUpdated), SettingsEffect.NavigateBack), effects)
        assertEquals("New Name", users.lastUpdate?.displayName)
        assertNull(users.lastUpdate?.bio)
        assertFalse(controller.state.value.isSaving)
    }

    @Test
    fun editProfileSaveFailureShowsServerMessage() = runTest {
        val users = SettingsFakeUsersRepository(updateResult = SettingsFakeUsersRepository.apiFailure)
        val controller = editController(users)
        controller.save()
        assertEquals(SettingsEffect.ShowError("Server said no"), controller.effects.first())
    }

    @Test
    fun changePhotoAcknowledgesLikeFlutter() = runTest {
        val controller = editController(SettingsFakeUsersRepository())
        controller.changePhoto()
        assertEquals(SettingsEffect.ShowSuccess(SettingsStrings.photoReady), controller.effects.first())
    }

    // Security

    private fun securityController(
        auth: SettingsFakeAuthRepository = SettingsFakeAuthRepository(),
        users: SettingsFakeUsersRepository = SettingsFakeUsersRepository(),
    ) = SecurityController(ChangePasswordUseCase(auth), DeactivateAccountUseCase(users), LogoutUseCase(auth))

    @Test
    fun changePasswordValidatesFields() = runTest {
        val auth = SettingsFakeAuthRepository()
        val controller = securityController(auth)
        controller.submitPasswordChange()
        assertEquals(SettingsEffect.ShowError(AppStrings.fillRequiredFields), controller.effects.first())

        controller.onCurrentPasswordChange("old")
        controller.onNewPasswordChange("short")
        controller.onConfirmPasswordChange("short")
        controller.submitPasswordChange()
        assertEquals(SettingsEffect.ShowError(AppStrings.passwordTooShort), controller.effects.first())

        controller.onNewPasswordChange("longenough1")
        controller.onConfirmPasswordChange("longenough2")
        controller.submitPasswordChange()
        assertEquals(SettingsEffect.ShowError(AppStrings.passwordsDoNotMatch), controller.effects.first())
        assertTrue(auth.calls.isEmpty())
    }

    @Test
    fun changePasswordSuccessClearsFields() = runTest {
        val auth = SettingsFakeAuthRepository()
        val controller = securityController(auth)
        controller.onCurrentPasswordChange("oldpass")
        controller.onNewPasswordChange("newpassword")
        controller.onConfirmPasswordChange("newpassword")
        controller.submitPasswordChange()
        assertEquals(SettingsEffect.ShowSuccess(SettingsStrings.passwordUpdated), controller.effects.first())
        assertEquals(listOf("change:oldpass:newpassword"), auth.calls)
        val s = controller.state.value
        assertEquals("", s.currentPassword + s.newPassword + s.confirmPassword)
        assertFalse(s.isUpdating)
    }

    @Test
    fun changePasswordFailureKeepsFields() = runTest {
        val auth = SettingsFakeAuthRepository(changePasswordResult = SettingsFakeUsersRepository.apiFailure)
        val controller = securityController(auth)
        controller.onCurrentPasswordChange("oldpass")
        controller.onNewPasswordChange("newpassword")
        controller.onConfirmPasswordChange("newpassword")
        controller.submitPasswordChange()
        assertEquals(SettingsEffect.ShowError("Server said no"), controller.effects.first())
        assertEquals("oldpass", controller.state.value.currentPassword)
    }

    @Test
    fun visibilityTogglesFlip() = runTest {
        val controller = securityController()
        controller.toggleCurrentVisibility()
        controller.toggleNewVisibility()
        controller.toggleConfirmVisibility()
        val s = controller.state.value
        assertFalse(s.obscureCurrent || s.obscureNew || s.obscureConfirm)
    }

    @Test
    fun deactivateLogsOutAndSignsOut() = runTest {
        val auth = SettingsFakeAuthRepository()
        val users = SettingsFakeUsersRepository()
        val controller = securityController(auth, users)
        controller.deactivate()
        assertEquals(SettingsEffect.SignedOut, controller.effects.first())
        assertEquals(listOf("deactivate"), users.calls)
        assertEquals(listOf("logout"), auth.calls)
    }

    @Test
    fun deactivateFailureStaysSignedIn() = runTest {
        val auth = SettingsFakeAuthRepository()
        val users = SettingsFakeUsersRepository(deactivateResult = SettingsFakeUsersRepository.apiFailure)
        val controller = securityController(auth, users)
        controller.deactivate()
        assertEquals(SettingsEffect.ShowError("Server said no"), controller.effects.first())
        assertTrue(auth.calls.isEmpty())
        assertFalse(controller.state.value.isDeactivating)
    }

    // Sessions

    private fun sessionsController(users: SettingsFakeUsersRepository) =
        SessionsController(GetSessionsUseCase(users), RevokeSessionUseCase(users))

    @Test
    fun sessionsLoadOnStart() = runTest {
        val state = sessionsController(SettingsFakeUsersRepository()).state.value
        assertFalse(state.isLoading)
        assertEquals(listOf("s1", "s2"), state.sessions.map { it.sessionId })
        assertNull(state.error)
    }

    @Test
    fun sessionsEmptyAndErrorStates() = runTest {
        val empty = sessionsController(SettingsFakeUsersRepository(sessionsResult = Either.Right(emptyList()))).state.value
        assertTrue(empty.sessions.isEmpty())
        assertNull(empty.error)

        val failed = sessionsController(SettingsFakeUsersRepository(sessionsResult = SettingsFakeUsersRepository.apiFailure)).state.value
        assertEquals("Server said no", failed.error)
        assertFalse(failed.isLoading)
    }

    @Test
    fun revokeShowsSuccessAndReloads() = runTest {
        val users = SettingsFakeUsersRepository()
        val controller = sessionsController(users)
        users.sessionsResult = Either.Right(SettingsFakeUsersRepository.sampleSessions.take(1))
        controller.revoke("s2")
        assertEquals(SettingsEffect.ShowSuccess(SettingsStrings.sessionRevoked), controller.effects.first())
        assertEquals(listOf("sessions", "revoke:s2", "sessions"), users.calls)
        assertEquals(1, controller.state.value.sessions.size)
        assertNull(controller.state.value.revokingId)
    }

    @Test
    fun revokeFailureKeepsList() = runTest {
        val users = SettingsFakeUsersRepository(revokeResult = SettingsFakeUsersRepository.apiFailure)
        val controller = sessionsController(users)
        controller.revoke("s2")
        assertEquals(SettingsEffect.ShowError("Server said no"), controller.effects.first())
        assertEquals(2, controller.state.value.sessions.size)
    }

    // Appearance

    @Test
    fun appearanceSetsAndPersistsThemeMode() = runTest {
        val storage = InMemoryKeyValueStorage()
        val store = ThemeModeStore(storage)
        val controller = AppearanceController(store)
        assertEquals(LeoThemeMode.Light, controller.state.value.mode)
        controller.setMode(LeoThemeMode.Dark)
        assertEquals(LeoThemeMode.Dark, controller.state.value.mode)
        assertEquals(LeoThemeMode.Dark, ThemeModeStore(storage).mode.value)
        controller.setMode(LeoThemeMode.System)
        assertEquals(LeoThemeMode.System, store.mode.value)
    }

    @Test
    fun appearanceSelectsAccent() = runTest {
        val controller = AppearanceController(ThemeModeStore(InMemoryKeyValueStorage()))
        controller.selectAccent(3)
        assertEquals(3, controller.state.value.accentIndex)
    }
}
