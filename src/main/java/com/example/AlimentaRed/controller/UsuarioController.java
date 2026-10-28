package com.example.AlimentaRed.controller;

import com.example.AlimentaRed.config.UsuarioAutenticado;
import com.example.AlimentaRed.dto.UsuarioResponse;
import com.example.AlimentaRed.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Perfil del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final AuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Datos del usuario que inició sesión")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return authService.perfil(usuario.id());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @Operation(summary = "Listar todos los usuarios registrados (exclusivo para administradores)")
    public List<UsuarioResponse> listarTodos(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return authService.listarUsuarios();
    }
}

