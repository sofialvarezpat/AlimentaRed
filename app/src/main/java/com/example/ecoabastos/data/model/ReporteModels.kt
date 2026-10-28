package com.example.ecoabastos.data.model

data class CategoriaImpacto(
    val categoria: String,
    val cantidadRecuperada: Double,
    val entregas: Long
)

data class ReporteImpactoResponse(
    val totalLotesPublicados: Long,
    val totalSolicitudes: Long,
    val solicitudesAprobadas: Long,
    val entregasCompletadas: Long,
    val cantidadRecuperada: Double,
    val porCategoria: List<CategoriaImpacto>?
)