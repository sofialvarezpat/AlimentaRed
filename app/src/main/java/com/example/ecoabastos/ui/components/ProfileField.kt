package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/**
 * Fila de "Perfil": etiqueta arriba, valor abajo dentro de un recuadro con flecha.
 * Al tocarla (onClick) abre el editor de ese dato. Hoy no hace nada real porque
 * el backend no tiene endpoint para editar el perfil.
 */
@Composable
fun ProfileField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)
    Column(modifier = modifier) {
        Text(
            text = label,
            color = EcoColors.Texto,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(EcoColors.Superficie, shape)
                .border(1.dp, EcoColors.Borde, shape)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val texto = if (isPassword) "•".repeat(value.length.coerceAtLeast(8)) else value
            Text(
                text = texto.ifEmpty { "Sin información" },
                fontSize = 14.sp,
                fontStyle = if (value.isEmpty()) FontStyle.Italic else FontStyle.Normal,
                color = if (value.isEmpty()) EcoColors.Gris else EcoColors.Texto
            )
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Editar $label",
                tint = EcoColors.Gris
            )
        }
    }
}

@Composable
private fun PreviewContent() {
    Column(
        modifier = Modifier
            .background(EcoColors.Fondo)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ProfileField(label = "Nombre", value = "Romeo Iglecias", onClick = {})
        ProfileField(label = "Contraseña", value = "12345678", onClick = {}, isPassword = true)
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun ProfileFieldPreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun ProfileFieldDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}