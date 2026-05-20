package com.tufondo.ahorros.application.port.output;

import com.tufondo.ahorros.domain.model.CuentaAhorro;
import com.tufondo.ahorros.domain.model.Movimiento;

/**
 * Puerto de salida que el módulo de Ahorros usa para notificar
 * cada operación de saldo al módulo de Contabilidad.
 *
 * <p>Sub-issue #267 del EPIC Contabilidad #263.</p>
 *
 * <h2>Contrato</h2>
 * <ul>
 *   <li>El hook se invoca DENTRO del mismo {@code @Transactional} del use case
 *       que mueve el saldo. Si la generación del asiento falla, la transacción
 *       hace rollback completo (saldo no cambia, movimiento no persiste).</li>
 *   <li>La contabilidad NO es best-effort. A diferencia de notificaciones
 *       (email/sms), un asiento no generado significa que la operación queda
 *       inconsistente con la BD contable — eso viola partida doble y nos
 *       deja en riesgo regulatorio con SUDECA.</li>
 *   <li>Las excepciones de contabilidad se propagan al caller sin envoltorio
 *       adicional — el global exception handler las traduce a HTTP 422.</li>
 * </ul>
 *
 * <h2>Mapeo conceptual</h2>
 * <p>El adapter es responsable de mapear el evento de negocio a los códigos
 * VEN-NIF correctos según la moneda de la cuenta. Por ejemplo:</p>
 * <ul>
 *   <li>Depósito Bs:  DEBE 1.1.01 (Caja Principal)        / HABER 2.1.01 (Cuentas de Ahorro Bs)</li>
 *   <li>Depósito USD: DEBE 1.1.05 (Bancos Cuentas USD)    / HABER 2.1.02 (Cuentas de Ahorro USD)</li>
 *   <li>Retiro Bs:    DEBE 2.1.01                          / HABER 1.1.01</li>
 *   <li>Retiro USD:   DEBE 2.1.02                          / HABER 1.1.05</li>
 * </ul>
 */
public interface AhorrosContabilidadPort {

    /**
     * Genera el asiento contable correspondiente a un depósito ya registrado.
     *
     * @param cuenta     cuenta destino del depósito (provee moneda y socioId)
     * @param movimiento movimiento ya persistido (provee numeroOperacion y monto)
     * @throws com.tufondo.contabilidad.application.exception.AsientoContableException
     *         si alguna validación contable falla (cuenta inexistente, monto
     *         excesivo, etc). La excepción rollback el {@code @Transactional}
     *         del caller — la operación se aborta.
     */
    void registrarDeposito(CuentaAhorro cuenta, Movimiento movimiento);

    /**
     * Genera el asiento contable correspondiente a un retiro ya registrado.
     *
     * @param cuenta     cuenta origen del retiro
     * @param movimiento movimiento ya persistido
     * @throws com.tufondo.contabilidad.application.exception.AsientoContableException
     *         si alguna validación contable falla
     */
    void registrarRetiro(CuentaAhorro cuenta, Movimiento movimiento);
}
