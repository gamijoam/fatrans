package com.tufondo.compliance.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Declaración jurada LOCDOFT capturada al momento de una operación grande
 * (#218 PR-C).
 *
 * <p>Se registra cuando el socio confirma origen lícito de los fondos en
 * una operación que supera el umbral configurado en
 * {@code parametros_sistema.LOCDOFT_UMBRAL_*}. Se persiste ANTES de
 * intentar la operación — si la operación falla por otra razón (saldo
 * insuficiente, límite excedido), el consentimiento queda igual como
 * evidencia del intento + declaración.</p>
 *
 * <p>Conservación: mínimo 5 años (Art. 9 LOCDOFT).</p>
 */
@Getter
@Builder
public class ConsentimientoLocdoftOperacion {

    private final UUID id;
    private final UUID socioId;
    private final UUID cuentaAhorroId;
    /** Null si el consentimiento se persistió pero la operación falló después. */
    private final UUID movimientoId;
    private final TipoOperacion tipoOperacion;
    private final BigDecimal monto;
    private final String moneda;
    /** Snapshot del umbral vigente al momento — no se recalcula a posteriori. */
    private final BigDecimal umbralAplicado;
    private final boolean aceptaOrigenLicito;
    /** Texto libre del socio. Útil para PEPs o casos con justificación específica. */
    private final String origenFondos;
    private final String ipOrigen;
    private final String userAgent;
    private final String sessionId;
    private final String requestId;
    private final Instant createdAt;

    public enum TipoOperacion {
        DEPOSITO, RETIRO
    }
}
