package com.example.AlimentaRed.entity;

import com.example.AlimentaRed.entity.enums.EstadoLote;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "lotes_alimento")
@Getter
@Setter
@NoArgsConstructor
public class LoteAlimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String categoria;

    // Cantidad con la que se publicó el lote (no cambia, sirve para los indicadores)
    @Column(nullable = false)
    private Double cantidadInicial;

    // Lo que aún se puede solicitar (disminuye al aprobar solicitudes)
    @Column(nullable = false)
    private Double cantidadDisponible;

    @Column(nullable = false, length = 50)
    private String unidadMedida;

    @Column(nullable = false)
    private LocalDateTime fechaLimite;

    // Ubicación general (barrio o localidad) para poder filtrar
    @Column(length = 150)
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoLote estado = EstadoLote.DISPONIBLE;

    @Column(nullable = false)
    private LocalDateTime fechaPublicacion;

    // Organización donante que publica el lote
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donante_id")
    private Organizacion donante;

    @PrePersist
    void antesDeGuardar() {
        if (fechaPublicacion == null) {
            fechaPublicacion = LocalDateTime.now();
        }
        if (cantidadDisponible == null) {
            cantidadDisponible = cantidadInicial;
        }
    }

    // Un lote es vigente si está disponible y su fecha límite no ha pasado
    public boolean esVigente() {
        return estado == EstadoLote.DISPONIBLE
                && fechaLimite != null
                && fechaLimite.isAfter(LocalDateTime.now());
    }

    // Regla de negocio: no se puede asignar más de lo disponible
    public void decrementarStock(Double cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (cantidad > cantidadDisponible) {
            throw new IllegalArgumentException("La cantidad supera la disponibilidad del lote");
        }
        cantidadDisponible = cantidadDisponible - cantidad;
        if (cantidadDisponible <= 0) {
            estado = EstadoLote.RESERVADO;
        }
    }
}