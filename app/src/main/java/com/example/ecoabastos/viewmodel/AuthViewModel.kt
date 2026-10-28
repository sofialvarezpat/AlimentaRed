package com.example.ecoabastos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoabastos.data.model.RegisterRequest
import com.example.ecoabastos.data.model.Rol
import com.example.ecoabastos.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun login(email: String, clave: String, onSuccess: (Rol) -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = authRepository.login(email.trim(), clave)
                _isLoading.value = false
                result.onSuccess { authResponse ->
                    val rol = authResponse.usuario.rol ?: Rol.DONANTE
                    onSuccess(rol)
                }.onFailure { error ->
                    Log.e("API_ERROR", "Login error: ${error.message}", error)
                    _errorMessage.value = error.message ?: "Credenciales inválidas"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e("API_ERROR", "Excepción en login: ${e.message}", e)
                _errorMessage.value = e.localizedMessage ?: "Error al iniciar sesión"
            }
        }
    }

    fun register(request: RegisterRequest, onSuccess: (Rol) -> Unit) {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = authRepository.register(request)
                _isLoading.value = false
                result.onSuccess { authResponse ->
                    val rol = authResponse.usuario.rol ?: request.rol
                    onSuccess(rol)
                }.onFailure { error ->
                    Log.e("API_ERROR", "Register error: ${error.message}", error)
                    _errorMessage.value = error.message ?: "Error al registrar la cuenta"
                }
            } catch (e: Exception) {
                _isLoading.value = false
                Log.e("API_ERROR", "Excepción en register: ${e.message}", e)
                _errorMessage.value = e.localizedMessage ?: "Error al registrar la cuenta"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun logout() {
        authRepository.logout()
    }
}
