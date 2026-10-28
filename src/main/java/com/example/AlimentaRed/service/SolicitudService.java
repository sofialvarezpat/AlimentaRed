package com.example.AlimentaRed.service;

import com.example.AlimentaRed.dto.ResolverSolicitudRequest;
import com.example.AlimentaRed.dto.SolicitudRequest;
import com.example.AlimentaRed.dto.SolicitudResponse;
import java.util.List;

public interface SolicitudService {
    SolicitudResponse crear(SolicitudRequest request, Long usuarioId);
    List<SolicitudResponse> listarMisSolicitudes(Long usuarioId);
    SolicitudResponse cancelar(Long id, Long usuarioId);
    SolicitudResponse resolver(Long id, ResolverSolicitudRequest request, Long usuarioId);
}