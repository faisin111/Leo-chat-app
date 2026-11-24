package com.leochat.features.settings.presentation.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.SettingsStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.buttons.LeoPrimaryButton
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.core.widgets.common.layout.LeoScreen
import com.leochat.features.settings.presentation.controllers.EditProfileController
import com.leochat.features.settings.presentation.widgets.CollectSettingsEffects
import com.leochat.features.settings.presentation.widgets.EditableAvatar
import com.leochat.features.settings.presentation.widgets.PhotoStatusBanner
import com.leochat.features.settings.presentation.widgets.ProfileBioField
import com.leochat.features.settings.presentation.widgets.ProfileInfoNote
import com.leochat.features.settings.presentation.widgets.ProfileTextField
import com.leochat.features.settings.presentation.widgets.SettingsAppBar
import com.leochat.features.settings.presentation.widgets.SettingsTextScope
import com.leochat.features.settings.presentation.widgets.settingsPressable
import org.koin.compose.viewmodel.koinViewModel

/** Profile editor. Mirrors `EditProfilePage`; saving pops back via [onBack]. */
@Composable
fun EditProfilePage(
    onBack: () -> Unit,
    controller: EditProfileController = koinViewModel(),
) {
    val state by controller.state.collectAsStateWithLifecycle()
    CollectSettingsEffects(controller.effects, LocalLeoSnackBar.current, onNavigateBack = onBack)
    val palette = LeoThemeTokens.palette

    LeoScreen(background = palette.background) {
        SettingsTextScope {
            Column(Modifier.fillMaxSize()) {
                SettingsAppBar(
                    title = SettingsStrings.editProfile,
                    onBack = onBack,
                    background = palette.background,
                    titleSize = 22.sp,
                    titleWeight = FontWeight.Black,
                    titleLetterSpacing = (-0.5).sp,
                ) {
                    if (state.isSaving) {
                        Box(Modifier.padding(12.dp)) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = LeoColors.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    } else {
                        IconButton(onClick = controller::save) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = SettingsStrings.save,
                                tint = palette.textPrimary,
                                modifier = Modifier.size(26.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    EditableAvatar(name = state.avatarName, imageUrl = state.avatarUrl)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        SettingsStrings.changePhoto,
                        color = LeoColors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.settingsPressable(onClick = controller::changePhoto),
                    )
                    Spacer(Modifier.height(22.dp))
                    PhotoStatusBanner()
                    Spacer(Modifier.height(22.dp))

                    ProfileTextField(
                        SettingsStrings.displayName,
                        state.displayName,
                        controller::onDisplayNameChange,
                        Icons.Rounded.PersonOutline,
                        iconSize = 22.dp,
                    )
                    Spacer(Modifier.height(16.dp))
                    ProfileTextField(
                        SettingsStrings.username,
                        state.username,
                        controller::onUsernameChange,
                        Icons.Rounded.AlternateEmail,
                    )
                    Spacer(Modifier.height(16.dp))
                    ProfileTextField(
                        SettingsStrings.emailAddress,
                        state.email,
                        controller::onEmailChange,
                        Icons.Rounded.MailOutline,
                        keyboardType = KeyboardType.Email,
                    )
                    Spacer(Modifier.height(16.dp))
                    ProfileBioField(state.bio, state.bioCounter, controller::onBioChange)
                    Spacer(Modifier.height(16.dp))
                    ProfileInfoNote()
                    Spacer(Modifier.height(28.dp))
                    LeoPrimaryButton(
                        label = SettingsStrings.saveChanges,
                        onClick = controller::save,
                        isLoading = state.isSaving,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
