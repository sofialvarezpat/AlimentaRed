package com.example.AlimentaRed.service;

import com.example.AlimentaRed.Exception.BusinessRuleException;
import com.example.AlimentaRed.Exception.ForbiddenOperationException;
import com.example.AlimentaRed.Exception.ResourceNotFoundException;
import com.example.AlimentaRed.dto.LoteRequest;
import com.example.AlimentaRed.dto.LoteResponse;
import com.example.AlimentaRed.entity.LoteAlimento;
import com.example.AlimentaRed.entity.Organizacion;
import com.example.AlimentaRed.entity.Usuario;
import com.example.AlimentaRed.entity.enums.EstadoLote;
import com.example.AlimentaRed.entity.enums.Rol;
import com.example.AlimentaRed.entity.enums.TipoOrganizacion;
import com.example.AlimentaRed.repository.LoteAlimentoRepository;
import com.example.AlimentaRed.repository.UsuarioRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoteService {

    private final LoteAlimentoRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialService historialService;

    // RF03: un donante publica un lote
    @Transactional
    public LoteResponse crear(LoteRequest request, Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        Organizacion organizacion = usuario.getOrganizacion();

        if (usuario.getRol() != Rol.DONANTE
                || organizacion == null
                || organizacion.getTipo() != TipoOrganizacion.DONANTE) {
            throw new ForbiddenOperationException("Solo un donante con organización puede publicar lotes");
        }

        LoteAlimento lote = new LoteAlimento();
        lote.setNombre(request.nombre().trim());
        lote.setCategoria(request.categoria().trim());
        lote.setCantidadInicial(request.cantidad());
        lote.setCantidadDisponible(request.cantidad());
        lote.setUnidadMedida(request.unidadMedida().trim());
        lote.setFechaLimite(request.fechaLimite());
        lote.setUbicacion(request.ubicacion());
        lote.setEstado(EstadoLote.DISPONIBLE);
        lote.setDonante(organizacion);

        LoteAlimento guardado = loteRepository.save(lote);
        historialService.registrar(HistorialService.LOTE, guardado.getId(),
                null, EstadoLote.DISPONIBLE.name(), usuario);
        return LoteResponse.desde(guardado);
    }

    // RF04: consultar y filtrar lotes
    @Transactional(readOnly = true)
    public List<LoteResponse> listar(String categoria, String ubicacion,
                                     EstadoLote estado, LocalDate fechaLimiteHasta) {
        Specification<LoteAlimento> filtros = construirFiltros(categoria, ubicacion, estado, fechaLimiteHasta);
        return loteRepository.findAll(filtros, Sort.by("fechaLimite").ascending())
                .stream()
                .map(LoteResponse::desde)
                .toList();
    }

    // "Mis donaciones": lotes de la organización del donante
    @Transactional(readOnly = true)
    public List<LoteResponse> listarMisLotes(Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        if (usuario.getOrganizacion() == null) {
            return List.of();
        }
        return loteRepository.findByDonanteId(usuario.getOrganizacion().getId())
                .stream()
                .map(LoteResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public LoteResponse obtener(Long id) {
        return LoteResponse.desde(buscarLote(id));
    }

    // El donante dueño del lote, o un coordinador/administrador, puede cancelarlo
    @Transactional
    public LoteResponse cancelar(Long id, Long usuarioId) {
        Usuario usuario = cargarUsuario(usuarioId);
        LoteAlimento lote = buscarLote(id);

        boolean esDueno = usuario.getOrganizacion() != null
                && usuario.getOrganizacion().getId().equals(lote.getDonante().getId());
        boolean esCoordinador = usuario.getRol() == Rol.COORDINADOR
                || usuario.getRol() == Rol.ADMINISTRADOR;
        if (!esDueno && !esCoordinador) {
            throw new ForbiddenOperationException("No tienes permiso para cancelar este lote");
        }

        if (lote.getEstado() == EstadoLote.ENTREGADO || lote.getEstado() == EstadoLote.CANCELADO) {
            throw new BusinessRuleException("No se puede cancelar un lote en estado " + lote.getEstado());
        }

        String estadoAnterior = lote.getEstado().name();
        lote.setEstado(EstadoLote.CANCELADO);
        historialService.registrar(HistorialService.LOTE, lote.getId(),
                estadoAnterior, EstadoLote.CANCELADO.name(), usuario);
        return LoteResponse.desde(lote);
    }

    private LoteAlimento buscarLote(Long id) {
        return loteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con id " + id));
    }

    private Usuario cargarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId));
    }

    // Arma los filtros opcionales: solo se aplican los que vengan con valor
    private Specification<LoteAlimento> construirFiltros(String categoria, String ubicacion,
                                                         EstadoLote estado, LocalDate fechaLimiteHasta) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();

            if (categoria != null && !categoria.isBlank()) {
                condiciones.add(cb.equal(cb.lower(root.get("categoria")), categoria.trim().toLowerCase()));
            }
            if (ubicacion != null && !ubicacion.isBlank()) {
                condiciones.add(cb.like(cb.lower(root.get("ubicacion")),
                        "%" + ubicacion.trim().toLowerCase() + "%"));
            }
            if (estado != null) {
                condiciones.add(cb.equal(root.get("estado"), estado));
                // Un lote "disponible" cuya fecha límite ya pasó no debe mostrarse
                if (estado == EstadoLote.DISPONIBLE) {
                    condiciones.add(cb.greaterThan(root.get("fechaLimite"), LocalDateTime.now()));
                }
            }
            if (fechaLimiteHasta != null) {
                condiciones.add(cb.lessThanOrEqualTo(root.get("fechaLimite"),
                        fechaLimiteHasta.atTime(LocalTime.MAX)));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }
}