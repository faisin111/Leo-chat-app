package com.leochat.features.auth.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.branding.LeoAvatar
import com.leochat.resources.Res
import com.leochat.resources.avatar_portrait

/** Avatar with camera badge plus "Add your best hello" copy. */
@Composable
fun ProfilePhotoPrompt(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box {
            LeoAvatar(radius = 34.dp, drawable = Res.drawable.avatar_portrait)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(LeoColors.primary, CircleShape)
                    .padding(5.dp),
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(AppStrings.profilePhotoPrompt, fontSize = 16.sp, fontWeight = FontWeight.Black, color = LeoColors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(AppStrings.profilePhotoSubtext, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LeoColors.textSecondary)
        }
    }
}

/** Green "strong password" hint under the password field. */
@Composable
fun PasswordStrengthHint(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.CheckCircleOutline, contentDescription = null, tint = LeoColors.online, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(AppStrings.strongPassword, color = LeoColors.online, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

/** Terms checkbox row. */
@Composable
fun TermsAgreementRow(accepted: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Checkbox(
            checked = accepted,
            onCheckedChange = onChange,
            colors = CheckboxDefaults.colors(checkedColor = LeoColors.primary),
            modifier = Modifier.size(32.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            AppStrings.termsAgreement,
            color = LeoColors.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = (13 * 1.35).sp,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
