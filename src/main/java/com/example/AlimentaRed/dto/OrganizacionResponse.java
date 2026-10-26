package com.example.AlimentaRed.dto;

import com.example.AlimentaRed.entity.Organizacion;
import com.example.AlimentaRed.entity.enums.TipoOrganizacion;

public record OrganizacionResponse(
        Long id,
        String nombre,
        TipoOrganizacion tipo,
        String direccion,
        String telefono
) {
    public static OrganizacionResponse desde(Organizacion o) {
        return new OrganizacionResponse(
                o.getId(),
                o.getNombre(),
                o.getTipo(),
                o.getDireccion(),
                o.getTelefono()
        );
    }
}