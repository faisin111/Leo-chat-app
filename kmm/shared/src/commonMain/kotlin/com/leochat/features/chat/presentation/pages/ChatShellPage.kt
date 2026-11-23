package com.leochat.features.chat.presentation.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.AppStrings
import com.leochat.core.responsive.LeoLayout
import com.leochat.core.responsive.LocalLeoLayout
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.features.chat.domain.entities.Conversation
import com.leochat.features.chat.presentation.controllers.ChatController
import com.leochat.features.chat.presentation.states.ChatState
import com.leochat.features.chat.presentation.widgets.ChatsListPane
import com.leochat.features.chat.presentation.widgets.ConversationDetailView
import com.leochat.features.chat.presentation.widgets.ShellBottomNav
import com.leochat.features.chat.presentation.widgets.ShellPlaceholderPane
import com.leochat.features.chat.presentation.widgets.ShellRail
import com.leochat.features.chat.presentation.widgets.ShellTab

/**
 * `/app`. Mobile: tab content + bottom bar, conversations open full screen.
 * Tablet/desktop: rail + 360dp list + detail. Mirrors `ChatShellPage`.
 */
@Composable
fun ChatShellPage(
    controller: ChatController,
    onOpenConversation: () -> Unit,
    onSignedOut: () -> Unit,
) {
    val state by controller.state.collectAsStateWithLifecycle()
    val snackBar = LocalLeoSnackBar.current
    var tab by rememberSaveable { mutableStateOf(ShellTab.Chats) }
    val isMobile = LocalLeoLayout.current == LeoLayout.Mobile
    LaunchedEffect(state.signedOut) { if (state.signedOut) onSignedOut() }

    val chatsList: @Composable (Modifier, (Conversation) -> Unit) -> Unit = { modifier, open ->
        ChatsListPane(
            state = state,
            onOpenConversation = open,
            onOpenSettings = { tab = ShellTab.Settings },
            onCompose = { tab = ShellTab.Contacts },
            onSearch = controller::search,
            onFilter = controller::setFilter,
            onRefresh = { controller.refresh().join() },
            modifier = modifier,
        )
    }
    val otherTab: @Composable (ShellTab) -> Unit = { t ->
        when (t) {
            ShellTab.Ai -> ShellPlaceholderPane(AppStrings.aiAssistant)
            ShellTab.Contacts -> ShellPlaceholderPane(AppStrings.contacts)
            else -> ShellPlaceholderPane(AppStrings.settings, onSignOut = if (state.isSigningOut) null else controller::signOut)
        }
    }

    if (isMobile) {
        Column(Modifier.fillMaxSize().background(LeoColors.background)) {
            Box(Modifier.weight(1f).statusBarsPadding()) {
                if (tab == ShellTab.Chats) {
                    chatsList(Modifier) { conversation ->
                        controller.selectConversation(conversation)
                        onOpenConversation()
                    }
                } else {
                    otherTab(tab)
                }
            }
            ShellBottomNav(tab) { tab = it }
        }
    } else {
        Row(Modifier.fillMaxSize().background(LeoColors.background).systemBarsPadding().imePadding()) {
            ShellRail(tab) { tab = it }
            if (tab == ShellTab.Chats) {
                chatsList(Modifier.width(360.dp).fillMaxHeight(), controller::selectConversation)
                VerticalDivider(color = LeoColors.border)
                DetailPane(state, controller, showBackButton = false, onBack = {}, onComingSoon = { snackBar.showInfo(it) })
            } else {
                Box(Modifier.weight(1f)) { otherTab(tab) }
            }
        }
    }
}

@Composable
internal fun DetailPane(
    state: ChatState,
    controller: ChatController,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onComingSoon: (String) -> Unit,
) {
    ConversationDetailView(
        state = state,
        showBackButton = showBackButton,
        onBack = onBack,
        onSend = { controller.sendMessage(it) },
        onTyping = controller::sendTyping,
        onRetry = controller::retry,
        onLeave = controller::leaveConversation,
        onComingSoon = onComingSoon,
    )
}
