package com.example.AlimentaRed.entity;

import com.example.AlimentaRed.entity.enums.EstadoSolicitud;
import com.example.AlimentaRed.entity.enums.TipoOrganizacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_donacion")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudDonacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id")
    private LoteAlimento lote;

    // Organización receptora que hace la solicitud
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacion_id")
    private Organizacion organizacion;

    @Column(nullable = false)
    private Double cantidadSolicitada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    @PrePersist
    void antesDeGuardar() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }
    }

    // Reglas de negocio que se validan al crear la solicitud
    public void validarReglasNegocio() {
        if (lote == null) {
            throw new IllegalArgumentException("La solicitud debe tener un lote");
        }
        if (organizacion == null || organizacion.getTipo() != TipoOrganizacion.RECEPTORA) {
            throw new IllegalArgumentException("Solo una organización receptora puede solicitar lotes");
        }
        if (cantidadSolicitada == null || cantidadSolicitada <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser mayor a cero");
        }
        if (!lote.esVigente()) {
            throw new IllegalArgumentException("El lote no está disponible: está vencido, cancelado, reservado o entregado");
        }
        if (cantidadSolicitada > lote.getCantidadDisponible()) {
            throw new IllegalArgumentException("La cantidad solicitada supera la disponibilidad del lote");
        }
    }
}