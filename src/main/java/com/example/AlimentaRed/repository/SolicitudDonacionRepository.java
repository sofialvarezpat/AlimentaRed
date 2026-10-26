package com.example.AlimentaRed.repository;

import com.example.AlimentaRed.entity.SolicitudDonacion;
import com.example.AlimentaRed.entity.enums.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

import java.util.List;

public interface SolicitudDonacionRepository extends JpaRepository<SolicitudDonacion, Long> {

    // "Mis solicitudes" de una organización receptora
    List<SolicitudDonacion> findByOrganizacionId(Long organizacionId);

    // Solicitudes recibidas por los lotes de un donante
    List<SolicitudDonacion> findByLoteDonanteId(Long donanteId);

    List<SolicitudDonacion> findByLoteId(Long loteId);

    List<SolicitudDonacion> findByEstado(EstadoSolicitud estado);

    boolean existsByLoteIdAndOrganizacionIdAndEstado(Long loteId, Long organizacionId, EstadoSolicitud estado);

    @Query("""
            SELECT COUNT(s) FROM SolicitudDonacion s
            WHERE s.fechaSolicitud BETWEEN :desde AND :hasta
              AND (:categoria = '' OR LOWER(s.lote.categoria) = :categoria)
            """)
    long contarSolicitudes(@Param("desde") LocalDateTime desde,
                           @Param("hasta") LocalDateTime hasta,
                           @Param("categoria") String categoria);

    @Query("""
            SELECT COUNT(s) FROM SolicitudDonacion s
            WHERE s.estado = :estado
              AND s.fechaSolicitud BETWEEN :desde AND :hasta
              AND (:categoria = '' OR LOWER(s.lote.categoria) = :categoria)
            """)
    long contarSolicitudesPorEstado(@Param("estado") EstadoSolicitud estado,
                                    @Param("desde") LocalDateTime desde,
                                    @Param("hasta") LocalDateTime hasta,
                                    @Param("categoria") String categoria);

}