package com.example.AlimentaRed.service.serviceIMP;


import com.example.AlimentaRed.Exception.ResourceNotFoundException;
import com.example.AlimentaRed.dto.IncidenciaRequest;
import com.example.AlimentaRed.dto.IncidenciaResponse;
import com.example.AlimentaRed.entity.Entrega;
import com.example.AlimentaRed.entity.Incidencia;
import com.example.AlimentaRed.entity.Usuario;
import com.example.AlimentaRed.repository.EntregaRepository;
import com.example.AlimentaRed.repository.IncidenciaRepository;
import com.example.AlimentaRed.repository.UsuarioRepository;
import com.example.AlimentaRed.service.IncidenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidenciaServiceIMP implements IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final EntregaRepository entregaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaResponse> listarPorEntrega(Long entregaId) {
        return incidenciaRepository.findByEntregaId(entregaId).stream()
                .map(IncidenciaResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public IncidenciaResponse buscarPorId(Long id) {
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada con id " + id));
        return IncidenciaResponse.desde(incidencia);
    }

    @Override
    @Transactional
    public IncidenciaResponse crearIncidencia(Long entregaId, IncidenciaRequest request, Long usuarioId) {
        Entrega entrega = entregaRepository.findById(entregaId)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada con id " + entregaId));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId));

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
    @Transactional
    public IncidenciaResponse resolverIncidencia(Long id) {
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada con id " + id));

        incidencia.setResuelta(true);
        Incidencia actualizada = incidenciaRepository.save(incidencia);
        return IncidenciaResponse.desde(actualizada);
    }

    @Override
    @Transactional
    public void eliminarIncidencia(Long id) {
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada con id " + id));
        incidenciaRepository.delete(incidencia);
    }
}