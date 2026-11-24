package com.leochat.features.chat.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leochat.core.constants.AppStrings
import com.leochat.core.constants.MediaStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LocalLeoPalette
import com.leochat.core.widgets.common.buttons.leoPressable
import com.leochat.core.widgets.common.feedback.LocalLeoSnackBar
import com.leochat.features.chat.domain.usecases.AiPrompt
import com.leochat.features.media.domain.entities.VoiceRecording
import com.leochat.features.media.presentation.permissions.MediaPermission
import com.leochat.features.media.presentation.permissions.rememberMediaPermissionRequester
import com.leochat.features.media.presentation.providers.rememberVoiceRecordingController
import com.leochat.features.media.presentation.states.formatRecordingTime

private val SendGlow = Color(0x33E64A19)

// Recording pill surfaces from `chat_composer.dart`.
private val RecordingPill = Color(0xFFFFEEE3)
private val RecordingPillDark = Color(0xFF3B1E12)
private val RecordingBorder = Color(0xFFFFD7C2)
private val RecordingBorderDark = Color(0xFF7A3416)
private val RecordingCancel = Color(0xFFFFD7C2)
private val RecordingCancelDark = Color(0xFF5A2414)
private val RecordingRed = Color(0xFFDC2626)

/**
 * Message input with attachment, `/ai` toggle, and send/mic button. Mirrors `LeoChatComposer`.
 * Tapping the mic starts recording; tapping again stops and delivers the file to
 * [onVoiceRecorded]; the trash button cancels. [onVoiceClick] runs only when the
 * platform has no recorder.
 */
@Composable
fun ChatComposer(
    onSend: (String) -> Unit,
    onTyping: (Boolean) -> Unit,
    onAttachmentClick: () -> Unit,
    onVoiceClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    onVoiceRecorded: (VoiceRecording) -> Unit = {},
) {
    val palette = LocalLeoPalette.current
    val snackBar = LocalLeoSnackBar.current
    val permissions = rememberMediaPermissionRequester()
    val recorder = rememberVoiceRecordingController()
    val recording by recorder.state.collectAsStateWithLifecycle()
    DisposableEffect(recorder) { onDispose { recorder.cancel() } }
    var field by remember { mutableStateOf(TextFieldValue("")) }
    val hasText = field.text.isNotBlank()
    val isAiMode = AiPrompt.isAiCommand(field.text)

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

    fun stopAndSendRecording() {
        recorder.stop()?.let(onVoiceRecorded)
    }

    fun toggleAi() {
        val current = field.text
        val next = if (AiPrompt.isAiCommand(current)) {
            current.replaceFirst(Regex("^/ai\\s*", RegexOption.IGNORE_CASE), "")
        } else if (current.isEmpty()) "/ai " else "/ai ${current.trim()} "
        update(TextFieldValue(next, TextRange(next.length)))
    }

    val border = if (palette.isDark) LeoColors.darkBorder else LeoColors.border
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(if (palette.isDark) LeoColors.darkSurface else Color.White)
            .drawBehind { drawLine(border, androidx.compose.ui.geometry.Offset(0f, 0f), androidx.compose.ui.geometry.Offset(size.width, 0f), 1.dp.toPx()) }
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 16.dp),
    ) {
        val chip = if (palette.isDark) LeoColors.darkSurfaceElevated else LeoColors.input
        if (!recording.isRecording) {
            RoundAction(Icons.Filled.Add, chip, LeoColors.primary, 24, onClick = onAttachmentClick)
            Spacer(Modifier.width(8.dp))
            RoundAction(
                icon = Icons.Filled.AutoAwesome,
                background = if (isAiMode) LeoColors.primarySoft else chip,
                tint = if (isAiMode) LeoColors.primary else if (palette.isDark) LeoColors.darkTextSecondary else LeoColors.textSecondary,
                iconSize = 20,
                bordered = isAiMode,
                onClick = ::toggleAi,
            )
            Spacer(Modifier.width(8.dp))
        }
        if (recording.isRecording) {
            RecordingPill(
                elapsedSeconds = recording.elapsedSeconds,
                isDark = palette.isDark,
                onCancel = recorder::cancel,
                modifier = Modifier.weight(1f),
            )
        } else BasicTextField(
            value = field,
            onValueChange = ::update,
            textStyle = TextStyle(color = if (palette.isDark) LeoColors.darkTextPrimary else LeoColors.textPrimary, fontSize = 15.sp),
            cursorBrush = SolidColor(LeoColors.primary),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { send() }),
            maxLines = 5,
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .background(if (palette.isDark) LeoColors.darkInput else LeoColors.input, RoundedCornerShape(30.dp))
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                ) {
                    if (field.text.isEmpty()) {
                        Text(
                            if (isAiMode) AppStrings.aiMessageHint else AppStrings.messageHint,
                            color = if (palette.isDark) LeoColors.darkTextSecondary else LeoColors.textSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    inner()
                }
            },
        )
        Spacer(Modifier.width(12.dp))
        val interaction = remember { MutableInteractionSource() }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .leoPressable(interaction)
                .size(46.dp)
                .shadow(6.dp, CircleShape, ambientColor = SendGlow, spotColor = SendGlow)
                .clip(CircleShape)
                .background(LeoColors.primary)
                .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple()) {
                    when {
                        hasText -> send()
                        recording.isRecording -> stopAndSendRecording()
                        else -> startRecording()
                    }
                },
        ) {
            Icon(
                when {
                    isAiMode -> Icons.Filled.AutoAwesome
                    hasText -> Icons.AutoMirrored.Rounded.Send
                    recording.isRecording -> Icons.Rounded.ArrowUpward
                    else -> Icons.Rounded.MicNone
                },
                contentDescription = when {
                    hasText -> null
                    recording.isRecording -> MediaStrings.sendRecording
                    else -> MediaStrings.recordVoice
                },
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun RoundAction(
    icon: ImageVector,
    background: Color,
    tint: Color,
    iconSize: Int,
    bordered: Boolean = false,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val base = Modifier.leoPressable(interaction).size(40.dp).clip(CircleShape).background(background)
    Box(
        contentAlignment = Alignment.Center,
        modifier = (if (bordered) base.border(1.5.dp, LeoColors.primary, CircleShape) else base)
            .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClick = onClick),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize.dp))
    }
}

/** Live recording row: cancel, red dot, `mm:ss`, animated bars. */
@Composable
private fun RecordingPill(elapsedSeconds: Int, isDark: Boolean, onCancel: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(48.dp)
            .background(if (isDark) RecordingPillDark else RecordingPill, shape)
            .border(1.dp, if (isDark) RecordingBorderDark else RecordingBorder, shape)
            .padding(horizontal = 14.dp),
    ) {
        val interaction = remember { MutableInteractionSource() }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .leoPressable(interaction)
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isDark) RecordingCancelDark else RecordingCancel)
                .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClick = onCancel),
        ) {
            Icon(Icons.Rounded.DeleteOutline, MediaStrings.cancelRecording, tint = RecordingRed, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Box(Modifier.size(8.dp).background(RecordingRed, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(formatRecordingTime(elapsedSeconds), color = LeoColors.primary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.width(12.dp))
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            repeat(14) { i ->
                val barHeight = 6 + ((i * 3 + elapsedSeconds * 4) % 18)
                Box(
                    Modifier
                        .width(2.5.dp)
                        .height(barHeight.dp)
                        .background(LeoColors.primary.copy(alpha = 0.7f), RoundedCornerShape(2.dp)),
                )
            }
        }
    }
}
