package com.example.AlimentaRed.service;

import com.example.AlimentaRed.dto.IncidenciaRequest;
import com.example.AlimentaRed.dto.IncidenciaResponse;
import java.util.List;

public interface IncidenciaService {
    List<IncidenciaResponse> listarPorEntrega(Long entregaId);
    IncidenciaResponse buscarPorId(Long id);
    IncidenciaResponse crearIncidencia(Long entregaId, IncidenciaRequest request, Long usuarioId);
    IncidenciaResponse resolverIncidencia(Long id);
    void eliminarIncidencia(Long id);
}