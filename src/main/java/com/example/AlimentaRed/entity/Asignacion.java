package com.example.AlimentaRed.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "asignaciones")
@Getter
@Setter
@NoArgsConstructor
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Una solicitud aprobada genera una sola asignación
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_id", unique = true)
    private SolicitudDonacion solicitud;

    // Coordinador que aprobó la solicitud
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coordinador_id")
    private Usuario coordinador;

    @Column(nullable = false)
    private Double cantidadAsignada;

    @Column(nullable = false)
    private LocalDateTime fechaAsignacion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @PrePersist
    void antesDeGuardar() {
        if (fechaAsignacion == null) {
            fechaAsignacion = LocalDateTime.now();
        }
    }
}