package com.leochat.feature.settings.presentation.editprofile

import androidx.lifecycle.viewModelScope
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.leochat.feature.auth.domain.usecase.ResendVerificationUseCase
import com.leochat.feature.media.domain.model.PickedAttachment
import com.leochat.feature.media.domain.usecase.UploadAttachmentUseCase
import com.leochat.feature.settings.presentation.SettingsEffectViewModel
import com.leochat.feature.settings.presentation.settings.EditProfileUiState
import com.leochat.feature.settings.presentation.settings.SettingsEffect
import com.leochat.feature.users.domain.model.ProfileUpdate
import com.leochat.feature.users.domain.usecase.DeleteProfilePictureUseCase
import com.leochat.feature.users.domain.usecase.GetMyProfileUseCase
import com.leochat.feature.users.domain.usecase.UpdateProfileUseCase
import kotlinx.coroutines.launch

/** Profile editor with real avatar upload/delete, editable bio/phone/region/age, and email verification resend. */
class EditProfileViewModel(
    private val getMyProfile: GetMyProfileUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val deleteProfilePicture: DeleteProfilePictureUseCase,
    private val uploadAttachment: UploadAttachmentUseCase,
    private val resendVerification: ResendVerificationUseCase,
) : SettingsEffectViewModel<EditProfileUiState>(EditProfileUiState(isLoading = true)) {

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
                    phoneNumber = s.phoneNumber.ifEmpty { profile?.phoneNumber.orEmpty() },
                    region = s.region.ifEmpty { profile?.region.orEmpty() },
                    age = s.age.ifEmpty { profile?.age?.toString().orEmpty() },
                    bio = s.bio.ifEmpty { profile?.bio.orEmpty().take(SettingsStrings.bioMaxLength) },
                    avatarUrl = profile?.profilePictureUrl ?: user?.avatarUrl,
                    isEmailVerified = profile?.isEmailVerified == true,
                    isLoading = false,
                )
            }
        }
    }

    fun onDisplayNameChange(value: String) = update { it.copy(displayName = value) }
    fun onUsernameChange(value: String) = update { it.copy(username = value) }
    fun onEmailChange(value: String) = update { it.copy(email = value) }
    fun onPhoneNumberChange(value: String) = update { it.copy(phoneNumber = value) }
    fun onRegionChange(value: String) = update { it.copy(region = value) }
    fun onAgeChange(value: String) = update { it.copy(age = value.filter(Char::isDigit).take(3)) }
    fun onBioChange(value: String) = update { it.copy(bio = value.take(SettingsStrings.bioMaxLength)) }

    fun onPhotoPicked(attachment: PickedAttachment) {
        if (state.value.isUploadingPhoto || state.value.isRemovingPhoto) return
        update { it.copy(isUploadingPhoto = true) }
        viewModelScope.launch {
            val result = uploadAttachment(attachment.uriOrPath, attachment.fileName, attachment.mimeType)
            result.fold(
                ifLeft = { failure ->
                    update { it.copy(isUploadingPhoto = false) }
                    emit(SettingsEffect.ShowError(failure.message))
                },
                ifRight = { uploaded ->
                    update {
                        it.copy(
                            avatarUrl = uploaded.url,
                            uploadedStorageKey = uploaded.storageKey,
                            isUploadingPhoto = false,
                        )
                    }
                    emit(SettingsEffect.ShowSuccess(SettingsStrings.photoReady))
                },
            )
        }
    }

    fun onPhotoPickError(message: String) = emit(SettingsEffect.ShowError(message))

    fun removePhoto() {
        val s = state.value
        if (s.avatarUrl.isNullOrBlank() && s.uploadedStorageKey.isNullOrBlank()) return
        if (s.isUploadingPhoto || s.isRemovingPhoto) return
        update { it.copy(isRemovingPhoto = true) }
        viewModelScope.launch {
            val result = deleteProfilePicture()
            result.fold(
                ifLeft = { failure ->
                    update { it.copy(isRemovingPhoto = false) }
                    emit(SettingsEffect.ShowError(failure.message))
                },
                ifRight = {
                    update {
                        it.copy(
                            avatarUrl = null,
                            uploadedStorageKey = null,
                            isRemovingPhoto = false,
                        )
                    }
                    emit(SettingsEffect.ShowSuccess(SettingsStrings.photoRemoved))
                },
            )
        }
    }

    fun resendVerificationEmail() {
        val s = state.value
        if (s.isEmailVerified || s.isResendingVerification || s.email.isBlank()) return
        update { it.copy(isResendingVerification = true) }
        viewModelScope.launch {
            val result = resendVerification(s.email)
            update { it.copy(isResendingVerification = false) }
            result.fold(
                ifLeft = { emit(SettingsEffect.ShowError(it.message)) },
                ifRight = { emit(SettingsEffect.ShowSuccess(SettingsStrings.verificationSent)) },
            )
        }
    }

    fun save() {
        val s = state.value
        if (s.isSaving || s.isUploadingPhoto || s.isRemovingPhoto) return
        val ageText = s.age.trim()
        val parsedAge = if (ageText.isEmpty()) {
            null
        } else {
            val value = ageText.toIntOrNull()
            if (value == null || value !in 13..120) {
                emit(SettingsEffect.ShowError(SettingsStrings.invalidAgeError))
                return
            }
            value
        }

        val name = s.displayName.trim()
        val bio = s.bio.trim()
        val phone = s.phoneNumber.trim()
        val region = s.region.trim()
        update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val result = updateProfile(
                ProfileUpdate(
                    displayName = name.ifEmpty { null },
                    profilePictureStorageKey = s.uploadedStorageKey,
                    phoneNumber = phone.ifEmpty { null },
                    bio = bio.ifEmpty { null },
                    age = parsedAge,
                    region = region.ifEmpty { null },
                ),
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
