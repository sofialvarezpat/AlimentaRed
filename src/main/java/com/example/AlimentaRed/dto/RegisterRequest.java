package com.example.AlimentaRed.dto;

import com.example.AlimentaRed.entity.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 255)
        String nombre,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password,

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        // Datos de la organización (para donantes y receptoras)
        @Size(max = 255)
        String nombreOrganizacion,

        @Size(max = 255)
        String direccionOrganizacion,

        @Size(max = 50)
        String telefonoOrganizacion
) {
}