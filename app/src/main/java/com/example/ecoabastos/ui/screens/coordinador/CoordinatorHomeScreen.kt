package com.example.ecoabastos.ui.screens.coordinador

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.data.model.EstadoSolicitud
import com.example.ecoabastos.ui.components.BottomBarItem
import com.example.ecoabastos.ui.components.EcoActionButton
import com.example.ecoabastos.ui.components.EcoBottomBar
import com.example.ecoabastos.ui.components.EcoStateCard
import com.example.ecoabastos.ui.components.EcoTag
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

data class CoordinatorSolicitudUi(
    val id: Long,
    val organizacion: String,
    val nombreAlimento: String = "",
    val cantidad: String = "",
    val fecha: String = "",
    val estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE
)

sealed interface CoordinatorUiState {
    data object Cargando : CoordinatorUiState
    data class Exito(val solicitudes: List<CoordinatorSolicitudUi>) : CoordinatorUiState
    data object Vacio : CoordinatorUiState
    data object Fallo : CoordinatorUiState
}

val destinosCoordinador = listOf(
    BottomBarItem("Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    BottomBarItem("Historial", Icons.Outlined.ShowChart, Icons.Filled.ShowChart),
    BottomBarItem("Perfil", Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
)

/**
 * Pantalla principal del Coordinador: idéntica a los mockups (Ecoabastos / Pendientes / Aprobadas / Rechazadas).
 */
@Composable
fun CoordinatorHomeScreen(
    state: CoordinatorUiState,
    selectedBottomTab: Int,
    onBottomTabSelected: (Int) -> Unit,
    onResolverSolicitud: (id: Long, aprobada: Boolean) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pestañaFiltro by remember { mutableIntStateOf(0) } // 0: Pendientes, 1: Aprobadas, 2: Rechazadas
    var filtroFecha by remember { mutableStateOf("Hoy") }
    var menuFechaAbierto by remember { mutableStateOf(false) }

    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
    ) {
        // ── Título y Selector de Pestañas ─────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = statusBar + 14.dp, bottom = 6.dp)
        ) {
            Text(
                text = "Ecoabastos",
                color = EcoColors.Texto,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(14.dp))

            // Selector horizontal de pestañas en píldora
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val countPendientes = if (state is CoordinatorUiState.Exito) state.solicitudes.count { it.estado == EstadoSolicitud.PENDIENTE } else 0
                val opciones = listOf("Pendientes ($countPendientes)", "Aprobadas", "Rechazadas")
                opciones.forEachIndexed { index, titulo ->
                    val seleccionado = pestañaFiltro == index
                    val chipShape = RoundedCornerShape(20.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(chipShape)
                            .background(if (seleccionado) EcoColors.Suave else EcoColors.Superficie)
                            .border(1.dp, if (seleccionado) EcoColors.Accion else EcoColors.Borde, chipShape)
                            .clickable { pestañaFiltro = index }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = titulo,
                            fontSize = 11.sp,
                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
                            color = if (seleccionado) EcoColors.VerdeOscuro else EcoColors.Gris
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Filtro fecha desplegable "Filtrar: Hoy ⌄"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtrar:",
                    fontSize = 13.sp,
                    color = EcoColors.Gris,
                    fontWeight = FontWeight.Medium
                )

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .background(EcoColors.Superficie, RoundedCornerShape(12.dp))
                        .border(1.dp, EcoColors.Borde, RoundedCornerShape(12.dp))
                        .clickable { menuFechaAbierto = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = filtroFecha, fontSize = 12.sp, color = EcoColors.Texto)
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = EcoColors.Texto,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuFechaAbierto,
                        onDismissRequest = { menuFechaAbierto = false },
                        modifier = Modifier.background(EcoColors.Superficie)
                    ) {
                        listOf("Hoy", "Esta Semana", "Este Mes").forEach { f ->
                            DropdownMenuItem(
                                text = { Text(f, color = EcoColors.Texto) },
                                onClick = {
                                    filtroFecha = f
                                    menuFechaAbierto = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Contenido de solicitudes en acordeón ──────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (state) {
                CoordinatorUiState.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp),
                        color = EcoColors.Accion
                    )
                }

                is CoordinatorUiState.Exito -> {
                    val filtradas = state.solicitudes.filter {
                        when (pestañaFiltro) {
                            0 -> it.estado == EstadoSolicitud.PENDIENTE
                            1 -> it.estado == EstadoSolicitud.APROBADA
                            2 -> it.estado == EstadoSolicitud.RECHAZADA
                            else -> true
                        }
                    }

                    if (filtradas.isEmpty()) {
                        Text(
                            text = "No hay datos",
                            color = EcoColors.Gris,
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(filtradas, key = { it.id }) { sol ->
                                CoordinatorAccordionCard(
                                    solicitud = sol,
                                    mostrarAcciones = pestañaFiltro == 0,
                                    onAceptar = { onResolverSolicitud(sol.id, true) },
                                    onRechazar = { onResolverSolicitud(sol.id, false) }
                                )
                            }
                        }
                    }
                }

                CoordinatorUiState.Vacio -> {
                    EcoStateCard(
                        title = "No hay datos",
                        message = "No hay solicitudes registradas en el servidor.",
                        actionText = "Actualizar",
                        onActionClick = onRetry,
                        modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp)
                    )
                }

                CoordinatorUiState.Fallo -> {
                    EcoStateCard(
                        title = "Error al sincronizar",
                        message = "No se pudieron obtener las solicitudes.",
                        actionText = "Reintentar",
                        onActionClick = onRetry,
                        isError = true,
                        modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp)
                    )
                }
            }
        }

        // Barra inferior: Inicio / Historial
        EcoBottomBar(
            items = destinosCoordinador,
            selectedIndex = selectedBottomTab,
            onSelect = onBottomTabSelected
        )
    }
}

