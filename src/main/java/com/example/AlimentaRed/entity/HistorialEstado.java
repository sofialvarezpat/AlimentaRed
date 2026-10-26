package com.example.AlimentaRed.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_estados")
@Getter
@Setter
@NoArgsConstructor
public class HistorialEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Qué tipo de registro cambió: LOTE, SOLICITUD o ENTREGA
    @Column(nullable = false, length = 50)
    private String tipoEntidad;

    // Id del lote, solicitud o entrega que cambió
    @Column(nullable = false)
    private Long entidadId;

    @Column(length = 50)
    private String estadoAnterior;

    @Column(nullable = false, length = 50)
    private String estadoNuevo;

    // Usuario responsable del cambio
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    void antesDeGuardar() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}