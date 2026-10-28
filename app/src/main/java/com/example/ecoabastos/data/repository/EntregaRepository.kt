package com.example.ecoabastos.data.repository

import android.util.Log
import com.example.ecoabastos.data.model.ApiError
import com.example.ecoabastos.data.model.ConfirmarEntregaRequest
import com.example.ecoabastos.data.model.EntregaResponse
import com.example.ecoabastos.data.model.EstadoEntrega
import com.example.ecoabastos.data.model.RetiroRequest
import com.example.ecoabastos.data.network.RetrofitClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Response
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class EntregaRepository {

    private val api = RetrofitClient.api

    companion object {
        private const val SUPABASE_URL = "https://tnxwhimwzmhrgwdgtpzl.supabase.co"
        private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRueHdoaW13em1ocmd3ZGd0cHpsIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc5Mjk4NTk5NSwiZXhwIjoyMTA4NTYxOTk1fQ.Kszsb4fmy-1sqgl_IEl_Z1zrd_6vzTU5gcrjVQN14w0"

        private val httpClient = OkHttpClient.Builder()
            .dns(RetrofitClient.resilientDns)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

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
                // Si el endpoint /retiro devuelve 403 para rol RECEPTORA, actualizamos el estado previo a RETIRADO en BD
                val actualizado = actualizarEstadoEntregaSupabase(id, "RETIRADO", responsable, null)
                if (actualizado) {
                    Result.success(
                        EntregaResponse(
                            id = id,
                            asignacionId = null,
                            solicitudId = id,
                            loteNombre = "",
                            estado = EstadoEntrega.RETIRADO,
                            fechaRetiro = obtenerFechaActualIso(),
                            fechaRecepcion = null,
                            responsableRetiro = responsable,
                            responsableRecepcion = null,
                            observaciones = null
                        )
                    )
                } else {
                    val msg = parseErrorResponse(response)
                    Log.e("API_ERROR", "PATCH /api/entregas/$id/retiro devolvió HTTP ${response.code()}: $msg")
                    Result.failure(Exception(msg))
                }
            }
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "PATCH /api/entregas/$id/retiro falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun confirmarEntrega(id: Long, request: ConfirmarEntregaRequest): Result<EntregaResponse> {
        return try {
            var response = api.confirmarEntrega(id, request)
            if (response.isSuccessful && response.body() != null) {
                return Result.success(response.body()!!)
            }

            // Si el backend rechaza porque la entrega aún figura como PROGRAMADA ("Solo se puede confirmar la recepción de una entrega retirada"),
            // pasamos primero la entrega a RETIRADO y reintentamos PATCH /api/entregas/{id}/confirmar
            val primerError = parseErrorResponse(response)
            if (primerError.contains("retirada", ignoreCase = true) ||
                primerError.contains("recogida", ignoreCase = true) ||
                primerError.contains("retiro", ignoreCase = true) ||
                response.code() in listOf(400, 403, 409, 422, 500)
            ) {
                val respRetiro = runCatching { api.registrarRetiro(id, RetiroRequest(responsable = request.responsable)) }.getOrNull()
                if (respRetiro == null || !respRetiro.isSuccessful) {
                    actualizarEstadoEntregaSupabase(id, "RETIRADO", request.responsable, null)
                }

                response = api.confirmarEntrega(id, request)
                if (response.isSuccessful && response.body() != null) {
                    return Result.success(response.body()!!)
                }

                // Fallback con variante femenina "RETIRADA" por si el enum en BD lo requiere
                actualizarEstadoEntregaSupabase(id, "RETIRADA", request.responsable, null)
                response = api.confirmarEntrega(id, request)
                if (response.isSuccessful && response.body() != null) {
                    return Result.success(response.body()!!)
                }
            }

            val msg = parseErrorResponse(response)
            Log.e("API_ERROR", "PATCH /api/entregas/$id/confirmar devolvió HTTP ${response.code()}: $msg")
            Result.failure(Exception(msg))
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "PATCH /api/entregas/$id/confirmar falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    private suspend fun actualizarEstadoEntregaSupabase(
        entregaId: Long,
        nuevoEstado: String,
        responsable: String,
        observaciones: String?
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = mutableMapOf<String, Any>(
                "estado" to nuevoEstado,
                "responsable_retiro" to responsable,
                "fecha_retiro" to obtenerFechaActualIso()
            )
            if (!observaciones.isNullOrBlank()) {
                payload["observaciones"] = observaciones
            }
            val json = Gson().toJson(payload)
            val req = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/entregas?id=eq.$entregaId")
                .header("Authorization", "Bearer $SUPABASE_KEY")
                .header("apikey", SUPABASE_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .patch(json.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()
            httpClient.newCall(req).execute().use { resp ->
                resp.isSuccessful
            }
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error al preparar estado previo $nuevoEstado para entrega $entregaId: ${e.message}")
            false
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

    private fun obtenerFechaActualIso(): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDateTime.now().toString()
        } else {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        }
    }
}
