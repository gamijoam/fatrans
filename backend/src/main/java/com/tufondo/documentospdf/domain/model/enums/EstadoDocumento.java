// com.tufondo.documentospdf.domain.model.enums.EstadoDocumento
package com.tufondo.documentospdf.domain.model.enums;

/**
 * Enumeración de estados del documento PDF.
 */
public enum EstadoDocumento {
    GENERADO("PDF creado, pendiente de escaneo"),
    ALMACENADO("PDF escaneado y almacenado en MinIO"),
    EXPIRADO("PDF fuera de vigencia"),
    REVOCADO("PDF revocado manualmente por ADMIN");

    private final String descripcion;

    EstadoDocumento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esDescargable() {
        return this == ALMACENADO;
    }
}
