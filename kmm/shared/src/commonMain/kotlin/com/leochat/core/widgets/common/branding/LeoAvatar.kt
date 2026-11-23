package com.leochat.core.widgets.common.branding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.leochat.core.theme.LeoColors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Circular avatar: remote image, bundled drawable, or initials/icon fallback,
 * with optional online badge. Mirrors `LeoAvatar` (radius → [radius]).
 */
@Composable
fun LeoAvatar(
    radius: Dp,
    modifier: Modifier = Modifier,
    name: String? = null,
    imageUrl: String? = null,
    drawable: DrawableResource? = null,
    fallbackText: String? = null,
    fallbackIcon: ImageVector = Icons.Filled.Person,
    backgroundColor: Color = LeoColors.avatarBackground,
    iconColor: Color = LeoColors.primary,
    hasOnlineBadge: Boolean = false,
) {
    val diameter = radius * 2
    val fallback: @Composable () -> Unit = {
        AvatarFallback(radius, name, fallbackText, fallbackIcon, iconColor)
    }
    Box(modifier = modifier.size(diameter)) {
        Box(
            modifier = Modifier.size(diameter).clip(CircleShape).background(backgroundColor),
            contentAlignment = Alignment.Center,
        ) {
            when {
                !imageUrl.isNullOrBlank() -> SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(diameter),
                    loading = { fallback() },
                    error = { fallback() },
                )
                drawable != null -> Image(
                    painter = painterResource(drawable),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(diameter),
                )
                else -> fallback()
            }
        }
        if (hasOnlineBadge) {
            val badge = radius * 0.38f
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-1).dp, y = (-1).dp)
                    .size(badge)
                    .clip(CircleShape)
                    .background(LeoColors.online)
                    .border(2.dp, Color.White, CircleShape),
            )
        }
    }
}

@Composable
private fun AvatarFallback(
    radius: Dp,
    name: String?,
    fallbackText: String?,
    fallbackIcon: ImageVector,
    iconColor: Color,
) {
    val text = fallbackText?.trim() ?: initialsFor(name)
    if (!text.isNullOrEmpty()) {
        Text(text = text, color = iconColor, fontSize = (radius.value * 0.62f).sp, fontWeight = FontWeight.ExtraBold)
    } else {
        Icon(fallbackIcon, contentDescription = null, tint = iconColor, modifier = Modifier.size(radius * 1.1f))
    }
}

/** First two characters of the trimmed name, upper-cased. Mirrors the Flutter fallback. */
fun initialsFor(name: String?): String? {
    val trimmed = name?.trim().orEmpty()
    if (trimmed.isEmpty()) return null
    return trimmed.take(2).uppercase()
}