/** Tarjeta acordeón expandible exactamente igual al mockup */
@Composable
private fun CoordinatorAccordionCard(
    solicitud: CoordinatorSolicitudUi,
    mostrarAcciones: Boolean,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    var expandido by remember { mutableStateOf(mostrarAcciones) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, cardShape, ambientColor = Color.Black.copy(0.06f), spotColor = Color.Black.copy(0.06f))
            .background(EcoColors.Superficie, cardShape)
            .border(1.dp, EcoColors.Borde, cardShape)
            .clickable { expandido = !expandido }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = solicitud.organizacion,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = EcoColors.Texto
            )

            Icon(
                imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = EcoColors.Texto,
                modifier = Modifier.size(20.dp)
            )
        }

        // Si no está expandido pero ya está aprobada o rechazada, muestra su etiqueta y fecha
        if (!expandido && !mostrarAcciones) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (solicitud.estado == EstadoSolicitud.APROBADA) {
                    EcoTag("Aprobada", backgroundColor = EcoColors.Suave, textColor = EcoColors.VerdeOscuro)
                } else if (solicitud.estado == EstadoSolicitud.RECHAZADA) {
                    EcoTag("Rechazada", backgroundColor = Color(0xFFFFEBEE), textColor = Color(0xFFC62828))
                }
                Text(solicitud.fecha, fontSize = 10.sp, color = EcoColors.Gris)
            }
        }

        // Vista expandida con detalles y auto-validación
        AnimatedVisibility(visible = expandido) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Nombre alimento:", fontSize = 11.sp, color = EcoColors.Gris)
                    Text(solicitud.nombreAlimento, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = EcoColors.Texto)
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Cantidad:", fontSize = 11.sp, color = EcoColors.Gris)
                    Text(solicitud.cantidad, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = EcoColors.Texto)
                }

                // Cuadro de Auto-validación del Sistema
                val valShape = RoundedCornerShape(12.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EcoColors.MensajeFondo, valShape)
                        .border(1.dp, EcoColors.Borde, valShape)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Auto-validación del Sistema",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoColors.Texto
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = EcoColors.Accion, modifier = Modifier.size(14.dp))
                        Text("Stock Suficiente", fontSize = 11.sp, color = EcoColors.Texto)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = EcoColors.Accion, modifier = Modifier.size(14.dp))
                        Text("Lote Vigente", fontSize = 11.sp, color = EcoColors.Texto)
                    }
                }

                // Botones Aceptar / Rechazar si está pendiente
                if (mostrarAcciones) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .background(EcoColors.Secundario)
                                .clickable { onAceptar() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aceptar", color = EcoColors.VerdeOscuro, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(50))
                                .background(EcoColors.Superficie)
                                .border(1.dp, EcoColors.Borde, RoundedCornerShape(50))
                                .clickable { onRechazar() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Rechazar", color = EcoColors.Texto, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Text(
                    text = "🕒 ${solicitud.fecha}",
                    fontSize = 10.sp,
                    color = EcoColors.Gris,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                )
            }
        }
    }
}

// ── Vistas previas ────────────────────────────────────────────────────────────
private val solicitudesDemoCoordinador = listOf(
    CoordinatorSolicitudUi(1, "Fundación Sonrisas", "Papas", "50Kg", "20 noviembre 2026 - 13:54", EstadoSolicitud.PENDIENTE),
    CoordinatorSolicitudUi(2, "Iglesia Bautista", "Manzanas", "30Kg", "20 noviembre 2026 - 13:54", EstadoSolicitud.PENDIENTE),
    CoordinatorSolicitudUi(3, "Refugio Como en Casa", "Pan", "15Kg", "20 noviembre 2026 - 13:54", EstadoSolicitud.PENDIENTE)
)

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun CoordinatorHomeExactPreview() {
    EcoTheme(darkTheme = false) {
        CoordinatorHomeScreen(
            state = CoordinatorUiState.Exito(solicitudesDemoCoordinador),
            selectedBottomTab = 0,
            onBottomTabSelected = {},
            onResolverSolicitud = { _, _ -> },
            onRetry = {}
        )
    }
}
