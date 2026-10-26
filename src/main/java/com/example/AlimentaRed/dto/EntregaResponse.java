package com.example.AlimentaRed.dto;

import com.example.AlimentaRed.entity.Entrega;
import com.example.AlimentaRed.entity.enums.EstadoEntrega;

import java.time.LocalDateTime;

public record EntregaResponse(
        Long id,
        Long asignacionId,
        Long solicitudId,
        String loteNombre,
        EstadoEntrega estado,
        LocalDateTime fechaRetiro,
        LocalDateTime fechaRecepcion,
        String responsableRetiro,
        String responsableRecepcion,
        String observaciones
) {
    public static EntregaResponse desde(Entrega e) {
        return new EntregaResponse(
                e.getId(),
                e.getAsignacion().getId(),
                e.getAsignacion().getSolicitud().getId(),
                e.getAsignacion().getSolicitud().getLote().getNombre(),
                e.getEstado(),
                e.getFechaRetiro(),
                e.getFechaRecepcion(),
                e.getResponsableRetiro(),
                e.getResponsableRecepcion(),
                e.getObservaciones()
        );
    }
}