package com.example.ecoabastos.ui.screens.organizacion

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.data.model.EstadoSolicitud
import com.example.ecoabastos.ui.components.EcoActionButton
import com.example.ecoabastos.ui.components.EcoBottomBar
import com.example.ecoabastos.ui.components.EcoDropdown
import com.example.ecoabastos.ui.components.EcoTextField
import com.example.ecoabastos.ui.components.SegmentedControl
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

/** UI model para el estado de una solicitud con todos sus datos */
data class SolicitudUi(
    val id: Long,
    val nombreLote: String,
    val cantidadSolicitada: String,
    val estado: EstadoSolicitud,
    val fecha: String,
    val fechaRetiro: String? = null,
    val novedades: String? = null,
    val recogido: Boolean = false
)

sealed interface SolicitudesUiState {
    data object Cargando : SolicitudesUiState
    data class Exito(val solicitudes: List<SolicitudUi>) : SolicitudesUiState
    data object Vacio : SolicitudesUiState
    data object Fallo : SolicitudesUiState
}

private val CategoriasSolicitud = listOf("Frutas", "Verduras", "Panadería", "Lácteos", "Carnes", "Otros")
private val UnidadesSolicitud = listOf("Kg", "g", "L", "ml", "Unidades")

/**
 * Pantalla de Solicitudes: 2 pestañas según el mockup exacto.
 * ─ Pestaña 1 "Solicitud"        → formulario para crear solicitud
 * ─ Pestaña 2 "Estado Solicitud" → seguimiento paso a paso por solicitud seleccionada
 */
