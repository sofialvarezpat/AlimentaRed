package com.example.AlimentaRed.dto;

public record AuthResponse(
        String token,
        String tipo,
        UsuarioResponse usuario
) {
}