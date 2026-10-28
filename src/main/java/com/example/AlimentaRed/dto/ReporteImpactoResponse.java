package com.example.AlimentaRed.dto;

import java.util.List;

public record ReporteImpactoResponse(
        Long totalLotesPublicados,
        Long totalSolicitudes,
        Long solicitudesAprobadas,
        Long entregasCompletadas,
        Double cantidadRecuperada,
        List<CategoriaImpacto> porCategoria
) {
    public record CategoriaImpacto(
            String categoria,
            Double cantidadRecuperada,
            Long entregas
    ) {
    }
}