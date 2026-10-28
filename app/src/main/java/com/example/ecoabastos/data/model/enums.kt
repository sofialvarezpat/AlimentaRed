package com.example.ecoabastos.data.model

import com.google.gson.annotations.SerializedName

enum class Rol {
    @SerializedName("DONANTE") DONANTE,
    @SerializedName("RECEPTORA") RECEPTORA,
    @SerializedName("COORDINADOR") COORDINADOR,
    @SerializedName("ADMINISTRADOR") ADMINISTRADOR
}

enum class TipoOrganizacion {
    @SerializedName("DONANTE") DONANTE,
    @SerializedName("RECEPTORA") RECEPTORA
}

enum class EstadoLote {
    @SerializedName("DISPONIBLE", alternate = ["AVAILABLE", "PENDIENTE", "PENDING"])
    DISPONIBLE,

    @SerializedName("RESERVADO", alternate = ["RESERVED", "ASIGNADO", "ASSIGNED"])
    RESERVADO,

    @SerializedName("ENTREGADO", alternate = ["DELIVERED", "RECOGIDO", "RECIBIDO", "COMPLETADO", "COMPLETED"])
    ENTREGADO,

    @SerializedName("VENCIDO", alternate = ["EXPIRED"])
    VENCIDO,

    @SerializedName("CANCELADO", alternate = ["CANCELLED", "CANCELED"])
    CANCELADO
}

enum class EstadoSolicitud {
    @SerializedName("PENDIENTE", alternate = ["PENDING"])
    PENDIENTE,

    @SerializedName("APROBADA", alternate = ["APPROVED", "ACEPTADA"])
    APROBADA,

    @SerializedName("RECHAZADA", alternate = ["REJECTED"])
    RECHAZADA,

    @SerializedName("CANCELADA", alternate = ["CANCELLED", "CANCELED"])
    CANCELADA
}

enum class EstadoEntrega {
    @SerializedName("PROGRAMADA", alternate = ["SCHEDULED", "PENDIENTE", "PENDING"])
    PROGRAMADA,

    @SerializedName("RETIRADO", alternate = ["RETIRADA", "RECOGIDO", "EN_RETIRO", "EN_TRANSITO", "PICKED_UP"])
    RETIRADO,

    @SerializedName("COMPLETADO", alternate = ["RECIBIDA", "COMPLETED", "RECIBIDO", "ENTREGADO", "FINALIZADO"])
    COMPLETADO,

    @SerializedName("INCIDENCIA", alternate = ["ISSUE", "INCIDENT"])
    INCIDENCIA
}
