package com.example.AlimentaRed.service;

import com.example.AlimentaRed.entity.HistorialEstado;
import com.example.AlimentaRed.entity.Usuario;
import com.example.AlimentaRed.entity.enums.EstadoEntrega;
import com.example.AlimentaRed.repository.HistorialEstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HistorialService {

    public static final String LOTE = "LOTE";
    public static final String SOLICITUD = "SOLICITUD";
    public static final String ENTREGA = "ENTREGA";

    private final HistorialEstadoRepository historialRepository;

    @Transactional
    public void registrar(String tipoEntidad, Long entidadId,
                          String estadoAnterior, String estadoNuevo, Usuario usuario) {
        HistorialEstado registro = new HistorialEstado();
        registro.setTipoEntidad(tipoEntidad);
        registro.setEntidadId(entidadId);
        registro.setEstadoAnterior(estadoAnterior);
        registro.setEstadoNuevo(estadoNuevo);
        registro.setUsuario(usuario);
        historialRepository.save(registro);
    }
}