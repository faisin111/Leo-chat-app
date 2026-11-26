package com.leochat.core.designsystem.component.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.theme.SettingsTokens
import com.leochat.core.designsystem.component.branding.LeoAvatar

/** 54dp-radius avatar with the brand camera badge and optional upload/remove spinner. */
@Composable
fun EditableAvatar(
    name: String,
    imageUrl: String?,
    onClick: () -> Unit = {},
    isBusy: Boolean = false,
) {
    val palette = LeoThemeTokens.palette
    Box(
        modifier = Modifier.clickable(enabled = !isBusy, onClick = onClick),
    ) {
        LeoAvatar(radius = 54.dp, name = name, imageUrl = imageUrl)
        if (isBusy) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.42f), CircleShape),
            ) {
                CircularProgressIndicator(
                    strokeWidth = 2.5.dp,
                    color = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .shadow(6.dp, CircleShape, ambientColor = SettingsTokens.avatarBadgeShadow, spotColor = SettingsTokens.avatarBadgeShadow)
                .size(32.dp)
                .background(LeoColors.primary, CircleShape)
                .border(2.5.dp, if (palette.isDark) palette.surface else Color.White, CircleShape),
        ) {
            Icon(Icons.Rounded.CameraAlt, contentDescription = SettingsStrings.changePhoto, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

/** Green "photo checked" banner shown above the form. */
@Composable
fun PhotoStatusBanner() {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(16.dp)
    val background = if (palette.isDark) Color(0xFF064E3B).copy(alpha = 0.35f) else SettingsTokens.bannerBackground
    val border = if (palette.isDark) Color(0xFF047857).copy(alpha = 0.5f) else SettingsTokens.bannerBorder
    val titleColor = if (palette.isDark) Color(0xFF6EE7B7) else SettingsTokens.bannerTitle
    val bodyColor = if (palette.isDark) Color(0xFFA7F3D0) else SettingsTokens.bannerBody

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, shape)
            .border(1.dp, border, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Icon(
            Icons.Rounded.CheckCircleOutline,
            contentDescription = null,
            tint = titleColor,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                SettingsStrings.photoCheckedTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                SettingsStrings.photoCheckedBody,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Normal,
                color = bodyColor,
            )
        }
    }
}

/** Label above a white rounded box. Mirrors `_FormFieldWrapper`. */
@Composable
fun ProfileFieldWrapper(label: String, content: @Composable () -> Unit) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(16.dp)
    Column(Modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (palette.isDark) palette.textSecondary else SettingsTokens.fieldLabel,
        )
        Spacer(Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.surface, shape)
                .border(1.dp, palette.border, shape),
        ) { content() }
    }
}

@Composable
private fun profileFieldStyle(weight: FontWeight, lineHeight: Float? = null): TextStyle =
    LocalTextStyle.current.merge(
        TextStyle(
            fontSize = 15.sp,
            fontWeight = weight,
            color = LeoThemeTokens.palette.textPrimary,
            lineHeight = lineHeight?.em ?: LocalTextStyle.current.lineHeight,
        ),
    )

/** Borderless single-line input with optional read-only mode, badge, and trailing icon. */
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    iconSize: Dp = 20.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true,
    placeholder: String? = null,
    trailingBadge: (@Composable () -> Unit)? = null,
) {
    val palette = LeoThemeTokens.palette
    val textColor = if (enabled) palette.textPrimary else palette.textSecondary
    ProfileFieldWrapper(label) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = !enabled,
            singleLine = true,
            textStyle = LocalTextStyle.current.merge(
                TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                ),
            ),
            cursorBrush = SolidColor(LeoColors.primary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                    Box(Modifier.weight(1f).padding(vertical = 14.dp)) {
                        if (value.isEmpty() && !placeholder.isNullOrBlank()) {
                            Text(
                                placeholder,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = palette.textMuted,
                            )
                        }
                        inner()
                    }
                    if (trailingBadge != null) {
                        trailingBadge()
                        Spacer(Modifier.width(4.dp))
                    }
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = palette.textSecondary, modifier = Modifier.size(iconSize))
                    }
                }
            },
        )
    }
}

/** Three-line bio box with a `n / 140` counter. */
@Composable
fun ProfileBioField(value: String, counter: String, onValueChange: (String) -> Unit) {
    val palette = LeoThemeTokens.palette
    ProfileFieldWrapper(SettingsStrings.aboutYou) {
        Column(Modifier.fillMaxWidth()) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                minLines = 3,
                maxLines = 3,
                textStyle = profileFieldStyle(FontWeight.Medium, lineHeight = 1.4f),
                cursorBrush = SolidColor(LeoColors.primary),
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 6.dp),
            )
            Text(
                counter,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textMuted,
                modifier = Modifier.align(Alignment.End).padding(end = 14.dp, bottom = 10.dp),
            )
        }
    }
}

/** Small info line under the form. */
@Composable
fun ProfileInfoNote() {
    val palette = LeoThemeTokens.palette
    Row(Modifier.fillMaxWidth()) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = palette.textSecondary,
            modifier = Modifier.padding(top = 2.dp).size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            SettingsStrings.profileNote,
            fontSize = 12.sp,
            lineHeight = 1.4.em,
            color = palette.textSecondary,
            modifier = Modifier.weight(1f),
        )
    }
}
