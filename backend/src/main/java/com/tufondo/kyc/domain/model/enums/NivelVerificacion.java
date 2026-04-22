// com.tufondo.kyc.domain.model.enums.NivelVerificacion
package com.tufondo.kyc.domain.model.enums;

/**
 * Niveles de verificación KYC disponibles.
 */
public enum NivelVerificacion {
    BASICO("KYC Basico", 4),
    MEDIO("KYC Medio", 6),
    COMPLETO("KYC Completo", 8);

    private final String descripcion;
    private final int cantidadDocumentosRequeridos;

    NivelVerificacion(String descripcion, int cantidadDocs) {
        this.descripcion = descripcion;
        this.cantidadDocumentosRequeridos = cantidadDocs;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getCantidadDocumentosRequeridos() {
        return cantidadDocumentosRequeridos;
    }
}