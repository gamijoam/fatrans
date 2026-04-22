// com.tufondo.kyc.domain.model.enums.EstadoVerificacion
package com.tufondo.kyc.domain.model.enums;

/**
 * Estados posibles de una verificacion KYC.
 */
public enum EstadoVerificacion {
    PENDIENTE("Documentos enviados, esperando validacion"),
    EN_REVISION("En revision por analista"),
    APROBADO("Verificacion exitosa"),
    RECHAZADO("Rechazado"),
    REENVIADO("Documentos reenviados despues de rechazo"),
    EXPIRADO("Verificacion expirada, requiere renovacion"),
    CANCELADO("Cancelado por el usuario");

    private final String descripcion;

    EstadoVerificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esEditable() {
        return this == PENDIENTE || this == RECHAZADO || this == REENVIADO;
    }

    public boolean puedeTransicionarA(EstadoVerificacion nuevoEstado) {
        return switch (this) {
            case PENDIENTE -> nuevoEstado == EN_REVISION;
            case EN_REVISION -> nuevoEstado == APROBADO || nuevoEstado == RECHAZADO || nuevoEstado == PENDIENTE;
            case RECHAZADO -> nuevoEstado == PENDIENTE || nuevoEstado == REENVIADO;
            case REENVIADO -> nuevoEstado == EN_REVISION;
            case APROBADO -> nuevoEstado == EXPIRADO;
            case EXPIRADO -> nuevoEstado == PENDIENTE;
            case CANCELADO -> false;
        };
    }
}