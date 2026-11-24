package com.leochat.core.designsystem.component.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leochat.core.designsystem.theme.LeoColors

/** Transparent 56dp app bar with a back arrow (Flutter `AppBar(leading: IconButton)`). */
@Composable
fun LeoBackBar(onBack: () -> Unit, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Box(modifier = modifier.fillMaxWidth().height(56.dp).padding(start = 4.dp), contentAlignment = Alignment.CenterStart) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = contentDescription, tint = LeoColors.textPrimary)
        }
    }
}
