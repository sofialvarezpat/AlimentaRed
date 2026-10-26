package com.example.AlimentaRed.dto;

import com.example.AlimentaRed.entity.Usuario;
import com.example.AlimentaRed.entity.enums.Rol;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        Rol rol,
        Boolean activo,
        Long organizacionId,
        String organizacionNombre
) {
    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getEmail(),
                u.getRol(),
                u.getActivo(),
                u.getOrganizacion() != null ? u.getOrganizacion().getId() : null,
                u.getOrganizacion() != null ? u.getOrganizacion().getNombre() : null
        );
    }
}