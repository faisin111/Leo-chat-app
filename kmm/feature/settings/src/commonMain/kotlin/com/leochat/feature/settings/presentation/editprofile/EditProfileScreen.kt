package com.leochat.feature.settings.presentation.editprofile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.media.presentation.picker.rememberAttachmentPicker
import com.leochat.feature.settings.presentation.CollectSettingsEffects
import org.koin.compose.viewmodel.koinViewModel

/** Profile editor with real image picker upload/delete. */
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    controller: EditProfileViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    val picker = rememberAttachmentPicker(
        onPicked = controller::onPhotoPicked,
        onError = controller::onPhotoPickError,
    )
    CollectSettingsEffects(controller.effects, LocalLeoSnackBar.current, onNavigateBack = onBack)
    EditProfileContent(
        state = state,
        onBack = onBack,
        onSave = controller::save,
        onChangePhoto = picker::pickImage,
        onRemovePhoto = controller::removePhoto,
        onResendVerification = controller::resendVerificationEmail,
        onDisplayNameChange = controller::onDisplayNameChange,
        onPhoneNumberChange = controller::onPhoneNumberChange,
        onRegionChange = controller::onRegionChange,
        onAgeChange = controller::onAgeChange,
        onBioChange = controller::onBioChange,
    )
}
