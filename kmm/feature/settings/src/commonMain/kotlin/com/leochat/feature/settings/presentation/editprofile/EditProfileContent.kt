package com.leochat.feature.settings.presentation.editprofile

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.component.buttons.LeoPrimaryButton
import com.leochat.core.designsystem.component.feedback.LeoShimmer
import com.leochat.core.designsystem.component.feedback.LeoSkeleton
import com.leochat.core.designsystem.component.feedback.LeoSkeletonCircle
import com.leochat.core.designsystem.component.layout.LeoScreen
import com.leochat.core.designsystem.component.settings.EditableAvatar
import com.leochat.core.designsystem.component.settings.PhotoStatusBanner
import com.leochat.core.designsystem.component.settings.ProfileBioField
import com.leochat.core.designsystem.component.settings.ProfileInfoNote
import com.leochat.core.designsystem.component.settings.ProfileTextField
import com.leochat.core.designsystem.component.settings.SettingsAppBar
import com.leochat.core.designsystem.component.settings.SettingsTextScope
import com.leochat.core.designsystem.component.settings.settingsPressable
import com.leochat.feature.settings.presentation.settings.EditProfileUiState

/** Stateless profile-editor layout driven by [state]. */
@Composable
fun EditProfileContent(
    state: EditProfileUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onChangePhoto: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
) {
    val palette = LeoThemeTokens.palette

    LeoScreen(background = palette.background) {
        SettingsTextScope {
            Column(Modifier.fillMaxSize()) {
                SettingsAppBar(
                    title = SettingsStrings.editProfile,
                    onBack = onBack,
                    background = palette.background,
                    titleSize = 21.sp,
                    titleWeight = FontWeight.Bold,
                    titleLetterSpacing = (-0.3).sp,
                ) {
                    if (state.isSaving) {
                        Box(Modifier.padding(12.dp)) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = LeoColors.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    } else if (!state.isLoading) {
                        IconButton(onClick = onSave) {
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
                if (state.isLoading) {
                    EditProfileShimmerSkeleton()
                } else {
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
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.settingsPressable(onClick = onChangePhoto),
                        )
                        Spacer(Modifier.height(22.dp))
                        PhotoStatusBanner()
                        Spacer(Modifier.height(22.dp))

                        ProfileTextField(
                            SettingsStrings.displayName,
                            state.displayName,
                            onDisplayNameChange,
                            Icons.Rounded.PersonOutline,
                            iconSize = 22.dp,
                        )
                        Spacer(Modifier.height(16.dp))
                        ProfileTextField(
                            SettingsStrings.username,
                            state.username,
                            onUsernameChange,
                            Icons.Rounded.AlternateEmail,
                        )
                        Spacer(Modifier.height(16.dp))
                        ProfileTextField(
                            SettingsStrings.emailAddress,
                            state.email,
                            onEmailChange,
                            Icons.Rounded.MailOutline,
                            keyboardType = KeyboardType.Email,
                        )
                        Spacer(Modifier.height(16.dp))
                        ProfileBioField(state.bio, state.bioCounter, onBioChange)
                        Spacer(Modifier.height(16.dp))
                        ProfileInfoNote()
                        Spacer(Modifier.height(28.dp))
                        LeoPrimaryButton(
                            label = SettingsStrings.saveChanges,
                            onClick = onSave,
                            isLoading = state.isSaving,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProfileShimmerSkeleton() {
    LeoShimmer(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            LeoSkeletonCircle(size = 108.dp)
            Spacer(Modifier.height(14.dp))
            LeoSkeleton(modifier = Modifier.width(130.dp).height(16.dp), cornerRadius = 8.dp)
            Spacer(Modifier.height(24.dp))
            LeoSkeleton(modifier = Modifier.fillMaxWidth().height(60.dp), cornerRadius = 16.dp)
            Spacer(Modifier.height(22.dp))

            repeat(3) {
                Column(Modifier.fillMaxWidth()) {
                    LeoSkeleton(modifier = Modifier.width(90.dp).height(14.dp), cornerRadius = 6.dp)
                    Spacer(Modifier.height(8.dp))
                    LeoSkeleton(modifier = Modifier.fillMaxWidth().height(52.dp), cornerRadius = 16.dp)
                    Spacer(Modifier.height(16.dp))
                }
            }

            Column(Modifier.fillMaxWidth()) {
                LeoSkeleton(modifier = Modifier.width(60.dp).height(14.dp), cornerRadius = 6.dp)
                Spacer(Modifier.height(8.dp))
                LeoSkeleton(modifier = Modifier.fillMaxWidth().height(96.dp), cornerRadius = 16.dp)
                Spacer(Modifier.height(16.dp))
            }

            LeoSkeleton(modifier = Modifier.fillMaxWidth().height(42.dp), cornerRadius = 12.dp)
            Spacer(Modifier.height(28.dp))
            LeoSkeleton(modifier = Modifier.fillMaxWidth().height(52.dp), cornerRadius = 16.dp)
            Spacer(Modifier.height(24.dp))
        }
    }
}
