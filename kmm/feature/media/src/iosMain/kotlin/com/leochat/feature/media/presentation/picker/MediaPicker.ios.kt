package com.leochat.feature.media.presentation.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.leochat.feature.media.domain.model.PickedAttachment

@Composable
actual fun rememberAttachmentPicker(
    onPicked: (PickedAttachment) -> Unit,
    onError: (String) -> Unit,
): AttachmentPickerController = remember {
    object : AttachmentPickerController {
        override fun pickImage() = Unit
        override fun pickFile() = Unit
    }
}
