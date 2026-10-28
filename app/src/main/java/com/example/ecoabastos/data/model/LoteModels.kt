package com.example.ecoabastos.data.model

import com.google.gson.annotations.SerializedName

data class LoteRequest(
    val nombre: String,
    val categoria: String,
    val cantidad: Double,
    val unidadMedida: String,
    val fechaLimite: String,
    val ubicacion: String? = null
)

data class LoteResponse(
    val id: Long,
    val nombre: String,
    val categoria: String,
    val cantidadInicial: Double,
    val cantidadDisponible: Double,
    val unidadMedida: String,
    val fechaLimite: String?,
    val ubicacion: String?,
    val estado: EstadoLote?,
    val fechaPublicacion: String?,
    val donanteId: Long,
    val donanteNombre: String,
    @SerializedName("imagenUrl", alternate = ["imagen_url", "fotoUrl", "foto_url", "imageUrl"])
    val imagenUrl: String? = null,
    @SerializedName("receptorNombre", alternate = ["organizacionReceptora", "receptor", "nombreReceptor", "empresaReceptora"])
    val receptorNombre: String? = null
)
