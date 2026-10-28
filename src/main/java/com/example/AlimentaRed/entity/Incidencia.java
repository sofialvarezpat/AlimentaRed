package com.example.AlimentaRed.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "incidencias")
@Getter
@Setter
@NoArgsConstructor
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entrega_id")
    private Entrega entrega;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    // Usuario que reporta la incidencia
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reportada_por_id")
    private Usuario reportadaPor;

    @Column(nullable = false)
    private LocalDateTime fechaReporte;

    @Column(nullable = false)
    private Boolean resuelta = false;

    @PrePersist
    void antesDeGuardar() {
        if (fechaReporte == null) {
            fechaReporte = LocalDateTime.now();
        }
    }
}