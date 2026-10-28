package com.example.AlimentaRed.service.serviceIMP;

import com.example.AlimentaRed.Exception.ResourceNotFoundException;
import com.example.AlimentaRed.dto.LoteRequest;
import com.example.AlimentaRed.dto.LoteResponse;
import com.example.AlimentaRed.entity.LoteAlimento;
import com.example.AlimentaRed.entity.Usuario;
import com.example.AlimentaRed.entity.enums.EstadoLote;
import com.example.AlimentaRed.repository.LoteAlimentoRepository;

import com.example.AlimentaRed.repository.UsuarioRepository;
import com.example.AlimentaRed.service.HistorialService;
import com.example.AlimentaRed.service.LoteService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoteServiceIMP implements LoteService {

    private final LoteAlimentoRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialService historialService;

    @Override
    @Transactional(readOnly = true)
    public List<LoteResponse> listar(String categoria, String ubicacion, EstadoLote estado, LocalDate fechaLimiteHasta) {
        // Puedes implementar especificaciones JPA o filtros sencillos
        return loteRepository.findAll().stream()
                .map(LoteResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteResponse> listarMisLotes(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (usuario.getOrganizacion() == null) return List.of();
        return loteRepository.findByDonanteId(usuario.getOrganizacion().getId()).stream()
                .map(LoteResponse::desde)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public LoteResponse obtener(Long id) {
        LoteAlimento lote = loteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con id " + id));
        return LoteResponse.desde(lote);
    }

    @Override
    @Transactional
    public LoteResponse crear(LoteRequest request, MultipartFile imagen, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        LoteAlimento lote = new LoteAlimento();
        lote.setNombre(request.nombre());
        lote.setCategoria(request.categoria());
        lote.setCantidadInicial(request.cantidad());
        lote.setCantidadDisponible(request.cantidad());
        lote.setUnidadMedida(request.unidadMedida());
        lote.setFechaLimite(request.fechaLimite());
        lote.setUbicacion(request.ubicacion());
        lote.setEstado(EstadoLote.DISPONIBLE);
        lote.setFechaPublicacion(LocalDateTime.now());
        lote.setDonante(usuario.getOrganizacion());

        // Opcional: Manejar la subida de la imagen (guardarla en disco/S3 y guardar la URL en el lote)
        if (imagen != null && !imagen.isEmpty()) {
            // lote.setImagenUrl(almacenamientoService.guardar(imagen));
        }

        LoteAlimento guardado = loteRepository.save(lote);
        historialService.registrar(HistorialService.LOTE, guardado.getId(), null, EstadoLote.DISPONIBLE.name(), usuario);
        return LoteResponse.desde(guardado);
    }

    @Override
    @Transactional
    public LoteResponse cancelar(Long id, Long usuarioId) {
        LoteAlimento lote = loteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado"));

        String estadoAnterior = lote.getEstado().name();
        lote.setEstado(EstadoLote.CANCELADO);
        loteRepository.save(lote);

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        historialService.registrar(HistorialService.LOTE, lote.getId(), estadoAnterior, EstadoLote.CANCELADO.name(), usuario);

        return LoteResponse.desde(lote);
    }
}