package com.leochat.core.designsystem.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.component.branding.LeoAvatar
import com.leochat.core.designsystem.component.branding.LeoLogo
import com.leochat.core.designsystem.component.buttons.leoPressable
import com.leochat.core.designsystem.theme.ChatTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoThemeTokens

/** Logo + profile avatar row, then the "Your chats" title with the compose button. */
@Composable
fun ChatsListHeader(userName: String?, avatarUrl: String?, onOpenSettings: () -> Unit, onCompose: () -> Unit) {
    val palette = LeoThemeTokens.palette
    Row(verticalAlignment = Alignment.CenterVertically) {
        LeoLogo()
        Spacer(Modifier.weight(1f))
        val interaction = remember { MutableInteractionSource() }
        LeoAvatar(
            radius = 20.dp,
            name = userName,
            imageUrl = avatarUrl,
            modifier = Modifier.leoPressable(interaction).clickable(interactionSource = interaction, indication = null, onClick = onOpenSettings),
        )
    }
    Spacer(Modifier.height(20.dp))
    Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(
                AppStrings.yourChats,
                fontSize = 34.sp,
                lineHeight = (34 * 1.1).sp,
                letterSpacing = (-1.2).sp,
                fontWeight = FontWeight.Black,
                color = palette.textPrimary,
            )
            Spacer(Modifier.height(4.dp))
            Text(AppStrings.yourChatsSubtitle, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = palette.textSecondary)
        }
        ChatsComposeButton(onCompose)
    }
}

/** 48dp brand squircle with the compose icon. */
@Composable
fun ChatsComposeButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(16.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .leoPressable(interaction)
            .size(48.dp)
            .shadow(8.dp, shape, ambientColor = ChatTokens.sendGlow, spotColor = ChatTokens.sendGlow)
            .background(LeoColors.primary, shape)
            .clip(shape)
            .clickable(interactionSource = interaction, indication = ripple(), onClick = onClick),
    ) {
        Icon(Icons.Rounded.EditNote, null, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

/** "Search people or messages" field with a clear button. */
@Composable
fun ChatSearchField(query: String, onSearch: (String) -> Unit) {
    val palette = LeoThemeTokens.palette
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().height(50.dp).background(palette.input, RoundedCornerShape(16.dp)).padding(horizontal = 16.dp),
    ) {
        Icon(Icons.Rounded.Search, null, tint = palette.textSecondary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(AppStrings.searchPeopleOrMessages, color = palette.textSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            BasicTextField(
                value = query,
                onValueChange = onSearch,
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = palette.textPrimary),
                cursorBrush = SolidColor(LeoColors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Icon(Icons.Rounded.Close, null, tint = palette.textSecondary, modifier = Modifier.size(20.dp).clickable { onSearch("") })
        }
    }
}

/** Empty conversation list: chat icon over [message]. */
@Composable
fun ChatsEmptyState(message: String) {
    val palette = LeoThemeTokens.palette
    Column(
        Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Rounded.ChatBubbleOutline, null, tint = palette.textMuted, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(12.dp))
        Text(message, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = palette.textSecondary)
    }
}

/** "Made for connection" footer under the chats list. */
@Composable
fun MadeForConnectionFooter() {
    val palette = LeoThemeTokens.palette
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.FavoriteBorder, null, tint = palette.textMuted, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(8.dp))
        Text(AppStrings.madeForConnection, fontSize = 12.sp, color = palette.textSecondary, fontWeight = FontWeight.SemiBold)
    }
}
