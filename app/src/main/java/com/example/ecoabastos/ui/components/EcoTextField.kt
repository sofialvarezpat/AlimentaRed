package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/** Campo de texto redondeado con sombra suave, como en el diseño. */
@Composable
fun EcoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    placeholderSize: TextUnit = 14.sp,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    minLines: Int = 1
) {
    val shape = RoundedCornerShape(20.dp)
    val singleLine = minLines == 1

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (singleLine) Modifier.height(53.dp)
                else Modifier.heightIn(min = 80.dp)
            )
            .shadow(
                elevation = 3.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .background(EcoColors.Superficie, shape)
            .border(1.dp, EcoColors.Borde, shape)
            .padding(horizontal = 16.dp, vertical = if (singleLine) 0.dp else 12.dp),
        singleLine = singleLine,
        minLines = minLines,
        textStyle = TextStyle(fontSize = 14.sp, color = EcoColors.Texto),
        cursorBrush = SolidColor(EcoColors.VerdeOscuro),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            Row(
                modifier = if (singleLine) Modifier.fillMaxHeight() else Modifier,
                verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = EcoColors.Gris,
                        modifier = Modifier.size(18.dp).then(Modifier.padding(end = 8.dp))
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontSize = placeholderSize,
                            fontStyle = FontStyle.Italic,
                            color = EcoColors.Gris
                        )
                    }
                    innerTextField()
                }
                if (trailingIcon != null) trailingIcon()
            }
        }
    )
}

/** El ojito para mostrar u ocultar la contraseña. */
@Composable
fun PasswordToggle(visible: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle, modifier = Modifier.size(24.dp)) {
        Icon(
            imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
            contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
            tint = EcoColors.VerdeClaro
        )
    }
}

@Composable
private fun PreviewContent() {
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("12345678") }
    var visible by remember { mutableStateOf(false) }
    var nota by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .background(EcoColors.Fondo)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EcoTextField(
            value = correo,
            onValueChange = { correo = it },
            placeholder = "Buscar",
            keyboardType = KeyboardType.Email,
            leadingIcon = Icons.Outlined.Visibility
        )
        EcoTextField(
            value = clave,
            onValueChange = { clave = it },
            placeholder = "Contraseña",
            keyboardType = KeyboardType.Password,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { PasswordToggle(visible) { visible = !visible } }
        )
        EcoTextField(
            value = nota,
            onValueChange = { nota = it },
            placeholder = "Detalles del retiro…",
            minLines = 3
        )
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoTextFieldPreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoTextFieldDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}
