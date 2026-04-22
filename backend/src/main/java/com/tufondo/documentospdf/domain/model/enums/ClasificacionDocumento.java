// com.tufondo.documentospdf.domain.model.enums.ClasificacionDocumento
package com.tufondo.documentospdf.domain.model.enums;

/**
 * Enumeración de clasificación de seguridad de documentos.
 */
public enum ClasificacionDocumento {
    CONFIDENCIAL("Datos financieros sensibles"),
    RESTRINGIDO("Contratos y pagarés"),
    PUBLICO("Constancias de afiliación");

    private final String descripcion;

    ClasificacionDocumento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
