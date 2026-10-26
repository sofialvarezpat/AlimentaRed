package com.example.AlimentaRed.dto;

import com.example.AlimentaRed.entity.LoteAlimento;
import com.example.AlimentaRed.entity.enums.EstadoLote;

import java.time.LocalDateTime;

public record LoteResponse(
        Long id,
        String nombre,
        String categoria,
        Double cantidadInicial,
        Double cantidadDisponible,
        String unidadMedida,
        LocalDateTime fechaLimite,
        String ubicacion,
        EstadoLote estado,
        LocalDateTime fechaPublicacion,
        Long donanteId,
        String donanteNombre
) {
    public static LoteResponse desde(LoteAlimento l) {
        return new LoteResponse(
                l.getId(),
                l.getNombre(),
                l.getCategoria(),
                l.getCantidadInicial(),
                l.getCantidadDisponible(),
                l.getUnidadMedida(),
                l.getFechaLimite(),
                l.getUbicacion(),
                l.getEstado(),
                l.getFechaPublicacion(),
                l.getDonante().getId(),
                l.getDonante().getNombre()
        );
    }
}