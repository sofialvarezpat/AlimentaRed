package com.example.ecoabastos.ui.screens.donante

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Handshake
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.ecoabastos.ui.components.EcoActionButton
import com.example.ecoabastos.ui.components.EcoTag
import com.example.ecoabastos.ui.components.EntregaEstadoUi
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/**
 * Pantalla "Detalle de la Donación" según el mockup de Figma.
 * Muestra todos los datos de una entrega individual al pulsar "Ver Detalles" en el historial.
 */
@Composable
fun DonationDetailScreen(
    entrega: EntregaUi,
    unidadMedida: String = "kg",
    hora: String = "",
    organizacion: String = "",
    observaciones: String = "",
    onVolverClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val (tagBg, tagColor) = when (entrega.estado) {
        EntregaEstadoUi.COMPLETADO -> Pair(EcoColors.VerdeFondo, EcoColors.VerdeOscuro)
        EntregaEstadoUi.PROGRAMADA -> Pair(EcoColors.Suave, EcoColors.VerdeOscuro)
        EntregaEstadoUi.RETIRADO -> Pair(EcoColors.Alerta, Color.White)
        EntregaEstadoUi.INCIDENCIA -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
    ) {
        // ─── Encabezado ───────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(EcoColors.Fondo)
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = statusBar + 12.dp,
                    bottom = 16.dp
                )
        ) {
            // ← Historial (link de regreso)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onVolverClick() }
                    .padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = EcoColors.Accion,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "  Historial",
                    color = EcoColors.Accion,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Detalle de la donación",
                color = EcoColors.Texto,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Consulta la información de tu entrega.",
                color = EcoColors.Gris,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // ─── Contenido desplazable ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tarjeta 1: Nombre + categoría + cantidad + unidad
            DetalleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = entrega.nombreLote,
                            color = EcoColors.Texto,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = entrega.categoria,
                            color = EcoColors.Accion,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    EcoTag(
                        text = entrega.estado.etiqueta,
                        backgroundColor = tagBg,
                        textColor = tagColor
                    )
                }

                HorizontalDivider(color = EcoColors.Borde, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Cantidad retirada", color = EcoColors.Gris, fontSize = 12.sp)
                        Text(
                            text = entrega.cantidad,
                            color = EcoColors.Texto,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Unidad de medida", color = EcoColors.Gris, fontSize = 12.sp)
                        Text(
                            text = if (unidadMedida == "kg") "Kilogramos (kg)"
                            else unidadMedida,
                            color = EcoColors.Texto,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Tarjeta 2: Fecha y hora del retiro
            DetalleCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        tint = EcoColors.Accion,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Fecha y hora del retiro",
                        color = EcoColors.Texto,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Fecha", color = EcoColors.Gris, fontSize = 12.sp)
                        Text(
                            text = entrega.fecha.ifEmpty { "Pendiente" },
                            color = EcoColors.Texto,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Hora", color = EcoColors.Gris, fontSize = 12.sp)
                        Text(
                            text = hora.ifEmpty { "—" },
                            color = EcoColors.Texto,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Tarjeta 3: Organización que retira
            if (entrega.recogidoPor.isNotEmpty() || organizacion.isNotEmpty()) {
                DetalleCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Handshake,
                            contentDescription = null,
                            tint = EcoColors.Accion,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Organización que retira",
                                color = EcoColors.Gris,
                                fontSize = 12.sp
                            )
                            Text(
                                text = organizacion.ifEmpty { entrega.recogidoPor },
                                color = EcoColors.Texto,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tarjeta 4: Observaciones
            if (observaciones.isNotEmpty()) {
                DetalleCard {
                    Text(
                        text = "Observaciones de la entrega",
                        color = EcoColors.Texto,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = observaciones,
                        color = EcoColors.Gris,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Botón Volver
            EcoActionButton(
                text = "Volver al historial",
                onClick = onVolverClick,
                modifier = Modifier.fillMaxWidth(),
                containerColor = EcoColors.Secundario,
                contentColor = EcoColors.Texto
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetalleCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .background(EcoColors.Superficie, shape)
            .border(1.dp, EcoColors.Borde, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}

// ─── Vistas previas ───────────────────────────────────────────────────────────
private val entregaDetalleDemo = EntregaUi(
    id = 1,
    nombreLote = "Manzanas",
    categoria = "Fruta",
    cantidad = "15 kg",
    estado = EntregaEstadoUi.COMPLETADO,
    recogidoPor = "Fundación Sonrisas",
    fecha = "24 de octubre de 2026"
)

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Detalle Claro")
@Composable
private fun DonationDetailPreview() {
    EcoTheme(darkTheme = false) {
        DonationDetailScreen(
            entrega = entregaDetalleDemo,
            unidadMedida = "kg",
            hora = "10:30 a. m.",
            organizacion = "Fundación Sonrisas",
            observaciones = "Se entregaron 3 cajas de manzanas en buen estado. Retiro completo, sin novedades.",
            onVolverClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Detalle Oscuro")
@Composable
private fun DonationDetailDarkPreview() {
    EcoTheme(darkTheme = true) {
        DonationDetailScreen(
            entrega = entregaDetalleDemo,
            unidadMedida = "kg",
            hora = "10:30 a. m.",
            organizacion = "Fundación Sonrisas",
            observaciones = "Se entregaron 3 cajas de manzanas en buen estado.",
            onVolverClick = {}
        )
    }
}
