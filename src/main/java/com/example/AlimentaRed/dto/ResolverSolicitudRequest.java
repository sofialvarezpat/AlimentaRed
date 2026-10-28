package com.example.AlimentaRed.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ResolverSolicitudRequest(
        @NotNull(message = "Debe indicar si la solicitud se aprueba o se rechaza")
        Boolean aprobada,

        // Opcional: si no se envía, se asigna toda la cantidad solicitada
        @Positive(message = "La cantidad asignada debe ser mayor a cero")
        Double cantidadAsignada,

        @Size(max = 1000)
        String observaciones
) {
}
