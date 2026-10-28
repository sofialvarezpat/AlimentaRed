package com.example.AlimentaRed.service.serviceIMP;

import com.example.AlimentaRed.Exception.ResourceNotFoundException;
import com.example.AlimentaRed.dto.*;
import com.example.AlimentaRed.entity.Entrega;
import com.example.AlimentaRed.entity.Incidencia;
import com.example.AlimentaRed.entity.Usuario;
import com.example.AlimentaRed.entity.enums.EstadoEntrega;
import com.example.AlimentaRed.repository.EntregaRepository;
import com.example.AlimentaRed.repository.IncidenciaRepository;
import com.example.AlimentaRed.repository.UsuarioRepository;
import com.example.AlimentaRed.service.EntregaService;
import com.example.AlimentaRed.service.HistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntregaServiceIMP implements EntregaService {

    private final EntregaRepository entregaRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialService historialService;

    @Override
    @Transactional(readOnly = true)
    public List<EntregaResponse> listarMisEntregas(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Long orgId = usuario.getOrganizacion() != null ? usuario.getOrganizacion().getId() : null;

        List<Entrega> entregas = switch (usuario.getRol()) {
            case RECEPTORA -> orgId == null ? List.of() : entregaRepository.findByAsignacionSolicitudOrganizacionId(orgId);
            case DONANTE -> orgId == null ? List.of() : entregaRepository.findByAsignacionSolicitudLoteDonanteId(orgId);
            case COORDINADOR, ADMINISTRADOR -> entregaRepository.findAll();
        };
        return entregas.stream().map(EntregaResponse::desde).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EntregaResponse obtener(Long id, Long usuarioId) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada"));
        return EntregaResponse.desde(entrega);
    }

    @Override
    @Transactional
    public EntregaResponse registrarRetiro(Long id, RetiroRequest request, Long usuarioId) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada"));

        String estadoAnterior = entrega.getEstado().name();
        entrega.setEstado(EstadoEntrega.RETIRADA);
        entrega.setFechaRetiro(LocalDateTime.now());
        entrega.setResponsableRetiro(request.responsable());
        entregaRepository.save(entrega);

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        historialService.registrar(HistorialService.ENTREGA, entrega.getId(), estadoAnterior, EstadoEntrega.RETIRADA.name(), usuario);

        return EntregaResponse.desde(entrega);
    }

    @Override
    @Transactional
    public EntregaResponse confirmarRecepcion(Long id, ConfirmarEntregaRequest request, Long usuarioId) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada"));

        String estadoAnterior = entrega.getEstado().name();
        entrega.setEstado(EstadoEntrega.RECIBIDA);
        entrega.setFechaRecepcion(LocalDateTime.now());
        entrega.setResponsableRecepcion(request.responsable());
        if (request.observaciones() != null) entrega.setObservaciones(request.observaciones());
        entregaRepository.save(entrega);

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        // CORREGIDO: Se cambió HistorialService.ENTREGADA por HistorialService.ENTREGA
        historialService.registrar(HistorialService.ENTREGA, entrega.getId(), estadoAnterior, EstadoEntrega.RECIBIDA.name(), usuario);

        return EntregaResponse.desde(entrega);
    }

    @Override
    @Transactional
    public IncidenciaResponse reportarIncidencia(Long id, IncidenciaRequest request, Long usuarioId) {
        Entrega entrega = entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Incidencia incidencia = new Incidencia();
        incidencia.setEntrega(entrega);
        incidencia.setDescripcion(request.descripcion());
        incidencia.setReportadaPor(usuario);
        incidencia.setFechaReporte(LocalDateTime.now());
        incidencia.setResuelta(false);

        Incidencia guardada = incidenciaRepository.save(incidencia);
        return IncidenciaResponse.desde(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaResponse> listarIncidencias(Long id, Long usuarioId) {
        return incidenciaRepository.findByEntregaId(id).stream()
                .map(IncidenciaResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public IncidenciaResponse resolverIncidencia(Long incidenciaId, Long usuarioId) {
        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada"));
        incidencia.setResuelta(true);
        Incidencia actualizada = incidenciaRepository.save(incidencia);
        return IncidenciaResponse.desde(actualizada);
    }
}
