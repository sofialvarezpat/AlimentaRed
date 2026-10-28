package com.example.ecoabastos.data.model

data class SolicitudRequest(
    val loteId: Long,
    val cantidadSolicitada: Double
)

data class ResolverSolicitudRequest(
    val aprobada: Boolean,
    val cantidadAsignada: Double? = null,
    val observaciones: String? = null
)

data class SolicitudResponse(
    val id: Long,
    val loteId: Long,
    val loteNombre: String,
    val organizacionId: Long,
    val organizacionNombre: String,
    val cantidadSolicitada: Double,
    val estado: EstadoSolicitud?,
    val fechaSolicitud: String?,
    // Fecha programada de retiro (asignada por el Coordinador al aprobar)
    val fechaRetiro: String? = null,
    val observaciones: String? = null
)