package com.example.AlimentaRed.dto;

import java.util.List;

public record UsuarioMeResponse(
        Long id, String email, String nombre, String rol, Boolean activo,
        List<OrganizacionResponse> organizaciones
) {}