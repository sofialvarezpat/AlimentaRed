package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/** Círculo verde con la(s) inicial(es) del nombre, como en la cabecera de Perfil. */
@Composable
fun ProfileAvatar(
    nombre: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val iniciales = nombre
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifEmpty { "—" }

    Box(
        modifier = modifier
            .size(size)
            .background(EcoColors.Accion, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iniciales,
            color = Color.White,
            fontSize = (size.value / 2.4).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PreviewContent() {
    Row(
        modifier = Modifier.background(EcoColors.Fondo).padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileAvatar(nombre = "Romeo Iglecias")
        ProfileAvatar(nombre = "")
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun ProfileAvatarPreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun ProfileAvatarDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}