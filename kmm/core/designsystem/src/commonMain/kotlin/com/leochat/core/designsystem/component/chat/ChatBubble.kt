package com.leochat.core.designsystem.component.chat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.component.chat.model.BubbleSendStatus
import com.leochat.core.designsystem.component.chat.model.ChatBubbleBody
import com.leochat.core.designsystem.component.chat.model.ChatBubbleUi
import com.leochat.core.designsystem.component.chat.model.ReactionUi
import com.leochat.core.designsystem.component.chat.model.ReplyQuoteUi
import com.leochat.core.designsystem.theme.ChatTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LocalLeoPalette

private val BubbleShape = RoundedCornerShape(22.dp)
private val White70 = Color.White.copy(alpha = 0.7f)

/**
 * One message: outgoing (brand) or incoming (white) bubble. Mirrors `LeoChatBubble`.
 * [voiceContent] draws a [ChatBubbleBody.Voice] body (the voice player lives in the media feature).
 */
@Composable
fun ChatBubble(
    bubble: ChatBubbleUi,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    voiceContent: @Composable () -> Unit = {},
) {
    val appear = remember(bubble.id) { Animatable(0f) }
    LaunchedEffect(bubble.id) { appear.animateTo(1f, tween(220, easing = EaseOutCubic)) }
    Box(
        modifier = modifier.fillMaxWidth().graphicsLayer {
            val s = 0.96f + 0.04f * appear.value
            scaleX = s
            scaleY = s
            alpha = appear.value
            transformOrigin = if (bubble.isOwn) TransformOrigin(1f, 1f) else TransformOrigin(0f, 1f)
        },
    ) {
        if (bubble.isOwn) OutgoingBubble(bubble, onRetry, voiceContent) else IncomingBubble(bubble, voiceContent)
    }
}

@Composable
private fun OutgoingBubble(bubble: ChatBubbleUi, onRetry: () -> Unit, voiceContent: @Composable () -> Unit) {
    val isFailed = bubble.sendStatus == BubbleSendStatus.Failed
    val isSending = bubble.sendStatus == BubbleSendStatus.Sending
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.End) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.widthIn(max = 520.dp).background(LeoColors.primary, BubbleShape).padding(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 12.dp),
        ) {
            bubble.replyTo?.let { ReplyQuote(it, isOwn = true) }
            MessageBody(bubble.body, isOwn = true, voiceContent)
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(bubble.time, color = White70, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(4.dp))
                when {
                    isSending -> CircularProgressIndicator(Modifier.size(10.dp), color = White70, strokeWidth = 1.5.dp)
                    !isFailed -> Icon(
                        if (bubble.isRead) Icons.Filled.DoneAll else Icons.Filled.Done,
                        contentDescription = null,
                        tint = if (bubble.isRead) Color.White else White70,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        if (isFailed) {
            Text(
                AppStrings.notSentRetry,
                color = LeoColors.destructive,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 6.dp).clickable(onClick = onRetry),
            )
        }
        if (bubble.reactions.isNotEmpty()) ReactionsRow(bubble.reactions)
    }
}

@Composable
private fun IncomingBubble(bubble: ChatBubbleUi, voiceContent: @Composable () -> Unit) {
    val palette = LocalLeoPalette.current
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.Start) {
        Row(Modifier.padding(start = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (bubble.isAiGenerated) AppStrings.aiAssistant else bubble.senderName,
                color = LeoColors.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            if (bubble.isAiGenerated) {
                Spacer(Modifier.width(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(palette.primarySoft, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 1.5.dp),
                ) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = LeoColors.primary, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(AppStrings.aiBadge, color = LeoColors.primary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .background(palette.incomingBubble, BubbleShape)
                .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 12.dp),
        ) {
            bubble.replyTo?.let { ReplyQuote(it, isOwn = false) }
            MessageBody(bubble.body, isOwn = false, voiceContent)
            Spacer(Modifier.height(5.dp))
            Text(
                bubble.time,
                color = palette.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.End),
            )
        }
        if (bubble.reactions.isNotEmpty()) ReactionsRow(bubble.reactions)
    }
}

@Composable
private fun MessageBody(body: ChatBubbleBody, isOwn: Boolean, voiceContent: @Composable () -> Unit) {
    val palette = LocalLeoPalette.current
    when (body) {
        ChatBubbleBody.Deleted -> Row(verticalAlignment = Alignment.CenterVertically) {
            val color = if (isOwn) White70 else ChatTokens.deletedGrey
            Icon(Icons.Outlined.Delete, null, tint = color, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(AppStrings.messageDeleted, fontStyle = FontStyle.Italic, fontSize = 14.sp, color = color)
        }
        ChatBubbleBody.Voice -> voiceContent()
        is ChatBubbleBody.Image -> ImageContent(body.url)
        is ChatBubbleBody.Text -> Text(
            body.text,
            color = if (isOwn) Color.White else palette.textPrimary,
            fontSize = 16.sp,
            lineHeight = (16 * 1.35).sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ImageContent(url: String) {
    val palette = LocalLeoPalette.current
    SubcomposeAsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(280.dp, 200.dp).clip(RoundedCornerShape(14.dp)),
        loading = { Box(Modifier.size(280.dp, 200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(strokeWidth = 2.dp) } },
        error = {
            Box(Modifier.size(280.dp, 120.dp).background(palette.input), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.BrokenImage, null, tint = palette.textMuted, modifier = Modifier.size(32.dp))
            }
        },
    )
}

@Composable
private fun ReplyQuote(reply: ReplyQuoteUi, isOwn: Boolean) {
    val palette = LocalLeoPalette.current
    val accent = if (isOwn) White70 else LeoColors.primary
    val quoteBackground = if (isOwn) {
        Color.Black.copy(alpha = 0.12f)
    } else if (palette.isDark) {
        palette.surfaceElevated
    } else {
        ChatTokens.incomingReplyBackground
    }
    Column(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(quoteBackground)
            .drawBehind { drawRect(accent, topLeft = Offset.Zero, size = size.copy(width = 3.dp.toPx())) }
            .padding(start = 13.dp, top = 8.dp, end = 10.dp, bottom = 8.dp),
    ) {
        Text(
            reply.senderName,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isOwn) Color.White else LeoColors.primary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            reply.content,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isOwn) White70 else palette.textSecondary,
        )
    }
}

@Composable
private fun ReactionsRow(reactions: List<ReactionUi>) {
    val palette = LocalLeoPalette.current
    Row(Modifier.padding(top = 4.dp, start = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        reactions.forEach { r ->
            Text(
                r.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                modifier = Modifier
                    .background(if (palette.isDark) palette.surfaceElevated else Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, if (palette.isDark) palette.border else ChatTokens.reactionBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}
