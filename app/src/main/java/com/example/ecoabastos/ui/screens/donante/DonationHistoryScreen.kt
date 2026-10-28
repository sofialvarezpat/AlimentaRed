package com.example.ecoabastos.ui.screens.donante

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.components.BottomBarItem
import com.example.ecoabastos.ui.components.EcoActionButton
import com.example.ecoabastos.ui.components.EcoBottomBar
import com.example.ecoabastos.ui.components.EcoDropdown
import com.example.ecoabastos.ui.components.EcoStateCard
import com.example.ecoabastos.ui.components.EntregaCard
import com.example.ecoabastos.ui.components.EntregaEstadoUi
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

data class EntregaUi(
    val id: Long,
    val nombreLote: String,
    val categoria: String,
    val cantidad: String,
    val estado: EntregaEstadoUi,
    val recogidoPor: String,
    val fecha: String,
    val hora: String = "10:30 a. m.",
    val unidadMedida: String = "kg",
    val observaciones: String = ""
)

sealed interface HistorialUiState {
    data object Cargando : HistorialUiState
    data class Exito(val entregas: List<EntregaUi>) : HistorialUiState
    data object Vacio : HistorialUiState
    data object Fallo : HistorialUiState
}

private val destinosDonador = listOf(
    BottomBarItem("Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    BottomBarItem("Historial", Icons.Outlined.Search, Icons.Filled.Search),
    BottomBarItem("Perfil", Icons.Outlined.Person, Icons.Filled.Person)
)

private val FiltrosEstado = listOf("Todos", "Pendiente", "Recogido", "Incidencia")

@Composable
fun DonationHistoryScreen(
    state: HistorialUiState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onPublishLoteClick: () -> Unit,
    onVerDetalle: (EntregaUi) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit = onRetry,
    modifier: Modifier = Modifier
) {
    var filtro by remember { mutableStateOf("") }
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
    ) {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = statusBar + 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial de Donaciones",
                    color = EcoColors.Texto,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = EcoColors.Accion
                    )
                }
            }
            EcoActionButton(
                text = "+ Publicar Lote",
                onClick = onPublishLoteClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
            EcoDropdown(
                value = filtro,
                onValueChange = { filtro = it },
                options = FiltrosEstado,
                placeholder = "Filtrar",
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (state) {
                HistorialUiState.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 48.dp),
                        color = EcoColors.Accion
                    )
                }

                is HistorialUiState.Exito -> {
                    val visibles = state.entregas.filter {
                        filtro.isBlank() || filtro.equals("Todos", ignoreCase = true) || it.estado.etiqueta.equals(filtro, ignoreCase = true)
                    }
                    if (visibles.isEmpty()) {
                        Text(
                            text = "No hay entregas con ese filtro.",
                            color = EcoColors.Gris,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 32.dp)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(visibles, key = { it.id }) { entrega ->
                                EntregaCard(
                                    nombreLote = entrega.nombreLote,
                                    categoria = entrega.categoria,
                                    cantidad = entrega.cantidad,
                                    estado = entrega.estado,
                                    recogidoPor = entrega.recogidoPor,
                                    fecha = entrega.fecha,
                                    onVerDetalleClick = { onVerDetalle(entrega) }
                                )
                            }
                        }
                    }
                }

                HistorialUiState.Vacio -> {
                    EcoStateCard(
                        title = "AÃºn no tienes donaciones",
                        message = "Cuando confirmes la entrega de un lote, aparecerÃ¡ aquÃ­.",
                        actionText = "Publicar lote",
                        onActionClick = onPublishLoteClick,
                        modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp)
                    )
                }

                HistorialUiState.Fallo -> {
                    EcoStateCard(
                        title = "No se cargÃ³ tu historial",
                        message = "Comprueba tu conexiÃ³n. Tu historial no se ha eliminado.",
                        actionText = "Reintentar",
                        onActionClick = onRetry,
                        isError = true,
                        modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp)
                    )
                }
            }
        }

        EcoBottomBar(
            items = destinosDonador,
            selectedIndex = selectedTab,
            onSelect = onTabSelected
        )
    }
}

// â”€â”€â”€ Vistas previas â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
private val entregasEjemplo = listOf(
    EntregaUi(1, "Manzanas", "Fruta", "15Kg", EntregaEstadoUi.COMPLETADO, "FundaciÃ³n Sonrisas", "2026-10-20"),
    EntregaUi(2, "Pan", "PanaderÃ­a", "10Kg", EntregaEstadoUi.PROGRAMADA, "", "")
)

@Composable
private fun PreviewPantalla(state: HistorialUiState, oscuro: Boolean = false) {
    EcoTheme(darkTheme = oscuro) {
        DonationHistoryScreen(
            state = state,
            selectedTab = 1,
            onTabSelected = {},
            onPublishLoteClick = {},
            onVerDetalle = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Ã‰xito")
@Composable
private fun DonationHistoryExitoPreview() = PreviewPantalla(HistorialUiState.Exito(entregasEjemplo))

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "VacÃ­o")
@Composable
private fun DonationHistoryVacioPreview() = PreviewPantalla(HistorialUiState.Vacio)

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Error")
@Composable
private fun DonationHistoryFalloPreview() = PreviewPantalla(HistorialUiState.Fallo)
