// com.tufondo.kyc.domain.model.enums.EstadoDocumento
package com.tufondo.kyc.domain.model.enums;

/**
 * Estados posibles de un documento de identidad.
 */
public enum EstadoDocumento {
    PENDIENTE("Pendiente de validacion"),
    VALIDADO("Documento validado"),
    RECHAZADO("Documento rechazado"),
    EXPIRADO("Documento vencido");

    private final String descripcion;

    EstadoDocumento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}