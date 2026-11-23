package com.leochat.features.chat.presentation.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AppStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.widgets.common.buttons.LeoOutlineButton

/** Stand-in for the AI, Contacts, and Settings tabs until their phases land. */
@Composable
fun ShellPlaceholderPane(title: String, onSignOut: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Black, color = LeoColors.textPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            AppStrings.comingSoonBody,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = LeoColors.textSecondary,
            modifier = Modifier.widthIn(max = 360.dp),
        )
        if (onSignOut != null) {
            Spacer(Modifier.height(28.dp))
            LeoOutlineButton(label = AppStrings.signOut, onClick = onSignOut)
        }
    }
}
