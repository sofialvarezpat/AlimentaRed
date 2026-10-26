package com.example.AlimentaRed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IncidenciaRequest(
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 1000)
        String descripcion
) {
}
