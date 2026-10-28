package com.example.ecoabastos.data.repository

import android.util.Log
import com.example.ecoabastos.data.model.ApiError
import com.example.ecoabastos.data.model.EstadoLote
import com.example.ecoabastos.data.model.LoteRequest
import com.example.ecoabastos.data.model.LoteResponse
import com.example.ecoabastos.data.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.TimeUnit

class LoteRepository {

    private val api = RetrofitClient.api

    companion object {
        private const val SUPABASE_URL = "https://tnxwhimwzmhrgwdgtpzl.supabase.co"
        private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRueHdoaW13em1ocmd3ZGd0cHpsIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc5Mjk4NTk5NSwiZXhwIjoyMTA4NTYxOTk1fQ.Kszsb4fmy-1sqgl_IEl_Z1zrd_6vzTU5gcrjVQN14w0"
        private const val SUPABASE_BUCKET = "lote-imagenes"

        private val httpClient = OkHttpClient.Builder()
            .dns(RetrofitClient.resilientDns)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    suspend fun listarLotes(
        categoria: String? = null,
        ubicacion: String? = null,
        estado: EstadoLote? = null
    ): Result<List<LoteResponse>> {
        return try {
            val lotes = api.listarLotes(categoria = categoria, ubicacion = ubicacion, estado = estado)
            // En Render, LoteServiceIMP.listar() devuelve findAll() sin filtrar por estado; filtramos estrictamente si se pidió estado
            val filtrados = if (estado != null) {
                lotes.filter { it.estado == estado }
            } else {
                lotes
            }
            Result.success(enriquecerConImagenesRemotas(filtrados))
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "GET /api/lotes falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun listarMisLotes(): Result<List<LoteResponse>> {
        return try {
            val lotes = api.misLotes()
            Result.success(enriquecerConImagenesRemotas(lotes))
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "GET /api/lotes/mios falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun obtenerLote(id: Long): Result<LoteResponse> {
        return try {
            val lote = api.obtenerLote(id)
            val enriquecido = enriquecerConImagenesRemotas(listOf(lote)).first()
            Result.success(enriquecido)
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "GET /api/lotes/$id falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun crearLote(request: LoteRequest, imagenPart: MultipartBody.Part? = null): Result<LoteResponse> {
        return try {
            val jsonLote = Gson().toJson(request)
            val lotePart = jsonLote.toRequestBody("application/json".toMediaTypeOrNull())

            // Extraer bytes de MultipartBody.Part antes de consumirlo por si Render no persiste imagen_url
            val imageBytes = imagenPart?.let { extraerBytes(it) }
            val contentType = imagenPart?.body?.contentType()?.toString() ?: "image/jpeg"

            val response = api.crearLote(lote = lotePart, imagen = imagenPart)
            if (response.isSuccessful && response.body() != null) {
                var loteCreado = response.body()!!
                // Como el backend desplegado en Render tiene comentado lote.setImagenUrl(...) y LoteResponse no devuelve imagenUrl,
                // subimos el archivo a Supabase Storage y persistimos la URL remota https://... en lotes_alimento.imagen_url
                if (loteCreado.imagenUrl.isNullOrBlank() && imageBytes != null && imageBytes.isNotEmpty()) {
                    val remoteUrl = subirImagenASupabase(loteCreado.id, imageBytes, contentType)
                    if (!remoteUrl.isNullOrBlank()) {
                        loteCreado = loteCreado.copy(imagenUrl = remoteUrl)
                    }
                }
                Result.success(loteCreado)
            } else {
                val msg = parseErrorResponse(response)
                Log.e("API_ERROR", "POST /api/lotes devolvió HTTP ${response.code()}: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "POST /api/lotes falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun cancelarLote(id: Long): Result<LoteResponse> {
        return try {
            val response = api.cancelarLote(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = parseErrorResponse(response)
                Log.e("API_ERROR", "PATCH /api/lotes/$id/cancelar devolvió HTTP ${response.code()}: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "PATCH /api/lotes/$id/cancelar falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    private fun extraerBytes(part: MultipartBody.Part): ByteArray? {
        return try {
            val buffer = Buffer()
            part.body.writeTo(buffer)
            buffer.readByteArray()
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error leyendo bytes de MultipartBody.Part: ${e.message}", e)
            null
        }
    }

    /**
     * Sube la imagen a Supabase Storage (devolviendo una URL https://... pública) y actualiza
     * la columna imagen_url en la tabla lotes_alimento de Supabase PostgreSQL.
     */
    private suspend fun subirImagenASupabase(loteId: Long, bytes: ByteArray, contentType: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val ext = if (contentType.contains("png", ignoreCase = true)) "png" else "jpg"
                val fileName = "lote_${loteId}.$ext"
                val uploadUrl = "$SUPABASE_URL/storage/v1/object/$SUPABASE_BUCKET/$fileName"

                val uploadReq = Request.Builder()
                    .url(uploadUrl)
                    .header("Authorization", "Bearer $SUPABASE_KEY")
                    .header("apikey", SUPABASE_KEY)
                    .header("x-upsert", "true")
                    .post(bytes.toRequestBody(contentType.toMediaTypeOrNull()))
                    .build()

                var finalRemoteUrl: String? = null
                httpClient.newCall(uploadReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        finalRemoteUrl = "$SUPABASE_URL/storage/v1/object/public/$SUPABASE_BUCKET/$fileName"
                    } else {
                        Log.e("API_ERROR", "Supabase Storage devolvió HTTP ${resp.code} al subir $fileName")
                    }
                }

                // Persistir imagen_url en la tabla lotes_alimento de Supabase PostgREST
                val urlToSave = finalRemoteUrl
                if (!urlToSave.isNullOrBlank()) {
                    val patchJson = Gson().toJson(mapOf("imagen_url" to urlToSave))
                    val patchReq = Request.Builder()
                        .url("$SUPABASE_URL/rest/v1/lotes_alimento?id=eq.$loteId")
                        .header("Authorization", "Bearer $SUPABASE_KEY")
                        .header("apikey", SUPABASE_KEY)
                        .header("Content-Type", "application/json")
                        .header("Prefer", "return=minimal")
                        .patch(patchJson.toRequestBody("application/json".toMediaTypeOrNull()))
                        .build()
                    httpClient.newCall(patchReq).execute().close()
                }
                finalRemoteUrl
            } catch (e: Exception) {
                Log.e("API_ERROR", "Fallo al sincronizar imagen en Supabase para lote $loteId: ${e.message}", e)
                null
            }
        }

    /**
     * Como el DTO LoteResponse desplegado en Render omite el campo imagenUrl en el JSON de respuesta,
     * consultamos imagen_url directamente en Supabase PostgREST para aquellos lotes que vengan con imagenUrl == null.
     */
    private suspend fun enriquecerConImagenesRemotas(lotes: List<LoteResponse>): List<LoteResponse> =
        withContext(Dispatchers.IO) {
            if (lotes.isEmpty() || lotes.all { !it.imagenUrl.isNullOrBlank() }) {
                return@withContext lotes
            }
            try {
                val ids = lotes.map { it.id }.joinToString(",")
                val url = "$SUPABASE_URL/rest/v1/lotes_alimento?select=id,imagen_url&id=in.($ids)"
                val req = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $SUPABASE_KEY")
                    .header("apikey", SUPABASE_KEY)
                    .get()
                    .build()

                httpClient.newCall(req).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val bodyStr = resp.body?.string()
                        if (!bodyStr.isNullOrBlank()) {
                            val type = object : TypeToken<List<Map<String, Any?>>>() {}.type
                            val rows: List<Map<String, Any?>> = Gson().fromJson(bodyStr, type)
                            val urlPorId = rows.mapNotNull { row ->
                                val idNum = (row["id"] as? Number)?.toLong()
                                val img = row["imagen_url"] as? String
                                if (idNum != null && !img.isNullOrBlank()) idNum to img else null
                            }.toMap()

                            if (urlPorId.isNotEmpty()) {
                                return@withContext lotes.map { l ->
                                    if (l.imagenUrl.isNullOrBlank() && urlPorId.containsKey(l.id)) {
                                        l.copy(imagenUrl = urlPorId[l.id])
                                    } else {
                                        l
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "No se pudo consultar imagen_url en Supabase PostgREST: ${e.message}")
            }
            lotes
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
