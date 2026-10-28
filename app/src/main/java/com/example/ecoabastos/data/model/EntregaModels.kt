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
    val asignacionId: Long,
    val solicitudId: Long,
    val loteNombre: String,
    val estado: EstadoEntrega?,
    val fechaRetiro: String?,
    val fechaRecepcion: String?,
    val responsableRetiro: String?,
    val responsableRecepcion: String?,
    val observaciones: String?
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