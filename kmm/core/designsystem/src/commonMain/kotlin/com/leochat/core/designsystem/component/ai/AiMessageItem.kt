package com.leochat.core.designsystem.component.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AiStrings
import com.leochat.core.designsystem.component.ai.model.AiMessageUi
import com.leochat.core.designsystem.component.feedback.LocalLeoSnackBar
import com.leochat.core.designsystem.theme.AiTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LocalLeoPalette

/** One AI conversation turn: user bubble or assistant card. Mirrors `AiMessageItem`. */
@Composable
fun AiMessageItem(message: AiMessageUi, modifier: Modifier = Modifier) {
    if (message.isUser) UserBubble(message, modifier) else AssistantCard(message, modifier)
}

@Composable
private fun UserBubble(message: AiMessageUi, modifier: Modifier) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Text(
            message.content,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            lineHeight = (15 * 1.4).sp,
            modifier = Modifier
                .padding(vertical = 6.dp)
                .widthIn(max = 540.dp)
                .background(LeoColors.primary, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun AssistantCard(message: AiMessageUi, modifier: Modifier) {
    val palette = LocalLeoPalette.current
    val clipboard = LocalClipboardManager.current
    val snackBar = LocalLeoSnackBar.current
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .shadow(3.dp, shape, ambientColor = AiTokens.cardShadow, spotColor = AiTokens.cardShadow)
            .background(palette.surface, shape)
            .border(1.dp, palette.border, shape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AiGradientBadge(size = 28, cornerRadius = 8, iconSize = 16)
            Spacer(Modifier.width(10.dp))
            Text(AiStrings.assistantName, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = palette.textPrimary)
            Spacer(Modifier.width(8.dp))
            Text(
                message.modelLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = LeoColors.primary,
                modifier = Modifier
                    .background(palette.primarySoft, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Rounded.ContentCopy,
                contentDescription = null,
                tint = palette.textMuted,
                modifier = Modifier
                    .aiPressable {
                        clipboard.setText(AnnotatedString(message.content))
                        snackBar.showSuccess(AiStrings.copiedResponse)
                    }
                    .padding(4.dp)
                    .size(18.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        val thinking = message.thinkingContent
        if (thinking != null) {
            AiThinkingBox(thinkingContent = thinking, durationText = message.thinkingDurationText)
        }
        SelectionContainer {
            Text(message.content, fontSize = 14.sp, lineHeight = (14 * 1.55).sp, color = palette.textPrimary)
        }
    }
}

/** Rounded square with the brand gradient and a sparkle icon. */
@Composable
fun AiGradientBadge(size: Int, cornerRadius: Int, iconSize: Int, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size.dp).background(AiTokens.gradient, RoundedCornerShape(cornerRadius.dp)),
    ) {
        Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(iconSize.dp))
    }
}
