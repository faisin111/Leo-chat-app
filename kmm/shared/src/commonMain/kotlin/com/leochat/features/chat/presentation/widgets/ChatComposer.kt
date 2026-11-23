package com.leochat.features.chat.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.rounded.MicNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LocalLeoPalette
import com.leochat.core.widgets.common.buttons.leoPressable
import com.leochat.features.chat.domain.usecases.AiPrompt

private val SendGlow = Color(0x33E64A19)

/** Message input with attachment, `/ai` toggle, and send/mic button. Mirrors `LeoChatComposer`. */
@Composable
fun ChatComposer(
    onSend: (String) -> Unit,
    onTyping: (Boolean) -> Unit,
    onAttachmentClick: () -> Unit,
    onVoiceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = LocalLeoPalette.current
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
        BasicTextField(
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
                    if (hasText) send() else onVoiceClick()
                },
        ) {
            Icon(
                when {
                    isAiMode -> Icons.Filled.AutoAwesome
                    hasText -> Icons.AutoMirrored.Rounded.Send
                    else -> Icons.Rounded.MicNone
                },
                contentDescription = null,
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
