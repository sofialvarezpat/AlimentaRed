package com.example.AlimentaRed.dto;

import com.example.AlimentaRed.entity.SolicitudDonacion;
import com.example.AlimentaRed.entity.enums.EstadoSolicitud;

import java.time.LocalDateTime;

public record SolicitudResponse(
        Long id,
        Long loteId,
        String loteNombre,
        Long organizacionId,
        String organizacionNombre,
        Double cantidadSolicitada,
        EstadoSolicitud estado,
        LocalDateTime fechaSolicitud
) {
    public static SolicitudResponse desde(SolicitudDonacion s) {
        return new SolicitudResponse(
                s.getId(),
                s.getLote().getId(),
                s.getLote().getNombre(),
                s.getOrganizacion().getId(),
                s.getOrganizacion().getNombre(),
                s.getCantidadSolicitada(),
                s.getEstado(),
                s.getFechaSolicitud()
        );
    }
}