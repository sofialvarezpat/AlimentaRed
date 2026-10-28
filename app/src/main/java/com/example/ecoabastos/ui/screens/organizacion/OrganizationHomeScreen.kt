package com.example.ecoabastos.ui.screens.organizacion

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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.components.BottomBarItem
import com.example.ecoabastos.ui.components.EcoActionButton
import com.example.ecoabastos.ui.components.EcoBottomBar
import com.example.ecoabastos.ui.components.EcoStateCard
import com.example.ecoabastos.ui.components.EcoTag
import com.example.ecoabastos.ui.components.EcoTextField
import com.example.ecoabastos.ui.components.LoteFoto
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/** Representa un lote disponible para solicitar */
data class CatalogoLoteUi(
    val id: Long,
    val nombre: String,
    val donante: String,
    val cantidadDisponible: String,
    val categoria: String,
    val distancia: String = "1.5 km",
    val imagenUrl: String? = null
)

sealed interface CatalogoUiState {
    data object Cargando : CatalogoUiState
    data class Exito(val lotes: List<CatalogoLoteUi>) : CatalogoUiState
    data object Vacio : CatalogoUiState
    data object Fallo : CatalogoUiState
}

val destinosOrganizacion = listOf(
    BottomBarItem("Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    BottomBarItem("Solicitudes", Icons.Outlined.ShowChart, Icons.Filled.ShowChart),
    BottomBarItem("Perfil", Icons.Outlined.Person, Icons.Filled.Person)
)

/**
 * Catálogo de alimentos: grilla 2 columnas igual al mockup con soporte para foto.
 */
@Composable
fun OrganizationHomeScreen(
    nombreOrganizacion: String,
    state: CatalogoUiState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onSolicitarClick: (CatalogoLoteUi) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var busqueda by remember { mutableStateOf("") }
    var filtroDistanciaActivo by remember { mutableStateOf(false) }
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
    ) {
        // ── Encabezado ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = statusBar + 12.dp, bottom = 8.dp)
        ) {
            // Buscador
            EcoTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = "Buscar",
                leadingIcon = Icons.Default.Search
            )
            Spacer(Modifier.height(10.dp))
            // Filtro distancia
            Text(
                text = "Filtrar:",
                color = EcoColors.Texto,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            val chipShape = RoundedCornerShape(20.dp)
            Box(
                modifier = Modifier
                    .clip(chipShape)
                    .background(if (filtroDistanciaActivo) EcoColors.VerdeOscuro else EcoColors.Superficie)
                    .border(1.dp, EcoColors.Borde, chipShape)
                    .clickable { filtroDistanciaActivo = !filtroDistanciaActivo }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "A menos de 5 Km",
                    fontSize = 12.sp,
                    color = if (filtroDistanciaActivo) Color.White else EcoColors.Texto,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── Contenido ────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (state) {
                CatalogoUiState.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp),
                        color = EcoColors.Accion
                    )
                }

                is CatalogoUiState.Exito -> {
                    val visibles = state.lotes.filter {
                        it.nombre.contains(busqueda.trim(), ignoreCase = true) ||
                                it.donante.contains(busqueda.trim(), ignoreCase = true)
                    }

                    if (visibles.isEmpty()) {
                        Text(
                            text = "No hay lotes disponibles.",
                            color = EcoColors.Gris,
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(
                                start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(visibles, key = { it.id }) { lote ->
                                CatalogoGridCard(
                                    lote = lote,
                                    onSolicitarClick = { onSolicitarClick(lote) }
                                )
                            }
                        }
                    }
                }

                CatalogoUiState.Vacio -> {
                    EcoStateCard(
                        title = "Sin alimentos disponibles",
                        message = "Actualmente no hay lotes publicados por donantes.",
                        actionText = "Actualizar",
                        onActionClick = onRetry,
                        modifier = Modifier.padding(top = 40.dp, start = 24.dp, end = 24.dp)
                    )
                }

                CatalogoUiState.Fallo -> {
                    EcoStateCard(
                        title = "Error al cargar el catálogo",
                        message = "No se pudieron obtener los alimentos disponibles.",
                        actionText = "Reintentar",
                        onActionClick = onRetry,
                        isError = true,
                        modifier = Modifier.padding(top = 40.dp, start = 24.dp, end = 24.dp)
                    )
                }
            }
        }

        EcoBottomBar(
            items = destinosOrganizacion,
            selectedIndex = selectedTab,
            onSelect = onTabSelected
        )
    }
}

/** Tarjeta individual de la grilla del catálogo con imagen real si existe */
@Composable
private fun CatalogoGridCard(
    lote: CatalogoLoteUi,
    onSolicitarClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, shape, ambientColor = Color.Black.copy(0.06f), spotColor = Color.Black.copy(0.06f))
            .background(EcoColors.Superficie, shape)
            .border(1.dp, EcoColors.Borde, shape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Foto del lote si existe
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EcoColors.Suave)
        ) {
            LoteFoto(imagenUrl = lote.imagenUrl)
        }

        Text(
            text = "Peso Disponible: ${lote.cantidadDisponible}",
            color = EcoColors.Texto,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Donador: ${lote.donante}",
            color = EcoColors.Gris,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        EcoTag(
            text = "${lote.distancia} de Distancia",
            backgroundColor = EcoColors.Alerta,
            textColor = Color.White
        )
        EcoActionButton(
            text = "Solicitar Donación",
            onClick = onSolicitarClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = EcoColors.VerdeOscuro,
            height = 32.dp
        )
    }
}

// ── Vistas previas ────────────────────────────────────────────────────────
private val catalogoDemo = listOf(
    CatalogoLoteUi(1, "Fresas", "Alacena JR", "150 Kg", "Frutas", "1.5 km"),
    CatalogoLoteUi(2, "Manzanas", "Alacena JR", "150 Kg", "Frutas", "1.5 km"),
    CatalogoLoteUi(3, "Papas", "Alacena JR", "150 Kg", "Verduras", "1.3 km"),
    CatalogoLoteUi(4, "Pan", "Alacena JR", "150 Kg", "Panadería", "1.5 km")
)

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Catálogo")
@Composable
private fun OrgHomePreview() {
    EcoTheme(darkTheme = false) {
        OrganizationHomeScreen(
            nombreOrganizacion = "Romeo iglesias",
            state = CatalogoUiState.Exito(catalogoDemo),
            selectedTab = 0,
            onTabSelected = {},
            onSolicitarClick = {},
            onRetry = {}
        )
    }
}
