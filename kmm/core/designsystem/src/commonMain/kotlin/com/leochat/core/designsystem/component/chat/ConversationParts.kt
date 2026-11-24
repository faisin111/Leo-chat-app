package com.leochat.core.designsystem.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.theme.ChatTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens

/** Soft orange "Reconnecting…" strip under the conversation header. */
@Composable
fun ReconnectingBanner() {
    val palette = LeoThemeTokens.palette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().background(palette.primarySoft).padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        Icon(Icons.Rounded.WifiOff, null, tint = LeoColors.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(AppStrings.reconnecting, color = LeoColors.primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Centered icon disc, title, and body. [circle] above 60 uses the large (no conversation selected) style. */
@Composable
fun ChatEmptyPane(icon: ImageVector, circle: Int, iconSize: Int, title: String, body: String, modifier: Modifier = Modifier) {
    val palette = LeoThemeTokens.palette
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(circle.dp).background(palette.input, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = palette.textMuted, modifier = Modifier.size(iconSize.dp))
        }
        Spacer(Modifier.height(if (circle > 60) 16.dp else 12.dp))
        Text(title, fontSize = if (circle > 60) 18.sp else 16.sp, fontWeight = if (circle > 60) FontWeight.Bold else FontWeight.SemiBold, color = palette.textPrimary)
        Spacer(Modifier.height(if (circle > 60) 6.dp else 4.dp))
        Text(body, textAlign = TextAlign.Center, fontSize = if (circle > 60) 14.sp else 13.sp, fontWeight = FontWeight.Medium, color = palette.textSecondary)
    }
}

/** Centered day separator at the top of the message list (e.g. TODAY). */
@Composable
fun ChatDayLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        textAlign = TextAlign.Center,
        color = ChatTokens.dayLabel,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.4.sp,
        modifier = modifier.fillMaxWidth().padding(bottom = 20.dp),
    )
}

/** Floating "Latest" pill that jumps to the newest message. */
@Composable
fun JumpToLatestPill(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LeoThemeTokens.palette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = ChatTokens.latestPillShadow, spotColor = ChatTokens.latestPillShadow)
            .background(palette.surface, RoundedCornerShape(24.dp))
            .border(1.dp, palette.border, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Icon(Icons.Rounded.ArrowDownward, null, tint = LeoColors.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text(AppStrings.latest, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = LeoColors.primary)
    }
}
