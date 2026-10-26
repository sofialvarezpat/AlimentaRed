package com.example.AlimentaRed.dto;

import com.example.AlimentaRed.entity.Incidencia;

import java.time.LocalDateTime;

public record IncidenciaResponse(
        Long id,
        Long entregaId,
        String descripcion,
        Long reportadaPorId,
        String reportadaPorNombre,
        LocalDateTime fechaReporte,
        Boolean resuelta
) {
    public static IncidenciaResponse desde(Incidencia i) {
        return new IncidenciaResponse(
                i.getId(),
                i.getEntrega().getId(),
                i.getDescripcion(),
                i.getReportadaPor().getId(),
                i.getReportadaPor().getNombre(),
                i.getFechaReporte(),
                i.getResuelta()
        );
    }
}