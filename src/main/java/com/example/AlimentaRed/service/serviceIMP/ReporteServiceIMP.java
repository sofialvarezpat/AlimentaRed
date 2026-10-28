package com.example.AlimentaRed.service.serviceIMP;

import com.example.AlimentaRed.dto.ReporteImpactoResponse;
import com.example.AlimentaRed.entity.enums.EstadoEntrega;
import com.example.AlimentaRed.entity.enums.EstadoSolicitud;
import com.example.AlimentaRed.repository.EntregaRepository;
import com.example.AlimentaRed.repository.LoteAlimentoRepository;
import com.example.AlimentaRed.repository.SolicitudDonacionRepository;
import com.example.AlimentaRed.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ReporteServiceIMP implements ReporteService {

    private final EntregaRepository entregaRepository;
    private final LoteAlimentoRepository loteRepository;
    private final SolicitudDonacionRepository solicitudRepository;

    @Override
    @Transactional(readOnly = true)
    public ReporteImpactoResponse impacto(LocalDate desdeDate, LocalDate hastaDate, String categoria, Long usuarioId) {
        LocalDateTime desde = desdeDate != null ? desdeDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime hasta = hastaDate != null ? hastaDate.atTime(23, 59, 59) : LocalDateTime.now();
        String cat = categoria != null ? categoria.toLowerCase() : "";

        // 1. Total de lotes publicados en el periodo
        long totalPublicados = loteRepository.contarPublicados(desde, hasta, cat);

        // 2. Total de solicitudes registradas
        long totalSolicitudes = solicitudRepository.contarSolicitudes(desde, hasta, cat);

        // 3. Solicitudes aprobadas
        long solicitudesAprobadas = solicitudRepository.contarSolicitudesPorEstado(EstadoSolicitud.APROBADA, desde, hasta, cat);

        // 4. Entregas completadas (recibidas)
        long entregasCompletadas = entregaRepository.contarPorEstado(EstadoEntrega.COMPLETADO, desde, hasta, cat);

        // 5. Cantidad total recuperada
        Double cantidadRecuperada = entregaRepository.sumarCantidadRecuperada(EstadoEntrega.COMPLETADO, desde, hasta, cat);
        double totalRecuperado = cantidadRecuperada != null ? cantidadRecuperada : 0.0;

        // 6. Impacto agrupado por categoría
        List<Object[]> resultadosPorCategoria = entregaRepository.impactoPorCategoria(EstadoEntrega.COMPLETADO, desde, hasta, cat);
        List<ReporteImpactoResponse.CategoriaImpacto> porCategoria = resultadosPorCategoria.stream()
                .map(row -> new ReporteImpactoResponse.CategoriaImpacto(
                        (String) row[0],
                        (Double) row[1],
                        (Long) row[2]
                ))
                .toList();

        return new ReporteImpactoResponse(
                totalPublicados,
                totalSolicitudes,
                solicitudesAprobadas,
                entregasCompletadas,
                totalRecuperado,
                porCategoria
        );
    }
}