package com.example.ecoabastos.ui.screens.Profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecoabastos.ui.components.BottomBarItem
import com.example.ecoabastos.ui.components.EcoActionButton
import com.example.ecoabastos.ui.components.EcoBottomBar
import com.example.ecoabastos.ui.components.EcoContextMessage
import com.example.ecoabastos.ui.components.ProfileAvatar
import com.example.ecoabastos.ui.components.ProfileField
import com.example.ecoabastos.ui.theme.EcoColors

data class PerfilUi(
    val nombre: String,
    val email: String,
    val direccion: String
)

sealed interface PerfilUiState {
    data object Cargando : PerfilUiState
    data class Exito(val perfil: PerfilUi) : PerfilUiState
    data object Vacio : PerfilUiState
    data object Fallo : PerfilUiState
}

/**
 * Pantalla "Perfil", reutilizable para Donador y Organización receptora
 * (el diseño es idéntico en ambos roles). Se le pasan los destinos de la
 * barra inferior porque cambian según el rol.
 *
 * ⚠️ El backend solo tiene GET /api/usuarios/me: no hay forma de editar estos
 * datos ni de eliminar la cuenta todavía. onFieldClick y onDeleteAccountClick
 * quedan sin conexión real hasta que existan esos endpoints.
 */
@Composable
fun ProfileScreen(
    state: PerfilUiState,
    bottomBarItems: List<BottomBarItem>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onFieldClick: (campo: String) -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EcoColors.Fondo)
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (state) {
                PerfilUiState.Cargando -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                            .padding(top = 64.dp),
                        color = EcoColors.Accion
                    )
                }
                is PerfilUiState.Exito -> {
                    Column(
                        modifier = Modifier
                            .statusBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 28.dp)
                    ) {
                        Spacer(Modifier.height(24.dp))
                        PerfilHeader(nombre = state.perfil.nombre)
                        Spacer(Modifier.height(24.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            ProfileField(label = "Nombre", value = state.perfil.nombre, onClick = { onFieldClick("nombre") })
                            ProfileField(label = "Email", value = state.perfil.email, onClick = { onFieldClick("email") })
                            ProfileField(label = "Dirección", value = state.perfil.direccion, onClick = { onFieldClick("direccion") })
                            ProfileField(label = "Contraseña", value = "12345678", onClick = { onFieldClick("password") }, isPassword = true)
                        }
                        Spacer(Modifier.height(24.dp))
                        EcoActionButton(
                            text = "Cerrar Sesión",
                            onClick = onLogoutClick,
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = EcoColors.Secundario,
                            contentColor = EcoColors.Texto
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Eliminar Cuenta",
                            color = EcoColors.Texto,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                                .clickable(onClick = onDeleteAccountClick),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                PerfilUiState.Vacio -> {
                    Column(modifier = Modifier.statusBarsPadding().padding(24.dp)) {
                        PerfilHeader(nombre = "")
                        Spacer(Modifier.height(16.dp))
                        EcoContextMessage(title = "Aún no hay información de perfil", message = "Los datos de la cuenta aparecerán aquí.")
                    }
                }
                PerfilUiState.Fallo -> {
                    Column(modifier = Modifier.statusBarsPadding().padding(24.dp)) {
                        PerfilHeader(nombre = "")
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No se pudo cargar el perfil",
                            color = EcoColors.Texto,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        EcoActionButton(text = "Reintentar", onClick = onRetry, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
        EcoBottomBar(items = bottomBarItems, selectedIndex = selectedTab, onSelect = onTabSelected)
    }
}

@Composable
private fun PerfilHeader(nombre: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(nombre = nombre)
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(
                    text = nombre.ifEmpty { "Perfil" },
                    color = EcoColors.Texto,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (nombre.isEmpty()) "Información de la cuenta" else "Nombre ,Usuario",
                    color = EcoColors.Gris,
                    fontSize = 12.sp
                )
            }
        }
    }
}