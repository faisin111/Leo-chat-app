package com.leochat.features.settings.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.SettingsStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoPalette
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.buttons.leoPressable
import com.leochat.core.widgets.common.feedback.LeoSnackBarController
import com.leochat.features.settings.presentation.states.SettingsEffect
import kotlinx.coroutines.flow.Flow

internal val SettingsCardShape = RoundedCornerShape(22.dp)

/** Flutter text uses the font's natural line height; drop Material's fixed body line height. */
@Composable
internal fun SettingsTextScope(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(lineHeight = TextUnit.Unspecified),
        content = content,
    )
}

/** Tap with the shared LeoPressable scale and no ripple. */
internal fun Modifier.settingsPressable(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    this
        .leoPressable(interaction, enabled)
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
}

/** Routes controller effects to the snack bar and navigation callbacks. */
@Composable
internal fun CollectSettingsEffects(
    effects: Flow<SettingsEffect>,
    snackBar: LeoSnackBarController,
    onNavigateBack: () -> Unit = {},
    onSignedOut: () -> Unit = {},
) {
    LaunchedEffect(effects) {
        effects.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowSuccess -> snackBar.showSuccess(effect.message)
                is SettingsEffect.ShowError -> snackBar.showError(effect.message)
                SettingsEffect.NavigateBack -> onNavigateBack()
                SettingsEffect.SignedOut -> onSignedOut()
            }
        }
    }
}

/** 56dp Material-style app bar with a back arrow, title, and optional actions (Flutter `AppBar`). */
@Composable
internal fun SettingsAppBar(
    title: String,
    onBack: () -> Unit,
    background: Color,
    titleSize: TextUnit = 20.sp,
    titleWeight: FontWeight = FontWeight.ExtraBold,
    titleLetterSpacing: TextUnit = TextUnit.Unspecified,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val palette = LeoThemeTokens.palette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(56.dp).background(background).padding(start = 4.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = SettingsStrings.back,
                tint = palette.textPrimary,
            )
        }
        Spacer(Modifier.width(20.dp))
        Text(
            title,
            fontSize = titleSize,
            fontWeight = titleWeight,
            letterSpacing = titleLetterSpacing,
            color = palette.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        actions()
    }
}

/** White rounded card with the warm border used for every settings group. */
@Composable
internal fun SettingsCard(
    modifier: Modifier = Modifier,
    radius: Dp = 22.dp,
    borderColor: Color = LeoThemeTokens.palette.border,
    padding: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(LeoThemeTokens.palette.surface, shape)
            .border(1.dp, borderColor, shape)
            .padding(padding),
        content = content,
    )
}

/** Hairline between rows, inset 18dp on both sides. */
@Composable
internal fun SettingsDivider() {
    val palette = LeoThemeTokens.palette
    HorizontalDivider(
        thickness = 1.dp,
        color = palette.dividerColor(),
        modifier = Modifier.padding(horizontal = 18.dp),
    )
}

internal fun LeoPalette.dividerColor(): Color = if (isDark) borderSubtle else input

/** Brand icon plus small-caps section title. Mirrors `_SectionHeader`. */
@Composable
internal fun SettingsSectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = LeoColors.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp,
            color = LeoThemeTokens.palette.textSecondary,
        )
    }
}

/** Uppercase label above a group on the sub-pages (sessions, appearance). */
@Composable
internal fun SettingsGroupLabel(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.6.sp,
        color = LeoThemeTokens.palette.textSecondary,
    )
}

/** Title, optional subtitle, and a brand switch. Mirrors `_SettingSwitchTile`. */
@Composable
internal fun SettingsSwitchTile(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
) {
    val palette = LeoThemeTokens.palette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = palette.textPrimary)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, fontSize = 12.5.sp, fontWeight = FontWeight.Medium, color = palette.textSecondary)
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = LeoColors.primary,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = SettingsColors.switchTrackOff,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

/** Title, optional trailing value, and chevron. Mirrors `_SettingNavigationTile`. */
@Composable
internal fun SettingsNavigationTile(
    title: String,
    onClick: () -> Unit,
    trailingText: String? = null,
) {
    val palette = LeoThemeTokens.palette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(SettingsCardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Text(
            title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = palette.textPrimary,
            modifier = Modifier.weight(1f),
        )
        if (trailingText != null) {
            Text(trailingText, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = palette.textSecondary)
            Spacer(Modifier.width(4.dp))
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = palette.textSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Rounded tinted square holding a feature icon (security and sessions cards). */
@Composable
internal fun SettingsIconBadge(
    icon: ImageVector,
    tint: Color,
    background: Color,
    size: Dp = 40.dp,
    radius: Dp = 12.dp,
    iconSize: Dp = 22.dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size).background(background, RoundedCornerShape(radius)),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}
