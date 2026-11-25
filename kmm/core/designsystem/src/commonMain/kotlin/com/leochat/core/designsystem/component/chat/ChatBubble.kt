package com.leochat.core.designsystem.component.chat

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallMissed
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.Videocam
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.common.constants.MediaStrings
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
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    bubble: ChatBubbleUi,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    voiceContent: @Composable () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    onReactionClick: ((String) -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null,
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
        when (bubble.body) {
            is ChatBubbleBody.System -> SystemEventPill(bubble.body.text)
            is ChatBubbleBody.Call -> CallEventPill(bubble, bubble.body)
            else -> if (bubble.isOwn) {
                OutgoingBubble(bubble, onRetry, voiceContent, onLongClick, onReactionClick, onImageClick)
            } else {
                IncomingBubble(bubble, voiceContent, onLongClick, onReactionClick, onImageClick)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OutgoingBubble(
    bubble: ChatBubbleUi,
    onRetry: () -> Unit,
    voiceContent: @Composable () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onReactionClick: ((String) -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null,
) {
    val isFailed = bubble.sendStatus == BubbleSendStatus.Failed
    val isSending = bubble.sendStatus == BubbleSendStatus.Sending
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.End) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .widthIn(max = 520.dp)
                .clip(BubbleShape)
                .background(LeoColors.primary, BubbleShape)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick,
                )
                .padding(start = 18.dp, top = 14.dp, end = 18.dp, bottom = 12.dp),
        ) {
            bubble.replyTo?.let { ReplyQuote(it, isOwn = true) }
            MessageBody(bubble.body, isOwn = true, voiceContent, onImageClick)
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
        if (bubble.reactions.isNotEmpty()) ReactionsRow(bubble.reactions, onReactionClick)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IncomingBubble(
    bubble: ChatBubbleUi,
    voiceContent: @Composable () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onReactionClick: ((String) -> Unit)? = null,
    onImageClick: ((String) -> Unit)? = null,
) {
    val palette = LocalLeoPalette.current
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalAlignment = Alignment.Start) {
        Row(Modifier.padding(start = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (bubble.isAiGenerated) AppStrings.aiAssistant else bubble.senderName,
                color = LeoColors.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            if (bubble.isAiGenerated) {
                Spacer(Modifier.width(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(palette.primarySoft, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 1.5.dp),
                ) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = LeoColors.primary, modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(3.dp))
                    Text(AppStrings.aiBadge, color = LeoColors.primary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .clip(BubbleShape)
                .background(palette.incomingBubble, BubbleShape)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick,
                )
                .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 12.dp),
        ) {
            bubble.replyTo?.let { ReplyQuote(it, isOwn = false) }
            MessageBody(bubble.body, isOwn = false, voiceContent, onImageClick)
            Spacer(Modifier.height(5.dp))
            Text(
                bubble.time,
                color = palette.textMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.End),
            )
        }
        if (bubble.reactions.isNotEmpty()) ReactionsRow(bubble.reactions, onReactionClick)
    }
}

@Composable
private fun MessageBody(
    body: ChatBubbleBody,
    isOwn: Boolean,
    voiceContent: @Composable () -> Unit,
    onImageClick: ((String) -> Unit)? = null,
) {
    val palette = LocalLeoPalette.current
    when (body) {
        ChatBubbleBody.Deleted -> Row(verticalAlignment = Alignment.CenterVertically) {
            val color = if (isOwn) White70 else ChatTokens.deletedGrey
            Icon(Icons.Outlined.Delete, null, tint = color, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(AppStrings.messageDeleted, fontStyle = FontStyle.Italic, fontSize = 14.sp, color = color)
        }
        ChatBubbleBody.Voice -> voiceContent()
        is ChatBubbleBody.Image -> ImageContent(body, isOwn, onImageClick)
        is ChatBubbleBody.File -> FileContent(body, isOwn)
        is ChatBubbleBody.Call -> CallEventPillContent(body)
        is ChatBubbleBody.System -> Text(
            body.text,
            color = if (isOwn) Color.White else palette.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
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
private fun FileContent(file: ChatBubbleBody.File, isOwn: Boolean) {
    val palette = LocalLeoPalette.current
    val uriHandler = LocalUriHandler.current
    val hasDownloadUrl = file.url.startsWith("http://") || file.url.startsWith("https://")
    val fg = if (isOwn) Color.White else palette.textPrimary
    val subFg = if (isOwn) White70 else palette.textSecondary
    val bg = if (isOwn) Color.Black.copy(alpha = 0.16f) else palette.surfaceElevated
    val subtitleText = when {
        file.isLoading -> if (hasDownloadUrl) MediaStrings.uploading else MediaStrings.loadingLink
        file.sizeBytes != null && file.sizeBytes > 0 -> "${formatFileSizeLabel(file.sizeBytes)} · ${MediaStrings.tapToDownload}"
        else -> MediaStrings.tapToDownload
    }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(bg)
                .clickable(enabled = hasDownloadUrl) { runCatching { uriHandler.openUri(file.url) } }
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isOwn) Color.White.copy(alpha = 0.18f) else LeoColors.primary.copy(alpha = 0.14f)),
            ) {
                if (file.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = if (isOwn) Color.White else LeoColors.primary,
                    )
                } else {
                    Icon(
                        Icons.Rounded.InsertDriveFile,
                        contentDescription = null,
                        tint = if (isOwn) Color.White else LeoColors.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f, fill = false)) {
                Text(
                    file.fileName,
                    color = fg,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subtitleText,
                    color = subFg,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (hasDownloadUrl && !file.isLoading) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = fg.copy(alpha = 0.7f),
                    modifier = Modifier.size(15.dp),
                )
            }
        }
        if (!file.caption.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                file.caption,
                color = if (isOwn) Color.White else palette.textPrimary,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

private fun formatFileSizeLabel(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024L -> "${(bytes / 1024.0).let { ((it * 10).toInt() / 10.0) }} KB"
    else -> "${(bytes / (1024.0 * 1024.0)).let { ((it * 10).toInt() / 10.0) }} MB"
}

@Composable
private fun SystemEventPill(text: String) {
    if (text.isBlank()) return
    val palette = LocalLeoPalette.current
    Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = palette.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(palette.surfaceSubtle)
                .border(1.dp, palette.borderSubtle, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun CallEventPill(bubble: ChatBubbleUi, call: ChatBubbleBody.Call) {
    val palette = LocalLeoPalette.current
    Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(palette.surfaceElevated)
                .border(1.dp, palette.border, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            CallEventPillContent(call)
            if (bubble.time.isNotBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(
                    bubble.time,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.textMuted,
                )
            }
        }
    }
}

@Composable
private fun CallEventPillContent(call: ChatBubbleBody.Call) {
    val palette = LocalLeoPalette.current
    val accent = if (call.isMissed) LeoColors.destructive else LeoColors.success
    val icon = when {
        call.isMissed -> Icons.Rounded.CallMissed
        call.isVideo -> Icons.Rounded.Videocam
        else -> Icons.Rounded.Call
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.16f)),
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            call.label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.textPrimary,
        )
    }
}

@Composable
private fun ImageContent(
    image: ChatBubbleBody.Image,
    isOwn: Boolean,
    onImageClick: ((String) -> Unit)? = null,
) {
    val palette = LocalLeoPalette.current
    Column {
        when {
            image.url.isNotBlank() -> {
                Box(
                    modifier = Modifier
                        .widthIn(min = 160.dp, max = 260.dp)
                        .heightIn(min = 120.dp, max = 260.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(enabled = onImageClick != null) { onImageClick?.invoke(image.url) },
                ) {
                    SubcomposeAsyncImage(
                        model = image.url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 260.dp),
                        loading = {
                            Box(
                                Modifier.size(220.dp, 180.dp).background(palette.input),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(strokeWidth = 2.dp)
                            }
                        },
                        error = {
                            Box(
                                Modifier.size(220.dp, 120.dp).background(palette.input),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Outlined.BrokenImage, null, tint = palette.textMuted, modifier = Modifier.size(32.dp))
                            }
                        },
                    )
                    if (image.isLoading) {
                        Box(
                            Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(strokeWidth = 2.5.dp, color = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }
            image.isLoading -> {
                Box(
                    Modifier
                        .size(220.dp, 160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isOwn) Color.Black.copy(alpha = 0.2f) else palette.surfaceElevated),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = if (isOwn) Color.White else LeoColors.primary)
                }
            }
            else -> {
                Box(
                    Modifier.size(220.dp, 120.dp).clip(RoundedCornerShape(14.dp)).background(palette.input),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.BrokenImage, null, tint = palette.textMuted, modifier = Modifier.size(32.dp))
                }
            }
        }
        if (!image.caption.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                image.caption,
                color = if (isOwn) Color.White else palette.textPrimary,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
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
            fontWeight = FontWeight.SemiBold,
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
private fun ReactionsRow(
    reactions: List<ReactionUi>,
    onReactionClick: ((String) -> Unit)? = null,
) {
    val palette = LocalLeoPalette.current
    Row(Modifier.padding(top = 4.dp, start = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        reactions.forEach { r ->
            Text(
                r.label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (palette.isDark) palette.surfaceElevated else Color.White, RoundedCornerShape(12.dp))
                    .border(1.dp, if (palette.isDark) palette.border else ChatTokens.reactionBorder, RoundedCornerShape(12.dp))
                    .clickable(enabled = onReactionClick != null) { onReactionClick?.invoke(r.emoji) }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}
