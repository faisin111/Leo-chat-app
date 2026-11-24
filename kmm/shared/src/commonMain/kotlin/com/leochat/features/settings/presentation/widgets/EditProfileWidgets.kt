package com.leochat.features.settings.presentation.widgets

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircleOutline
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
import com.leochat.core.constants.SettingsStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.branding.LeoAvatar

/** 54dp-radius avatar with the brand camera badge. */
@Composable
internal fun EditableAvatar(name: String, imageUrl: String?) {
    Box {
        LeoAvatar(radius = 54.dp, name = name, imageUrl = imageUrl)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .shadow(6.dp, CircleShape, ambientColor = SettingsColors.avatarBadgeShadow, spotColor = SettingsColors.avatarBadgeShadow)
                .size(32.dp)
                .background(LeoColors.primary, CircleShape)
                .border(2.5.dp, Color.White, CircleShape),
        ) {
            Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

/** Green "photo checked" banner shown above the form. */
@Composable
internal fun PhotoStatusBanner() {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SettingsColors.bannerBackground, shape)
            .border(1.dp, SettingsColors.bannerBorder, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Icon(
            Icons.Rounded.CheckCircleOutline,
            contentDescription = null,
            tint = SettingsColors.bannerTitle,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                SettingsStrings.photoCheckedTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SettingsColors.bannerTitle,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                SettingsStrings.photoCheckedBody,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                color = SettingsColors.bannerBody,
            )
        }
    }
}

/** Label above a white rounded box. Mirrors `_FormFieldWrapper`. */
@Composable
internal fun ProfileFieldWrapper(label: String, content: @Composable () -> Unit) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(16.dp)
    Column(Modifier.fillMaxWidth()) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (palette.isDark) palette.textSecondary else SettingsColors.fieldLabel,
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

/** Borderless single-line input with a trailing icon. */
@Composable
internal fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    iconSize: Dp = 20.dp,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    val palette = LeoThemeTokens.palette
    ProfileFieldWrapper(label) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = profileFieldStyle(FontWeight.SemiBold),
            cursorBrush = SolidColor(LeoColors.primary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                    Box(Modifier.weight(1f).padding(vertical = 14.dp)) { inner() }
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
internal fun ProfileBioField(value: String, counter: String, onValueChange: (String) -> Unit) {
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
internal fun ProfileInfoNote() {
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