@Composable
fun OrganizationRequestsScreen(
    loteSeleccionado: CatalogoLoteUi? = null,
    solicitudesState: SolicitudesUiState,
    mensajeExito: String? = null,
    isRecogiendo: Boolean = false,
    onMensajeConsumido: () -> Unit = {},
    selectedTab: Int,          // barra inferior
    onTabSelected: (Int) -> Unit,
    onGenerarSolicitud: (nombreAlimento: String, categoria: String, cantidad: Double, unidad: String, detalles: String) -> Unit,
    onConfirmarRetiro: (solicitudId: Long, novedades: String) -> Unit,
    onIrAlCatalogo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar Snackbar cuando hay mensaje (éxito o error de API)
    LaunchedEffect(mensajeExito) {
        if (mensajeExito != null) {
            snackbarHostState.showSnackbar(mensajeExito)
            onMensajeConsumido()
        }
    }
    // pestaña interna 0 = Solicitud, 1 = Estado Solicitud
    var pestanaInterna by rememberSaveable { mutableIntStateOf(0) }

    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = EcoColors.Fondo
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            // Selector de pestañas interno
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = statusBar + 12.dp, bottom = 8.dp)
            ) {
                SegmentedControl(
                    opciones = listOf("Solicitud", "Estado Solicitud"),
                    seleccionado = pestanaInterna,
                    onSeleccionar = { pestanaInterna = it }
                )
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (pestanaInterna) {
                    0 -> PanelFormularioSolicitud(
                        lotePreseleccionado = loteSeleccionado,
                        onGenerarSolicitud = onGenerarSolicitud
                    )
                    else -> PanelEstadoSolicitud(
                        state = solicitudesState,
                        isRecogiendo = isRecogiendo,
                        onConfirmarRetiro = onConfirmarRetiro,
                        onIrAlCatalogo = onIrAlCatalogo
                    )
                }
            }

            EcoBottomBar(
                items = destinosOrganizacion,
                selectedIndex = selectedTab,
                onSelect = onTabSelected
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Panel 1: Formulario "Solicitud"
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun PanelFormularioSolicitud(
    lotePreseleccionado: CatalogoLoteUi?,
    onGenerarSolicitud: (String, String, Double, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf(lotePreseleccionado?.nombre ?: "") }
    var categoria by remember { mutableStateOf(lotePreseleccionado?.categoria ?: "") }
    var cantidad by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("Kg") }
    var detalles by remember { mutableStateOf("") }

    val cantidadDouble = cantidad.replace(",", ".").toDoubleOrNull()
    val valido = nombre.isNotBlank() && categoria.isNotBlank() && cantidadDouble != null && cantidadDouble > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LabelCampo("Nombre del Alimento") {
            EcoTextField(value = nombre, onValueChange = { nombre = it }, placeholder = "Ej: Fresas")
        }

        LabelCampo("Categoría") {
            EcoDropdown(value = categoria, onValueChange = { categoria = it },
                options = CategoriasSolicitud, placeholder = "Frutas")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                LabelCampo("Cantidad") {
                    EcoTextField(value = cantidad, onValueChange = { cantidad = it },
                        placeholder = "50", keyboardType = KeyboardType.Number)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                LabelCampo("Unidad de medida") {
                    EcoDropdown(value = unidad, onValueChange = { unidad = it },
                        options = UnidadesSolicitud, placeholder = "Kg")
                }
            }
        }

        LabelCampo("Detalles del retiro") {
            EcoTextField(value = detalles, onValueChange = { detalles = it },
                placeholder = "", minLines = 3)
        }

        Spacer(Modifier.height(8.dp))

        EcoActionButton(
            text = "Generar Solicitud",
            onClick = {
                onGenerarSolicitud(nombre.trim(), categoria, cantidadDouble ?: 0.0, unidad, detalles.trim())
            },
            modifier = Modifier.fillMaxWidth(),
            containerColor = EcoColors.Secundario,
            contentColor = EcoColors.VerdeOscuro,
            enabled = valido
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Panel 2: "Estado Solicitud" – selector + stepper + confirmar retiro
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun PanelEstadoSolicitud(
    state: SolicitudesUiState,
    onConfirmarRetiro: (Long, String) -> Unit,
    onIrAlCatalogo: () -> Unit,
    isRecogiendo: Boolean = false
) {
    when (state) {
        SolicitudesUiState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = EcoColors.Accion)
        }

        SolicitudesUiState.Vacio -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No hay datos", fontWeight = FontWeight.Bold, color = EcoColors.Texto, fontSize = 16.sp)
                Text("No tienes solicitudes activas pendientes de retiro.",
                    color = EcoColors.Gris, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(20.dp))
                EcoActionButton(text = "Ir al Catálogo", onClick = onIrAlCatalogo,
                    modifier = Modifier.fillMaxWidth(), containerColor = EcoColors.Accion)
            }
        }

        SolicitudesUiState.Fallo -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Error al cargar solicitudes", fontWeight = FontWeight.Bold, color = EcoColors.Texto, fontSize = 16.sp)
                Text("No se pudo conectar con el servidor.",
                    color = EcoColors.Gris, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.height(20.dp))
                EcoActionButton(text = "Ir al Catálogo", onClick = onIrAlCatalogo,
                    modifier = Modifier.fillMaxWidth(), containerColor = EcoColors.Accion)
            }
        }

        is SolicitudesUiState.Exito -> {
            val solicitudes = state.solicitudes

            // Solicitud seleccionada – por defecto la primera
            var solicitudSeleccionadaId by rememberSaveable {
                mutableStateOf(solicitudes.firstOrNull()?.id)
            }

            val solicitud = solicitudes.find { it.id == solicitudSeleccionadaId }
                ?: solicitudes.firstOrNull()

            if (solicitud == null) {
                PanelEstadoSolicitud(state = SolicitudesUiState.Vacio, isRecogiendo = isRecogiendo, onConfirmarRetiro = onConfirmarRetiro, onIrAlCatalogo = onIrAlCatalogo)
                return
            }

            var novedades by remember(solicitud.id) { mutableStateOf(solicitud.novedades ?: "") }
            var mostrarDialogoRechazado by remember(solicitud.id, solicitud.estado) {
                mutableStateOf(solicitud.estado == EstadoSolicitud.RECHAZADA)
            }

            if (mostrarDialogoRechazado) {
                AlertDialog(
                    onDismissRequest = { mostrarDialogoRechazado = false },
                    title = { Text("Solicitud Rechazada", fontWeight = FontWeight.Bold) },
                    text = {
                        Text("Esta solicitud para '${solicitud.nombreLote}' ha sido rechazada por la coordinación.")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            mostrarDialogoRechazado = false
                            onIrAlCatalogo()
                        }) {
                            Text("Ir al Catálogo", color = EcoColors.VerdeOscuro)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogoRechazado = false }) {
                            Text("Cerrar", color = EcoColors.Gris)
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Selector de solicitud ────────────────────────────────
                if (solicitudes.size > 1) {
                    SolicitudSelector(
                        solicitudes = solicitudes,
                        seleccionadaId = solicitud.id,
                        onSeleccionar = { solicitudSeleccionadaId = it }
                    )
                } else {
                    DetalleCard {
                        Text("Solicitud activa", color = EcoColors.Gris, fontSize = 12.sp)
                        Text(
                            solicitud.nombreLote,
                            color = EcoColors.Texto,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(solicitud.cantidadSolicitada, color = EcoColors.Gris, fontSize = 12.sp)
                    }
                }

                // ── Stepper de pasos (depende EXCLUSIVAMENTE del estado persistido en backend) ──
                StepperSolicitud(estado = solicitud.estado, esRetirado = solicitud.recogido)

                // ── Fecha y hora del retiro ───────────────────────────────
                if (solicitud.fechaRetiro != null) {
                    DetalleCard {
                        Text("Fecha Y hora del Retiro", color = EcoColors.Gris, fontSize = 12.sp)
                        Text(
                            solicitud.fechaRetiro,
                            color = EcoColors.Texto,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // ── Novedades del retiro ──────────────────────────────────
                LabelCampo("Novedades del Retiro") {
                    EcoTextField(
                        value = novedades,
                        onValueChange = { novedades = it },
                        placeholder = "",
                        minLines = 3
                    )
                }

                Spacer(Modifier.height(4.dp))

                // ── Confirmar Retiro (Sin mutación local optimista; espera HTTP 2xx) ──
                EcoActionButton(
                    text = when {
                        isRecogiendo -> "Confirmando en servidor..."
                        solicitud.recogido -> "Lote Recogido"
                        else -> "Recoger Lote"
                    },
                    onClick = {
                        onConfirmarRetiro(solicitud.id, novedades.trim())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = if (solicitud.recogido) EcoColors.VerdeFondo else EcoColors.Secundario,
                    contentColor = if (solicitud.recogido) EcoColors.VerdeOscuro else EcoColors.Texto,
                    enabled = !isRecogiendo && !solicitud.recogido && solicitud.estado == EstadoSolicitud.APROBADA
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Selector de solicitud (dropdown con nombre + estado + fecha)
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun SolicitudSelector(
    solicitudes: List<SolicitudUi>,
    seleccionadaId: Long,
    onSeleccionar: (Long) -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val seleccionada = solicitudes.find { it.id == seleccionadaId } ?: solicitudes.first()

    val shape = RoundedCornerShape(12.dp)

    Text(
        text = "Selecciona una solicitud",
        color = EcoColors.Texto,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    )

    Spacer(Modifier.height(4.dp))

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, shape, ambientColor = Color.Black.copy(0.06f), spotColor = Color.Black.copy(0.06f))
                .background(EcoColors.Superficie, shape)
                .border(1.dp, EcoColors.Borde, shape)
                .clip(shape)
                .clickable { menuAbierto = true }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = seleccionada.nombreLote,
                    color = EcoColors.Texto,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${estadoTexto(seleccionada.estado)}  ·  ${seleccionada.fecha}",
                    color = EcoColors.Gris,
                    fontSize = 12.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = EcoColors.Gris,
                modifier = Modifier.size(20.dp)
            )
        }

        DropdownMenu(
            expanded = menuAbierto,
            onDismissRequest = { menuAbierto = false },
            modifier = Modifier.background(EcoColors.Superficie)
        ) {
            solicitudes.forEach { s ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = s.nombreLote,
                                color = EcoColors.Texto,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${estadoTexto(s.estado)}  ·  ${s.fecha}",
                                color = EcoColors.Gris,
                                fontSize = 12.sp
                            )
                        }
                    },
                    onClick = {
                        onSeleccionar(s.id)
                        menuAbierto = false
                    }
                )
            }
        }
    }
}

