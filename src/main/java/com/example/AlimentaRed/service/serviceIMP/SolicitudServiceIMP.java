package com.example.AlimentaRed.service.serviceIMP;
import com.example.AlimentaRed.Exception.BusinessRuleException;
import com.example.AlimentaRed.Exception.ForbiddenOperationException;
import com.example.AlimentaRed.Exception.ResourceNotFoundException;
import com.example.AlimentaRed.dto.ResolverSolicitudRequest;
import com.example.AlimentaRed.dto.SolicitudRequest;
import com.example.AlimentaRed.dto.SolicitudResponse;
import com.example.AlimentaRed.entity.Asignacion;
import com.example.AlimentaRed.entity.Entrega;
import com.example.AlimentaRed.entity.LoteAlimento;
import com.example.AlimentaRed.entity.Organizacion;
import com.example.AlimentaRed.entity.SolicitudDonacion;
import com.example.AlimentaRed.entity.Usuario;
import com.example.AlimentaRed.entity.enums.EstadoSolicitud;
import com.example.AlimentaRed.entity.enums.Rol;
import com.example.AlimentaRed.entity.enums.TipoOrganizacion;
import com.example.AlimentaRed.repository.AsignacionRepository;
import com.example.AlimentaRed.repository.EntregaRepository;
import com.example.AlimentaRed.repository.LoteAlimentoRepository;
import com.example.AlimentaRed.repository.SolicitudDonacionRepository;
import com.example.AlimentaRed.repository.UsuarioRepository;
import com.example.AlimentaRed.service.HistorialService;
import com.example.AlimentaRed.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudServiceIMP implements SolicitudService {

    private final SolicitudDonacionRepository solicitudRepository;
    private final LoteAlimentoRepository loteRepository;
    private final AsignacionRepository asignacionRepository;
    private final EntregaRepository entregaRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialService historialService;

    @Override
    @Transactional
    public SolicitudResponse crear(SolicitudRequest request, Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        Organizacion organizacion = usuario.getOrganizacion();

        if (usuario.getRol() != Rol.RECEPTORA
                || organizacion == null
                || organizacion.getTipo() != TipoOrganizacion.RECEPTORA) {
            throw new ForbiddenOperationException("Solo una organización receptora puede solicitar lotes");
        }

        LoteAlimento lote = loteRepository.findById(request.loteId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con id " + request.loteId()));

        if (solicitudRepository.existsByLoteIdAndOrganizacionIdAndEstado(
                lote.getId(), organizacion.getId(), EstadoSolicitud.PENDIENTE)) {
            throw new BusinessRuleException("Ya tienes una solicitud pendiente para este lote");
        }

        SolicitudDonacion solicitud = new SolicitudDonacion();
        solicitud.setLote(lote);
        solicitud.setOrganizacion(organizacion);
        solicitud.setCantidadSolicitada(request.cantidadSolicitada());
        solicitud.validarReglasNegocio();

        SolicitudDonacion guardada = solicitudRepository.save(solicitud);
        historialService.registrar(HistorialService.SOLICITUD, guardada.getId(),
                null, EstadoSolicitud.PENDIENTE.name(), usuario);
        return SolicitudResponse.desde(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudResponse> listarMisSolicitudes(Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        Organizacion organizacion = usuario.getOrganizacion();

        List<SolicitudDonacion> solicitudes = switch (usuario.getRol()) {
            case RECEPTORA -> organizacion == null
                    ? List.of()
                    : solicitudRepository.findByOrganizacionId(organizacion.getId());
            case DONANTE -> organizacion == null
                    ? List.of()
                    : solicitudRepository.findByLoteDonanteId(organizacion.getId());
            case COORDINADOR, ADMINISTRADOR -> solicitudRepository.findAll();
        };

        return solicitudes.stream().map(SolicitudResponse::desde).toList();
    }

    @Override
    @Transactional
    public SolicitudResponse cancelar(Long id, Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        SolicitudDonacion solicitud = buscarSolicitud(id);

        boolean esDuena = usuario.getOrganizacion() != null
                && usuario.getOrganizacion().getId().equals(solicitud.getOrganizacion().getId());
        boolean esCoordinador = usuario.getRol() == Rol.COORDINADOR
                || usuario.getRol() == Rol.ADMINISTRADOR;
        if (!esDuena && !esCoordinador) {
            throw new ForbiddenOperationException("No tienes permiso para cancelar esta solicitud");
        }

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BusinessRuleException("Solo se pueden cancelar solicitudes pendientes");
        }

        cambiarEstado(solicitud, EstadoSolicitud.CANCELADA, usuario);
        return SolicitudResponse.desde(solicitud);
    }

    @Override
    @Transactional
    public SolicitudResponse resolver(Long id, ResolverSolicitudRequest request, Long usuarioId) {
        Usuario coordinador = cargarUsuario(usuarioId);
        if (coordinador.getRol() != Rol.COORDINADOR) {
            throw new ForbiddenOperationException("Solo un coordinador puede resolver solicitudes");
        }

        SolicitudDonacion solicitud = buscarSolicitud(id);
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new BusinessRuleException("La solicitud ya no está pendiente (estado " + solicitud.getEstado() + ")");
        }

        if (!request.aprobada()) {
            cambiarEstado(solicitud, EstadoSolicitud.RECHAZADA, coordinador);
            return SolicitudResponse.desde(solicitud);
        }

        LoteAlimento lote = loteRepository.findByIdParaActualizar(solicitud.getLote().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));

        if (!lote.esVigente()) {
            throw new BusinessRuleException("El lote ya no está disponible: puede estar vencido, cancelado o agotado");
        }

        Double cantidad = request.cantidadAsignada() != null
                ? request.cantidadAsignada()
                : solicitud.getCantidadSolicitada();

        if (cantidad > solicitud.getCantidadSolicitada()) {
            throw new BusinessRuleException("No se puede asignar más de lo que se solicitó");
        }

        String estadoLoteAnterior = lote.getEstado().name();
        lote.decrementarStock(cantidad);
        if (!lote.getEstado().name().equals(estadoLoteAnterior)) {
            historialService.registrar(HistorialService.LOTE, lote.getId(),
                    estadoLoteAnterior, lote.getEstado().name(), coordinador);
        }

        cambiarEstado(solicitud, EstadoSolicitud.APROBADA, coordinador);

        Asignacion asignacion = new Asignacion();
        asignacion.setSolicitud(solicitud);
        asignacion.setCoordinador(coordinador);
        asignacion.setCantidadAsignada(cantidad);
        asignacion.setObservaciones(request.observaciones());
        asignacionRepository.save(asignacion);

        Entrega entrega = new Entrega();
        entrega.setAsignacion(asignacion);
        entregaRepository.save(entrega);
        historialService.registrar(HistorialService.ENTREGA, entrega.getId(),
                null, entrega.getEstado().name(), coordinador);

        return SolicitudResponse.desde(solicitud);
    }

    private void cambiarEstado(SolicitudDonacion solicitud, EstadoSolicitud nuevo, Usuario usuario) {
        String anterior = solicitud.getEstado().name();
        solicitud.setEstado(nuevo);
        historialService.registrar(HistorialService.SOLICITUD, solicitud.getId(),
                anterior, nuevo.name(), usuario);
    }

    private SolicitudDonacion buscarSolicitud(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada con id " + id));
    }

    private Usuario cargarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId));
    }
}
