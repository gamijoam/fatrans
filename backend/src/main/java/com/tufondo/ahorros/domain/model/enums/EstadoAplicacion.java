// com/tufondo/ahorros/domain/model/enums/EstadoAplicacion.java
package com.tufondo.ahorros.domain.model.enums;

/**
 * Estados de aplicación de un rendimiento.
 */
public enum EstadoAplicacion {
    CALCULADO,  // Generado pero no aplicado
    APLICADO,   // Añadido al saldo
    CANCELADO   // Reversado
}