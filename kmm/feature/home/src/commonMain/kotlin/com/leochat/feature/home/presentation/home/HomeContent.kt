package com.leochat.feature.home.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leochat.core.designsystem.component.navigation.ShellBottomNav
import com.leochat.core.designsystem.component.navigation.ShellRail
import com.leochat.core.designsystem.component.navigation.ShellTab
import com.leochat.core.designsystem.theme.LeoThemeTokens

/**
 * Stateless shell layout. Mobile: tab content + bottom bar. Tablet/desktop: rail + 360dp
 * [chatsList] + [conversationPane] on the Chats tab. [otherTab] draws every non-Chats tab;
 * [overlay] draws on top of the shell (incoming-call banner).
 */
@Composable
fun HomeContent(
    selectedTab: ShellTab,
    isMobile: Boolean,
    onSelectTab: (ShellTab) -> Unit,
    chatsList: @Composable (Modifier) -> Unit,
    conversationPane: @Composable () -> Unit,
    otherTab: @Composable (ShellTab) -> Unit,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    val palette = LeoThemeTokens.palette
    Box(Modifier.fillMaxSize()) {
        if (isMobile) {
            Column(Modifier.fillMaxSize().background(palette.background)) {
                Box(Modifier.weight(1f).statusBarsPadding()) {
                    if (selectedTab == ShellTab.Chats) chatsList(Modifier) else otherTab(selectedTab)
                }
                ShellBottomNav(selectedTab, onSelectTab)
            }
        } else {
            Row(Modifier.fillMaxSize().background(palette.background).systemBarsPadding().imePadding()) {
                ShellRail(selectedTab, onSelectTab)
                if (selectedTab == ShellTab.Chats) {
                    chatsList(Modifier.width(360.dp).fillMaxHeight())
                    VerticalDivider(color = palette.border)
                    conversationPane()
                } else {
                    Box(Modifier.weight(1f)) { otherTab(selectedTab) }
                }
            }
        }
        overlay()
    }
}
