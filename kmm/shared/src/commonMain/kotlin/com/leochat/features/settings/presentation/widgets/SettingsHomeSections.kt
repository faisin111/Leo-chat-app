package com.leochat.features.settings.presentation.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.SettingsStrings
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.core.widgets.common.branding.LeoAvatar

/** Large title with a search action. */
@Composable
internal fun SettingsHeader(onSearch: () -> Unit) {
    val palette = LeoThemeTokens.palette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            SettingsStrings.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.8).sp,
            color = palette.textPrimary,
        )
        IconButton(onClick = onSearch) {
            Icon(
                Icons.Rounded.Search,
                contentDescription = SettingsStrings.search,
                tint = palette.textPrimary,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

/** Avatar, name, and `@username · Edit profile`, opening the editor. */
@Composable
internal fun SettingsProfileCard(
    displayName: String,
    username: String,
    avatarUrl: String?,
    onClick: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(22.dp)
    SettingsCard(
        modifier = Modifier
            .settingsPressable(onClick = onClick)
            .shadow(4.dp, shape, ambientColor = SettingsColors.cardShadow, spotColor = SettingsColors.cardShadow),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            LeoAvatar(radius = 28.dp, name = displayName, imageUrl = avatarUrl)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    displayName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "@$username${SettingsStrings.editProfileSuffix}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = palette.textSecondary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/** Help link and red sign-out link. */
@Composable
internal fun SettingsFooterLinks(onHelp: () -> Unit, onSignOut: () -> Unit, signingOut: Boolean) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    ) {
        Text(
            SettingsStrings.helpAndSupport,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = LeoThemeTokens.palette.textSecondary,
            modifier = Modifier.settingsPressable(onClick = onHelp),
        )
        Text(
            SettingsStrings.signOut,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SettingsColors.signOut,
            modifier = Modifier.settingsPressable(enabled = !signingOut, onClick = onSignOut),
        )
    }
}

@Composable
internal fun SettingsVersionFooter() {
    Text(
        SettingsStrings.footer,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = LeoThemeTokens.palette.textMuted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}
