package com.example.AlimentaRed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RetiroRequest(
        @NotBlank(message = "El responsable del retiro es obligatorio")
        @Size(max = 255)
        String responsable
) {
}