private fun estadoTexto(estado: EstadoSolicitud): String = when (estado) {
    EstadoSolicitud.PENDIENTE -> "Solicitado"
    EstadoSolicitud.APROBADA -> "Aprobado"
    EstadoSolicitud.RECHAZADA -> "Rechazado"
    EstadoSolicitud.CANCELADA -> "Cancelado"
}

// ──────────────────────────────────────────────────────────────────────────────
// Stepper: Solicitado → Aprobado → En retiro → Retirado
// ──────────────────────────────────────────────────────────────────────────────
@Composable
private fun StepperSolicitud(estado: EstadoSolicitud, esRetirado: Boolean = false) {
    val pasos = listOf("Solicitado", "Aprobado", "En retiro", "Retirado")
    val pasoActivo = when {
        esRetirado -> 3
        estado == EstadoSolicitud.PENDIENTE -> 0
        estado == EstadoSolicitud.APROBADA -> 2
        else -> -1
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
                    modifier = Modifier.size(24.dp)
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

@Composable
private fun DetalleCard(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, ambientColor = Color.Black.copy(0.06f), spotColor = Color.Black.copy(0.06f))
            .background(EcoColors.Superficie, shape)
            .border(1.dp, EcoColors.Borde, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        content = { content() }
    )
}

@Composable
private fun LabelCampo(label: String, campo: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, color = EcoColors.Texto, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        campo()
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 874, name = "Solicitud - Formulario")
@Composable
private fun OrgRequestsFormPreview() {
    EcoTheme(darkTheme = false) {
        OrganizationRequestsScreen(
            loteSeleccionado = CatalogoLoteUi(1, "Fresas", "Alacena JR", "150 Kg", "Frutas"),
            solicitudesState = SolicitudesUiState.Vacio,
            selectedTab = 1,
            onTabSelected = {},
            onGenerarSolicitud = { _, _, _, _, _ -> },
            onConfirmarRetiro = { _, _ -> },
            onIrAlCatalogo = {}
        )
    }
}
