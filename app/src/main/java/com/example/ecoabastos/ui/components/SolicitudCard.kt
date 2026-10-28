package com.example.ecoabastos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.ecoabastos.data.model.EstadoSolicitud
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/**
 * Tarjeta que representa una solicitud realizada por la organización receptora.
 */
@Composable
fun SolicitudCard(
    nombreLote: String,
    cantidadSolicitada: String,
    estado: EstadoSolicitud,
    fecha: String,
    onCancelarClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    val (colorFondoTag, colorTextoTag, textoEstado) = when (estado) {
        EstadoSolicitud.PENDIENTE -> Triple(EcoColors.Alerta, Color.White, "Pendiente")
        EstadoSolicitud.APROBADA -> Triple(EcoColors.Suave, EcoColors.VerdeOscuro, "Aprobada")
        EstadoSolicitud.RECHAZADA -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Rechazada")
        EstadoSolicitud.CANCELADA -> Triple(EcoColors.Borde, EcoColors.Gris, "Cancelada")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.08f),
                spotColor = Color.Black.copy(alpha = 0.08f)
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
            Text(
                text = nombreLote,
                color = EcoColors.Texto,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            EcoTag(
                text = textoEstado,
                backgroundColor = colorFondoTag,
                textColor = colorTextoTag
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cantidad solicitada",
                    color = EcoColors.Gris,
                    fontSize = 12.sp
                )
                Text(
                    text = cantidadSolicitada,
                    color = EcoColors.Texto,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Fecha de solicitud",
                    color = EcoColors.Gris,
                    fontSize = 12.sp
                )
                Text(
                    text = fecha.ifEmpty { "Fecha pendiente" },
                    color = EcoColors.Texto,
                    fontSize = 14.sp
                )
            }
        }

        // Si la solicitud está pendiente, se permite cancelarla
        if (estado == EstadoSolicitud.PENDIENTE && onCancelarClick != null) {
            Spacer(Modifier.height(4.dp))
            EcoActionButton(
                text = "Cancelar Solicitud",
                onClick = onCancelarClick,
                modifier = Modifier.fillMaxWidth(),
                containerColor = EcoColors.Secundario,
                contentColor = EcoColors.Texto,
                height = 34.dp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun SolicitudCardPendientePreview() {
    EcoTheme(darkTheme = false) {
        Box(modifier = Modifier.background(EcoColors.Fondo).padding(16.dp)) {
            SolicitudCard(
                nombreLote = "Manzanas Rojas",
                cantidadSolicitada = "10 Kg",
                estado = EstadoSolicitud.PENDIENTE,
                fecha = "2026-10-28",
                onCancelarClick = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun SolicitudCardAprobadaPreview() {
    EcoTheme(darkTheme = false) {
        Box(modifier = Modifier.background(EcoColors.Fondo).padding(16.dp)) {
            SolicitudCard(
                nombreLote = "Pan Fresco",
                cantidadSolicitada = "5 Kg",
                estado = EstadoSolicitud.APROBADA,
                fecha = "2026-10-27"
            )
        }
    }
}
