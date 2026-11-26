package com.leochat.feature.settings.presentation.editprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    onRemovePhoto: () -> Unit,
    onResendVerification: () -> Unit,
    onDisplayNameChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onRegionChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val photoBusy = state.isUploadingPhoto || state.isRemovingPhoto

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
                        IconButton(onClick = onSave, enabled = !photoBusy) {
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
                        EditableAvatar(
                            name = state.avatarName,
                            imageUrl = state.avatarUrl,
                            onClick = onChangePhoto,
                            isBusy = photoBusy,
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                when {
                                    state.isUploadingPhoto -> SettingsStrings.uploadingPhoto
                                    state.isRemovingPhoto -> SettingsStrings.removingPhoto
                                    else -> SettingsStrings.changePhoto
                                },
                                color = LeoColors.primary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.settingsPressable(enabled = !photoBusy, onClick = onChangePhoto),
                            )
                            if (!state.avatarUrl.isNullOrBlank() && !photoBusy) {
                                Text(
                                    SettingsStrings.removePhoto,
                                    color = palette.textSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.settingsPressable(onClick = onRemovePhoto),
                                )
                            }
                        }
                        if (!state.uploadedStorageKey.isNullOrBlank()) {
                            Spacer(Modifier.height(18.dp))
                            PhotoStatusBanner()
                        }
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
                            onValueChange = {},
                            Icons.Rounded.AlternateEmail,
                            enabled = false,
                        )
                        Spacer(Modifier.height(16.dp))
                        ProfileTextField(
                            SettingsStrings.emailAddress,
                            state.email,
                            onValueChange = {},
                            Icons.Rounded.MailOutline,
                            keyboardType = KeyboardType.Email,
                            enabled = false,
                            trailingBadge = {
                                if (state.email.isNotBlank()) {
                                    EmailStatusBadge(verified = state.isEmailVerified)
                                }
                            },
                        )
                        if (!state.isEmailVerified && state.email.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth()) {
                                Text(
                                    SettingsStrings.resendVerification,
                                    color = LeoColors.primary,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.settingsPressable(
                                        enabled = !state.isResendingVerification,
                                        onClick = onResendVerification,
                                    ),
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        ProfileTextField(
                            SettingsStrings.phoneNumber,
                            state.phoneNumber,
                            onPhoneNumberChange,
                            Icons.Rounded.Phone,
                            keyboardType = KeyboardType.Phone,
                            placeholder = SettingsStrings.phoneNumberHint,
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(Modifier.weight(1.3f)) {
                                ProfileTextField(
                                    SettingsStrings.region,
                                    state.region,
                                    onRegionChange,
                                    Icons.Rounded.LocationOn,
                                    placeholder = SettingsStrings.regionHint,
                                )
                            }
                            Box(Modifier.weight(0.9f)) {
                                ProfileTextField(
                                    SettingsStrings.age,
                                    state.age,
                                    onAgeChange,
                                    Icons.Rounded.Cake,
                                    keyboardType = KeyboardType.Number,
                                    placeholder = SettingsStrings.ageHint,
                                )
                            }
                        }
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
private fun EmailStatusBadge(verified: Boolean) {
    val palette = LeoThemeTokens.palette
    val bg = if (verified) Color(0xFF10B981).copy(alpha = if (palette.isDark) 0.22f else 0.14f) else Color(0xFFF59E0B).copy(alpha = 0.16f)
    val fg = if (verified) Color(0xFF059669) else Color(0xFFD97706)
    Text(
        text = if (verified) SettingsStrings.emailVerifiedBadge else SettingsStrings.emailUnverifiedBadge,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = fg,
        modifier = Modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
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
