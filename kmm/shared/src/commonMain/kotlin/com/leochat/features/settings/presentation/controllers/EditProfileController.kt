package com.leochat.features.settings.presentation.controllers

import androidx.lifecycle.viewModelScope
import com.leochat.core.constants.SettingsStrings
import com.leochat.features.auth.domain.usecases.GetCurrentUserUseCase
import com.leochat.features.settings.presentation.states.EditProfileState
import com.leochat.features.settings.presentation.states.SettingsEffect
import com.leochat.features.users.domain.entities.ProfileUpdate
import com.leochat.features.users.domain.usecases.GetMyProfileUseCase
import com.leochat.features.users.domain.usecases.UpdateProfileUseCase
import kotlinx.coroutines.launch

/** Profile editor. Mirrors `_EditProfilePageState`: prefill empty fields, save name and bio. */
class EditProfileController(
    private val getMyProfile: GetMyProfileUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val updateProfile: UpdateProfileUseCase,
) : SettingsEffectController<EditProfileState>(EditProfileState()) {

    init {
        viewModelScope.launch {
            val user = getCurrentUser().getOrNull()
            val profile = getMyProfile().getOrNull()
            // Profile values win, the signed-in user fills gaps; fields the user already typed are kept.
            update { s ->
                s.copy(
                    displayName = s.displayName.ifEmpty { profile?.displayName ?: user?.displayName.orEmpty() },
                    username = s.username.ifEmpty { profile?.username ?: user?.username.orEmpty() },
                    email = s.email.ifEmpty { profile?.email ?: user?.email.orEmpty() },
                    bio = s.bio.ifEmpty { profile?.bio.orEmpty().take(SettingsStrings.bioMaxLength) },
                    avatarUrl = profile?.profilePictureUrl ?: user?.avatarUrl,
                )
            }
        }
    }

    fun onDisplayNameChange(value: String) = update { it.copy(displayName = value) }
    fun onUsernameChange(value: String) = update { it.copy(username = value) }
    fun onEmailChange(value: String) = update { it.copy(email = value) }
    fun onBioChange(value: String) = update { it.copy(bio = value.take(SettingsStrings.bioMaxLength)) }

    /** Flutter only acknowledges the tap; photo upload is not wired there. */
    fun changePhoto() = emit(SettingsEffect.ShowSuccess(SettingsStrings.photoReady))

    fun save() {
        val s = state.value
        if (s.isSaving) return
        val name = s.displayName.trim()
        val bio = s.bio.trim()
        update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = updateProfile(
                ProfileUpdate(displayName = name.ifEmpty { null }, bio = bio.ifEmpty { null }),
            )
            update { it.copy(isSaving = false) }
            result.fold(
                ifLeft = { emit(SettingsEffect.ShowError(it.message)) },
                ifRight = {
                    emit(SettingsEffect.ShowSuccess(SettingsStrings.profileUpdated))
                    emit(SettingsEffect.NavigateBack)
                },
            )
        }
    }
}
