package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/** Recuadro con el "!" que muestra un título y un mensaje (por ejemplo, un error). */
@Composable
fun EcoContextMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EcoColors.MensajeFondo, shape)
            .border(1.dp, EcoColors.MensajeBorde, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(EcoColors.Superficie, CircleShape)
                    .border(1.dp, EcoColors.MensajeBorde, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EcoColors.VerdeOscuro
                )
            }
            Text(
                text = title,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Bold,
                color = EcoColors.Texto
            )
        }
        Text(
            text = message,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            color = EcoColors.Gris
        )
    }
}

@Composable
private fun PreviewContent() {
    Box(
        modifier = Modifier
            .background(EcoColors.Fondo)
            .padding(24.dp)
    ) {
        EcoContextMessage(
            title = "No pudimos iniciar sesión",
            message = "Revisa tu correo o usuario y tu contraseña."
        )
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoContextMessagePreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun EcoContextMessageDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}