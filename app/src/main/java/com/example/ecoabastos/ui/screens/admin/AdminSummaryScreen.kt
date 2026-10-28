package com.example.ecoabastos.ui.screens.admin

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.components.BottomBarItem
import com.example.ecoabastos.ui.components.EcoBottomBar
import com.example.ecoabastos.ui.components.EcoStateCard
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

data class AdminResumenUi(
    val toneladasRescatadas: String = "0",
    val organizacionesImpactadas: String = "0",
    val entregasExitosas: String = "0",
    val puntosGrafica: List<Float> = emptyList(),
    val donacionesRecientes: List<HistorialDonacionRow> = emptyList()
)

data class HistorialDonacionRow(
    val fecha: String,
    val kilos: String,
    val donante: String,
    val receptor: String
)

sealed interface AdminResumenUiState {
    data object Cargando : AdminResumenUiState
    data class Exito(val resumen: AdminResumenUi) : AdminResumenUiState
    data object Vacio : AdminResumenUiState
    data object Fallo : AdminResumenUiState
}

val destinosAdministrador = listOf(
    BottomBarItem("Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    BottomBarItem("Usuarios", Icons.Outlined.Person, Icons.Filled.Person),
    BottomBarItem("Perfil", Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
)

/**
 * Pantalla "Resumen Administrador" idéntica al mockup de Figma.
 */
@Composable
fun AdminSummaryScreen(
    state: AdminResumenUiState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filtroMes by remember { mutableStateOf("Este Mes") }
    var filtroCategoria by remember { mutableStateOf("Categoria") }
    var menuCategoriaAbierto by remember { mutableStateOf(false) }

    var filtroCategoriaHistorial by remember { mutableStateOf("Categoria") }
    var menuHistorialAbierto by remember { mutableStateOf(false) }

    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
    ) {
        // ── Encabezado ────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 16.dp, top = statusBar + 14.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resumen Administrador",
                color = EcoColors.Texto,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Row {
                IconButton(onClick = { /* Exportar reporte */ }) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = "Descargar",
                        tint = EcoColors.Texto
                    )
                }
                IconButton(onClick = { /* Compartir */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Compartir",
                        tint = EcoColors.Texto
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (state) {
                AdminResumenUiState.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp),
                        color = EcoColors.Accion
                    )
                }

                is AdminResumenUiState.Exito -> {
                    val resumen = state.resumen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Filtros superiores
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Filtrar:",
                                fontSize = 13.sp,
                                color = EcoColors.Gris,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Botón Este Mes
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .background(EcoColors.Superficie, RoundedCornerShape(14.dp))
                                        .border(1.dp, EcoColors.Borde, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = filtroMes,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = EcoColors.Texto
                                    )
                                }

                                // Selector Categoría
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                        .background(EcoColors.Superficie, RoundedCornerShape(14.dp))
                                        .border(1.dp, EcoColors.Borde, RoundedCornerShape(14.dp))
                                        .clickable { menuCategoriaAbierto = true }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = filtroCategoria,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = EcoColors.Texto
                                        )
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = EcoColors.Texto,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = menuCategoriaAbierto,
                                        onDismissRequest = { menuCategoriaAbierto = false },
                                        modifier = Modifier.background(EcoColors.Superficie)
                                    ) {
                                        listOf("Todas", "Frutas", "Verduras", "Panadería", "Lácteos").forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat, color = EcoColors.Texto) },
                                                onClick = {
                                                    filtroCategoria = cat
                                                    menuCategoriaAbierto = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // ── 3 Tarjetas de Métricas en fila ────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AdminMiniCard(
                                valor = resumen.toneladasRescatadas,
                                linea1 = "Toneladas",
                                linea2 = "Alimento",
                                linea3 = "Rescatado",
                                modifier = Modifier.weight(1f)
                            )
                            AdminMiniCard(
                                valor = resumen.organizacionesImpactadas,
                                linea1 = "Organizacio",
                                linea2 = "nes",
                                linea3 = "Impactadas",
                                modifier = Modifier.weight(1f)
                            )
                            AdminMiniCard(
                                valor = resumen.entregasExitosas,
                                linea1 = "Entregas",
                                linea2 = "Exitosas",
                                linea3 = "",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // ── Gráfica: Volumen Rescatado por Mes ─────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Volumen Rescatado por Mes:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EcoColors.Texto
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = EcoColors.Texto
                                )
                            }

                            AdminVolumenChart(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                            )
                        }

                        // ── Historial Reciente de Donaciones ───────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Historial Reciente de\nDonaciones:",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 18.sp,
                                    color = EcoColors.Texto
                                )

                                Box(
                                    modifier = Modifier
                                        .height(38.dp)
                                        .background(EcoColors.Superficie, RoundedCornerShape(14.dp))
                                        .border(1.dp, EcoColors.Borde, RoundedCornerShape(14.dp))
                                        .clickable { menuHistorialAbierto = true }
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = filtroCategoriaHistorial,
                                            fontSize = 12.sp,
                                            color = EcoColors.Texto
                                        )
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = EcoColors.Texto,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = menuHistorialAbierto,
                                        onDismissRequest = { menuHistorialAbierto = false },
                                        modifier = Modifier.background(EcoColors.Superficie)
                                    ) {
                                        listOf("Todas", "Frutas", "Verduras", "Panadería").forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat, color = EcoColors.Texto) },
                                                onClick = {
                                                    filtroCategoriaHistorial = cat
                                                    menuHistorialAbierto = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Tabla de donaciones
                            val tablaShape = RoundedCornerShape(16.dp)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(2.dp, tablaShape, ambientColor = Color.Black.copy(0.06f))
                                    .background(EcoColors.Superficie, tablaShape)
                                    .border(1.dp, EcoColors.Borde, tablaShape)
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Fecha", fontSize = 11.sp, color = EcoColors.Gris, modifier = Modifier.weight(1.1f))
                                    Text("Kilos\nRescatados", fontSize = 11.sp, color = EcoColors.Gris, lineHeight = 13.sp, modifier = Modifier.weight(1f))
                                    Text("Donante / Receptor", fontSize = 11.sp, color = EcoColors.Gris, modifier = Modifier.weight(1.4f))
                                }

                                HorizontalDivider(color = EcoColors.Borde, thickness = 1.dp)

                                if (resumen.donacionesRecientes.isEmpty()) {
                                    Text(
                                        text = "No hay datos",
                                        fontSize = 13.sp,
                                        color = EcoColors.Gris,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    resumen.donacionesRecientes.forEach { fila ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(fila.fecha, fontSize = 12.sp, color = EcoColors.Texto, modifier = Modifier.weight(1.1f))
                                            Text(fila.kilos, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = EcoColors.Texto, modifier = Modifier.weight(1f))
                                            Column(modifier = Modifier.weight(1.4f)) {
                                                Text(fila.donante, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EcoColors.Texto)
                                                Text(fila.receptor, fontSize = 11.sp, color = EcoColors.Gris)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }
                }

                AdminResumenUiState.Vacio -> {
                    EcoStateCard(
                        title = "No hay datos",
                        message = "Aún no se han generado registros en el servidor.",
                        actionText = "Actualizar",
                        onActionClick = onRetry,
                        modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp)
                    )
                }

                AdminResumenUiState.Fallo -> {
                    EcoStateCard(
                        title = "Error al cargar indicadores",
                        message = "Comprueba tu conexión e intenta nuevamente.",
                        actionText = "Reintentar",
                        onActionClick = onRetry,
                        isError = true,
                        modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp)
                    )
                }
            }
        }

        // Barra inferior: 3 pestañas (Inicio / Usuarios / Perfil)
        EcoBottomBar(
            items = destinosAdministrador,
            selectedIndex = selectedTab,
            onSelect = onTabSelected
        )
    }
}

