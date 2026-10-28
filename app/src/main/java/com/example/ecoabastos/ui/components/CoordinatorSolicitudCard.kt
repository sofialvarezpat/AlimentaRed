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
 * Tarjeta de solicitud vista desde el rol de Coordinador.
 * Muestra la organización que solicita, el lote, la cantidad y acciones para resolverla.
 */
@Composable
fun CoordinatorSolicitudCard(
    nombreLote: String,
    organizacion: String,
    cantidadSolicitada: String,
    fecha: String,
    estado: EstadoSolicitud,
    onResolverClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombreLote,
                    color = EcoColors.Texto,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Solicitante: $organizacion",
                    color = EcoColors.Gris,
                    fontSize = 13.sp
                )
            }

            val (bgTag, textTag, label) = when (estado) {
                EstadoSolicitud.PENDIENTE -> Triple(EcoColors.Alerta, Color.White, "Pendiente")
                EstadoSolicitud.APROBADA -> Triple(EcoColors.Suave, EcoColors.VerdeOscuro, "Aprobada")
                EstadoSolicitud.RECHAZADA -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Rechazada")
                EstadoSolicitud.CANCELADA -> Triple(EcoColors.Borde, EcoColors.Gris, "Cancelada")
            }

            EcoTag(text = label, backgroundColor = bgTag, textColor = textTag)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Cantidad requerida", color = EcoColors.Gris, fontSize = 12.sp)
                Text(
                    text = cantidadSolicitada,
                    color = EcoColors.Texto,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Fecha recibida", color = EcoColors.Gris, fontSize = 12.sp)
                Text(
                    text = fecha.ifEmpty { "Hoy" },
                    color = EcoColors.Texto,
                    fontSize = 14.sp
                )
            }
        }

        if (estado == EstadoSolicitud.PENDIENTE) {
            Spacer(Modifier.height(4.dp))
            EcoActionButton(
                text = "Revisar y Resolver",
                onClick = onResolverClick,
                modifier = Modifier.fillMaxWidth(),
                containerColor = EcoColors.Accion,
                height = 36.dp
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 402)
@Composable
private fun CoordinatorSolicitudCardPreview() {
    EcoTheme(darkTheme = false) {
        Box(modifier = Modifier.background(EcoColors.Fondo).padding(16.dp)) {
            CoordinatorSolicitudCard(
                nombreLote = "Manzanas Rojas",
                organizacion = "Comedor Esperanza",
                cantidadSolicitada = "15 Kg",
                fecha = "2026-10-28",
                estado = EstadoSolicitud.PENDIENTE,
                onResolverClick = {}
            )
        }
    }
}
