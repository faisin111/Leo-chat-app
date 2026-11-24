package com.leochat.core.designsystem.component.ai

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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

/** Prompt input with gradient send button and active processing animations. */
@Composable
fun AiComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    isSendEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val palette = LocalLeoPalette.current
    val border = palette.border
    val isFocusedOrFilled = text.isNotEmpty()
    val inputBorderColor by animateColorAsState(
        targetValue = if (isFocusedOrFilled) AiTokens.gradientStart.copy(alpha = 0.5f) else palette.border.copy(alpha = 0.5f),
        animationSpec = tween(250),
        label = "input_border",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(palette.surface)
            .drawBehind { drawLine(border, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 12.dp),
    ) {
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = palette.textPrimary,
            ),
            cursorBrush = SolidColor(LeoColors.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            modifier = Modifier.weight(1f).testTag(AI_INPUT_TAG),
            decorationBox = { inner ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .background(palette.input, RoundedCornerShape(24.dp))
                        .border(1.dp, inputBorderColor, RoundedCornerShape(24.dp))
                        // 16dp container inset + 18dp shared field content padding.
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    if (text.isEmpty()) {
                        Text(
                            AiStrings.askAnything,
                            fontSize = 14.sp,
                            color = palette.textMuted,
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
            AnimatedContent(
                targetState = isSendEnabled,
                transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(180)) },
                label = "ai_send_button_content",
            ) { enabled ->
                if (enabled) {
                    val hasText = text.isNotBlank()
                    val arrowScale by animateFloatAsState(
                        targetValue = if (hasText) 1.08f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "send_arrow_scale",
                    )
                    Icon(
                        Icons.Rounded.ArrowUpward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp).scale(arrowScale),
                    )
                } else {
                    // Asking / Generating state: smooth spinning and pulsing sparkle
                    val spinTransition = rememberInfiniteTransition(label = "composer_spin")
                    val spinAngle by spinTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
                        label = "spin_angle",
                    )
                    val sparklePulse by spinTransition.animateFloat(
                        initialValue = 0.88f,
                        targetValue = 1.12f,
                        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                        label = "sparkle_pulse",
                    )
                    Icon(
                        Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(19.dp)
                            .rotate(spinAngle)
                            .scale(sparklePulse),
                    )
                }
            }
        }
    }
}

const val AI_INPUT_TAG = "ai_composer_input"
const val AI_SEND_TAG = "ai_composer_send"
