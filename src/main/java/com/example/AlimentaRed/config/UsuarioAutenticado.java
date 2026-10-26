package com.example.AlimentaRed.config;

import com.example.AlimentaRed.entity.enums.Rol;

public record UsuarioAutenticado(Long id, String email, Rol rol) {
}