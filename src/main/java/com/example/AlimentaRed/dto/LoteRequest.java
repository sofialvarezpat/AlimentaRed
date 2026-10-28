package com.example.AlimentaRed.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record LoteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255)
        String nombre,

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 100)
        String categoria,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a cero")
        Double cantidad,

        @NotBlank(message = "La unidad de medida es obligatoria")
        @Size(max = 50)
        String unidadMedida,

        @NotNull(message = "La fecha límite es obligatoria")
        @Future(message = "La fecha límite debe ser futura")
        LocalDateTime fechaLimite,

        @Size(max = 150)
        String ubicacion
) {
}