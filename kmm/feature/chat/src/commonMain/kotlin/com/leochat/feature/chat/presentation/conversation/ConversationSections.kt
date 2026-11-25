package com.leochat.feature.chat.presentation.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.common.constants.CallStrings
import com.leochat.core.common.constants.MediaStrings
import com.leochat.core.designsystem.component.chat.ChatBubble
import com.leochat.core.designsystem.component.chat.ChatComposer
import com.leochat.core.designsystem.component.chat.ConversationHeader
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LocalLeoPalette
import com.leochat.feature.chat.domain.model.Conversation
import com.leochat.feature.chat.domain.model.Message
import com.leochat.feature.chat.domain.usecase.AiPrompt
import com.leochat.feature.chat.presentation.mapper.toBubbleUi
import com.leochat.feature.chat.presentation.mapper.toHeaderUi
import com.leochat.feature.chat.presentation.mapper.voiceAudioUrl
import com.leochat.feature.media.domain.model.VoiceRecording
import com.leochat.feature.media.presentation.formatRecordingTime
import com.leochat.feature.media.presentation.permission.MediaPermission
import com.leochat.feature.media.presentation.permission.rememberMediaPermissionRequester
import com.leochat.feature.media.presentation.voice.VoiceMessagePlayer
import com.leochat.feature.media.presentation.voice.rememberVoiceRecordingController

/** Conversation header; call buttons ask for the microphone first, then call [onCall]. */
@Composable
internal fun ConversationHeaderSection(
    conversation: Conversation,
    showBackButton: Boolean,
    onBack: () -> Unit,
    onOpenDetails: () -> Unit,
    onCall: (video: Boolean) -> Unit,
    onLeave: () -> Unit,
    onTogglePin: (() -> Unit)? = null,
    onToggleMute: (() -> Unit)? = null,
    onToggleArchive: (() -> Unit)? = null,
) {
    val permissions = rememberMediaPermissionRequester()
    val snackBar = LocalLeoSnackBar.current
    val header = remember(conversation) { conversation.toHeaderUi() }
    ConversationHeader(
        header = header,
        showBackButton = showBackButton,
        onBack = onBack,
        onOpenDetails = onOpenDetails,
        onCall = { video ->
            val perms = if (video) setOf(MediaPermission.Microphone, MediaPermission.Camera) else setOf(MediaPermission.Microphone)
            permissions.request(perms) { granted ->
                if (granted) onCall(video) else snackBar.showError(CallStrings.microphonePermissionRequired)
            }
        },
        onLeave = onLeave,
        onTogglePin = onTogglePin,
        onToggleMute = onToggleMute,
        onToggleArchive = onToggleArchive,
    )
}

/** One message bubble; voice messages play through the media feature's player. */
@Composable
internal fun MessageBubble(
    message: Message,
    fallbackSenderName: String? = null,
    resolvedMediaUrls: Map<String, String> = emptyMap(),
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    onReactionClick: ((String) -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null,
) {
    val bubble = remember(message, fallbackSenderName, resolvedMediaUrls) {
        message.toBubbleUi(fallbackSenderName = fallbackSenderName, resolvedMediaUrls = resolvedMediaUrls)
    }
    ChatBubble(
        bubble = bubble,
        onRetry = onRetry,
        modifier = modifier,
        voiceContent = { VoiceMessagePlayer(isOwn = message.isOwn, audioUrl = message.voiceAudioUrl(resolvedMediaUrls)) },
        onLongClick = onLongClick,
        onReactionClick = onReactionClick,
        onImageClick = onImageClick,
    )
}

/**
 * Replying-to preview bar above composer. Mirrors Web's reply banner in `ConversationPage.tsx`.
 */
@Composable
fun ReplyPreviewBanner(
    replyingTo: Message,
    onCancel: () -> Unit,
) {
    val palette = LocalLeoPalette.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(if (palette.isDark) palette.surfaceElevated else palette.input)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(36.dp)
                .background(LeoColors.primary, RoundedCornerShape(2.dp)),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                replyingTo.senderUsername ?: "User",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = LeoColors.primary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                replyingTo.content.orEmpty(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                color = palette.textSecondary,
            )
        }
        IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Rounded.Close, AppStrings.cancel, tint = palette.textSecondary, modifier = Modifier.size(16.dp))
        }
    }
}

/**
 * Composer bound to the `/ai` command rules, the voice recorder, and the microphone permission.
 * Tapping the mic starts recording; tapping again stops and delivers the file to
 * [onVoiceRecorded]; the trash button cancels. [onVoiceClick] runs only when the
 * platform has no recorder.
 */
@Composable
internal fun ChatComposerSection(
    onSend: (String) -> Unit,
    onTyping: (Boolean) -> Unit,
    onAttachmentClick: () -> Unit,
    onVoiceClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onVoiceRecorded: (VoiceRecording) -> Unit = {},
    replyingTo: Message? = null,
    onCancelReply: () -> Unit = {},
) {
    val snackBar = LocalLeoSnackBar.current
    val permissions = rememberMediaPermissionRequester()
    val recorder = rememberVoiceRecordingController()
    val recording by recorder.state.collectAsStateWithLifecycle()
    DisposableEffect(recorder) { onDispose { recorder.cancel() } }
    var field by remember { mutableStateOf(TextFieldValue("")) }

    fun update(value: TextFieldValue) {
        val changed = value.text.isNotBlank() != field.text.isNotBlank() || value.text != field.text
        field = value
        if (changed) onTyping(value.text.isNotBlank())
    }

    fun send() {
        val text = field.text.trim()
        if (text.isEmpty()) return
        onSend(text)
        field = TextFieldValue("")
        onTyping(false)
    }

    fun startRecording() {
        permissions.request(setOf(MediaPermission.Microphone)) { granted ->
            when {
                !granted -> snackBar.showError(MediaStrings.microphonePermissionRequired)
                !recorder.start() -> onVoiceClick()
            }
        }
    }

    fun toggleAi() {
        val current = field.text
        val next = if (AiPrompt.isAiCommand(current)) {
            current.replaceFirst(Regex("^/ai\\s*", RegexOption.IGNORE_CASE), "")
        } else if (current.isEmpty()) "/ai " else "/ai ${current.trim()} "
        update(TextFieldValue(next, TextRange(next.length)))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        replyingTo?.let {
            ReplyPreviewBanner(it, onCancel = onCancelReply)
        }
        ChatComposer(
            field = field,
            onFieldChange = ::update,
            isAiMode = AiPrompt.isAiCommand(field.text),
            onToggleAi = ::toggleAi,
            onSend = ::send,
            onAttachmentClick = onAttachmentClick,
            onMicClick = ::startRecording,
            isRecording = recording.isRecording,
            recordingElapsedSeconds = recording.elapsedSeconds,
            recordingLabel = formatRecordingTime(recording.elapsedSeconds),
            onStopRecording = { recorder.stop()?.let(onVoiceRecorded) },
            onCancelRecording = recorder::cancel,
        )
    }
}
