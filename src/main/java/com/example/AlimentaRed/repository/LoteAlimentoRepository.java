package com.example.AlimentaRed.repository;

import com.example.AlimentaRed.entity.LoteAlimento;
import com.example.AlimentaRed.entity.enums.EstadoLote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LoteAlimentoRepository
        extends JpaRepository<LoteAlimento, Long>, JpaSpecificationExecutor<LoteAlimento> {

    List<LoteAlimento> findByDonanteId(Long donanteId);

    // Para marcar como VENCIDOS los lotes disponibles cuya fecha límite ya pasó
    List<LoteAlimento> findByEstadoAndFechaLimiteBefore(EstadoLote estado, LocalDateTime fecha);

    // Bloquea la fila mientras se aprueba una solicitud, para evitar asignaciones duplicadas
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LoteAlimento l WHERE l.id = :id")
    Optional<LoteAlimento> findByIdParaActualizar(@Param("id") Long id);
    @Query("""
            SELECT COUNT(l) FROM LoteAlimento l
            WHERE l.fechaPublicacion BETWEEN :desde AND :hasta
              AND (:categoria = '' OR LOWER(l.categoria) = :categoria)
            """)
    long contarPublicados(@Param("desde") LocalDateTime desde,
                          @Param("hasta") LocalDateTime hasta,
                          @Param("categoria") String categoria);
}
