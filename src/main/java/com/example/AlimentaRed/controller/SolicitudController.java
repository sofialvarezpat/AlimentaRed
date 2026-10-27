package com.example.AlimentaRed.controller;

import com.example.AlimentaRed.config.UsuarioAutenticado;
import com.example.AlimentaRed.dto.ResolverSolicitudRequest;
import com.example.AlimentaRed.dto.SolicitudRequest;
import com.example.AlimentaRed.dto.SolicitudResponse;
import com.example.AlimentaRed.service.SolicitudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "Solicitud, aprobación y rechazo de lotes")
@SecurityRequirement(name = "bearerAuth")
public class SolicitudController {

    private final SolicitudService solicitudService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('RECEPTORA')")
    @Operation(summary = "Solicitar un lote (solo organizaciones receptoras)")
    public SolicitudResponse crear(@Valid @RequestBody SolicitudRequest request,
                                   @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return solicitudService.crear(request, usuario.id());
    }

    @GetMapping("/mias")
    @Operation(summary = "Mis solicitudes (según mi rol)")
    public List<SolicitudResponse> mias(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return solicitudService.listarMisSolicitudes(usuario.id());
    }

    @PatchMapping("/{id}/resolver")
    @PreAuthorize("hasRole('COORDINADOR')")
    @Operation(summary = "Aprobar o rechazar una solicitud (solo coordinadores)")
    public SolicitudResponse resolver(@PathVariable Long id,
                                      @Valid @RequestBody ResolverSolicitudRequest request,
                                      @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return solicitudService.resolver(id, request, usuario.id());
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('RECEPTORA','COORDINADOR','ADMINISTRADOR')")
    @Operation(summary = "Cancelar una solicitud pendiente")
    public SolicitudResponse cancelar(@PathVariable Long id,
                                      @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return solicitudService.cancelar(id, usuario.id());
    }
}