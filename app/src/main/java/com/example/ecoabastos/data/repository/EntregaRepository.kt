package com.example.ecoabastos.data.repository

import android.util.Log
import com.example.ecoabastos.data.model.ApiError
import com.example.ecoabastos.data.model.ConfirmarEntregaRequest
import com.example.ecoabastos.data.model.EntregaResponse
import com.example.ecoabastos.data.model.RetiroRequest
import com.example.ecoabastos.data.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response

class EntregaRepository {

    private val api = RetrofitClient.api

    suspend fun listarMisEntregas(): Result<List<EntregaResponse>> {
        return try {
            val entregas = api.misEntregas()
            Result.success(entregas)
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "GET /api/entregas falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun registrarRetiro(id: Long, responsable: String): Result<EntregaResponse> {
        return try {
            val response = api.registrarRetiro(id, RetiroRequest(responsable = responsable))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = parseErrorResponse(response)
                Log.e("API_ERROR", "PATCH /api/entregas/$id/retiro devolvió HTTP ${response.code()}: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "PATCH /api/entregas/$id/retiro falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun confirmarEntrega(id: Long, request: ConfirmarEntregaRequest): Result<EntregaResponse> {
        return try {
            val response = api.confirmarEntrega(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = parseErrorResponse(response)
                Log.e("API_ERROR", "PATCH /api/entregas/$id/confirmar devolvió HTTP ${response.code()}: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "PATCH /api/entregas/$id/confirmar falló: $msg", e)
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
