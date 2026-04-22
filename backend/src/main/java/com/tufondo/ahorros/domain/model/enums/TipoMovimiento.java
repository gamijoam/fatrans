// com/tufondo/ahorros/domain/model/enums/TipoMovimiento.java
package com.tufondo.ahorros.domain.model.enums;

/**
 * Tipos de movimiento financiero.
 * RN-006: Movimientos son INMUTABLES una vez creados.
 */
public enum TipoMovimiento {
    DEPOSITO,
    RETIRO,
    TRANSFERENCIA_ENTRADA,
    TRANSFERENCIA_SALIDA,
    COMISION,
    INTERES_CREDITO,
    INTERES_DEBITO,
    AJUSTE
}