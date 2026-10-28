package com.example.AlimentaRed.service;

import com.example.AlimentaRed.Exception.BusinessRuleException;
import com.example.AlimentaRed.Exception.ForbiddenOperationException;
import com.example.AlimentaRed.Exception.ResourceNotFoundException;
import com.example.AlimentaRed.dto.ReporteImpactoResponse;
import com.example.AlimentaRed.entity.Usuario;
import com.example.AlimentaRed.entity.enums.EstadoEntrega;
import com.example.AlimentaRed.entity.enums.EstadoSolicitud;
import com.example.AlimentaRed.entity.enums.Rol;
import com.example.AlimentaRed.repository.EntregaRepository;
import com.example.AlimentaRed.repository.LoteAlimentoRepository;
import com.example.AlimentaRed.repository.SolicitudDonacionRepository;
import com.example.AlimentaRed.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final LoteAlimentoRepository loteRepository;
    private final SolicitudDonacionRepository solicitudRepository;
    private final EntregaRepository entregaRepository;
    private final UsuarioRepository usuarioRepository;

    // RF09: indicadores de impacto por periodo y categoría (todos los filtros son opcionales)
    @Transactional(readOnly = true)
    public ReporteImpactoResponse impacto(LocalDate desde, LocalDate hasta,
                                          String categoria, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId));
        if (usuario.getRol() != Rol.COORDINADOR && usuario.getRol() != Rol.ADMINISTRADOR) {
            throw new ForbiddenOperationException("Solo un coordinador o administrador puede consultar los indicadores");
        }

        LocalDateTime inicio = desde != null ? desde.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fin = hasta != null ? hasta.atTime(LocalTime.MAX) : LocalDateTime.of(2100, 1, 1, 0, 0);
        if (inicio.isAfter(fin)) {
            throw new BusinessRuleException("La fecha inicial no puede ser posterior a la fecha final");
        }

        // Cadena vacía = sin filtro de categoría
        String cat = categoria == null ? "" : categoria.trim().toLowerCase();

        long lotes = loteRepository.contarPublicados(inicio, fin, cat);
        long solicitudes = solicitudRepository.contarSolicitudes(inicio, fin, cat);
        long aprobadas = solicitudRepository.contarSolicitudesPorEstado(
                EstadoSolicitud.APROBADA, inicio, fin, cat);
        long entregas = entregaRepository.contarPorEstado(
                EstadoEntrega.COMPLETADO, inicio, fin, cat);
        Double cantidad = entregaRepository.sumarCantidadRecuperada(
                EstadoEntrega.COMPLETADO, inicio, fin, cat);

        List<ReporteImpactoResponse.CategoriaImpacto> porCategoria = entregaRepository
                .impactoPorCategoria(EstadoEntrega.COMPLETADO, inicio, fin, cat)
                .stream()
                .map(fila -> new ReporteImpactoResponse.CategoriaImpacto(
                        (String) fila[0],
                        ((Number) fila[1]).doubleValue(),
                        ((Number) fila[2]).longValue()))
                .toList();

        return new ReporteImpactoResponse(lotes, solicitudes, aprobadas, entregas,
                cantidad != null ? cantidad : 0.0, porCategoria);
    }
}