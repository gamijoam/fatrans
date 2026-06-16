package com.tufondo.contabilidad.domain.model.enums;

/**
 * Estado de un asiento contable.
 *
 * <p>Los asientos NUNCA se DELETEan (auditoría requerida por SUDECA). En
 * cambio, se ANULAN con motivo y se genera un asiento de REVERSIÓN que
 * cierra el balance.</p>
 */
public enum EstadoAsiento {
    /**
     * Asiento activo. Afecta los saldos del libro mayor y aparece en los
     * reportes de movimientos.
     */
    REGISTRADO,
    /**
     * Asiento anulado. No afecta saldos pero permanece en BD para
     * auditoría. Debe haber un asiento de reversión asociado.
     */
    ANULADO,
}
