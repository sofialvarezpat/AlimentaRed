package com.example.ecoabastos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoabastos.data.model.EstadoEntrega
import com.example.ecoabastos.data.model.EstadoLote
import com.example.ecoabastos.data.model.Rol
import com.example.ecoabastos.data.model.TipoOrganizacion
import com.example.ecoabastos.data.repository.AuthRepository
import com.example.ecoabastos.data.repository.EntregaRepository
import com.example.ecoabastos.data.repository.LoteRepository
import com.example.ecoabastos.data.repository.ReporteRepository
import com.example.ecoabastos.ui.screens.admin.AdminResumenUi
import com.example.ecoabastos.ui.screens.admin.AdminResumenUiState
import com.example.ecoabastos.ui.screens.admin.AdminUsuarioUi
import com.example.ecoabastos.ui.screens.admin.AdminUsuariosUiState
import com.example.ecoabastos.ui.screens.admin.HistorialDonacionRow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val reporteRepository: ReporteRepository = ReporteRepository(),
    private val loteRepository: LoteRepository = LoteRepository(),
    private val entregaRepository: EntregaRepository = EntregaRepository()
) : ViewModel() {

    // Lista maestra alimentada EXCLUSIVAMENTE por respuestas exitosas de la API
    private var _allUsuarios: List<AdminUsuarioUi> = emptyList()

    private val _resumenState = MutableStateFlow<AdminResumenUiState>(AdminResumenUiState.Cargando)
    val resumenState: StateFlow<AdminResumenUiState> = _resumenState.asStateFlow()

    private val _usuariosState = MutableStateFlow<AdminUsuariosUiState>(AdminUsuariosUiState.Cargando)
    val usuariosState: StateFlow<AdminUsuariosUiState> = _usuariosState.asStateFlow()

    fun cargarUsuarios() {
        _usuariosState.value = AdminUsuariosUiState.Cargando
        viewModelScope.launch {
            try {
                // 1. Intentar GET /api/usuarios primero
                val resultUsuarios = authRepository.listarUsuarios()
                if (resultUsuarios.isSuccess) {
                    val lista = resultUsuarios.getOrThrow().map { u ->
                        AdminUsuarioUi(
                            id = u.id,
                            nombre = u.nombre,
                            email = u.email,
                            rol = u.rol ?: Rol.DONANTE,
                            activo = u.activo ?: true
                        )
                    }
                    _allUsuarios = lista
                    _usuariosState.value = if (lista.isEmpty()) {
                        AdminUsuariosUiState.Vacio
                    } else {
                        AdminUsuariosUiState.Exito(lista)
                    }
                    return@launch
                }

                // 2. Si el backend solo expone GET /api/organizaciones, consultar organizaciones reales
                val resultOrgs = authRepository.listarOrganizaciones()
                resultOrgs.onSuccess { orgs ->
                    val lista = orgs.map { org ->
                        val rol = if (org.tipo == TipoOrganizacion.DONANTE) Rol.DONANTE else Rol.RECEPTORA
                        AdminUsuarioUi(
                            id = org.id,
                            nombre = org.nombre,
                            email = org.telefono ?: org.direccion ?: "",
                            rol = rol,
                            activo = true
                        )
                    }
                    _allUsuarios = lista
                    _usuariosState.value = if (lista.isEmpty()) {
                        AdminUsuariosUiState.Vacio
                    } else {
                        AdminUsuariosUiState.Exito(lista)
                    }
                }.onFailure { error ->
                    Log.e("API_ERROR", "Error al cargar usuarios/organizaciones en Admin: ${error.message}", error)
                    _allUsuarios = emptyList()
                    _usuariosState.value = AdminUsuariosUiState.Fallo
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción al cargar usuarios en Admin: ${e.message}", e)
                _allUsuarios = emptyList()
                _usuariosState.value = AdminUsuariosUiState.Fallo
            }
        }
    }

    fun filtrarUsuariosPorRol(rolTexto: String) {
        if (_allUsuarios.isEmpty()) {
            _usuariosState.value = AdminUsuariosUiState.Vacio
            return
        }
        val filtrados = when (rolTexto.trim().lowercase()) {
            "donante"     -> _allUsuarios.filter { it.rol == Rol.DONANTE }
            "receptora"   -> _allUsuarios.filter { it.rol == Rol.RECEPTORA }
            "coordinador" -> _allUsuarios.filter { it.rol == Rol.COORDINADOR }
            else          -> _allUsuarios
        }
        _usuariosState.value = if (filtrados.isEmpty()) {
            AdminUsuariosUiState.Vacio
        } else {
            AdminUsuariosUiState.Exito(filtrados)
        }
    }

    fun toggleUsuarioActivo(id: Long) {
        viewModelScope.launch {
            try {
                val usuarioActual = _allUsuarios.find { it.id == id } ?: return@launch
                val nuevoEstado = !usuarioActual.activo
                val res = authRepository.cambiarEstadoUsuario(id, nuevoEstado)
                res.onSuccess {
                    cargarUsuarios()
                }.onFailure { error ->
                    Log.e("API_ERROR", "No se pudo cambiar el estado del usuario $id en el backend: ${error.message}", error)
                    cargarUsuarios()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción en toggleUsuarioActivo($id): ${e.message}", e)
            }
        }
    }

    fun cargarResumenImpacto() {
        _resumenState.value = AdminResumenUiState.Cargando
        viewModelScope.launch {
            try {
                val resultadoImpacto  = reporteRepository.getImpacto()
                val resultadoLotes    = loteRepository.listarLotes()
                val resultadoEntregas = entregaRepository.listarMisEntregas()

                if (resultadoImpacto.isFailure && resultadoLotes.isFailure && resultadoEntregas.isFailure) {
                    val err = resultadoImpacto.exceptionOrNull()
                    Log.e("API_ERROR", "Error al cargar resumen de impacto Admin: ${err?.message}", err)
                    _resumenState.value = AdminResumenUiState.Fallo
                    return@launch
                }

                val lotes = resultadoLotes.getOrNull() ?: emptyList()
                val entregas = resultadoEntregas.getOrNull() ?: emptyList()
                val impacto = resultadoImpacto.getOrNull()

                val tieneDatos = (impacto != null && (impacto.totalLotesPublicados > 0 || impacto.totalSolicitudes > 0 || impacto.entregasCompletadas > 0 || impacto.cantidadRecuperada > 0.0)) ||
                        lotes.isNotEmpty() || entregas.isNotEmpty()

                if (!tieneDatos) {
                    _resumenState.value = AdminResumenUiState.Vacio
                    return@launch
                }

                val cantidadRecuperadaKg = if (impacto != null && impacto.cantidadRecuperada > 0.0) {
                    impacto.cantidadRecuperada
                } else {
                    lotes.filter { it.estado == EstadoLote.ENTREGADO || it.estado == EstadoLote.RESERVADO }
                        .sumOf { (it.cantidadInicial - it.cantidadDisponible).coerceAtLeast(0.0) }
                        .takeIf { it > 0.0 } ?: lotes.sumOf { it.cantidadInicial }
                }

                val toneladasTexto = if (cantidadRecuperadaKg >= 1000.0) {
                    String.format("%.1f", cantidadRecuperadaKg / 1000.0)
                } else {
                    "${cantidadRecuperadaKg.toInt()}"
                }

                val entregasExitosasCount = if (impacto != null && impacto.entregasCompletadas > 0) {
                    impacto.entregasCompletadas
                } else {
                    val completadasFromEntregas = entregas.count {
                        it.estado == EstadoEntrega.COMPLETADO || it.estado == EstadoEntrega.RETIRADO
                    }.toLong()
                    val completadasFromLotes = lotes.count { it.estado == EstadoLote.ENTREGADO }.toLong()
                    maxOf(completadasFromEntregas, completadasFromLotes)
                }

                val orgsImpactadasCount = impacto?.totalSolicitudes
                    ?.takeIf { it > 0 }
                    ?: entregas.map { it.solicitudId }.distinct().size.toLong()

                // Filas reales del backend (SIN mocks ni datos quemados)
                val filasReales = lotes.map { lote ->
                    val entregaAsoc = entregas.find { it.loteNombre.equals(lote.nombre, ignoreCase = true) }
                    val receptor = lote.receptorNombre?.takeIf { it.isNotBlank() }
                        ?: entregaAsoc?.responsableRecepcion?.takeIf { it.isNotBlank() }
                        ?: entregaAsoc?.responsableRetiro?.takeIf { it.isNotBlank() }
                        ?: "Sin asignar"
                    HistorialDonacionRow(
                        fecha    = entregaAsoc?.fechaRecepcion?.take(10)
                            ?: entregaAsoc?.fechaRetiro?.take(10)
                            ?: lote.fechaPublicacion?.take(10)
                            ?: "—",
                        kilos    = "${lote.cantidadInicial} ${lote.unidadMedida}",
                        donante  = lote.donanteNombre,
                        receptor = receptor
                    )
                }

                val puntosReales = lotes.take(7).map { it.cantidadInicial.toFloat() }

                val ui = AdminResumenUi(
                    toneladasRescatadas      = toneladasTexto,
                    organizacionesImpactadas = "$orgsImpactadasCount",
                    entregasExitosas         = "$entregasExitosasCount",
                    puntosGrafica            = puntosReales,
                    donacionesRecientes      = filasReales
                )
                _resumenState.value = AdminResumenUiState.Exito(ui)
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción en cargarResumenImpacto: ${e.message}", e)
                _resumenState.value = AdminResumenUiState.Fallo
            }
        }
    }
}
