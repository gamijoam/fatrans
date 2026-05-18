package com.tufondo.compliance.application.service;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Lanzada cuando una operación financiera (depósito/retiro) supera el
 * umbral LOCDOFT configurado y el cliente NO envió la declaración
 * jurada {@code confirmaOrigenLicito=true}.
 *
 * <p>Mapea a HTTP 422 (Unprocessable Entity) con código de error
 * {@code LOCDOFT_CONSENT_REQUIRED}. El frontend usa este código para
 * abrir el modal de declaración y reintentar con el flag.</p>
 */
@Getter
public class LocdoftConsentimientoRequeridoException extends RuntimeException {

    public static final String CODE = "LOCDOFT_CONSENT_REQUIRED";

    private final BigDecimal montoSolicitado;
    private final String moneda;
    private final BigDecimal umbral;

    public LocdoftConsentimientoRequeridoException(BigDecimal montoSolicitado, String moneda, BigDecimal umbral) {
        super("La operación supera el umbral LOCDOFT (" + umbral + " " + moneda
                + "). Se requiere declaración jurada de origen lícito de los fondos.");
        this.montoSolicitado = montoSolicitado;
        this.moneda = moneda;
        this.umbral = umbral;
    }
}
