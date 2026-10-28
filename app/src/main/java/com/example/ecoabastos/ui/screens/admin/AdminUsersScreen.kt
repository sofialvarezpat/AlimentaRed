package com.example.ecoabastos.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.data.model.Rol
import com.example.ecoabastos.ui.components.EcoBottomBar
import com.example.ecoabastos.ui.components.EcoStateCard
import com.example.ecoabastos.ui.components.EcoTag
import com.example.ecoabastos.ui.theme.EcoColors
import com.example.ecoabastos.ui.theme.EcoTheme

data class AdminUsuarioUi(
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: Rol = Rol.DONANTE,
    val activo: Boolean = true
)

sealed interface AdminUsuariosUiState {
    data object Cargando : AdminUsuariosUiState
    data class Exito(val usuarios: List<AdminUsuarioUi>) : AdminUsuariosUiState
    data object Vacio : AdminUsuariosUiState
    data object Fallo : AdminUsuariosUiState
}

/**
 * Pantalla "Usuarios" del Administrador, idéntica al mockup de Figma.
 */
@Composable
fun AdminUsersScreen(
    state: AdminUsuariosUiState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onToggleUsuarioActivo: (id: Long) -> Unit,
    onFiltrarPorRol: (String) -> Unit = {},
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    var categoriaFiltro by remember { mutableStateOf("Categorías") }
    var menuAbierto by remember { mutableStateOf(false) }
    var filtroTodosActivo by remember { mutableStateOf(true) }

    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
    ) {
        // ── Encabezado ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = statusBar + 14.dp, bottom = 12.dp)
        ) {
            Text(
                text = "Usuarios",
                color = EcoColors.Texto,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Filtrar:",
                fontSize = 13.sp,
                color = EcoColors.Gris,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón "Todos" estilo píldora verde oliva
                val chipShape = RoundedCornerShape(12.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(chipShape)
                        .background(if (filtroTodosActivo) EcoColors.Secundario else EcoColors.Superficie)
                        .border(1.dp, EcoColors.Borde, chipShape)
                        .clickable {
                            filtroTodosActivo = true
                            categoriaFiltro = "Categorías"
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Todos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = EcoColors.VerdeOscuro
                    )
                }

                // Dropdown "Categorías"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(chipShape)
                        .background(EcoColors.Superficie)
                        .border(1.dp, EcoColors.Borde, chipShape)
                        .clickable { menuAbierto = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = categoriaFiltro,
                            fontSize = 13.sp,
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
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false },
                        modifier = Modifier.background(EcoColors.Superficie)
                    ) {
                        listOf("Todos", "Donante", "Receptora", "Coordinador").forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(rol, color = EcoColors.Texto) },
                                onClick = {
                                    categoriaFiltro = rol
                                    filtroTodosActivo = (rol == "Todos")
                                    menuAbierto = false
                                    onFiltrarPorRol(rol)
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Listado de tarjetas de usuario ────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (state) {
                AdminUsuariosUiState.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp),
                        color = EcoColors.Accion
                    )
                }

                is AdminUsuariosUiState.Exito -> {
                    val filtrados = state.usuarios.filter { u ->
                        when (categoriaFiltro) {
                            "Donante" -> u.rol == Rol.DONANTE
                            "Receptora" -> u.rol == Rol.RECEPTORA
                            "Coordinador" -> u.rol == Rol.COORDINADOR
                            else -> true
                        }
                    }

                    if (filtrados.isEmpty()) {
                        Text(
                            text = "No hay datos",
                            color = EcoColors.Gris,
                            fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(filtrados, key = { it.id }) { user ->
                                UsuarioMockCard(
                                    usuario = user,
                                    onToggle = { onToggleUsuarioActivo(user.id) }
                                )
                            }
                        }
                    }
                }

                AdminUsuariosUiState.Vacio -> {
                    EcoStateCard(
                        title = "No hay datos",
                        message = "No hay usuarios u organizaciones registrados en el sistema.",
                        actionText = "Actualizar",
                        onActionClick = onRetry,
                        modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp)
                    )
                }

                AdminUsuariosUiState.Fallo -> {
                    EcoStateCard(
                        title = "Error al cargar usuarios",
                        message = "Comprueba tu conexión e intenta de nuevo.",
                        actionText = "Reintentar",
                        onActionClick = onRetry,
                        isError = true,
                        modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp)
                    )
                }
            }
        }

        // Barra inferior: Solo 2 pestañas (Inicio / Usuarios)
        EcoBottomBar(
            items = destinosAdministrador,
            selectedIndex = selectedTab,
            onSelect = onTabSelected
        )
    }
}

/** Tarjeta individual de usuario exactamente igual al mockup */
@Composable
private fun UsuarioMockCard(
    usuario: AdminUsuarioUi,
    onToggle: () -> Unit
) {
    val cardShape = RoundedCornerShape(16.dp)
    var switchActivo by remember { mutableStateOf(usuario.activo) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, cardShape, ambientColor = Color.Black.copy(0.08f), spotColor = Color.Black.copy(0.08f))
            .background(EcoColors.Superficie, cardShape)
            .border(1.dp, EcoColors.Borde, cardShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar circular verde oliva con la inicial
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(EcoColors.Secundario, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = usuario.nombre.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = EcoColors.VerdeOscuro
                    )
                }

                Column {
                    Text(
                        text = usuario.nombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EcoColors.Texto
                    )
                    Text(
                        text = usuario.email,
                        fontSize = 12.sp,
                        color = EcoColors.Gris
                    )
                }
            }

            // Switch toggle a la derecha
            Switch(
                checked = switchActivo,
                onCheckedChange = {
                    switchActivo = it
                    onToggle()
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = EcoColors.Accion,
                    uncheckedThumbColor = EcoColors.Gris,
                    uncheckedTrackColor = EcoColors.Borde
                )
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Rol",
                fontSize = 12.sp,
                color = EcoColors.Gris,
                fontWeight = FontWeight.Medium
            )
            val etiquetaRol = when (usuario.rol) {
                Rol.DONANTE -> "Donante"
                Rol.RECEPTORA -> "Receptora"
                Rol.COORDINADOR -> "Coordinador"
                Rol.ADMINISTRADOR -> "Admin"
            }
            EcoTag(
                text = etiquetaRol,
                backgroundColor = EcoColors.Suave,
                textColor = EcoColors.VerdeOscuro
            )
        }
    }
}

// ── Vistas previas ────────────────────────────────────────────────────────────
private val listaUsuariosMock = listOf(
    AdminUsuarioUi(1, "Rodrigo", "Rodri@Gmail.com", Rol.DONANTE, true),
    AdminUsuarioUi(2, "Rodrigo", "Rodri@Gmail.com", Rol.DONANTE, true),
    AdminUsuarioUi(3, "Rodrigo", "Rodri@Gmail.com", Rol.DONANTE, true),
    AdminUsuarioUi(4, "Rodrigo", "Rodri@Gmail.com", Rol.DONANTE, true)
)

@Preview(showBackground = true, widthDp = 402, heightDp = 874)
@Composable
private fun AdminUsersExactPreview() {
    EcoTheme(darkTheme = false) {
        AdminUsersScreen(
            state = AdminUsuariosUiState.Exito(listaUsuariosMock),
            selectedTab = 1,
            onTabSelected = {},
            onToggleUsuarioActivo = {},
            onRetry = {}
        )
    }
}
