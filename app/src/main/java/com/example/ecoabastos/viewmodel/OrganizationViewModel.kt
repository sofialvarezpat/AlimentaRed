package com.example.ecoabastos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoabastos.data.model.ConfirmarEntregaRequest
import com.example.ecoabastos.data.model.EstadoEntrega
import com.example.ecoabastos.data.model.EstadoLote
import com.example.ecoabastos.data.model.EstadoSolicitud
import com.example.ecoabastos.data.network.SessionManager
import com.example.ecoabastos.data.repository.EntregaRepository
import com.example.ecoabastos.data.repository.LoteRepository
import com.example.ecoabastos.data.repository.SolicitudRepository
import com.example.ecoabastos.ui.screens.organizacion.CatalogoLoteUi
import com.example.ecoabastos.ui.screens.organizacion.CatalogoUiState
import com.example.ecoabastos.ui.screens.organizacion.SolicitudUi
import com.example.ecoabastos.ui.screens.organizacion.SolicitudesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrganizationViewModel(
    private val loteRepository: LoteRepository = LoteRepository(),
    private val solicitudRepository: SolicitudRepository = SolicitudRepository(),
    private val entregaRepository: EntregaRepository = EntregaRepository()
) : ViewModel() {

    private val _catalogoState = MutableStateFlow<CatalogoUiState>(CatalogoUiState.Cargando)
    val catalogoState: StateFlow<CatalogoUiState> = _catalogoState.asStateFlow()

    private val _solicitudesState = MutableStateFlow<SolicitudesUiState>(SolicitudesUiState.Cargando)
    val solicitudesState: StateFlow<SolicitudesUiState> = _solicitudesState.asStateFlow()

    private val _mensajeUi = MutableStateFlow<String?>(null)
    val mensajeUi: StateFlow<String?> = _mensajeUi.asStateFlow()

    private val _isRecogiendo = MutableStateFlow(false)
    val isRecogiendo: StateFlow<Boolean> = _isRecogiendo.asStateFlow()

    fun limpiarMensaje() { _mensajeUi.value = null }

    private var _allSolicitudesLista: List<SolicitudUi> = emptyList()
    private val _solicitudesRecogidasIds = mutableSetOf<Long>()

    private fun esEstadoRecogidoOCompletado(estadoNombre: String?): Boolean {
        if (estadoNombre.isNullOrBlank()) return false
        return estadoNombre.equals("RECOGIDO", ignoreCase = true) ||
               estadoNombre.equals("RECOGIDA", ignoreCase = true) ||
               estadoNombre.equals("COMPLETADO", ignoreCase = true) ||
               estadoNombre.equals("COMPLETADA", ignoreCase = true) ||
               estadoNombre.equals("ENTREGADO", ignoreCase = true) ||
               estadoNombre.equals("ENTREGADA", ignoreCase = true) ||
               estadoNombre.equals("RECIBIDA", ignoreCase = true) ||
               estadoNombre.equals("RECIBIDO", ignoreCase = true) ||
               estadoNombre.equals("FINALIZADO", ignoreCase = true)
    }

    fun cargarCatalogo() {
        _catalogoState.value = CatalogoUiState.Cargando
        viewModelScope.launch {
            try {
                val result = loteRepository.listarLotes(estado = EstadoLote.DISPONIBLE)
                result.onSuccess { lotes ->
                    if (lotes.isEmpty()) {
                        _catalogoState.value = CatalogoUiState.Vacio
                    } else {
                        val items = lotes.map { l ->
                            val fotoRemota = l.imagenUrl?.takeIf { it.isNotBlank() }
                            CatalogoLoteUi(
                                id = l.id,
                                nombre = l.nombre,
                                donante = l.donanteNombre,
                                cantidadDisponible = "${l.cantidadDisponible} ${l.unidadMedida}",
                                categoria = l.categoria,
                                distancia = "1.5 km",
                                imagenUrl = fotoRemota
                            )
                        }
                        _catalogoState.value = CatalogoUiState.Exito(items)
                    }
                }.onFailure { error ->
                    Log.e("API_ERROR", "OrganizationViewModel.cargarCatalogo falló: ${error.message}", error)
                    _catalogoState.value = CatalogoUiState.Fallo
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción en OrganizationViewModel.cargarCatalogo: ${e.message}", e)
                _catalogoState.value = CatalogoUiState.Fallo
            }
        }
    }

    fun solicitarDonacion(loteId: Long, cantidad: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = solicitudRepository.crearSolicitud(loteId, cantidad)
                result.onSuccess {
                    _mensajeUi.value = "Solicitud creada con éxito"
                    cargarCatalogo()
                    cargarMisSolicitudes()
                    onSuccess()
                }.onFailure { error ->
                    Log.e("API_ERROR", "OrganizationViewModel.solicitarDonacion($loteId) falló: ${error.message}", error)
                    _mensajeUi.value = error.message ?: "Error al crear la solicitud en el servidor"
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción en OrganizationViewModel.solicitarDonacion($loteId): ${e.message}", e)
                _mensajeUi.value = e.message ?: "Error de red al crear la solicitud"
            }
        }
    }

    fun cargarMisSolicitudes() {
        _solicitudesState.value = SolicitudesUiState.Cargando
        viewModelScope.launch {
            try {
                val resultSolicitudes = solicitudRepository.listarMisSolicitudes()
                val resultEntregas = entregaRepository.listarMisEntregas()

                resultSolicitudes.onFailure { e ->
                    Log.e("API_ERROR", "OrganizationViewModel.cargarMisSolicitudes -> listarMisSolicitudes falló: ${e.message}", e)
                }
                resultEntregas.onFailure { e ->
                    Log.e("API_ERROR", "OrganizationViewModel.cargarMisSolicitudes -> listarMisEntregas falló: ${e.message}", e)
                }

                val entregas = resultEntregas.getOrNull() ?: emptyList()

                // Identificar entregas ya recogidas o completadas para filtrarlas de la lista activa
                val solicitudesCompletadasIds = entregas
                    .filter { e ->
                        e.estado == EstadoEntrega.COMPLETADO ||
                        esEstadoRecogidoOCompletado(e.estado?.name) ||
                        !e.fechaRecepcion.isNullOrBlank()
                    }
                    .map { it.solicitudId }
                    .toSet()

                resultSolicitudes.onSuccess { solicitudes ->
                    // Filtro local excluyendo RECOGIDO, COMPLETADO y CANCELADA (conservando RECHAZADA para detonar la alerta visual en UI)
                    val solicitudesFiltradas = solicitudes
                        .filter { s ->
                            val estadoStr = s.estado?.name ?: ""
                            !estadoStr.equals("RECOGIDO", ignoreCase = true) &&
                            !estadoStr.equals("RECOGIDA", ignoreCase = true) &&
                            !estadoStr.equals("COMPLETADO", ignoreCase = true) &&
                            !estadoStr.equals("COMPLETADA", ignoreCase = true) &&
                            !estadoStr.equals("CANCELADA", ignoreCase = true) &&
                            !estadoStr.equals("CANCELADO", ignoreCase = true) &&
                            !esEstadoRecogidoOCompletado(estadoStr) &&
                            s.id !in solicitudesCompletadasIds &&
                            s.id !in _solicitudesRecogidasIds
                        }

                    if (solicitudesFiltradas.isEmpty()) {
                        _allSolicitudesLista = emptyList()
                        _solicitudesState.value = SolicitudesUiState.Vacio
                    } else {
                        val items = solicitudesFiltradas.map { s ->
                            val yaRecogida = s.id in solicitudesCompletadasIds ||
                                    s.id in _solicitudesRecogidasIds ||
                                    esEstadoRecogidoOCompletado(s.estado?.name)
                            val entregaAsociada = entregas.find { it.solicitudId == s.id }
                            SolicitudUi(
                                id = s.id,
                                nombreLote = s.loteNombre,
                                cantidadSolicitada = "${s.cantidadSolicitada} Kg",
                                estado = s.estado ?: EstadoSolicitud.PENDIENTE,
                                fecha = s.fechaSolicitud?.take(10) ?: "",
                                fechaRetiro = s.fechaRetiro?.let { f ->
                                    val datePart = f.take(10)
                                    val timePart = if (f.length >= 16) f.substring(11, 16) else ""
                                    if (timePart.isNotBlank()) "$datePart, $timePart" else datePart
                                } ?: entregaAsociada?.fechaRetiro?.let { f ->
                                    val datePart = f.take(10)
                                    val timePart = if (f.length >= 16) f.substring(11, 16) else ""
                                    if (timePart.isNotBlank()) "$datePart, $timePart" else datePart
                                } ?: s.fechaSolicitud?.let { f ->
                                    val datePart = f.take(10)
                                    val timePart = if (f.length >= 16) f.substring(11, 16) else ""
                                    if (timePart.isNotBlank()) "$datePart, $timePart" else datePart
                                } ?: "Pendiente de asignación",
                                novedades = entregaAsociada?.observaciones ?: "",
                                recogido = yaRecogida
                            )
                        }.filter { !it.recogido }

                        _allSolicitudesLista = items
                        _solicitudesState.value = if (items.isEmpty()) {
                            SolicitudesUiState.Vacio
                        } else {
                            SolicitudesUiState.Exito(items)
                        }
                    }
                }.onFailure {
                    _solicitudesState.value = SolicitudesUiState.Fallo
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción en OrganizationViewModel.cargarMisSolicitudes: ${e.message}", e)
                _solicitudesState.value = SolicitudesUiState.Fallo
            }
        }
    }

    /**
     * Confirma el retiro del lote cuando la solicitud está en estado Aprobado / En Retiro.
     * - Asegura que la entrega pase primero por el estado de retiro (RETIRADO / EN_RETIRO) antes de confirmar recepción,
     *   evitando el bloqueo "Solo se puede confirmar la recepción de una entrega retirada".
     * - Al recibir HTTP 200 OK, elimina inmediatamente la solicitud de la lista local en memoria.
     */
    fun confirmarRetiro(solicitudId: Long, novedades: String, onExito: () -> Unit = {}) {
        if (_isRecogiendo.value) return

        val solicitudActual = _allSolicitudesLista.find { it.id == solicitudId }
        val estadoActualStr = solicitudActual?.estado?.name ?: "APROBADA"
        val estadoPermitido = solicitudActual == null ||
                solicitudActual.estado == EstadoSolicitud.APROBADA ||
                estadoActualStr.equals("APROBADO", ignoreCase = true) ||
                estadoActualStr.equals("APROBADA", ignoreCase = true) ||
                estadoActualStr.equals("EN_RETIRO", ignoreCase = true) ||
                estadoActualStr.equals("EN RETIRO", ignoreCase = true) ||
                estadoActualStr.equals("RETIRADO", ignoreCase = true)

        if (!estadoPermitido) {
            _mensajeUi.value = "La solicitud debe estar Aprobada o En Retiro para confirmar su recogida"
            return
        }

        _isRecogiendo.value = true

        viewModelScope.launch {
            try {
                val misEntregasRes = entregaRepository.listarMisEntregas()
                val entregas = misEntregasRes.getOrNull() ?: emptyList()
                val entrega = entregas.find { it.solicitudId == solicitudId }
                val targetId = entrega?.id ?: solicitudId

                val nombreResponsable = SessionManager.usuario?.organizacionNombre
                    ?: SessionManager.usuario?.nombre
                    ?: "Organización Receptora"

                // 1. Si la entrega aún está en estado PROGRAMADA / EN_RETIRO (y no RETIRADO),
                // registramos primero el retiro para evitar el error "Solo se puede confirmar la recepción de una entrega retirada"
                val estadoEntregaStr = entrega?.estado?.name ?: ""
                val yaEstaRetirado = entrega?.estado == EstadoEntrega.RETIRADO ||
                        estadoEntregaStr.equals("RETIRADO", ignoreCase = true) ||
                        estadoEntregaStr.equals("RETIRADA", ignoreCase = true) ||
                        estadoEntregaStr.equals("RECOGIDO", ignoreCase = true)

                if (!yaEstaRetirado) {
                    entregaRepository.registrarRetiro(targetId, nombreResponsable)
                }

                // 2. Confirmar recepción de la entrega (PATCH /api/entregas/{id}/confirmar)
                val confirmarRes = entregaRepository.confirmarEntrega(
                    id = targetId,
                    request = ConfirmarEntregaRequest(
                        responsable = nombreResponsable,
                        observaciones = novedades.ifBlank { null }
                    )
                )

                if (confirmarRes.isSuccess) {
                    // 3. Al recibir HTTP 200 OK, eliminar inmediatamente el ítem de la lista local en memoria
                    _solicitudesRecogidasIds.add(solicitudId)
                    _allSolicitudesLista = _allSolicitudesLista.filter { it.id != solicitudId }

                    _solicitudesState.value = if (_allSolicitudesLista.isEmpty()) {
                        SolicitudesUiState.Vacio
                    } else {
                        SolicitudesUiState.Exito(_allSolicitudesLista)
                    }

                    _mensajeUi.value = "Lote recogido correctamente"
                    onExito()
                } else {
                    val err = confirmarRes.exceptionOrNull()
                    Log.e("API_ERROR", "Fallo en PATCH /api/entregas/$targetId/confirmar: ${err?.message}", err)
                    _mensajeUi.value = err?.message ?: "Error del servidor al confirmar la recogida"
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción en OrganizationViewModel.confirmarRetiro(solicitudId=$solicitudId): ${e.message}", e)
                _mensajeUi.value = "Error de conexión: ${e.message ?: "No se pudo confirmar la recogida"}"
            } finally {
                _isRecogiendo.value = false
            }
        }
    }

    fun filtrarSolicitudes(query: String) {
        val q = query.trim()
        val activas = _allSolicitudesLista.filter {
            !it.recogido &&
            !it.estado.name.equals("RECOGIDO", ignoreCase = true) &&
            !it.estado.name.equals("COMPLETADO", ignoreCase = true)
        }
        val filtradas = if (q.isBlank()) {
            activas
        } else {
            activas.filter {
                it.nombreLote.contains(q, ignoreCase = true) ||
                it.estado.name.contains(q, ignoreCase = true)
            }
        }
        _solicitudesState.value = if (filtradas.isEmpty()) {
            SolicitudesUiState.Vacio
        } else {
            SolicitudesUiState.Exito(filtradas)
        }
    }

    fun cancelarSolicitud(id: Long) {
        viewModelScope.launch {
            try {
                val result = solicitudRepository.cancelarSolicitud(id)
                result.onSuccess {
                    cargarMisSolicitudes()
                }.onFailure { error ->
                    Log.e("API_ERROR", "OrganizationViewModel.cancelarSolicitud($id) falló: ${error.message}", error)
                    _mensajeUi.value = error.message ?: "No se pudo cancelar la solicitud"
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción en OrganizationViewModel.cancelarSolicitud($id): ${e.message}", e)
                _mensajeUi.value = e.message ?: "Error de red al cancelar la solicitud"
            }
        }
    }
}
