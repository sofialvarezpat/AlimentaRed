package com.example.AlimentaRed.repository;

import com.example.AlimentaRed.entity.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    List<Incidencia> findByEntregaId(Long entregaId);
}