package com.example.AlimentaRed.controller;

import com.example.AlimentaRed.config.UsuarioAutenticado;
import com.example.AlimentaRed.dto.LoteRequest;
import com.example.AlimentaRed.dto.LoteResponse;
import com.example.AlimentaRed.entity.enums.EstadoLote;
import com.example.AlimentaRed.service.LoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
@Tag(name = "Lotes", description = "Publicación y consulta de lotes de alimentos")
public class LoteController {

    private final LoteService loteService;

    @GetMapping
    @Operation(summary = "Listar y filtrar lotes por categoría, ubicación, estado y fecha límite")
    public List<LoteResponse> listar(
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) EstadoLote estado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaLimiteHasta) {
        return loteService.listar(categoria, ubicacion, estado, fechaLimiteHasta);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DONANTE')")
    @Operation(summary = "Crear un lote (solo donantes)")
    public LoteResponse crear(@Valid @RequestBody LoteRequest request,
                              @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return loteService.crear(request, usuario.id());
    }

    @GetMapping("/mios")
    @PreAuthorize("hasRole('DONANTE')")
    @Operation(summary = "Lotes publicados por mi organización")
    public List<LoteResponse> mios(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return loteService.listarMisLotes(usuario.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver el detalle de un lote")
    public LoteResponse obtener(@PathVariable Long id) {
        return loteService.obtener(id);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('DONANTE','COORDINADOR','ADMINISTRADOR')")
    @Operation(summary = "Cancelar un lote")
    public LoteResponse cancelar(@PathVariable Long id,
                                 @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return loteService.cancelar(id, usuario.id());
    }
}