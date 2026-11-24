package com.leochat.core.designsystem.component.chat

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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.common.constants.MediaStrings
import com.leochat.core.designsystem.component.buttons.leoPressable
import com.leochat.core.designsystem.theme.ChatTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LocalLeoPalette

/**
 * Stateless message input with attachment, `/ai` toggle, and send/mic button. Mirrors `LeoChatComposer`.
 *
 * The send button sends when [field] has text, calls [onStopRecording] while [isRecording],
 * and otherwise calls [onMicClick]. While recording, the input is replaced by a recording pill
 * showing [recordingLabel]; its trash button calls [onCancelRecording].
 */
@Composable
fun ChatComposer(
    field: TextFieldValue,
    onFieldChange: (TextFieldValue) -> Unit,
    isAiMode: Boolean,
    onToggleAi: () -> Unit,
    onSend: () -> Unit,
    onAttachmentClick: () -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRecording: Boolean = false,
    recordingElapsedSeconds: Int = 0,
    recordingLabel: String = "",
    onStopRecording: () -> Unit = {},
    onCancelRecording: () -> Unit = {},
) {
    val palette = LocalLeoPalette.current
    val hasText = field.text.isNotBlank()
    val border = palette.border
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(palette.surface)
            .drawBehind { drawLine(border, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 16.dp),
    ) {
        val chip = if (palette.isDark) palette.surfaceElevated else palette.input
        if (!isRecording) {
            RoundAction(Icons.Filled.Add, chip, LeoColors.primary, 24, onClick = onAttachmentClick)
            Spacer(Modifier.width(8.dp))
            RoundAction(
                icon = Icons.Filled.AutoAwesome,
                background = if (isAiMode) palette.primarySoft else chip,
                tint = if (isAiMode) LeoColors.primary else palette.textSecondary,
                iconSize = 20,
                bordered = isAiMode,
                onClick = onToggleAi,
            )
            Spacer(Modifier.width(8.dp))
        }
        if (isRecording) {
            RecordingPill(
                elapsedSeconds = recordingElapsedSeconds,
                label = recordingLabel,
                isDark = palette.isDark,
                onCancel = onCancelRecording,
                modifier = Modifier.weight(1f),
            )
        } else BasicTextField(
            value = field,
            onValueChange = onFieldChange,
            textStyle = TextStyle(color = palette.textPrimary, fontSize = 15.sp),
            cursorBrush = SolidColor(LeoColors.primary),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            maxLines = 5,
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .background(palette.input, RoundedCornerShape(30.dp))
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                ) {
                    if (field.text.isEmpty()) {
                        Text(
                            if (isAiMode) AppStrings.aiMessageHint else AppStrings.messageHint,
                            color = palette.textSecondary,
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
                .shadow(6.dp, CircleShape, ambientColor = ChatTokens.sendGlow, spotColor = ChatTokens.sendGlow)
                .clip(CircleShape)
                .background(LeoColors.primary)
                .clickable(interactionSource = interaction, indication = ripple()) {
                    when {
                        hasText -> onSend()
                        isRecording -> onStopRecording()
                        else -> onMicClick()
                    }
                },
        ) {
            Icon(
                when {
                    isAiMode -> Icons.Filled.AutoAwesome
                    hasText -> Icons.AutoMirrored.Rounded.Send
                    isRecording -> Icons.Rounded.ArrowUpward
                    else -> Icons.Rounded.MicNone
                },
                contentDescription = when {
                    hasText -> null
                    isRecording -> MediaStrings.sendRecording
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
            .clickable(interactionSource = interaction, indication = ripple(), onClick = onClick),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize.dp))
    }
}

/** Live recording row: cancel, red dot, `mm:ss` [label], animated bars driven by [elapsedSeconds]. */
@Composable
private fun RecordingPill(elapsedSeconds: Int, label: String, isDark: Boolean, onCancel: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(48.dp)
            .background(if (isDark) ChatTokens.recordingPillDark else ChatTokens.recordingPill, shape)
            .border(1.dp, if (isDark) ChatTokens.recordingBorderDark else ChatTokens.recordingBorder, shape)
            .padding(horizontal = 14.dp),
    ) {
        val interaction = remember { MutableInteractionSource() }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .leoPressable(interaction)
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isDark) ChatTokens.recordingCancelDark else ChatTokens.recordingCancel)
                .clickable(interactionSource = interaction, indication = ripple(), onClick = onCancel),
        ) {
            Icon(Icons.Rounded.DeleteOutline, MediaStrings.cancelRecording, tint = ChatTokens.recordingRed, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Box(Modifier.size(8.dp).background(ChatTokens.recordingRed, CircleShape))
        Spacer(Modifier.width(8.dp))
        Text(label, color = LeoColors.primary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
