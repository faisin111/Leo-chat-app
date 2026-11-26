package com.leochat.core.designsystem.component.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens
import com.leochat.core.designsystem.component.branding.LeoLogo
import com.leochat.core.designsystem.component.buttons.leoPressable

/** Shell tabs with standard outlined (inactive) and filled rounded (active) Material icons. */
enum class ShellTab(val label: String, val icon: ImageVector, val activeIcon: ImageVector) {
    Chats(AppStrings.chats, Icons.Outlined.ChatBubbleOutline, Icons.Rounded.ChatBubble),
    Ai(AppStrings.aiAssistant, Icons.Outlined.AutoAwesome, Icons.Rounded.AutoAwesome),
    Contacts(AppStrings.contacts, Icons.Outlined.Group, Icons.Rounded.Group),
    Notifications(AppStrings.notifications, Icons.Outlined.Notifications, Icons.Rounded.Notifications),
    Settings(AppStrings.settings, Icons.Outlined.Settings, Icons.Rounded.Settings),
}

/** Mobile bottom bar with modern pill active indicator and live unread badges. */
@Composable
fun ShellBottomNav(
    selected: ShellTab,
    onSelect: (ShellTab) -> Unit,
    badges: Map<ShellTab, Int> = emptyMap(),
) {
    val palette = LeoThemeTokens.palette
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.background)
            .drawBehind { drawLine(palette.border, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
            .navigationBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp),
    ) {
        ShellTab.entries.forEach { tab ->
            val active = tab == selected
            val badgeCount = badges[tab] ?: 0
            val interaction = remember { MutableInteractionSource() }
            val pillColor by animateColorAsState(
                targetValue = if (active) LeoColors.primary.copy(alpha = if (palette.isDark) 0.18f else 0.12f) else Color.Transparent,
                animationSpec = tween(200),
                label = "navPill",
            )
            val contentColor by animateColorAsState(
                targetValue = if (active) LeoColors.primary else palette.textSecondary,
                animationSpec = tween(200),
                label = "navContent",
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .leoPressable(interaction)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(interactionSource = interaction, indication = null) { onSelect(tab) }
                    .padding(horizontal = 2.dp, vertical = 2.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(48.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(pillColor),
                ) {
                    Icon(
                        imageVector = if (active) tab.activeIcon else tab.icon,
                        contentDescription = tab.label,
                        tint = contentColor,
                        modifier = Modifier.size(21.dp),
                    )
                    if (badgeCount > 0) {
                        NavUnreadBadge(
                            count = badgeCount,
                            ringColor = palette.background,
                            modifier = Modifier.align(Alignment.TopEnd).offset(x = (-3).dp, y = 1.dp),
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    tab.label,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    color = contentColor,
                )
            }
        }
    }
}

/** Tablet/desktop rail with live unread badges. Mirrors `NavigationRail`. */
@Composable
fun ShellRail(
    selected: ShellTab,
    onSelect: (ShellTab) -> Unit,
    badges: Map<ShellTab, Int> = emptyMap(),
) {
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
            val badgeCount = badges[tab] ?: 0
            val interaction = remember { MutableInteractionSource() }
            val pillColor by animateColorAsState(
                targetValue = if (active) LeoColors.primary else Color.Transparent,
                animationSpec = tween(200),
                label = "railPill",
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .leoPressable(interaction)
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(pillColor)
                    .clickable(interactionSource = interaction, indication = null) { onSelect(tab) },
            ) {
                Icon(
                    imageVector = if (active) tab.activeIcon else tab.icon,
                    contentDescription = tab.label,
                    tint = if (active) Color.White else palette.textSecondary,
                    modifier = Modifier.size(22.dp),
                )
                if (badgeCount > 0) {
                    NavUnreadBadge(
                        count = badgeCount,
                        ringColor = palette.rail,
                        modifier = Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp),
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun NavUnreadBadge(count: Int, ringColor: Color, modifier: Modifier = Modifier) {
    val label = if (count > 99) "99+" else count.toString()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .widthIn(min = 16.dp)
            .height(16.dp)
            .clip(CircleShape)
            .background(LeoColors.destructive)
            .border(1.5.dp, ringColor, CircleShape)
            .padding(horizontal = 4.dp),
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            lineHeight = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}
