package com.example.ecoabastos.ui.screens.donante

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.components.BottomBarItem
import com.example.ecoabastos.ui.components.EcoBottomBar
import com.example.ecoabastos.ui.components.EcoStateCard
import com.example.ecoabastos.data.model.EstadoLote
import com.example.ecoabastos.ui.components.HomeHeader
import com.example.ecoabastos.ui.components.LoteCard
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/** Los datos de un lote tal como los muestra la tarjeta (ya listos para pintar). */
data class LoteUi(
    val id: Long,
    val nombre: String,
    val cantidad: String,
    val categoria: String,
    val vigencia: String,
    val imagenUrl: String? = null,
    val estado: EstadoLote = EstadoLote.DISPONIBLE
)

/** Los cuatro estados de la pantalla: cargando, con lotes, vacío y error. */
sealed interface PublicacionesUiState {
    data object Cargando : PublicacionesUiState
    data class Exito(val lotes: List<LoteUi>) : PublicacionesUiState
    data object Vacio : PublicacionesUiState
    data object Fallo : PublicacionesUiState
}

private val destinosDonador = listOf(
    BottomBarItem("Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    BottomBarItem("Historial", Icons.Outlined.Search, Icons.Filled.Search),
    BottomBarItem("Perfil", Icons.Outlined.Person, Icons.Filled.Person)
)

/**
 * Pantalla principal del donador: "Tus publicaciones" (solo diseño).
 * No habla con el backend: recibe el estado y avisa los toques mediante funciones.
 */
@Composable
fun DonorHomeScreen(
    nombreUsuario: String,
    state: PublicacionesUiState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onAddLoteClick: () -> Unit,
    onEditLote: (Long) -> Unit,
    onLoteDetail: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var busqueda by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
    ) {
        HomeHeader(
            nombre = nombreUsuario,
            titulo = "Tus Publicaciones",
            busqueda = busqueda,
            onBusquedaChange = { busqueda = it },
            onAddClick = onAddLoteClick
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (state) {
                PublicacionesUiState.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 96.dp),
                        color = EcoColors.Accion
                    )
                }

                is PublicacionesUiState.Exito -> {
                    val visibles = state.lotes.filter {
                        it.nombre.contains(busqueda.trim(), ignoreCase = true)
                    }
                    if (visibles.isEmpty()) {
                        Text(
                            text = "No hay lotes con ese nombre.",
                            color = EcoColors.Gris,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 76.dp)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 36.dp,
                                end = 36.dp,
                                top = 14.dp,
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            items(visibles, key = { it.id }) { lote ->
                                LoteCard(
                                    nombre = lote.nombre,
                                    cantidad = lote.cantidad,
                                    categoria = lote.categoria,
                                    vigencia = lote.vigencia,
                                    estado = lote.estado,
                                    imagenUrl = lote.imagenUrl,
                                    onEditClick = { onEditLote(lote.id) },
                                    onDetailClick = { onLoteDetail(lote.id) }
                                )
                            }
                        }
                    }
                }

                PublicacionesUiState.Vacio -> {
                    EcoStateCard(
                        title = "Aún no tienes publicaciones",
                        message = "Publica tu primer lote para compartir los alimentos disponibles.",
                        actionText = "Publicar lote",
                        onActionClick = onAddLoteClick,
                        modifier = Modifier.padding(top = 76.dp, start = 24.dp, end = 24.dp)
                    )
                }

                PublicacionesUiState.Fallo -> {
                    EcoStateCard(
                        title = "No se cargaron tus publicaciones",
                        message = "Comprueba tu conexión. Tus publicaciones no se han eliminado.",
                        actionText = "Reintentar",
                        onActionClick = onRetry,
                        isError = true,
                        modifier = Modifier.padding(top = 76.dp, start = 24.dp, end = 24.dp)
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

// ---------- Vistas previas ----------

private val lotesEjemplo = listOf(
    LoteUi(1, "Manzanas", "15Kg", "Frutas", "Vence en 4 días"),
    LoteUi(2, "Pan", "15Kg", "Panadería", "Vence en 4 días"),
    LoteUi(3, "Papas", "15Kg", "Verduras", "Vence en 4 días")
)

@Composable
private fun PreviewPantalla(state: PublicacionesUiState, oscuro: Boolean = false) {
    EcoTheme(darkTheme = oscuro) {
        DonorHomeScreen(
            nombreUsuario = "Juan",
            state = state,
            selectedTab = 0,
            onTabSelected = {},
            onAddLoteClick = {},
            onEditLote = {},
            onLoteDetail = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Éxito")
@Composable
private fun DonorHomeExitoPreview() =
    PreviewPantalla(PublicacionesUiState.Exito(lotesEjemplo))

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Vacío")
@Composable
private fun DonorHomeVacioPreview() =
    PreviewPantalla(PublicacionesUiState.Vacio)

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Error")
@Composable
private fun DonorHomeFalloPreview() =
    PreviewPantalla(PublicacionesUiState.Fallo)

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Oscuro")
@Composable
private fun DonorHomeOscuroPreview() =
    PreviewPantalla(PublicacionesUiState.Exito(lotesEjemplo), oscuro = true)
