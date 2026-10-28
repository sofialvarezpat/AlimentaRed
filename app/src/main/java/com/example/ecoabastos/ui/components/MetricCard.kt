package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/**
 * Tarjeta de métrica destacada para el panel de administración.
 */
@Composable
fun MetricCard(
    titulo: String,
    valor: String,
    subtitulo: String? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .background(EcoColors.Superficie, shape)
            .border(1.dp, EcoColors.Borde, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = titulo,
            color = EcoColors.Gris,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = valor,
            color = EcoColors.VerdeOscuro,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        if (subtitulo != null) {
            Text(
                text = subtitulo,
                color = EcoColors.Gris,
                fontSize = 11.sp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
private fun MetricCardPreview() {
    EcoTheme(darkTheme = false) {
        MetricCard(
            titulo = "Total Recuperado",
            valor = "1,450 Kg",
            subtitulo = "+12% este mes"
        )
    }
}
