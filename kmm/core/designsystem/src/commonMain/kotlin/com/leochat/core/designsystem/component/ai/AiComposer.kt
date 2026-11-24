package com.leochat.core.designsystem.component.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AiStrings
import com.leochat.core.designsystem.theme.AiTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LocalLeoPalette

/** Prompt input with gradient send button. Text state is hoisted so presets can clear it. */
@Composable
fun AiComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isSendEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalLeoPalette.current.isDark
    val border = if (isDark) LeoColors.darkBorder else AiTokens.slate200
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDark) LeoColors.darkSurface else Color.White)
            .drawBehind { drawLine(border, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp),
    ) {
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = if (isDark) LeoColors.darkTextPrimary else LeoColors.textPrimary,
            ),
            cursorBrush = SolidColor(LeoColors.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            modifier = Modifier.weight(1f).testTag(AI_INPUT_TAG),
            decorationBox = { inner ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .background(if (isDark) LeoColors.darkInput else AiTokens.slate100, RoundedCornerShape(24.dp))
                        // 16dp container inset + 18dp shared field content padding.
                        .padding(horizontal = 34.dp, vertical = 16.dp),
                ) {
                    if (text.isEmpty()) {
                        Text(
                            AiStrings.askAnything,
                            fontSize = 14.sp,
                            color = if (isDark) LeoColors.darkTextMuted else AiTokens.slate400,
                        )
                    }
                    inner()
                }
            },
        )
        Spacer(Modifier.width(10.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .aiPressable(enabled = isSendEnabled, onClick = onSend)
                .size(44.dp)
                .shadow(6.dp, CircleShape, ambientColor = AiTokens.gradientGlow, spotColor = AiTokens.gradientGlow)
                .background(AiTokens.gradient, CircleShape)
                .testTag(AI_SEND_TAG),
        ) {
            Icon(Icons.Rounded.ArrowUpward, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

const val AI_INPUT_TAG = "ai_composer_input"
const val AI_SEND_TAG = "ai_composer_send"
