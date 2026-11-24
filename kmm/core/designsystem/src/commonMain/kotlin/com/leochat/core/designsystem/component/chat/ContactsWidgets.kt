package com.leochat.core.designsystem.component.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.ContactsStrings
import com.leochat.core.designsystem.theme.ChatTokens
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.component.buttons.leoPressable


/** Tap with the LeoPressable scale effect and no ripple. */
fun Modifier.contactsTap(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    leoPressable(interaction, enabled).clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
}

/** Transparent 56dp app bar: optional back arrow, title, trailing actions (Flutter `AppBar`). */
@Composable
fun ContactsTopBar(
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    title: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().height(56.dp).background(LeoColors.background),
    ) {
        if (onBack != null) {
            Box(Modifier.width(56.dp), contentAlignment = Alignment.Center) {
                ContactsIconButton(Icons.AutoMirrored.Rounded.ArrowBack, ContactsStrings.back, onBack)
            }
        }
        Box(Modifier.weight(1f).padding(horizontal = 16.dp)) { title() }
        actions()
    }
}

/** Material icon button with the LeoPressable scale effect. */
@Composable
fun ContactsIconButton(icon: ImageVector, contentDescription: String?, onClick: () -> Unit, iconSize: Dp = 24.dp) {
    val interaction = remember { MutableInteractionSource() }
    IconButton(onClick = onClick, interactionSource = interaction, modifier = Modifier.leoPressable(interaction)) {
        Icon(icon, contentDescription = contentDescription, tint = LeoColors.textPrimary, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun ContactsTitle(text: String, fontSize: TextUnit = 22.sp, fontWeight: FontWeight = FontWeight.Black, letterSpacing: TextUnit = (-0.5).sp) {
    Text(text, fontSize = fontSize, fontWeight = fontWeight, letterSpacing = letterSpacing, color = LeoColors.textPrimary, maxLines = 1)
}

/** Rounded input-colored search pill with leading search icon and optional clear button. */
@Composable
fun ContactsSearchPill(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    horizontalPadding: Dp = 16.dp,
    iconSize: Dp = 22.dp,
    gap: Dp = 10.dp,
    textStyle: TextStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Medium, color = LeoColors.textPrimary),
    placeholderColor: Color = LeoColors.textSecondary,
    onClear: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(LeoColors.input, RoundedCornerShape(height / 2))
            .padding(horizontal = horizontalPadding),
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = LeoColors.textSecondary, modifier = Modifier.size(iconSize))
        Spacer(Modifier.width(gap))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) Text(placeholder, style = textStyle.copy(color = placeholderColor), maxLines = 1)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = textStyle,
                cursorBrush = SolidColor(LeoColors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (onClear != null && value.isNotEmpty()) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = ContactsStrings.clearSearch,
                tint = LeoColors.textSecondary,
                modifier = Modifier.size(18.dp).clickable(onClick = onClear),
            )
        }
    }
}

/** White rounded card with the warm border, optionally with the soft Flutter drop shadow. */
@Composable
fun ContactsCard(
    modifier: Modifier = Modifier,
    withShadow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = ChatTokens.cardShape
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (withShadow) Modifier.shadow(3.dp, shape, ambientColor = ChatTokens.cardShadow, spotColor = ChatTokens.cardShadow) else Modifier)
            .background(LeoColors.surface, shape)
            .border(1.dp, LeoColors.border, shape),
        content = content,
    )
}

@Composable
fun ContactsDivider(indent: Dp) {
    HorizontalDivider(modifier = Modifier.padding(start = indent), thickness = 1.dp, color = LeoColors.input)
}

/** Flutter `CircularProgressIndicator(color: primary)` centered with optional padding. */
@Composable
fun ContactsLoader(modifier: Modifier = Modifier, padding: Dp = 0.dp) {
    Box(modifier.fillMaxWidth().padding(padding), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = LeoColors.primary, strokeWidth = 4.dp, modifier = Modifier.size(36.dp))
    }
}

/** Icon disc, bold title, muted centered subtitle. */
@Composable
fun ContactsEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    discSize: Dp = 56.dp,
    discColor: Color = LeoColors.input,
    iconColor: Color = LeoColors.textSecondary,
    subtitleLineHeight: TextUnit = TextUnit.Unspecified,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(Modifier.size(discSize).background(discColor, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = LeoColors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, fontSize = 13.sp, color = LeoColors.textSecondary, textAlign = TextAlign.Center, lineHeight = subtitleLineHeight)
    }
}

/** Centers [content] in the remaining space with 32dp padding (Flutter `Center(Padding(32))`). */
@Composable
fun ContactsCenteredPane(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) { content() }
}
