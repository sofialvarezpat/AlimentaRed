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
                            // Renderiza EXCLUSIVAMENTE la URL remota recibida desde el backend
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

                // Marcamos como ya recogida cuando la entrega está COMPLETADO/RECIBIDA o tiene fechaRecepcion registrada en BD
                val solicitudesCompletadasIds = entregas
                    .filter { it.estado == EstadoEntrega.COMPLETADO || !it.fechaRecepcion.isNullOrBlank() }
                    .map { it.solicitudId }
                    .toSet()

                resultSolicitudes.onSuccess { solicitudes ->
                    if (solicitudes.isEmpty()) {
                        _allSolicitudesLista = emptyList()
                        _solicitudesState.value = SolicitudesUiState.Vacio
                    } else {
                        val items = solicitudes
                            .filter {
                                it.estado != EstadoSolicitud.RECHAZADA &&
                                it.estado != EstadoSolicitud.CANCELADA
                            }
                            .map { s ->
                                val yaRecogida = s.id in solicitudesCompletadasIds
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
                            }

                        val activas = items.filter { !it.recogido }
                        _allSolicitudesLista = activas
                        _solicitudesState.value = if (activas.isEmpty()) {
                            SolicitudesUiState.Vacio
                        } else {
                            SolicitudesUiState.Exito(activas)
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
     * Confirma el retiro/aceptación de la recogida por parte de la Organización Receptora.
     * Manejo de estado estricto:
     * - La UI NO cambia a "Aceptado" / "Lote Recogido" hasta que Retrofit devuelva un código HTTP 2xx.
     * - Si la petición falla, NO muta el estado local y muestra el error exacto, registrándolo con Log.e("API_ERROR", ...).
     */
    fun confirmarRetiro(solicitudId: Long, novedades: String, onExito: () -> Unit = {}) {
        if (_isRecogiendo.value) return
        _isRecogiendo.value = true

        viewModelScope.launch {
            try {
                // 1. Obtener las entregas reales del backend para mapear solicitudId -> entrega.id
                val misEntregasRes = entregaRepository.listarMisEntregas()
                if (misEntregasRes.isFailure) {
                    val err = misEntregasRes.exceptionOrNull()
                    Log.e("API_ERROR", "Fallo al consultar entregas antes de confirmar retiro (solicitudId=$solicitudId): ${err?.message}", err)
                    _mensajeUi.value = err?.message ?: "Error al consultar la entrega en el servidor"
                    return@launch
                }

                val entregas = misEntregasRes.getOrNull() ?: emptyList()
                val entrega = entregas.find { it.solicitudId == solicitudId }
                if (entrega == null) {
                    Log.e("API_ERROR", "No existe registro de Entrega en base de datos para la solicitudId=$solicitudId. ¿Fue aprobada por el Coordinador?")
                    _mensajeUi.value = "No se encontró la orden de entrega asociada a esta solicitud en el servidor"
                    return@launch
                }

                val targetId = entrega.id
                val nombreResponsable = SessionManager.usuario?.organizacionNombre
                    ?: SessionManager.usuario?.nombre
                    ?: "Organización Receptora"

                // 2. PATCH /api/entregas/{id}/confirmar (impacta la base de datos pasando a RECIBIDA / COMPLETADO)
                var confirmarRes = entregaRepository.confirmarEntrega(
                    id = targetId,
                    request = ConfirmarEntregaRequest(
                        responsable = nombreResponsable,
                        observaciones = novedades.ifBlank { null }
                    )
                )

                // Si el backend exige transición previa por /retiro antes de /confirmar, intentamos /retiro y reintentamos /confirmar
                if (confirmarRes.isFailure && (entrega.estado == EstadoEntrega.PROGRAMADA || entrega.estado == null)) {
                    val retiroRes = entregaRepository.registrarRetiro(targetId, nombreResponsable)
                    if (retiroRes.isSuccess) {
                        confirmarRes = entregaRepository.confirmarEntrega(
                            id = targetId,
                            request = ConfirmarEntregaRequest(
                                responsable = nombreResponsable,
                                observaciones = novedades.ifBlank { null }
                            )
                        )
                    }
                }

                if (confirmarRes.isSuccess) {
                    // 4. EXCLUSIVAMENTE tras recibir HTTP 2xx actualizamos el estado en UI y recargamos desde el backend
                    _allSolicitudesLista = _allSolicitudesLista.map {
                        if (it.id == solicitudId) it.copy(recogido = true) else it
                    }.filter { !it.recogido }

                    _solicitudesState.value = if (_allSolicitudesLista.isEmpty()) {
                        SolicitudesUiState.Vacio
                    } else {
                        SolicitudesUiState.Exito(_allSolicitudesLista)
                    }

                    _mensajeUi.value = "Lote recogido y persistido correctamente"
                    cargarMisSolicitudes()
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
        val activas = _allSolicitudesLista.filter { !it.recogido }
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
