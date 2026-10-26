package com.example.AlimentaRed.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SolicitudRequest(
        @NotNull(message = "El lote es obligatorio")
        Long loteId,

        @NotNull(message = "La cantidad solicitada es obligatoria")
        @Positive(message = "La cantidad solicitada debe ser mayor a cero")
        Double cantidadSolicitada
) {
}