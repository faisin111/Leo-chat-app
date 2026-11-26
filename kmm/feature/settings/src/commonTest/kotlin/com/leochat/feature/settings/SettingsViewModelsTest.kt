package com.leochat.feature.settings

import com.leochat.core.common.constants.AppStrings
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.common.result.Either
import com.leochat.core.platform.storage.InMemoryKeyValueStorage
import com.leochat.core.platform.system.NoopSystemSettingsHelper
import com.leochat.core.designsystem.theme.LeoThemeMode
import com.leochat.core.designsystem.theme.ThemeModeStore
import com.leochat.feature.auth.domain.model.User
import com.leochat.feature.auth.domain.usecase.ChangePasswordUseCase
import com.leochat.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.leochat.feature.auth.domain.usecase.LogoutUseCase
import com.leochat.feature.auth.domain.usecase.ResendVerificationUseCase
import com.leochat.feature.media.domain.model.PickedAttachment
import com.leochat.feature.media.domain.usecase.UploadAttachmentUseCase
import com.leochat.feature.settings.data.local.SettingsLocalDataSource
import com.leochat.feature.settings.data.repository.SettingsPreferencesRepositoryImpl
import com.leochat.feature.settings.domain.usecase.GetBlockedUsersUseCase
import com.leochat.feature.settings.domain.usecase.GetSettingsPreferencesUseCase
import com.leochat.feature.settings.domain.usecase.SaveSettingsPreferencesUseCase
import com.leochat.feature.settings.presentation.appearance.AppearanceViewModel
import com.leochat.feature.settings.presentation.blocked.BlockedUsersViewModel
import com.leochat.feature.settings.presentation.editprofile.EditProfileViewModel
import com.leochat.feature.settings.presentation.security.SecurityViewModel
import com.leochat.feature.settings.presentation.sessions.SessionsViewModel
import com.leochat.feature.settings.presentation.settings.SettingsViewModel
import com.leochat.feature.settings.presentation.settings.SettingsEffect
import com.leochat.feature.users.domain.usecase.DeactivateAccountUseCase
import com.leochat.feature.users.domain.usecase.DeleteProfilePictureUseCase
import com.leochat.feature.users.domain.usecase.GetMyProfileUseCase
import com.leochat.feature.users.domain.usecase.GetSessionsUseCase
import com.leochat.feature.users.domain.usecase.RevokeSessionUseCase
import com.leochat.feature.users.domain.usecase.UpdateProfileUseCase
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
        blocked: SettingsFakeBlockedUsersRepository = SettingsFakeBlockedUsersRepository(),
    ): SettingsViewModel {
        val prefs = SettingsPreferencesRepositoryImpl(SettingsLocalDataSource(storage))
        return SettingsViewModel(
            GetMyProfileUseCase(users),
            GetCurrentUserUseCase(auth),
            GetSettingsPreferencesUseCase(prefs),
            SaveSettingsPreferencesUseCase(prefs),
            GetBlockedUsersUseCase(blocked),
            LogoutUseCase(auth),
            NoopSystemSettingsHelper,
        )
    }

    // Settings home

    @Test
    fun settingsShowsProfileNameUsernameAvatarAndBlockedCount() = runTest {
        val state = settingsController().state.value
        assertEquals("Alex Leo", state.displayName)
        assertEquals("alex", state.username)
        assertEquals("https://cdn/alex.png", state.avatarUrl)
        assertEquals(2, state.blockedCount)
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
    fun settingsTogglesPersistAcrossControllers() = runTest {
        val storage = InMemoryKeyValueStorage()
        val first = settingsController(storage = storage)
        assertTrue(first.state.value.preferences.messageNotifications)
        assertFalse(first.state.value.preferences.messagePreviews)
        first.toggleMessageNotifications(false)
        first.toggleMessagePreviews(true)
        first.toggleCallNotifications(false)
        first.toggleOnlineStatus(false)
        first.setProfileVisibility(SettingsStrings.profileVisibilityNobody)

        val second = settingsController(storage = storage).state.value
        val prefs = second.preferences
        assertFalse(prefs.messageNotifications)
        assertTrue(prefs.messagePreviews)
        assertFalse(prefs.callNotifications)
        assertFalse(prefs.onlineStatus)
        assertEquals(SettingsStrings.profileVisibilityNobody, second.profileVisibility)
    }

    @Test
    fun settingsSearchAndSheetsUpdateState() = runTest {
        val controller = settingsController()
        controller.toggleSearch()
        controller.onSearchQueryChange("notif")
        assertTrue(controller.state.value.isSearching)
        assertEquals("notif", controller.state.value.searchQuery)
        controller.toggleSearch()
        assertFalse(controller.state.value.isSearching)
        assertEquals("", controller.state.value.searchQuery)

        controller.openVisibilitySheet()
        assertTrue(controller.state.value.showVisibilitySheet)
        controller.setProfileVisibility(SettingsStrings.profileVisibilityEveryone)
        assertFalse(controller.state.value.showVisibilitySheet)
        assertEquals(SettingsStrings.profileVisibilityEveryone, controller.state.value.profileVisibility)

        controller.openHelpSheet()
        assertTrue(controller.state.value.showHelpSheet)
        controller.dismissHelpSheet()
        assertFalse(controller.state.value.showHelpSheet)
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

    private fun editController(
        users: SettingsFakeUsersRepository,
        auth: SettingsFakeAuthRepository = SettingsFakeAuthRepository(),
        media: SettingsFakeMediaRepository = SettingsFakeMediaRepository(),
    ) = EditProfileViewModel(
        GetMyProfileUseCase(users),
        GetCurrentUserUseCase(auth),
        UpdateProfileUseCase(users),
        DeleteProfilePictureUseCase(users),
        UploadAttachmentUseCase(media),
        ResendVerificationUseCase(auth),
    )

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
    fun editProfileSaveSendsFieldsAndUploadedPhotoKeyThenNavigatesBack() = runTest {
        val users = SettingsFakeUsersRepository()
        val media = SettingsFakeMediaRepository()
        val controller = editController(users, media = media)
        controller.onPhotoPicked(PickedAttachment("file:///tmp/p.jpg", "p.jpg", "image/jpeg", 256L))
        assertEquals("https://cdn/uploaded.jpg", controller.state.value.avatarUrl)
        assertEquals("avatars/uploaded.jpg", controller.state.value.uploadedStorageKey)

        controller.onDisplayNameChange("  New Name ")
        controller.onPhoneNumberChange(" +15550199 ")
        controller.onRegionChange(" Colombo ")
        controller.onAgeChange("27")
        controller.onBioChange("   ")
        controller.save()
        val effects = controller.effects.take(3).toList()
        assertEquals(
            listOf(
                SettingsEffect.ShowSuccess(SettingsStrings.photoReady),
                SettingsEffect.ShowSuccess(SettingsStrings.profileUpdated),
                SettingsEffect.NavigateBack,
            ),
            effects,
        )
        assertEquals("New Name", users.lastUpdate?.displayName)
        assertEquals("avatars/uploaded.jpg", users.lastUpdate?.profilePictureStorageKey)
        assertEquals("+15550199", users.lastUpdate?.phoneNumber)
        assertEquals("Colombo", users.lastUpdate?.region)
        assertEquals(27, users.lastUpdate?.age)
        assertNull(users.lastUpdate?.bio)
        assertFalse(controller.state.value.isSaving)
    }

    @Test
    fun editProfileRejectsInvalidAgeBeforeNetworkCall() = runTest {
        val users = SettingsFakeUsersRepository()
        val controller = editController(users)
        controller.onAgeChange("10")
        controller.save()
        assertEquals(SettingsEffect.ShowError(SettingsStrings.invalidAgeError), controller.effects.first())
        assertNull(users.lastUpdate)
    }

    @Test
    fun editProfileRemovePhotoCallsDeleteEndpointAndClearsAvatar() = runTest {
        val users = SettingsFakeUsersRepository()
        val controller = editController(users)
        controller.removePhoto()
        assertNull(controller.state.value.avatarUrl)
        assertNull(controller.state.value.uploadedStorageKey)
        assertTrue("deletePhoto" in users.calls)
        assertEquals(SettingsEffect.ShowSuccess(SettingsStrings.photoRemoved), controller.effects.first())
    }

    @Test
    fun editProfileResendVerificationCallsAuthUseCase() = runTest {
        val auth = SettingsFakeAuthRepository()
        val controller = editController(SettingsFakeUsersRepository(), auth = auth)
        controller.resendVerificationEmail()
        assertEquals(listOf("resend:alex@leo.chat"), auth.calls)
        assertEquals(SettingsEffect.ShowSuccess(SettingsStrings.verificationSent), controller.effects.first())
    }

    @Test
    fun editProfileSaveFailureShowsServerMessage() = runTest {
        val users = SettingsFakeUsersRepository(updateResult = SettingsFakeUsersRepository.apiFailure)
        val controller = editController(users)
        controller.save()
        assertEquals(SettingsEffect.ShowError("Server said no"), controller.effects.first())
    }

    @Test
    fun blockedUsersViewModelLoadsSuccessEmptyAndErrorStates() = runTest {
        val vm = BlockedUsersViewModel(GetBlockedUsersUseCase(SettingsFakeBlockedUsersRepository()))
        assertFalse(vm.state.value.isLoading)
        assertEquals(listOf("Mallory", "Eve"), vm.state.value.users.map { it.effectiveName })

        val failedRepo = SettingsFakeBlockedUsersRepository(result = SettingsFakeUsersRepository.apiFailure)
        val failedVm = BlockedUsersViewModel(GetBlockedUsersUseCase(failedRepo))
        assertEquals("Server said no", failedVm.state.value.error)

        failedRepo.result = Either.Right(emptyList())
        failedVm.refresh()
        assertNull(failedVm.state.value.error)
        assertTrue(failedVm.state.value.users.isEmpty())
    }

    // Security

    private fun securityController(
        auth: SettingsFakeAuthRepository = SettingsFakeAuthRepository(),
        users: SettingsFakeUsersRepository = SettingsFakeUsersRepository(),
    ) = SecurityViewModel(ChangePasswordUseCase(auth), DeactivateAccountUseCase(users), LogoutUseCase(auth))

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
        SessionsViewModel(GetSessionsUseCase(users), RevokeSessionUseCase(users))

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
        val controller = AppearanceViewModel(store)
        assertEquals(LeoThemeMode.Light, controller.state.value.mode)
        controller.setMode(LeoThemeMode.Dark)
        assertEquals(LeoThemeMode.Dark, controller.state.value.mode)
        assertEquals(LeoThemeMode.Dark, ThemeModeStore(storage).mode.value)
        controller.setMode(LeoThemeMode.System)
        assertEquals(LeoThemeMode.System, store.mode.value)
    }

    @Test
    fun appearanceSelectsAccent() = runTest {
        val controller = AppearanceViewModel(ThemeModeStore(InMemoryKeyValueStorage()))
        controller.selectAccent(3)
        assertEquals(3, controller.state.value.accentIndex)
    }
}
