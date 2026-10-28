package com.example.AlimentaRed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmarEntregaRequest(
        @NotBlank(message = "El responsable de la recepción es obligatorio")
        @Size(max = 255)
        String responsable,

        @Size(max = 1000)
        String observaciones
) {
}
