package com.example.AlimentaRed.repository;

import com.example.AlimentaRed.entity.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    List<HistorialEstado> findByTipoEntidadAndEntidadIdOrderByFechaAsc(String tipoEntidad, Long entidadId);
}