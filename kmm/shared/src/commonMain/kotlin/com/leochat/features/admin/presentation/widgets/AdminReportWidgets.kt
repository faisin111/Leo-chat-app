package com.leochat.features.admin.presentation.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leochat.core.constants.AdminStrings
import com.leochat.core.theme.LeoColors
import com.leochat.core.theme.LeoThemeTokens
import com.leochat.features.admin.domain.entities.AdminReport

/** Status chip color: green once RESOLVED, red otherwise (Flutter rule). */
fun AdminReport.statusColor(): Color = if (isResolved) LeoColors.success else LeoColors.destructive

@Composable
fun AdminReportCard(report: AdminReport, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val palette = LeoThemeTokens.palette
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .adminPressable(onClick)
            .background(palette.surface, shape)
            .border(1.dp, palette.border, shape)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AdminStatusBadge(report.status, report.statusColor())
            Spacer(Modifier.width(8.dp))
            Text(report.targetType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.textSecondary)
            Spacer(Modifier.weight(1f))
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = AdminTokens.chevron)
        }
        Spacer(Modifier.height(10.dp))
        Text(report.reason, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = palette.textPrimary)
        val details = report.details
        if (!details.isNullOrEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(details, fontSize = 13.sp, color = palette.textSecondary)
        }
        val resolution = report.resolution
        if (resolution != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                AdminStrings.reportResolution(resolution),
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                color = if (palette.isDark) palette.textSecondary else AdminTokens.resolutionText,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (palette.isDark) palette.input else AdminTokens.resolutionSurface, RoundedCornerShape(10.dp))
                    .padding(8.dp),
            )
        }
    }
}

/**
 * Moderation dialog: target info, optional details, resolution note, and
 * Cancel / Delete Message (MESSAGE targets only) / Resolve actions.
 * [onResolve] returns `false` when the note is blank so the dialog stays open.
 */
@Composable
fun AdminReportDialog(
    report: AdminReport,
    onDismiss: () -> Unit,
    onDeleteMessage: () -> Unit,
    onResolve: (String) -> Boolean,
) {
    val palette = LeoThemeTokens.palette
    var note by remember(report.id) { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = palette.surface,
        title = {
            Text(
                AdminStrings.reportTitle(report.reason),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = palette.textPrimary,
            )
        },
        text = {
            Column {
                Text(
                    AdminStrings.reportTarget(report.targetType, report.targetId),
                    fontSize = 12.sp,
                    color = palette.textSecondary,
                )
                Spacer(Modifier.height(8.dp))
                val details = report.details
                if (!details.isNullOrEmpty()) {
                    Text(AdminStrings.reportDetails(details), fontSize = 14.sp, color = palette.textPrimary)
                    Spacer(Modifier.height(14.dp))
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(AdminStrings.resolutionNote) },
                    placeholder = { Text(AdminStrings.resolutionHint) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LeoColors.primary,
                        focusedLabelColor = LeoColors.primary,
                        cursorColor = LeoColors.primary,
                        unfocusedBorderColor = palette.border,
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text(AdminStrings.cancel, color = LeoColors.primary) }
                if (report.isMessageTarget) {
                    TextButton(onClick = {
                        onDismiss()
                        onDeleteMessage()
                    }) { Text(AdminStrings.deleteMessage, color = LeoColors.destructive) }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (onResolve(note.trim())) onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = LeoColors.primary, contentColor = Color.White),
            ) { Text(AdminStrings.resolve) }
        },
    )
}
