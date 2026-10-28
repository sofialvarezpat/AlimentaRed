package com.example.AlimentaRed.controller;

import com.example.AlimentaRed.config.UsuarioAutenticado;
import com.example.AlimentaRed.dto.ReporteImpactoResponse;
import com.example.AlimentaRed.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Indicadores de impacto")
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/impacto")
    @PreAuthorize("hasAnyRole('COORDINADOR','ADMINISTRADOR')")
    @Operation(summary = "Consultar indicadores de impacto por periodo y categoría")
    public ReporteImpactoResponse impacto(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String categoria,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return reporteService.impacto(desde, hasta, categoria, usuario.id());
    }
}