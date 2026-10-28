package com.example.ecoabastos.data.network

import com.example.ecoabastos.data.model.UsuarioResponse

object SessionManager {
    var token: String? = null
    var usuario: UsuarioResponse? = null

    fun cerrarSesion() {
        token = null
        usuario = null
    }
}