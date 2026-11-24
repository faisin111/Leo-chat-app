package com.leochat.core.designsystem.component.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.leochat.core.common.constants.AppStrings
import com.leochat.core.designsystem.theme.LeoColors
import com.leochat.core.designsystem.theme.LeoFields

/** Labelled auth input with trailing icon or custom suffix. Mirrors `LeoAuthField`. */
@Composable
fun LeoAuthField(
    label: String,
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    obscureText: Boolean = false,
    suffix: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(modifier = modifier) {
        Text(label, style = LeoFields.authLabelStyle)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = LeoFields.authTextStyle,
            placeholder = { Text(hint, style = LeoFields.authHintStyle) },
            trailingIcon = suffix ?: icon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(LeoFields.iconSize.dp)) } },
            visualTransformation = if (obscureText) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (obscureText) KeyboardType.Password else keyboardType,
            ),
            shape = LeoFields.shape,
            colors = LeoFields.authColors(),
        )
    }
}

/** Show/hide toggle used as the password field suffix. */
@Composable
fun PasswordVisibilityToggle(obscured: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (obscured) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            contentDescription = if (obscured) AppStrings.showPassword else AppStrings.hidePassword,
            tint = LeoColors.textSecondary,
        )
    }
}
