package com.leochat.core.designsystem.component.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.common.constants.CallStrings
import com.leochat.core.designsystem.component.branding.LeoAvatar
import com.leochat.core.designsystem.component.call.model.IncomingCallUi
import com.leochat.core.designsystem.theme.CallTokens
import com.leochat.core.designsystem.theme.LeoColors

/** Stateless incoming call card with decline and accept actions. Mirrors `IncomingCallBanner`. */
@Composable
fun IncomingCallBanner(call: IncomingCallUi, onDecline: () -> Unit, onAccept: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(20.dp, CallTokens.bannerShape, ambientColor = CallTokens.bannerShadow, spotColor = CallTokens.bannerShadow)
            .background(CallTokens.background, CallTokens.bannerShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        LeoAvatar(radius = 24.dp, name = call.callerName, imageUrl = call.callerAvatarUrl)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                call.callerName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (call.isVideo) Icons.Rounded.Videocam else Icons.Rounded.PhoneInTalk,
                    null,
                    tint = LeoColors.primary,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    if (call.isVideo) CallStrings.incomingVideoCall else CallStrings.incomingVoiceCall,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }
        CallCircleAction(Icons.Rounded.CallEnd, 42.dp, 20.dp, LeoColors.destructive, Color.White, CallStrings.decline, onClick = onDecline)
        Spacer(Modifier.width(10.dp))
        CallCircleAction(Icons.Rounded.Call, 42.dp, 20.dp, LeoColors.success, Color.White, CallStrings.accept, onClick = onAccept)
    }
}
