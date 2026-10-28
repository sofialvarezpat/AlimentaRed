package com.example.ecoabastos.data.repository

import android.util.Log
import com.example.ecoabastos.data.model.ApiError
import com.example.ecoabastos.data.model.ReporteImpactoResponse
import com.example.ecoabastos.data.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.HttpException

class ReporteRepository {

    private val api = RetrofitClient.api

    suspend fun getImpacto(
        desde: String? = null,
        hasta: String? = null,
        categoria: String? = null
    ): Result<ReporteImpactoResponse> {
        return try {
            val response = api.reporteImpacto(desde = desde, hasta = hasta, categoria = categoria)
            Result.success(response)
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "GET /api/reportes/impacto falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    private fun parseError(e: Exception): String {
        if (e is HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                return try {
                    val apiError = Gson().fromJson(errorBody, ApiError::class.java)
                    apiError.message ?: apiError.detalles?.firstOrNull() ?: "Error (${e.code()})"
                } catch (_: Exception) {
                    "Error de servidor (${e.code()})"
                }
            }
        }
        return e.localizedMessage ?: "Error de conexión"
    }
}
