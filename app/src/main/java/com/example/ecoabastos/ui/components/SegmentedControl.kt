package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/**
 * Control de dos pestañas tipo "pill" como aparece en el mockup de Solicitud / Estado Solicitud.
 */
@Composable
fun SegmentedControl(
    opciones: List<String>,
    seleccionado: Int,
    onSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(EcoColors.Secundario, shape)
            .border(1.dp, EcoColors.Borde, shape)
    ) {
        opciones.forEachIndexed { index, texto ->
            val activo = seleccionado == index
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(shape)
                    .background(
                        if (activo) EcoColors.Accion else Color.Transparent,
                        shape
                    )
                    .clickable { onSeleccionar(index) }
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    text = texto,
                    fontSize = 13.sp,
                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
                    color = if (activo) Color.White else EcoColors.Texto
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun SegmentedControlPreview() {
    EcoTheme(darkTheme = false) {
        SegmentedControl(
            opciones = listOf("Solicitud", "Estado Solicitud"),
            seleccionado = 0,
            onSeleccionar = {}
        )
    }
}
