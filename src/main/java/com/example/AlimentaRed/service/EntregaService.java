package com.example.AlimentaRed.service;

import com.example.AlimentaRed.Exception.BusinessRuleException;
import com.example.AlimentaRed.Exception.ForbiddenOperationException;
import com.example.AlimentaRed.Exception.ResourceNotFoundException;
import com.example.AlimentaRed.dto.ConfirmarEntregaRequest;
import com.example.AlimentaRed.dto.EntregaResponse;
import com.example.AlimentaRed.dto.IncidenciaRequest;
import com.example.AlimentaRed.dto.IncidenciaResponse;
import com.example.AlimentaRed.dto.RetiroRequest;
import com.example.AlimentaRed.entity.Entrega;
import com.example.AlimentaRed.entity.Incidencia;
import com.example.AlimentaRed.entity.LoteAlimento;
import com.example.AlimentaRed.entity.Organizacion;
import com.example.AlimentaRed.entity.Usuario;
import com.example.AlimentaRed.entity.enums.EstadoEntrega;
import com.example.AlimentaRed.entity.enums.EstadoLote;
import com.example.AlimentaRed.entity.enums.Rol;
import com.example.AlimentaRed.repository.EntregaRepository;
import com.example.AlimentaRed.repository.IncidenciaRepository;
import com.example.AlimentaRed.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EntregaService {

    private final EntregaRepository entregaRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialService historialService;

    // Seguimiento: entregas según el rol del usuario
    @Transactional(readOnly = true)
    public List<EntregaResponse> listarMisEntregas(Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        Organizacion organizacion = usuario.getOrganizacion();

        List<Entrega> entregas = switch (usuario.getRol()) {
            case RECEPTORA -> organizacion == null
                    ? List.of()
                    : entregaRepository.findByAsignacionSolicitudOrganizacionId(organizacion.getId());
            case DONANTE -> organizacion == null
                    ? List.of()
                    : entregaRepository.findByAsignacionSolicitudLoteDonanteId(organizacion.getId());
            case COORDINADOR, ADMINISTRADOR -> entregaRepository.findAll();
        };

        return entregas.stream().map(EntregaResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public EntregaResponse obtener(Long id, Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        Entrega entrega = buscarEntrega(id);
        verificarParticipante(usuario, entrega);
        return EntregaResponse.desde(entrega);
    }

    // RF07: el donante (o un coordinador) registra la salida del lote
    @Transactional
    public EntregaResponse registrarRetiro(Long id, RetiroRequest request, Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        Entrega entrega = buscarEntrega(id);

        if (!esDonante(usuario, entrega) && !esCoordinador(usuario)) {
            throw new ForbiddenOperationException(
                    "Solo el donante responsable o un coordinador puede registrar la salida");
        }

        String anterior = entrega.getEstado().name();
        entrega.registrarRetiro(request.responsable());
        historialService.registrar(HistorialService.ENTREGA, entrega.getId(),
                anterior, entrega.getEstado().name(), usuario);
        return EntregaResponse.desde(entrega);
    }

    // RF07: la organización receptora (o un coordinador) confirma la recepción
    @Transactional
    public EntregaResponse confirmarRecepcion(Long id, ConfirmarEntregaRequest request, Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        Entrega entrega = buscarEntrega(id);

        if (!esReceptora(usuario, entrega) && !esCoordinador(usuario)) {
            throw new ForbiddenOperationException(
                    "Solo la organización receptora o un coordinador puede confirmar la recepción");
        }

        String anterior = entrega.getEstado().name();
        entrega.completarEntrega(request.responsable(), request.observaciones());
        historialService.registrar(HistorialService.ENTREGA, entrega.getId(),
                anterior, entrega.getEstado().name(), usuario);

        marcarLoteEntregadoSiCorresponde(entrega, usuario);
        return EntregaResponse.desde(entrega);
    }

    // RF08: reportar una incidencia sobre una entrega
    @Transactional
    public IncidenciaResponse reportarIncidencia(Long entregaId, IncidenciaRequest request, Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        Entrega entrega = buscarEntrega(entregaId);
        verificarParticipante(usuario, entrega);

        if (entrega.getEstado() == EstadoEntrega.COMPLETADO) {
            throw new BusinessRuleException("No se pueden reportar incidencias sobre una entrega completada");
        }
        if (entrega.getEstado() == EstadoEntrega.INCIDENCIA) {
            throw new BusinessRuleException("La entrega ya tiene una incidencia abierta");
        }

        Incidencia incidencia = new Incidencia();
        incidencia.setEntrega(entrega);
        incidencia.setDescripcion(request.descripcion().trim());
        incidencia.setReportadaPor(usuario);
        incidenciaRepository.save(incidencia);

        String anterior = entrega.getEstado().name();
        entrega.setEstado(EstadoEntrega.INCIDENCIA);
        historialService.registrar(HistorialService.ENTREGA, entrega.getId(),
                anterior, EstadoEntrega.INCIDENCIA.name(), usuario);

        return IncidenciaResponse.desde(incidencia);
    }

    @Transactional(readOnly = true)
    public List<IncidenciaResponse> listarIncidencias(Long entregaId, Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        Entrega entrega = buscarEntrega(entregaId);
        verificarParticipante(usuario, entrega);
        return incidenciaRepository.findByEntregaId(entregaId)
                .stream()
                .map(IncidenciaResponse::desde)
                .toList();
    }

    // El coordinador cierra la incidencia y la entrega retoma su curso
    @Transactional
    public IncidenciaResponse resolverIncidencia(Long incidenciaId, Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        if (usuario.getRol() != Rol.COORDINADOR) {
            throw new ForbiddenOperationException("Solo un coordinador puede resolver incidencias");
        }

        Incidencia incidencia = incidenciaRepository.findById(incidenciaId)
                .orElseThrow(() -> new ResourceNotFoundException("Incidencia no encontrada con id " + incidenciaId));
        if (Boolean.TRUE.equals(incidencia.getResuelta())) {
            throw new BusinessRuleException("La incidencia ya está resuelta");
        }

        incidencia.setResuelta(true);

        Entrega entrega = incidencia.getEntrega();
        if (entrega.getEstado() == EstadoEntrega.INCIDENCIA) {
            EstadoEntrega retomado = entrega.getFechaRetiro() != null
                    ? EstadoEntrega.RETIRADO
                    : EstadoEntrega.PROGRAMADA;
            entrega.setEstado(retomado);
            historialService.registrar(HistorialService.ENTREGA, entrega.getId(),
                    EstadoEntrega.INCIDENCIA.name(), retomado.name(), usuario);
        }
        return IncidenciaResponse.desde(incidencia);
    }

    // Cuando el lote quedó sin stock y todas sus entregas se completaron, pasa a ENTREGADO
    private void marcarLoteEntregadoSiCorresponde(Entrega entrega, Usuario usuario) {
        LoteAlimento lote = entrega.getAsignacion().getSolicitud().getLote();
        long pendientes = entregaRepository.countByAsignacionSolicitudLoteIdAndEstadoNot(
                lote.getId(), EstadoEntrega.COMPLETADO);

        if (lote.getCantidadDisponible() <= 0
                && lote.getEstado() == EstadoLote.RESERVADO
                && pendientes == 0) {
            lote.setEstado(EstadoLote.ENTREGADO);
            historialService.registrar(HistorialService.LOTE, lote.getId(),
                    EstadoLote.RESERVADO.name(), EstadoLote.ENTREGADO.name(), usuario);
        }
    }

    private boolean esDonante(Usuario usuario, Entrega entrega) {
        Long donanteId = entrega.getAsignacion().getSolicitud().getLote().getDonante().getId();
        return usuario.getOrganizacion() != null
                && usuario.getOrganizacion().getId().equals(donanteId);
    }

    private boolean esReceptora(Usuario usuario, Entrega entrega) {
        Long receptoraId = entrega.getAsignacion().getSolicitud().getOrganizacion().getId();
        return usuario.getOrganizacion() != null
                && usuario.getOrganizacion().getId().equals(receptoraId);
    }

    private boolean esCoordinador(Usuario usuario) {
        return usuario.getRol() == Rol.COORDINADOR || usuario.getRol() == Rol.ADMINISTRADOR;
    }

    private void verificarParticipante(Usuario usuario, Entrega entrega) {
        if (!esDonante(usuario, entrega) && !esReceptora(usuario, entrega) && !esCoordinador(usuario)) {
            throw new ForbiddenOperationException("No tienes acceso a esta entrega");
        }
    }

    private Entrega buscarEntrega(Long id) {
        return entregaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entrega no encontrada con id " + id));
    }

    private Usuario cargarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId));
    }
}