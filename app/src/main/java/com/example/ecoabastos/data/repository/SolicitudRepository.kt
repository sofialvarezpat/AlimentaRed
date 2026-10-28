package com.example.ecoabastos.data.repository

import android.util.Log
import com.example.ecoabastos.data.model.ApiError
import com.example.ecoabastos.data.model.ResolverSolicitudRequest
import com.example.ecoabastos.data.model.SolicitudRequest
import com.example.ecoabastos.data.model.SolicitudResponse
import com.example.ecoabastos.data.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response

class SolicitudRepository {

    private val api = RetrofitClient.api

    suspend fun crearSolicitud(loteId: Long, cantidad: Double): Result<SolicitudResponse> {
        return try {
            val response = api.crearSolicitud(SolicitudRequest(loteId = loteId, cantidadSolicitada = cantidad))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = parseErrorResponse(response)
                Log.e("API_ERROR", "POST /api/solicitudes devolvió HTTP ${response.code()}: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "POST /api/solicitudes falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun listarMisSolicitudes(): Result<List<SolicitudResponse>> {
        return try {
            val response = api.misSolicitudes()
            Result.success(response)
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "GET /api/solicitudes/mias falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun resolverSolicitud(id: Long, request: ResolverSolicitudRequest): Result<SolicitudResponse> {
        return try {
            val response = api.resolverSolicitud(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = parseErrorResponse(response)
                Log.e("API_ERROR", "PATCH /api/solicitudes/$id/resolver devolvió HTTP ${response.code()}: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "PATCH /api/solicitudes/$id/resolver falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun cancelarSolicitud(id: Long): Result<SolicitudResponse> {
        return try {
            val response = api.cancelarSolicitud(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = parseErrorResponse(response)
                Log.e("API_ERROR", "PATCH /api/solicitudes/$id/cancelar devolvió HTTP ${response.code()}: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "PATCH /api/solicitudes/$id/cancelar falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    private fun parseErrorResponse(response: Response<*>): String {
        val errorBody = response.errorBody()?.string()
        if (!errorBody.isNullOrBlank()) {
            return try {
                val apiError = Gson().fromJson(errorBody, ApiError::class.java)
                apiError.message ?: apiError.detalles?.firstOrNull() ?: "Error (${response.code()})"
            } catch (_: Exception) {
                "Error de servidor (${response.code()}): $errorBody"
            }
        }
        return "Error HTTP ${response.code()}"
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
