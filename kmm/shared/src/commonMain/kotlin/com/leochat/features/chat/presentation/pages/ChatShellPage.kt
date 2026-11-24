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
import com.leochat.core.constants.CallStrings
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
import com.leochat.features.chat.presentation.widgets.ShellRail
import com.leochat.features.chat.presentation.widgets.ShellTab

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import com.leochat.features.ai.presentation.pages.AiPage
import com.leochat.features.calls.domain.entities.CallType
import com.leochat.features.calls.presentation.controllers.CallController
import com.leochat.features.calls.presentation.widgets.IncomingCallDialog
import com.leochat.features.settings.presentation.pages.SettingsPage
import org.koin.compose.koinInject

/** Routes the shell and its conversation pane can open. Wired in `LeoNavHost`. */
class ShellNavigator(
    val onOpenConversation: () -> Unit,
    val onSignedOut: () -> Unit,
    val onNewGroup: () -> Unit,
    val onGroupDetails: (conversationId: String) -> Unit,
    val onSharedMedia: (conversationId: String) -> Unit,
    val onCall: () -> Unit,
    val onEditProfile: () -> Unit,
    val onSecurity: () -> Unit,
    val onSessions: () -> Unit,
    val onAppearance: () -> Unit,
    val onAdmin: () -> Unit,
)

/**
 * `/app`. Mobile: tab content + bottom bar, conversations open full screen.
 * Tablet/desktop: rail + 360dp list + detail. Mirrors `ChatShellPage`.
 */
@Composable
fun ChatShellPage(controller: ChatController, nav: ShellNavigator) {
    val state by controller.state.collectAsStateWithLifecycle()
    val snackBar = LocalLeoSnackBar.current
    var tab by rememberSaveable { mutableStateOf(ShellTab.Chats) }
    val isMobile = LocalLeoLayout.current == LeoLayout.Mobile
    LaunchedEffect(state.signedOut) { if (state.signedOut) nav.onSignedOut() }

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
    // Contacts → open the chat: refresh the list, select it, and show it (full screen on mobile).
    val openFromContacts: (Conversation) -> Unit = { conversation ->
        controller.refresh()
        controller.selectConversation(conversation)
        tab = ShellTab.Chats
        if (isMobile) nav.onOpenConversation()
    }
    val otherTab: @Composable (ShellTab) -> Unit = { t ->
        when (t) {
            ShellTab.Ai -> AiPage()
            ShellTab.Contacts -> NewConversationPage(
                onBack = null,
                onConversationOpened = openFromContacts,
                onNewGroup = nav.onNewGroup,
            )
            else -> SettingsPage(
                onEditProfile = nav.onEditProfile,
                onSecurity = nav.onSecurity,
                onSessions = nav.onSessions,
                onAppearance = nav.onAppearance,
                onAdmin = nav.onAdmin,
                onSignedOut = nav.onSignedOut,
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (isMobile) {
            Column(Modifier.fillMaxSize().background(LeoColors.background)) {
                Box(Modifier.weight(1f).statusBarsPadding()) {
                    if (tab == ShellTab.Chats) {
                        chatsList(Modifier) { conversation ->
                            controller.selectConversation(conversation)
                            nav.onOpenConversation()
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
                    DetailPane(state, controller, nav, showBackButton = false, onBack = {}, onComingSoon = { snackBar.showInfo(it) })
                } else {
                    Box(Modifier.weight(1f)) { otherTab(tab) }
                }
            }
        }
        // Flutter overlays the incoming-call banner on the shell: full width on mobile, inset on desktop.
        IncomingCallDialog(
            onAccepted = nav.onCall,
            onPermissionDenied = { snackBar.showInfo(CallStrings.microphonePermissionRequired) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .then(if (isMobile) Modifier.statusBarsPadding() else Modifier.systemBarsPadding().padding(start = 80.dp, end = 20.dp, top = 10.dp)),
        )
    }
}

@Composable
internal fun DetailPane(
    state: ChatState,
    controller: ChatController,
    nav: ShellNavigator,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onComingSoon: (String) -> Unit,
    callController: CallController = koinInject(),
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
        // Flutter: groups open group details; direct chats open shared media.
        onOpenDetails = { c -> if (c.isGroup) nav.onGroupDetails(c.id) else nav.onSharedMedia(c.id) },
        onCall = { c, video ->
            callController.startCall(
                conversationId = c.id,
                targetUserId = c.targetUserId ?: c.id,
                targetUserName = c.effectiveTitle,
                callType = if (video) CallType.VIDEO else CallType.AUDIO,
                targetAvatarUrl = c.preferredAvatarUrl,
            )
            nav.onCall()
        },
        onVoiceRecorded = controller::sendVoice,
    )
}
