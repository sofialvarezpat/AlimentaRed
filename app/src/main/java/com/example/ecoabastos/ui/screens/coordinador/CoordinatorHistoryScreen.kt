package com.example.ecoabastos.ui.screens.coordinador

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.components.EcoBottomBar
import com.example.ecoabastos.ui.components.EcoStateCard
import com.example.ecoabastos.ui.components.EcoTag
import com.example.ecoabastos.ui.components.EcoTextField
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

data class CoordinatorHistorialItemUi(
    val id: Long,
    val organizacion: String,
    val estadoTexto: String = "",
    val fechaRetiro: String = "",
    val novedades: String = ""
)

sealed interface CoordinatorHistorialUiState {
    data object Cargando : CoordinatorHistorialUiState
    data class Exito(val items: List<CoordinatorHistorialItemUi>) : CoordinatorHistorialUiState
    data object Vacio : CoordinatorHistorialUiState
    data object Fallo : CoordinatorHistorialUiState
}

/**
 * Pantalla "Historial de solicitudes" y detalle "Historial Solicitud" del Coordinador.
 * Coincide 100% con los Frames 5 y 6 del mockup.
 */
@Composable
fun CoordinatorHistoryScreen(
    state: CoordinatorHistorialUiState,
    selectedBottomTab: Int,
    onBottomTabSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var itemSeleccionadoParaDetalle by remember { mutableStateOf<CoordinatorHistorialItemUi?>(null) }
    var filtroFecha by remember { mutableStateOf("Hoy") }
    var menuFechaAbierto by remember { mutableStateOf(false) }

    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
    ) {
        // Si hay un item seleccionado, muestra la vista detallada (Frame 6)
        if (itemSeleccionadoParaDetalle != null) {
            val item = itemSeleccionadoParaDetalle!!
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(statusBar + 16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { itemSeleccionadoParaDetalle = null }
                        .padding(bottom = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = EcoColors.Texto,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "  Historial Solicitud",
                        color = EcoColors.Texto,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Tarjeta organización con icono
                val cardShape = RoundedCornerShape(14.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, cardShape, ambientColor = Color.Black.copy(0.06f))
                        .background(EcoColors.Superficie, cardShape)
                        .border(1.dp, EcoColors.Borde, cardShape)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CorporateFare,
                            contentDescription = null,
                            tint = EcoColors.Texto,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = item.organizacion,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = EcoColors.Texto
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = EcoColors.Texto
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Stepper de estados
                StepperCoordinador(item.estadoTexto)

                Spacer(Modifier.height(20.dp))

                // Fecha y hora del retiro
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "FECHA Y HORA DEL RETIRO",
                        fontSize = 11.sp,
                        color = EcoColors.Gris,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .background(EcoColors.Superficie, RoundedCornerShape(12.dp))
                            .border(1.dp, EcoColors.Borde, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(text = item.fechaRetiro, fontSize = 13.sp, color = EcoColors.Texto)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Novedades del retiro
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "NOVEDADES DEL RETIRO",
                        fontSize = 11.sp,
                        color = EcoColors.Gris,
                        fontWeight = FontWeight.Bold
                    )
                    EcoTextField(
                        value = item.novedades,
                        onValueChange = {},
                        placeholder = "",
                        minLines = 3
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        } else {
            // Vista de listado del Historial (Frame 5)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = statusBar + 14.dp, bottom = 10.dp)
            ) {
                Text(
                    text = "Historial de solicitudes",
                    color = EcoColors.Texto,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(10.dp))

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

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state) {
                    CoordinatorHistorialUiState.Cargando -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp),
                            color = EcoColors.Accion
                        )
                    }

                    is CoordinatorHistorialUiState.Exito -> {
                        if (state.items.isEmpty()) {
                            EcoStateCard(
                                title = "No hay datos",
                                message = "Las entregas e incidencias resueltas aparecerán aquí.",
                                actionText = "Actualizar",
                                onActionClick = onRetry,
                                modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp)
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(state.items, key = { it.id }) { item ->
                                    val shape = RoundedCornerShape(16.dp)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(3.dp, shape, ambientColor = Color.Black.copy(0.06f))
                                            .background(EcoColors.Superficie, shape)
                                            .border(1.dp, EcoColors.Borde, shape)
                                            .clickable { itemSeleccionadoParaDetalle = item }
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item.organizacion,
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EcoColors.Texto
                                            )

                                            Icon(
                                                imageVector = Icons.Default.KeyboardArrowDown,
                                                contentDescription = null,
                                                tint = EcoColors.Texto,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        EcoTag(
                                            text = item.estadoTexto,
                                            backgroundColor = EcoColors.Suave,
                                            textColor = EcoColors.VerdeOscuro
                                        )
                                    }
                                }
                            }
                        }
                    }

                    CoordinatorHistorialUiState.Vacio -> {
                        EcoStateCard(
                            title = "No hay datos",
                            message = "Las entregas e incidencias resueltas aparecerán aquí.",
                            actionText = "Actualizar",
                            onActionClick = onRetry,
                            modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp)
                        )
                    }

                    CoordinatorHistorialUiState.Fallo -> {
                        EcoStateCard(
                            title = "Error al cargar historial",
                            message = "Comprueba tu conexión e intenta de nuevo.",
                            actionText = "Reintentar",
                            onActionClick = onRetry,
                            isError = true,
                            modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp)
                        )
                    }
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

/** Stepper visual del historial del coordinador */
@Composable
private fun StepperCoordinador(estadoTexto: String) {
    val pasos = listOf("Solicitado", "Aprobado", "En retiro", "Retirado")
    val pasoActivo = when (estadoTexto) {
        "Solicitado" -> 0
        "Aprobado" -> 1
        "En retiro", "En proceso" -> 2
        "Retirado", "Finalizado" -> 3
        else -> 2
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        pasos.forEachIndexed { index, nombre ->
            val completado = index < pasoActivo
            val activo = index == pasoActivo

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (completado || activo) Icons.Outlined.CheckCircle
                    else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = nombre,
                    tint = when {
                        completado || activo -> EcoColors.Accion
                        else -> EcoColors.Gris
                    },
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = nombre,
                    color = if (completado || activo) EcoColors.Texto else EcoColors.Gris,
                    fontSize = 10.sp,
                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (index < pasos.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = if (index < pasoActivo) EcoColors.Accion else EcoColors.Borde,
                    thickness = 2.dp
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun CoordinatorHistoryExactPreview() {
    EcoTheme(darkTheme = false) {
        CoordinatorHistoryScreen(
            state = CoordinatorHistorialUiState.Vacio,
            selectedBottomTab = 1,
            onBottomTabSelected = {},
            onRetry = {}
        )
    }
}
