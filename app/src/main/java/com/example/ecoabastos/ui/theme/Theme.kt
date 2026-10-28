package com.example.ecoabastos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun EcoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkPalette else LightPalette

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.boton,
            onPrimary = Color.White,
            background = palette.fondo,
            onBackground = palette.texto,
            surface = palette.superficie,
            onSurface = palette.texto
        )
    } else {
        lightColorScheme(
            primary = palette.boton,
            onPrimary = Color.White,
            background = palette.fondo,
            onBackground = palette.texto,
            surface = palette.superficie,
            onSurface = palette.texto
        )
    }

    CompositionLocalProvider(LocalEcoPalette provides palette) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}