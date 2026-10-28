package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/** Etiqueta pequeña con fondo de color. */
@Composable
fun EcoTag(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
private fun PreviewContent() {
    Row(
        modifier = Modifier
            .background(EcoColors.Fondo)
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        EcoTag("Frutas", EcoColors.Suave, EcoColors.VerdeOscuro)
        EcoTag("Vence en 4 días", EcoColors.Alerta, Color.White)
    }
}

@Preview(showBackground = true)
@Composable
private fun EcoTagPreview() {
    EcoTheme(darkTheme = false) { PreviewContent() }
}

@Preview(showBackground = true)
@Composable
private fun EcoTagDarkPreview() {
    EcoTheme(darkTheme = true) { PreviewContent() }
}