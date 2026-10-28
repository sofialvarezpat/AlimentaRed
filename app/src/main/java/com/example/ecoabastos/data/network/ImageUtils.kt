package com.example.ecoabastos.data.network

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object ImageUtils {

    /**
     * Convierte un Uri seleccionado de la galería a MultipartBody.Part para enviarse
     * en la petición POST multipart/form-data hacia el backend (Spring Boot / Supabase).
     */
    fun uriToMultipart(context: Context, uri: Uri?): MultipartBody.Part? {
        if (uri == null) return null

        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.use { it.readBytes() }

            val fileName = getFileName(context, uri) ?: "lote_${System.currentTimeMillis()}.jpg"
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

            MultipartBody.Part.createFormData("imagen", fileName, requestBody)
        } catch (e: Exception) {
            Log.e("API_ERROR", "Error al convertir URI a MultipartBody.Part: ${e.message}", e)
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        name = it.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/') ?: -1
            if (cut != -1 && name != null) {
                name = name.substring(cut + 1)
            }
        }
        return name
    }
}
