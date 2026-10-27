package com.example.AlimentaRed.controller;

import com.example.AlimentaRed.config.UsuarioAutenticado;
import com.example.AlimentaRed.dto.ConfirmarEntregaRequest;
import com.example.AlimentaRed.dto.EntregaResponse;
import com.example.AlimentaRed.dto.IncidenciaRequest;
import com.example.AlimentaRed.dto.IncidenciaResponse;
import com.example.AlimentaRed.dto.RetiroRequest;
import com.example.AlimentaRed.service.EntregaService;
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
@RequestMapping("/api/entregas")
@RequiredArgsConstructor
@Tag(name = "Entregas", description = "Retiro, recepción e incidencias de las entregas")
@SecurityRequirement(name = "bearerAuth")
public class EntregaController {

    private final EntregaService entregaService;

    @GetMapping
    @Operation(summary = "Mis entregas (según mi rol)")
    public List<EntregaResponse> mias(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return entregaService.listarMisEntregas(usuario.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver el detalle de una entrega")
    public EntregaResponse obtener(@PathVariable Long id,
                                   @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return entregaService.obtener(id, usuario.id());
    }

    @PatchMapping("/{id}/retiro")
    @PreAuthorize("hasAnyRole('DONANTE','COORDINADOR','ADMINISTRADOR')")
    @Operation(summary = "Registrar el retiro del lote (donante o coordinador)")
    public EntregaResponse registrarRetiro(@PathVariable Long id,
                                           @Valid @RequestBody RetiroRequest request,
                                           @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return entregaService.registrarRetiro(id, request, usuario.id());
    }

    @PatchMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('RECEPTORA','COORDINADOR','ADMINISTRADOR')")
    @Operation(summary = "Confirmar la recepción de la entrega")
    public EntregaResponse confirmar(@PathVariable Long id,
                                     @Valid @RequestBody ConfirmarEntregaRequest request,
                                     @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return entregaService.confirmarRecepcion(id, request, usuario.id());
    }

    @PostMapping("/{id}/incidencias")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Reportar una incidencia sobre una entrega")
    public IncidenciaResponse reportarIncidencia(@PathVariable Long id,
                                                 @Valid @RequestBody IncidenciaRequest request,
                                                 @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return entregaService.reportarIncidencia(id, request, usuario.id());
    }

    @GetMapping("/{id}/incidencias")
    @Operation(summary = "Listar las incidencias de una entrega")
    public List<IncidenciaResponse> incidencias(@PathVariable Long id,
                                                @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return entregaService.listarIncidencias(id, usuario.id());
    }

    @PatchMapping("/incidencias/{incidenciaId}/resolver")
    @PreAuthorize("hasRole('COORDINADOR')")
    @Operation(summary = "Resolver una incidencia (solo coordinadores)")
    public IncidenciaResponse resolverIncidencia(@PathVariable Long incidenciaId,
                                                 @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return entregaService.resolverIncidencia(incidenciaId, usuario.id());
    }
}