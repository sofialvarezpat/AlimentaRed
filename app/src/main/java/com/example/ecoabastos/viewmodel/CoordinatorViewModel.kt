package com.example.ecoabastos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoabastos.data.model.EstadoEntrega
import com.example.ecoabastos.data.model.EstadoSolicitud
import com.example.ecoabastos.data.model.ResolverSolicitudRequest
import com.example.ecoabastos.data.repository.EntregaRepository
import com.example.ecoabastos.data.repository.SolicitudRepository
import com.example.ecoabastos.ui.screens.coordinador.CoordinatorHistorialItemUi
import com.example.ecoabastos.ui.screens.coordinador.CoordinatorHistorialUiState
import com.example.ecoabastos.ui.screens.coordinador.CoordinatorSolicitudUi
import com.example.ecoabastos.ui.screens.coordinador.CoordinatorUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CoordinatorViewModel(
    private val solicitudRepository: SolicitudRepository = SolicitudRepository(),
    private val entregaRepository: EntregaRepository = EntregaRepository()
) : ViewModel() {

    // Lista maestra completa (alimentada exclusivamente por la respuesta exitosa de la API)
    private var _allSolicitudes: List<CoordinatorSolicitudUi> = emptyList()

    private val _coordinatorState = MutableStateFlow<CoordinatorUiState>(CoordinatorUiState.Cargando)
    val coordinatorState: StateFlow<CoordinatorUiState> = _coordinatorState.asStateFlow()

    private val _historialState = MutableStateFlow<CoordinatorHistorialUiState>(CoordinatorHistorialUiState.Cargando)
    val historialState: StateFlow<CoordinatorHistorialUiState> = _historialState.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    fun limpiarError() {
        _mensajeError.value = null
    }

    fun cargarSolicitudes() {
        _coordinatorState.value = CoordinatorUiState.Cargando
        _historialState.value = CoordinatorHistorialUiState.Cargando
        viewModelScope.launch {
            try {
                val result = solicitudRepository.listarMisSolicitudes()
                val resultEntregas = entregaRepository.listarMisEntregas()
                val entregas = resultEntregas.getOrNull() ?: emptyList()

                result.onSuccess { list ->
                    if (list.isEmpty()) {
                        _allSolicitudes = emptyList()
                        _coordinatorState.value = CoordinatorUiState.Vacio
                        _historialState.value = CoordinatorHistorialUiState.Vacio
                    } else {
                        // ── Panel de gestión: TODAS las solicitudes reales para las 3 pestañas (Pendientes, Aprobadas, Rechazadas) ──
                        val itemsTodas = list.map { s ->
                            CoordinatorSolicitudUi(
                                id = s.id,
                                organizacion = s.organizacionNombre,
                                nombreAlimento = s.loteNombre,
                                cantidad = "${s.cantidadSolicitada} Kg",
                                fecha = s.fechaSolicitud?.take(16)?.replace("T", " ") ?: "—",
                                estado = s.estado ?: EstadoSolicitud.PENDIENTE
                            )
                        }
                        _allSolicitudes = itemsTodas
                        _coordinatorState.value = if (itemsTodas.isEmpty()) {
                            CoordinatorUiState.Vacio
                        } else {
                            CoordinatorUiState.Exito(itemsTodas)
                        }

                        // ── Historial: solicitudes gestionadas con estado real sincronizado ──
                        val gestionadas = list.filter {
                            it.estado == EstadoSolicitud.APROBADA ||
                            it.estado == EstadoSolicitud.RECHAZADA ||
                            it.estado == EstadoSolicitud.CANCELADA
                        }
                        val itemsHistorial = gestionadas.map { s ->
                            val entrega = entregas.find { it.solicitudId == s.id }
                            val estadoTexto = when {
                                s.estado == EstadoSolicitud.RECHAZADA -> "Rechazada"
                                s.estado == EstadoSolicitud.CANCELADA -> "Cancelada"
                                entrega?.estado == EstadoEntrega.COMPLETADO -> "Finalizado"
                                entrega?.estado == EstadoEntrega.RETIRADO -> "En retiro"
                                s.estado == EstadoSolicitud.APROBADA -> "Aprobada"
                                else -> "Gestionada"
                            }
                            CoordinatorHistorialItemUi(
                                id = s.id,
                                organizacion = s.organizacionNombre,
                                estadoTexto = estadoTexto,
                                fechaRetiro = entrega?.fechaRetiro?.take(16)?.replace("T", " ")
                                    ?: entrega?.fechaRecepcion?.take(16)?.replace("T", " ")
                                    ?: s.fechaRetiro?.take(16)?.replace("T", " ")
                                    ?: s.fechaSolicitud?.take(10)
                                    ?: "—",
                                novedades = buildString {
                                    append("Solicitud de ${s.cantidadSolicitada} Kg de ${s.loteNombre}")
                                    val obs = entrega?.observaciones ?: s.observaciones
                                    if (!obs.isNullOrBlank()) {
                                        append(" — $obs")
                                    }
                                }
                            )
                        }
                        _historialState.value = if (itemsHistorial.isEmpty()) {
                            CoordinatorHistorialUiState.Vacio
                        } else {
                            CoordinatorHistorialUiState.Exito(itemsHistorial)
                        }
                    }
                }.onFailure { error ->
                    Log.e("API_ERROR", "Error al cargar solicitudes en Coordinador: ${error.message}", error)
                    _allSolicitudes = emptyList()
                    _coordinatorState.value = CoordinatorUiState.Fallo
                    _historialState.value = CoordinatorHistorialUiState.Fallo
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción al cargar solicitudes en Coordinador: ${e.message}", e)
                _allSolicitudes = emptyList()
                _coordinatorState.value = CoordinatorUiState.Fallo
                _historialState.value = CoordinatorHistorialUiState.Fallo
            }
        }
    }

    fun filtrarSolicitudes(query: String) {
        if (_allSolicitudes.isEmpty()) {
            _coordinatorState.value = CoordinatorUiState.Vacio
            return
        }
        val q = query.trim()
        val filtradas = if (q.isBlank()) {
            _allSolicitudes
        } else {
            _allSolicitudes.filter {
                it.organizacion.contains(q, ignoreCase = true) ||
                it.nombreAlimento.contains(q, ignoreCase = true)
            }
        }
        _coordinatorState.value = if (filtradas.isEmpty()) {
            CoordinatorUiState.Vacio
        } else {
            CoordinatorUiState.Exito(filtradas)
        }
    }

    /**
     * Resuelve una solicitud (Aprobar o Rechazar) estrictamente contra el backend.
     * La UI solo se actualiza cuando Retrofit devuelve HTTP 2xx.
     */
    fun resolverSolicitud(id: Long, aprobada: Boolean, cantidad: Double?, obs: String?) {
        viewModelScope.launch {
            try {
                val request = ResolverSolicitudRequest(
                    aprobada = aprobada,
                    cantidadAsignada = cantidad,
                    observaciones = obs
                )
                val result = solicitudRepository.resolverSolicitud(id, request)
                result.onSuccess {
                    // Solo cuando el backend confirma con HTTP 2xx recargamos el estado
                    cargarSolicitudes()
                }.onFailure { error ->
                    Log.e("API_ERROR", "Error al resolver solicitud $id: ${error.message}", error)
                    _mensajeError.value = error.message ?: "Error al resolver la solicitud"
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción al resolver solicitud $id: ${e.message}", e)
                _mensajeError.value = e.localizedMessage ?: "Error inesperado al resolver la solicitud"
            }
        }
    }
}
