// com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC
package com.tufondo.kyc.domain.model.enums;

/**
 * Tipos de documentos de identidad aceptados en KYC.
 */
public enum TipoDocumentoKYC {
    CEDULA_ANVERSO("Cedula de Identidad - Anverso", true, false),
    CEDULA_REVERSO("Cedula de Identidad - Reverso", true, false),
    SELFIE_CEDULA("Selfie con Cedula", true, false),
    COMPROBANTE_DOMICILIO("Comprobante de Domicilio", true, true),
    PASAPORTE("Pasaporte", false, false),
    RIF_NIT("RIF/NIT", false, true),
    CONSTANCIA_TRABAJO("Constancia de Trabajo", false, true),
    ESTADO_CUENTA_BANCARIO("Estado de Cuenta Bancario", false, true),
    REFERENCIA_PERSONAL("Referencia Personal", false, true);

    private final String descripcion;
    private final boolean esRequeridoBasico;
    private final boolean tieneExpiracion;

    TipoDocumentoKYC(String descripcion, boolean requeridoBasico, boolean tieneExp) {
        this.descripcion = descripcion;
        this.esRequeridoBasico = requeridoBasico;
        this.tieneExpiracion = tieneExp;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean isRequeridoBasico() {
        return esRequeridoBasico;
    }

    public boolean tieneExpiracion() {
        return tieneExpiracion;
    }

    public static TipoDocumentoKYC[] getDocumentosRequeridosBasicos() {
        return new TipoDocumentoKYC[]{
            CEDULA_ANVERSO, CEDULA_REVERSO, SELFIE_CEDULA, COMPROBANTE_DOMICILIO
        };
    }
}