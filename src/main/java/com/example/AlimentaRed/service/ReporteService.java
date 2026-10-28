package com.example.AlimentaRed.service;

import com.example.AlimentaRed.dto.ReporteImpactoResponse;
import java.time.LocalDate;

public interface ReporteService {
    ReporteImpactoResponse impacto(LocalDate desde, LocalDate hasta, String categoria, Long usuarioId);
}
