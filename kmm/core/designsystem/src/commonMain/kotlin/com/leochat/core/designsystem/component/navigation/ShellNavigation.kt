package com.leochat.core.designsystem.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PeopleOutline
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.component.branding.LeoLogo
import com.leochat.core.designsystem.component.buttons.leoPressable

/** Shell tabs, in Flutter order. */
enum class ShellTab(val label: String, val icon: ImageVector, val activeIcon: ImageVector) {
    Chats(AppStrings.chats, Icons.Rounded.ChatBubbleOutline, Icons.Rounded.ChatBubble),
    Ai(AppStrings.aiAssistant, Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome),
    Contacts(AppStrings.contacts, Icons.Rounded.PeopleOutline, Icons.Rounded.People),
    Settings(AppStrings.settings, Icons.Rounded.Tune, Icons.Rounded.Tune),
}

/** Mobile bottom bar. Mirrors `_BottomNav`. */
@Composable
fun ShellBottomNav(selected: ShellTab, onSelect: (ShellTab) -> Unit) {
    val palette = LeoThemeTokens.palette
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.background)
            .drawBehind { drawLine(palette.border, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
            .navigationBarsPadding()
            .padding(vertical = 8.dp),
    ) {
        ShellTab.entries.forEach { tab ->
            val active = tab == selected
            val interaction = remember { MutableInteractionSource() }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .leoPressable(interaction)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple()) { onSelect(tab) }
                    .padding(horizontal = 14.dp, vertical = 4.dp),
            ) {
                val color = if (active) LeoColors.primary else palette.textSecondary
                Icon(if (active) tab.activeIcon else tab.icon, tab.label, tint = color, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(3.dp))
                Text(tab.label, fontSize = 11.sp, fontWeight = if (active) FontWeight.ExtraBold else FontWeight.SemiBold, color = color)
            }
        }
    }
}

/** Tablet/desktop rail. Mirrors `_DesktopRail`. */
@Composable
fun ShellRail(selected: ShellTab, onSelect: (ShellTab) -> Unit) {
    val palette = LeoThemeTokens.palette
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .fillMaxHeight()
            .background(palette.rail)
            .drawBehind { drawLine(palette.border, Offset(size.width, 0f), Offset(size.width, size.height), 1.dp.toPx()) },
    ) {
        Spacer(Modifier.height(24.dp))
        LeoLogo(compact = true)
        Spacer(Modifier.weight(1f))
        ShellTab.entries.forEach { tab ->
            val active = tab == selected
            val interaction = remember { MutableInteractionSource() }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .leoPressable(interaction)
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (active) LeoColors.primary else Color.Transparent)
                    .clickable(interactionSource = interaction, indication = null) { onSelect(tab) },
            ) {
                Icon(tab.activeIcon, tab.label, tint = if (active) Color.White else palette.textSecondary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