/** Tarjeta métrica vertical pequeña */
@Composable
private fun AdminMiniCard(
    valor: String,
    linea1: String,
    linea2: String,
    linea3: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .shadow(3.dp, shape, ambientColor = Color.Black.copy(0.08f))
            .background(EcoColors.Superficie, shape)
            .border(1.dp, EcoColors.Borde, shape)
            .padding(12.dp)
            .height(120.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = valor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = EcoColors.Texto
        )
        Column {
            Text(linea1, fontSize = 11.sp, color = EcoColors.Gris, lineHeight = 13.sp)
            if (linea2.isNotEmpty()) Text(linea2, fontSize = 11.sp, color = EcoColors.Gris, lineHeight = 13.sp)
            if (linea3.isNotEmpty()) Text(linea3, fontSize = 11.sp, color = EcoColors.Gris, lineHeight = 13.sp)
        }
    }
}

/** Canvas que dibuja la cuadrícula y línea exacta de "Peso en kg" */
@Composable
private fun AdminVolumenChart(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .shadow(2.dp, shape, ambientColor = Color.Black.copy(0.06f))
            .background(EcoColors.Superficie, shape)
            .border(1.dp, EcoColors.Borde, shape)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Peso en kg",
                fontSize = 10.sp,
                color = EcoColors.Gris,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Eje Y etiquetas
                Column(
                    modifier = Modifier.fillMaxHeight().padding(end = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("59,0", "58,5", "58,0", "57,5", "57,0", "56,5").forEach { yVal ->
                        Text(text = yVal, fontSize = 8.sp, color = EcoColors.Gris)
                    }
                }

                // Cuadrícula y línea gráfica
                Canvas(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    val w = size.width
                    val h = size.height

                    // Líneas horizontales de cuadrícula (6 líneas)
                    for (i in 0..5) {
                        val y = h * (i / 5f)
                        drawLine(
                            color = Color(0xFFE5E7EB),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Líneas verticales de cuadrícula (5 líneas)
                    for (i in 0..4) {
                        val x = w * (i / 4f)
                        drawLine(
                            color = Color(0xFFE5E7EB),
                            start = Offset(x, 0f),
                            end = Offset(x, h),
                            strokeWidth = 1f
                        )
                    }

                    // Puntos en escala: 56.5 min (h), 59.0 max (0)
                    fun escalaY(kg: Float): Float {
                        val min = 56.5f
                        val max = 59.0f
                        return h - ((kg - min) / (max - min)) * h
                    }

                    val pointsKg = listOf(58.0f, 57.5f, 58.5f, 57.5f, 58.5f, 57.0f, 58.0f)
                    val stepX = w / (pointsKg.size - 1)

                    val path = Path()
                    pointsKg.forEachIndexed { index, kg ->
                        val px = index * stepX
                        val py = escalaY(kg)
                        if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
                    }

                    // Dibujar línea negra
                    drawPath(path = path, color = Color.Black, style = Stroke(width = 2.5f))

                    // Dibujar puntos
                    pointsKg.forEachIndexed { index, kg ->
                        val px = index * stepX
                        val py = escalaY(kg)
                        drawCircle(color = Color.Black, radius = 3.5f, center = Offset(px, py))
                    }
                }
            }

            // Eje X etiquetas
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("21. ABR.", "1. JUN.", "13. JUL.", "24. AGO.", "5. OCT.", "16. NOV.").forEach { fecha ->
                    Text(text = fecha, fontSize = 7.sp, color = EcoColors.Gris)
                }
            }
        }
    }
}

private fun Modifier.fillMaxHeight() = this.then(Modifier.height(140.dp))

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun AdminSummaryExactPreview() {
    EcoTheme(darkTheme = false) {
        AdminSummaryScreen(
            state = AdminResumenUiState.Exito(AdminResumenUi()),
            selectedTab = 0,
            onTabSelected = {},
            onRetry = {}
        )
    }
}
