package com.example.AlimentaRed.service;

import com.example.AlimentaRed.dto.LoteRequest;
import com.example.AlimentaRed.dto.LoteResponse;
import com.example.AlimentaRed.entity.enums.EstadoLote;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDate;
import java.util.List;

public interface LoteService {
    List<LoteResponse> listar(String categoria, String ubicacion, EstadoLote estado, LocalDate fechaLimiteHasta);
    List<LoteResponse> listarMisLotes(Long usuarioId);
    LoteResponse obtener(Long id);
    LoteResponse crear(LoteRequest request, MultipartFile imagen, Long usuarioId);
    LoteResponse cancelar(Long id, Long usuarioId);
}