// com/tufondo/ahorros/domain/model/enums/EstadoCuenta.java
package com.tufondo.ahorros.domain.model.enums;

/**
 * Estados posibles de una cuenta de ahorro.
 * RN-005: Cuenta CERRADA no permite operaciones.
 */
public enum EstadoCuenta {
    ACTIVA,    // Operaciones permitidas
    SUSPENDIDA, // Solo consultas, sin operaciones
    CERRADA    // Sin operaciones, historial accesible
}