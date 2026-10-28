package com.example.ecoabastos.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoabastos.data.model.EstadoEntrega
import com.example.ecoabastos.data.model.EstadoLote
import com.example.ecoabastos.data.model.LoteRequest
import com.example.ecoabastos.data.network.ImageUtils
import com.example.ecoabastos.data.repository.EntregaRepository
import com.example.ecoabastos.data.repository.LoteRepository
import com.example.ecoabastos.ui.components.EntregaEstadoUi
import com.example.ecoabastos.ui.screens.donante.EntregaUi
import com.example.ecoabastos.ui.screens.donante.HistorialUiState
import com.example.ecoabastos.ui.screens.donante.LoteUi
import com.example.ecoabastos.ui.screens.donante.PublicacionesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DonorViewModel(
    private val loteRepository: LoteRepository = LoteRepository(),
    private val entregaRepository: EntregaRepository = EntregaRepository()
) : ViewModel() {

    private val _publicacionesState = MutableStateFlow<PublicacionesUiState>(PublicacionesUiState.Cargando)
    val publicacionesState: StateFlow<PublicacionesUiState> = _publicacionesState.asStateFlow()

    private val _historialState = MutableStateFlow<HistorialUiState>(HistorialUiState.Cargando)
    val historialState: StateFlow<HistorialUiState> = _historialState.asStateFlow()

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    private val _publishError = MutableStateFlow<String?>(null)
    val publishError: StateFlow<String?> = _publishError.asStateFlow()

    private val _loteDetalle = MutableStateFlow<com.example.ecoabastos.data.model.LoteResponse?>(null)
    val loteDetalle: StateFlow<com.example.ecoabastos.data.model.LoteResponse?> = _loteDetalle.asStateFlow()

    fun cargarDetalleLote(id: Long) {
        viewModelScope.launch {
            try {
                val result = loteRepository.obtenerLote(id)
                result.onSuccess {
                    _loteDetalle.value = it
                }.onFailure { e ->
                    Log.e("API_ERROR", "DonorViewModel.cargarDetalleLote($id) falló: ${e.message}", e)
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción en DonorViewModel.cargarDetalleLote($id): ${e.message}", e)
            }
        }
    }

    fun cargarMisLotes() {
        _publicacionesState.value = PublicacionesUiState.Cargando
        viewModelScope.launch {
            try {
                val result = loteRepository.listarMisLotes()
                result.onSuccess { lotes ->
                    if (lotes.isEmpty()) {
                        _publicacionesState.value = PublicacionesUiState.Vacio
                    } else {
                        val items = lotes.map { lote ->
                            // Renderiza EXCLUSIVAMENTE la URL remota recibida desde el backend
                            val fotoRemota = lote.imagenUrl?.takeIf { it.isNotBlank() }
                            LoteUi(
                                id = lote.id,
                                nombre = lote.nombre,
                                cantidad = "${lote.cantidadDisponible} ${lote.unidadMedida}",
                                categoria = lote.categoria,
                                vigencia = lote.fechaLimite?.let { "Vence: ${it.take(10)}" } ?: "Sin fecha",
                                imagenUrl = fotoRemota,
                                estado = lote.estado ?: EstadoLote.DISPONIBLE
                            )
                        }
                        _publicacionesState.value = PublicacionesUiState.Exito(items)
                    }
                }.onFailure { error ->
                    Log.e("API_ERROR", "DonorViewModel.cargarMisLotes falló: ${error.message}", error)
                    _publicacionesState.value = PublicacionesUiState.Fallo
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción en DonorViewModel.cargarMisLotes: ${e.message}", e)
                _publicacionesState.value = PublicacionesUiState.Fallo
            }
        }
    }

    /**
     * Publica un lote convirtiendo la imagen seleccionada a MultipartBody.Part y enviándola en el POST /api/lotes.
     * No guarda copias locales temporales; espera HTTP 2xx del backend antes de confirmar éxito.
     */
    fun publicarLote(context: Context, request: LoteRequest, fotoUri: Uri?, onSuccess: () -> Unit) {
        _isPublishing.value = true
        _publishError.value = null

        viewModelScope.launch {
            try {
                val imagenPart = ImageUtils.uriToMultipart(context, fotoUri)
                val result = loteRepository.crearLote(request, imagenPart)
                result.onSuccess {
                    cargarMisLotes()
                    cargarHistorial()
                    onSuccess()
                }.onFailure { error ->
                    Log.e("API_ERROR", "DonorViewModel.publicarLote falló: ${error.message}", error)
                    _publishError.value = error.message ?: "No se pudo publicar el lote en el servidor"
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción en DonorViewModel.publicarLote: ${e.message}", e)
                _publishError.value = e.message ?: "Error de red al publicar el lote"
            } finally {
                _isPublishing.value = false
            }
        }
    }

    /**
     * Carga el historial del donante estrictamente desde los endpoints GET /api/lotes/mis-lotes y GET /api/entregas/mis-entregas.
     */
    fun cargarHistorial() {
        _historialState.value = HistorialUiState.Cargando
        viewModelScope.launch {
            try {
                val resultLotes = loteRepository.listarMisLotes()
                val resultEntregas = entregaRepository.listarMisEntregas()

                resultLotes.onFailure { e ->
                    Log.e("API_ERROR", "DonorViewModel.cargarHistorial -> listarMisLotes falló: ${e.message}", e)
                }
                resultEntregas.onFailure { e ->
                    Log.e("API_ERROR", "DonorViewModel.cargarHistorial -> listarMisEntregas falló: ${e.message}", e)
                }

                if (resultLotes.isFailure && resultEntregas.isFailure) {
                    _historialState.value = HistorialUiState.Fallo
                    return@launch
                }

                val lotes = resultLotes.getOrNull() ?: emptyList()
                val entregas = resultEntregas.getOrNull() ?: emptyList()

                if (lotes.isEmpty() && entregas.isEmpty()) {
                    _historialState.value = HistorialUiState.Vacio
                    return@launch
                }

                val itemsFromLotes = lotes.map { lote ->
                    val entregaAsociada = entregas.find {
                        it.loteNombre?.equals(lote.nombre, ignoreCase = true) == true
                    }

                    val estadoUi = when {
                        entregaAsociada?.estado == EstadoEntrega.COMPLETADO -> EntregaEstadoUi.COMPLETADO
                        entregaAsociada?.estado == EstadoEntrega.RETIRADO -> EntregaEstadoUi.RETIRADO
                        entregaAsociada?.estado == EstadoEntrega.INCIDENCIA -> EntregaEstadoUi.INCIDENCIA
                        lote.estado == EstadoLote.ENTREGADO -> EntregaEstadoUi.COMPLETADO
                        lote.estado == EstadoLote.RESERVADO -> EntregaEstadoUi.RETIRADO
                        else -> EntregaEstadoUi.PROGRAMADA
                    }

                    val receptor = lote.receptorNombre
                        ?: entregaAsociada?.responsableRetiro
                        ?: entregaAsociada?.responsableRecepcion
                        ?: ""

                    val horaRetiro = entregaAsociada?.fechaRetiro?.let { if (it.length >= 16) it.substring(11, 16) else null }
                        ?: entregaAsociada?.fechaRecepcion?.let { if (it.length >= 16) it.substring(11, 16) else null }
                        ?: lote.fechaLimite?.let { if (it.length >= 16) it.substring(11, 16) else null }
                        ?: ""
                    val obs = entregaAsociada?.observaciones
                        ?: lote.ubicacion?.let { "Ubicación: $it" }
                        ?: ""

                    EntregaUi(
                        id = lote.id,
                        nombreLote = lote.nombre,
                        categoria = lote.categoria,
                        cantidad = "${lote.cantidadDisponible} ${lote.unidadMedida}",
                        estado = estadoUi,
                        recogidoPor = receptor,
                        fecha = entregaAsociada?.fechaRetiro?.take(10)
                            ?: entregaAsociada?.fechaRecepcion?.take(10)
                            ?: lote.fechaPublicacion?.take(10)
                            ?: "",
                        hora = horaRetiro,
                        unidadMedida = lote.unidadMedida,
                        observaciones = obs
                    )
                }

                val lotesNombres = lotes.map { it.nombre.lowercase().trim() }
                val itemsExtraEntregas = entregas.filter {
                    val loteNombre = it.loteNombre?.lowercase()?.trim() ?: ""
                    loteNombre.isNotBlank() && loteNombre !in lotesNombres
                }.map { e ->
                    EntregaUi(
                        id = e.id,
                        nombreLote = e.loteNombre ?: "",
                        categoria = "Alimento",
                        cantidad = "Entrega #${e.id}",
                        estado = when (e.estado) {
                            EstadoEntrega.COMPLETADO -> EntregaEstadoUi.COMPLETADO
                            EstadoEntrega.RETIRADO -> EntregaEstadoUi.RETIRADO
                            EstadoEntrega.INCIDENCIA -> EntregaEstadoUi.INCIDENCIA
                            else -> EntregaEstadoUi.PROGRAMADA
                        },
                        recogidoPor = e.responsableRetiro ?: e.responsableRecepcion ?: "",
                        fecha = e.fechaRetiro?.take(10) ?: e.fechaRecepcion?.take(10) ?: "Pendiente"
                    )
                }

                val itemsFinal = itemsFromLotes + itemsExtraEntregas
                _historialState.value = if (itemsFinal.isEmpty()) {
                    HistorialUiState.Vacio
                } else {
                    HistorialUiState.Exito(itemsFinal)
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Excepción en DonorViewModel.cargarHistorial: ${e.message}", e)
                _historialState.value = HistorialUiState.Fallo
            }
        }
    }
}
