package com.example.ecoabastos.data.repository

import android.util.Log
import com.example.ecoabastos.data.model.ApiError
import com.example.ecoabastos.data.model.AuthResponse
import com.example.ecoabastos.data.model.LoginRequest
import com.example.ecoabastos.data.model.OrganizacionResponse
import com.example.ecoabastos.data.model.RegisterRequest
import com.example.ecoabastos.data.model.UsuarioResponse
import com.example.ecoabastos.data.network.RetrofitClient
import com.example.ecoabastos.data.network.SessionManager
import com.google.gson.Gson
import retrofit2.HttpException
import retrofit2.Response

class AuthRepository {

    private val api = RetrofitClient.api

    suspend fun login(email: String, clave: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email = email, password = clave))
            SessionManager.token = response.token
            SessionManager.usuario = response.usuario
            Result.success(response)
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "POST /api/auth/login falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = api.register(request)
            SessionManager.token = response.token
            SessionManager.usuario = response.usuario
            Result.success(response)
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "POST /api/auth/register falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun getPerfil(): Result<UsuarioResponse> {
        return try {
            val user = api.me()
            SessionManager.usuario = user
            Result.success(user)
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "GET /api/usuarios/me falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun listarUsuarios(): Result<List<UsuarioResponse>> {
        return try {
            val response = api.listarUsuarios()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = parseErrorResponse(response)
                Log.e("API_ERROR", "GET /api/usuarios devolvió HTTP ${response.code()}: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "GET /api/usuarios falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun cambiarEstadoUsuario(id: Long, activo: Boolean): Result<UsuarioResponse> {
        return try {
            val response = api.cambiarEstadoUsuario(id, mapOf("activo" to activo))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val msg = parseErrorResponse(response)
                Log.e("API_ERROR", "PATCH /api/usuarios/$id/estado devolvió HTTP ${response.code()}: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "PATCH /api/usuarios/$id/estado falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    suspend fun listarOrganizaciones(): Result<List<OrganizacionResponse>> {
        return try {
            val orgs = api.listarOrganizaciones()
            Result.success(orgs)
        } catch (e: Exception) {
            val msg = parseError(e)
            Log.e("API_ERROR", "GET /api/organizaciones falló: $msg", e)
            Result.failure(Exception(msg))
        }
    }

    fun logout() {
        SessionManager.cerrarSesion()
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
