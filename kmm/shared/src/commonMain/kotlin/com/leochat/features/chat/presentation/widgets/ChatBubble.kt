package com.leochat.features.chat.presentation.widgets

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
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LocalLeoPalette
import com.leochat.core.utils.TimeFormat
import com.leochat.features.chat.domain.entities.Message
import com.leochat.features.chat.domain.entities.MessageReaction
import com.leochat.features.chat.domain.entities.MessageReply
import com.leochat.features.chat.domain.entities.MessageSendStatus
import com.leochat.features.media.presentation.widgets.VoiceMessagePlayer

private val BubbleShape = RoundedCornerShape(22.dp)
private val White70 = Color.White.copy(alpha = 0.7f)
private val DeletedGrey = Color(0xFF94A3B8)

/** One message: outgoing (brand) or incoming (white) bubble. Mirrors `LeoChatBubble`. */
@Composable
fun ChatBubble(message: Message, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val appear = remember(message.id) { Animatable(0f) }
    LaunchedEffect(message.id) { appear.animateTo(1f, tween(220, easing = EaseOutCubic)) }
    Box(
        modifier = modifier.fillMaxWidth().graphicsLayer {
            val s = 0.96f + 0.04f * appear.value
            scaleX = s
            scaleY = s
            alpha = appear.value
            transformOrigin = if (message.isOwn) TransformOrigin(1f, 1f) else TransformOrigin(0f, 1f)
        },
    ) {
        if (message.isOwn) OutgoingBubble(message, onRetry) else IncomingBubble(message)
    }
}

@Composable
private fun OutgoingBubble(message: Message, onRetry: () -> Unit) {
    val isFailed = message.sendStatus == MessageSendStatus.Failed
    val isSending = message.sendStatus == MessageSendStatus.Sending
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.End) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.widthIn(max = 520.dp).background(LeoColors.primary, BubbleShape).padding(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 12.dp),
        ) {
            if (message.replyTo != null && !message.deleted) ReplyQuote(message.replyTo, isOwn = true)
            MessageBody(message, isOwn = true)
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(TimeFormat.clock(message.createdAt), color = White70, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(4.dp))
                when {
                    isSending -> CircularProgressIndicator(Modifier.size(10.dp), color = White70, strokeWidth = 1.5.dp)
                    !isFailed -> Icon(
                        if (message.isRead) Icons.Filled.DoneAll else Icons.Filled.Done,
                        contentDescription = null,
                        tint = if (message.isRead) Color.White else White70,
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
        if (message.reactions.isNotEmpty() && !message.deleted) ReactionsRow(message.reactions)
    }
}

@Composable
private fun IncomingBubble(message: Message) {
    val palette = LocalLeoPalette.current
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.Start) {
        Row(Modifier.padding(start = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (message.aiGenerated) AppStrings.aiAssistant else message.effectiveSenderName,
                color = LeoColors.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            if (message.aiGenerated) {
                Spacer(Modifier.width(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(LeoColors.primarySoft, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 1.5.dp),
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
                .background(if (palette.isDark) LeoColors.darkIncomingBubble else Color.White, BubbleShape)
                .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 12.dp),
        ) {
            if (message.replyTo != null && !message.deleted) ReplyQuote(message.replyTo, isOwn = false)
            MessageBody(message, isOwn = false)
            Spacer(Modifier.height(5.dp))
            Text(
                TimeFormat.clock(message.createdAt),
                color = if (palette.isDark) Color.White.copy(alpha = 0.54f) else LeoColors.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.End),
            )
        }
        if (message.reactions.isNotEmpty() && !message.deleted) ReactionsRow(message.reactions)
    }
}

@Composable
private fun MessageBody(message: Message, isOwn: Boolean) {
    val palette = LocalLeoPalette.current
    when {
        message.deleted -> Row(verticalAlignment = Alignment.CenterVertically) {
            val color = if (isOwn) White70 else DeletedGrey
            Icon(Icons.Outlined.Delete, null, tint = color, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(AppStrings.messageDeleted, fontStyle = FontStyle.Italic, fontSize = 14.sp, color = color)
        }
        message.isVoice -> VoiceMessagePlayer(isOwn = isOwn, audioUrl = message.content?.trim())
        message.isImage -> ImageContent(message.content!!.trim())
        else -> Text(
            message.content.orEmpty(),
            color = if (isOwn) Color.White else if (palette.isDark) LeoColors.darkTextPrimary else LeoColors.textPrimary,
            fontSize = 16.sp,
            lineHeight = (16 * 1.35).sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ImageContent(url: String) {
    SubcomposeAsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(280.dp, 200.dp).clip(RoundedCornerShape(14.dp)),
        loading = { Box(Modifier.size(280.dp, 200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(strokeWidth = 2.dp) } },
        error = {
            Box(Modifier.size(280.dp, 120.dp).background(LeoColors.input), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.BrokenImage, null, tint = LeoColors.textMuted, modifier = Modifier.size(32.dp))
            }
        },
    )
}

@Composable
private fun ReplyQuote(reply: MessageReply, isOwn: Boolean) {
    val accent = if (isOwn) White70 else LeoColors.primary
    Column(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isOwn) Color.Black.copy(alpha = 0.12f) else Color(0xFFFFEEE3))
            .drawBehind { drawRect(accent, topLeft = Offset.Zero, size = size.copy(width = 3.dp.toPx())) }
            .padding(start = 13.dp, top = 8.dp, end = 10.dp, bottom = 8.dp),
    ) {
        Text(
            reply.senderDisplayName ?: reply.senderUsername ?: "User",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isOwn) Color.White else LeoColors.primary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            reply.content.orEmpty(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isOwn) White70 else LeoColors.textSecondary,
        )
    }
}

@Composable
private fun ReactionsRow(reactions: List<MessageReaction>) {
    Row(Modifier.padding(top = 4.dp, start = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        reactions.forEach { r ->
            Text(
                "${r.emoji} ${r.count}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LeoColors.textPrimary,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}
