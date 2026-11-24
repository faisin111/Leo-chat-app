package com.leochat.feature.settings.presentation.editprofile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.feature.settings.presentation.CollectSettingsEffects
import org.koin.compose.viewmodel.koinViewModel

/** Profile editor. Mirrors `EditProfileScreen`; saving pops back via [onBack]. */
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    controller: EditProfileViewModel = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectSettingsEffects(controller.effects, LocalLeoSnackBar.current, onNavigateBack = onBack)
    EditProfileContent(
        state = state,
        onBack = onBack,
        onSave = controller::save,
        onChangePhoto = controller::changePhoto,
        onDisplayNameChange = controller::onDisplayNameChange,
        onUsernameChange = controller::onUsernameChange,
        onEmailChange = controller::onEmailChange,
        onBioChange = controller::onBioChange,
    )
}
