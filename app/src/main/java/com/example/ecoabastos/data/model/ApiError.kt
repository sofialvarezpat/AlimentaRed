package com.example.ecoabastos.data.model

data class ApiError(
    val timestamp: String?,
    val status: Int,
    val error: String?,
    val message: String?,
    val path: String?,
    val detalles: List<String>?
)