package com.example.AlimentaRed.service;


import com.example.AlimentaRed.dto.OrganizacionRequest;

import com.example.AlimentaRed.entity.Organizacion;
import com.example.AlimentaRed.entity.enums.TipoOrganizacion;

import java.util.List;

public interface OrganizacionService {
    List<Organizacion> listarOrganizaciones();
    List<Organizacion> listarPorTipo(TipoOrganizacion tipo);
    Organizacion buscarPorId(Long id);
    Organizacion crearOrganizacion(OrganizacionRequest request);
    Organizacion actualizarOrganizacion(Long id, OrganizacionRequest request);
    void eliminarOrganizacion(Long id);
}
