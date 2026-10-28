package com.example.AlimentaRed.service;

import com.example.AlimentaRed.dto.ConfirmarEntregaRequest;
import com.example.AlimentaRed.dto.EntregaResponse;
import java.util.List;
import com.example.AlimentaRed.dto.*;
import java.util.List;

public interface EntregaService {
    List<EntregaResponse> listarMisEntregas(Long usuarioId);
    EntregaResponse obtener(Long id, Long usuarioId);
    EntregaResponse registrarRetiro(Long id, RetiroRequest request, Long usuarioId);
    EntregaResponse confirmarRecepcion(Long id, ConfirmarEntregaRequest request, Long usuarioId);
    IncidenciaResponse reportarIncidencia(Long id, IncidenciaRequest request, Long usuarioId);
    List<IncidenciaResponse> listarIncidencias(Long id, Long usuarioId);
    IncidenciaResponse resolverIncidencia(Long incidenciaId, Long usuarioId);
}