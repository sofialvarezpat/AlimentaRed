package com.example.AlimentaRed.repository;

import com.example.AlimentaRed.entity.Entrega;
import com.example.AlimentaRed.entity.enums.EstadoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EntregaRepository extends JpaRepository<Entrega, Long> {

    Optional<Entrega> findByAsignacionId(Long asignacionId);

    List<Entrega> findByEstado(EstadoEntrega estado);

    List<Entrega> findByAsignacionSolicitudOrganizacionId(Long organizacionId);

    List<Entrega> findByAsignacionSolicitudLoteDonanteId(Long donanteId);

    long countByAsignacionSolicitudLoteIdAndEstadoNot(Long loteId, EstadoEntrega estado);

    @Query("""
            SELECT COUNT(e) FROM Entrega e
            WHERE e.estado = :estado
              AND e.fechaRecepcion BETWEEN :desde AND :hasta
              AND (:categoria = '' OR LOWER(e.asignacion.solicitud.lote.categoria) = :categoria)
            """)
    long contarPorEstado(@Param("estado") EstadoEntrega estado,
                         @Param("desde") LocalDateTime desde,
                         @Param("hasta") LocalDateTime hasta,
                         @Param("categoria") String categoria);

    @Query("""
            SELECT COALESCE(SUM(e.asignacion.cantidadAsignada), 0.0) FROM Entrega e
            WHERE e.estado = :estado
              AND e.fechaRecepcion BETWEEN :desde AND :hasta
              AND (:categoria = '' OR LOWER(e.asignacion.solicitud.lote.categoria) = :categoria)
            """)
    Double sumarCantidadRecuperada(@Param("estado") EstadoEntrega estado,
                                   @Param("desde") LocalDateTime desde,
                                   @Param("hasta") LocalDateTime hasta,
                                   @Param("categoria") String categoria);

    @Query("""
            SELECT l.categoria, SUM(a.cantidadAsignada), COUNT(e)
            FROM Entrega e
            JOIN e.asignacion a
            JOIN a.solicitud s
            JOIN s.lote l
            WHERE e.estado = :estado
              AND e.fechaRecepcion BETWEEN :desde AND :hasta
              AND (:categoria = '' OR LOWER(l.categoria) = :categoria)
            GROUP BY l.categoria
            ORDER BY SUM(a.cantidadAsignada) DESC
            """)
    List<Object[]> impactoPorCategoria(@Param("estado") EstadoEntrega estado,
                                       @Param("desde") LocalDateTime desde,
                                       @Param("hasta") LocalDateTime hasta,
                                       @Param("categoria") String categoria);

}
