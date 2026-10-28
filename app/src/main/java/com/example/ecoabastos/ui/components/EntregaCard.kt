package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/** Estados visuales de una entrega, mapeados a EstadoEntrega del backend. */
enum class EntregaEstadoUi(val etiqueta: String) {
    PROGRAMADA("Pendiente"),
    RETIRADO("Recogido"),
    COMPLETADO("Recogido"),
    INCIDENCIA("Incidencia")
}

/** Tarjeta de una entrega en el Historial de Donaciones. */
@Composable
fun EntregaCard(
    nombreLote: String,
    categoria: String,
    cantidad: String,
    estado: EntregaEstadoUi,
    recogidoPor: String,
    fecha: String,
    onVerDetalleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    val (tagBg, tagColor) = when (estado) {
        EntregaEstadoUi.COMPLETADO -> Pair(EcoColors.VerdeFondo, EcoColors.VerdeOscuro)
        EntregaEstadoUi.RETIRADO -> Pair(EcoColors.VerdeFondo, EcoColors.VerdeOscuro)
        EntregaEstadoUi.PROGRAMADA -> Pair(EcoColors.Suave, EcoColors.VerdeOscuro)
        EntregaEstadoUi.INCIDENCIA -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.04f)
            )
            .background(EcoColors.Superficie, shape)
            .border(1.dp, EcoColors.Borde, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = nombreLote,
                    color = EcoColors.Texto,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = categoria,
                    color = EcoColors.Gris,
                    fontSize = 13.sp
                )
            }
            EcoTag(
                text = estado.etiqueta,
                backgroundColor = tagBg,
                textColor = tagColor
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Campo condicional que muestra la organizacin receptora
                if (recogidoPor.isNotBlank() && (estado == EntregaEstadoUi.COMPLETADO || estado == EntregaEstadoUi.RETIRADO)) {
                    Text(
                        text = "Recogido por: $recogidoPor",
                        color = EcoColors.Texto,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else if (recogidoPor.isNotBlank()) {
                    Text(
                        text = "Asignado a: $recogidoPor",
                        color = EcoColors.Texto,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Pendiente de retiro",
                        color = EcoColors.Gris,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "Fecha: $fecha",
                    color = EcoColors.Gris,
                    fontSize = 12.sp
                )
            }
            EcoActionButton(
                text = "Ver detalles",
                onClick = onVerDetalleClick,
                containerColor = EcoColors.Secundario,
                contentColor = EcoColors.VerdeOscuro
            )
        }
    }
}
