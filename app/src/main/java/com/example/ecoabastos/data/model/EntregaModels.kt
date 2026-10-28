package com.example.ecoabastos.data.model

data class RetiroRequest(
    val responsable: String
)

data class ConfirmarEntregaRequest(
    val responsable: String,
    val observaciones: String? = null
)

data class IncidenciaRequest(
    val descripcion: String
)

data class EntregaResponse(
    val id: Long,
    val asignacionId: Long? = null,
    val solicitudId: Long? = null,
    val loteNombre: String? = null,
    val estado: EstadoEntrega? = null,
    val fechaRetiro: String? = null,
    val fechaRecepcion: String? = null,
    val responsableRetiro: String? = null,
    val responsableRecepcion: String? = null,
    val observaciones: String? = null
)

data class IncidenciaResponse(
    val id: Long,
    val entregaId: Long,
    val descripcion: String,
    val reportadaPorId: Long,
    val reportadaPorNombre: String,
    val fechaReporte: String?,
    val resuelta: Boolean?
)