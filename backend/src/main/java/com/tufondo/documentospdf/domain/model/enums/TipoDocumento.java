// com.tufondo.documentospdf.domain.model.enums.TipoDocumento
package com.tufondo.documentospdf.domain.model.enums;

/**
 * Enumeración de tipos de documentos PDF generados por el sistema.
 * Algunos tipos requieren firma digital RSA SHA-256.
 */
public enum TipoDocumento {
    ESTADO_CUENTA("Estado de Cuenta", false),
    CONSTANCIA_AFILIACION("Constancia de Afiliación", false),
    CONTRATO_ADHESION("Contrato de Adhesión", true),
    PAGARE("Pagaré de Crédito", true),
    TABLA_AMORTIZACION("Tabla de Amortización", false),
    CARTA_BENEFICIARIOS("Carta de Beneficiarios", false),
    /**
     * Comprobante on-demand de un movimiento individual (issue #220).
     * NO se persiste en MinIO — se regenera cada vez desde el movimiento
     * almacenado (que es la fuente de verdad inmutable, RN-006).
     */
    COMPROBANTE_MOVIMIENTO("Comprobante de Movimiento", false);

    private final String descripcion;
    private final boolean requiereFirmaDigital;

    TipoDocumento(String descripcion, boolean requiereFirmaDigital) {
        this.descripcion = descripcion;
        this.requiereFirmaDigital = requiereFirmaDigital;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean requiereFirmaDigital() {
        return requiereFirmaDigital;
    }
}
