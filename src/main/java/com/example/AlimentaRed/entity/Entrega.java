package com.example.AlimentaRed.entity;

import com.example.AlimentaRed.entity.enums.EstadoEntrega;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "entregas")
@Getter
@Setter
@NoArgsConstructor
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asignacion_id", unique = true)
    private Asignacion asignacion;

    private LocalDateTime fechaRetiro;

    private LocalDateTime fechaRecepcion;

    private String responsableRetiro;

    private String responsableRecepcion;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoEntrega estado = EstadoEntrega.PROGRAMADA;

    // PROGRAMADA -> RETIRADO
    public void registrarRetiro(String responsable) {
        if (estado != EstadoEntrega.PROGRAMADA) {
            throw new IllegalStateException("Solo se puede registrar el retiro de una entrega programada");
        }
        this.responsableRetiro = responsable;
        this.fechaRetiro = LocalDateTime.now();
        this.estado = EstadoEntrega.RETIRADO;
    }

    // RETIRADO -> COMPLETADO
    public void completarEntrega(String responsable, String observaciones) {
        if (estado != EstadoEntrega.RETIRADO) {
            throw new IllegalStateException("Solo se puede confirmar la recepción de una entrega retirada");
        }
        this.responsableRecepcion = responsable;
        this.observaciones = observaciones;
        this.fechaRecepcion = LocalDateTime.now();
        this.estado = EstadoEntrega.COMPLETADO;
    }
}