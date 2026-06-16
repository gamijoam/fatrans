package com.tufondo.creditos.application.port.output;

import com.tufondo.creditos.domain.model.Amortizacion;
import com.tufondo.creditos.domain.model.SolicitudCredito;

import java.math.BigDecimal;

/**
 * Puerto de salida que el módulo de Créditos usa para notificar
 * cada operación crediticia al módulo de Contabilidad.
 *
 * <p>Sub-issue #268 del EPIC Contabilidad #263.</p>
 *
 * <h2>Contrato</h2>
 * <ul>
 *   <li>El hook se invoca DENTRO del mismo {@code @Transactional} del use case
 *       que ejecuta la operación. Si la generación del asiento falla, la
 *       transacción hace rollback completo.</li>
 *   <li>La contabilidad NO es best-effort: un asiento no generado significa
 *       BD contable desincronizada con la cartera, violación VEN-NIF y
 *       exposición regulatoria SUDECA.</li>
 *   <li>Las excepciones se propagan al caller sin envoltorio adicional.</li>
 * </ul>
 *
 * <h2>Mapping contable (post decisiones D-003 + D-004)</h2>
 *
 * <h3>Desembolso (origen {@code CREDITO_DESEMBOLSO})</h3>
 * <pre>
 * DEBE  1.3.01 Créditos Personales por Cobrar    [monto bruto = neto + comisión]
 * HABER 1.1.03 Bancos Cta Corriente Bs           [monto neto al socio]
 * HABER 4.1.02 Comisiones por Otorgamiento       [comisión, si &gt; 0]
 * </pre>
 *
 * <h3>Pago de cuota (origen {@code CREDITO_COBRO})</h3>
 * <pre>
 * DEBE  1.1.03 Bancos Cta Corriente Bs           [monto total cobrado]
 * HABER 1.3.01 Créditos por Cobrar               [capital amortizado]
 * HABER 4.1.01 Intereses sobre Créditos          [intereses normales]
 * HABER 4.1.03 Intereses Moratorios              [SI mora &gt; 0]
 * </pre>
 *
 * <p>Razón completa de los códigos en
 * {@code docs/modulos/contabilidad/_decisiones-contables.md}
 * (D-003 desembolso, D-004 pago cuota).</p>
 *
 * <p><strong>Asunción de moneda</strong>: todos los créditos actualmente son
 * en bolívares. Si Fatrans abre créditos USD en el futuro, requiere agregar
 * campo {@code moneda} a {@link SolicitudCredito} y variante del mapping
 * (cuentas {@code 1.3.04} / {@code 1.1.05}). Ver pendiente P2 en
 * {@code _pendientes-criticos.md}.</p>
 */
public interface CreditosContabilidadPort {

    /**
     * Genera el asiento contable del desembolso de un crédito.
     *
     * @param solicitud         la solicitud aprobada (provee numeroSolicitud, montoSolicitado=bruto, etc)
     * @param montoNeto         monto efectivamente transferido al socio (bruto - comisión)
     * @param comisionApertura  comisión cobrada al desembolsar (puede ser {@link BigDecimal#ZERO})
     * @throws com.tufondo.contabilidad.application.exception.AsientoContableException
     *         si alguna validación contable falla — rollback de la operación
     */
    void registrarDesembolso(SolicitudCredito solicitud,
                              BigDecimal montoNeto,
                              BigDecimal comisionApertura);

    /**
     * Genera el asiento contable del cobro de una cuota.
     *
     * @param solicitud       el crédito al que pertenece la cuota (provee socioId y numeroSolicitud)
     * @param cuota           la cuota cobrada (provee desglose capital + interés + mora)
     * @param montoCobrado    monto total efectivamente cobrado del socio
     * @param referenciaPago  referencia bancaria/operativa para auditoría cruzada
     * @throws com.tufondo.contabilidad.application.exception.AsientoContableException
     *         si alguna validación contable falla — rollback de la operación
     */
    void registrarPagoCuota(SolicitudCredito solicitud,
                            Amortizacion cuota,
                            BigDecimal montoCobrado,
                            String referenciaPago);
}
