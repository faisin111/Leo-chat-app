package com.leochat.features.chat.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.ContactsStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.branding.LeoAvatar
import com.leochat.features.users.domain.entities.UserSearchItem

/** Borderless text field with a 48dp leading icon slot (Flutter `InputDecoration(prefixIcon:)`). */
@Composable
internal fun GroupInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    leadingTint: Color,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth().height(48.dp)) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(leadingIcon, contentDescription = null, tint = leadingTint, modifier = Modifier.size(24.dp))
        }
        Box(Modifier.weight(1f).padding(end = 12.dp), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(placeholder, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = LeoColors.textMuted, maxLines = 1)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = textStyle,
                cursorBrush = SolidColor(LeoColors.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Selected member chip with avatar and remove icon (Flutter `Chip`). */
@Composable
internal fun SelectedMemberChip(member: UserSearchItem, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(LeoColors.primarySoft)
            .padding(start = 4.dp, end = 8.dp),
    ) {
        LeoAvatar(radius = 12.dp, name = member.effectiveName, imageUrl = member.profilePictureUrl)
        Spacer(Modifier.width(8.dp))
        Text(member.effectiveName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LeoColors.primary, maxLines = 1)
        Spacer(Modifier.width(6.dp))
        Icon(
            Icons.Rounded.Close,
            contentDescription = ContactsStrings.removeMember,
            tint = LeoColors.primary,
            modifier = Modifier.size(18.dp).clip(CircleShape).clickable(onClick = onRemove).padding(2.dp),
        )
    }
}

/** Search hit with a selection check, highlighted when selected. */
@Composable
internal fun SelectableUserRow(user: UserSearchItem, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .contactsTap(onClick = onClick)
            .background(if (selected) LeoColors.primarySoft.copy(alpha = 0.5f) else LeoColors.surface, shape)
            .border(1.dp, if (selected) LeoColors.primary else LeoColors.border, shape)
            .padding(12.dp),
    ) {
        LeoAvatar(radius = 22.dp, name = user.effectiveName, imageUrl = user.profilePictureUrl)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(user.effectiveName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = LeoColors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(ContactsStrings.handle(user.username), fontSize = 13.sp, color = LeoColors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Icon(
            if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (selected) LeoColors.primary else LeoColors.textMuted,
        )
    }
}
