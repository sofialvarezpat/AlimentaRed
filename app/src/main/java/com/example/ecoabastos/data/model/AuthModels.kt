package com.example.ecoabastos.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val rol: Rol,
    val nombreOrganizacion: String? = null,
    val direccionOrganizacion: String? = null,
    val telefonoOrganizacion: String? = null
)

data class UsuarioResponse(
    val id: Long,
    val nombre: String,
    val email: String,
    val rol: Rol?,
    val activo: Boolean?,
    val organizacionId: Long?,
    val organizacionNombre: String?
)

data class AuthResponse(
    val token: String,
    val tipo: String?,
    val usuario: UsuarioResponse
)

data class OrganizacionResponse(
    val id: Long,
    val nombre: String,
    val tipo: TipoOrganizacion,
    val direccion: String? = null,
    val telefono: String? = null
)
