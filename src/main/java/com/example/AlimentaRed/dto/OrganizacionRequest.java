package com.example.AlimentaRed.dto;

import com.example.AlimentaRed.entity.enums.TipoOrganizacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrganizacionRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255)
        String nombre,

        @NotNull(message = "El tipo es obligatorio")
        TipoOrganizacion tipo,

        @Size(max = 255)
        String direccion,

        @Size(max = 50)
        String telefono
) {
}
