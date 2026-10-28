package com.example.AlimentaRed.controller;

import com.example.AlimentaRed.config.UsuarioAutenticado;
import com.example.AlimentaRed.dto.LoteRequest;
import com.example.AlimentaRed.dto.LoteResponse;
import com.example.AlimentaRed.entity.enums.EstadoLote;
import com.example.AlimentaRed.service.LoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
@Tag(name = "Lotes", description = "Publicación y consulta de lotes de alimentos")
@SecurityRequirement(name = "bearerAuth")
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('DONANTE')")
    @Operation(
            summary = "Crear un lote con imagen (solo donantes)",
            description = "Permite registrar un lote enviando un JSON en la parte 'lote' y un archivo opcional en la parte 'imagen'."
    )
    @SecurityRequirement(name = "bearerAuth")
    public LoteResponse crear(
            @Parameter(description = "Datos del lote", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("lote") @Valid LoteRequest request,

            @Parameter(description = "Imagen del lote")
            @RequestPart(value = "imagen", required = false) MultipartFile imagen,

            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return loteService.crear(request, imagen, usuario.id());
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