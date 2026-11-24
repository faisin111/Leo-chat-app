package com.leochat.core.designsystem.component.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.DevicesOther
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.TabletMac
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.SettingsStrings
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.theme.SettingsTokens
import com.leochat.core.designsystem.component.settings.model.SessionDeviceUi
import com.leochat.core.designsystem.component.settings.model.SessionRowUi

/** Amber security tip at the top of the sessions page. */
@Composable
fun SessionsTipCard() {
    val dark = LeoThemeTokens.palette.isDark
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (dark) SettingsTokens.tipBackgroundDark else SettingsTokens.tipBackgroundLight, shape)
            .border(1.dp, if (dark) SettingsTokens.tipBorderDark else SettingsTokens.tipBorderLight, shape)
            .padding(16.dp),
    ) {
        Icon(Icons.Outlined.Shield, contentDescription = null, tint = SettingsTokens.tipIcon, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            SettingsStrings.sessionsTip,
            fontSize = 13.sp,
            lineHeight = 1.4.em,
            color = if (dark) SettingsTokens.tipTextDark else SettingsTokens.tipTextLight,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Shown when the API returns no sessions. */
@Composable
fun SessionsEmptyCard() {
    val palette = LeoThemeTokens.palette
    SettingsCard(radius = 20.dp, padding = 32.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(
                Icons.Rounded.DevicesOther,
                contentDescription = null,
                tint = palette.textMuted,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                SettingsStrings.currentDeviceActive,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
            )
        }
    }
}

private fun SessionDeviceUi.icon(): ImageVector = when (this) {
    SessionDeviceUi.Phone -> Icons.Rounded.Smartphone
    SessionDeviceUi.Tablet -> Icons.Rounded.TabletMac
    SessionDeviceUi.Laptop -> Icons.Rounded.LaptopMac
}

/** One logged-in device with a revoke action unless it is this device. */
@Composable
fun SessionTile(
    row: SessionRowUi,
    onRevoke: () -> Unit,
) {
    val palette = LeoThemeTokens.palette
    val isCurrent = row.isCurrent
    SettingsCard(
        radius = 20.dp,
        padding = 16.dp,
        borderColor = if (isCurrent) LeoColors.primary.copy(alpha = 0.5f) else palette.border,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SettingsIconBadge(
                icon = row.device.icon(),
                tint = when {
                    isCurrent -> LeoColors.primary
                    palette.isDark -> palette.textSecondary
                    else -> SettingsTokens.slateIcon
                },
                background = when {
                    isCurrent -> LeoColors.primarySoft
                    palette.isDark -> palette.surfaceElevated
                    else -> SettingsTokens.slateSurface
                },
                size = 44.dp,
                radius = 14.dp,
                iconSize = 24.dp,
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        row.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (isCurrent) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            SettingsStrings.thisDevice,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = LeoColors.success,
                            modifier = Modifier
                                .background(LeoColors.success.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text(row.subtitle, fontSize = 12.sp, color = palette.textSecondary)
            }
            if (!isCurrent) {
                Text(
                    SettingsStrings.revoke,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LeoColors.destructive,
                    modifier = Modifier
                        .settingsPressable(enabled = !row.isRevoking, onClick = onRevoke)
                        .background(LeoColors.destructive.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}
