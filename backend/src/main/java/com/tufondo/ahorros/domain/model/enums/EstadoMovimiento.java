// com/tufondo/ahorros/domain/model/enums/EstadoMovimiento.java
package com.tufondo.ahorros.domain.model.enums;

/**
 * Estados de un movimiento financiero.
 */
public enum EstadoMovimiento {
    PROCESADO,  // Completado exitosamente
    RECHAZADO,   // Rechazado por validación
    PENDIENTE,   // En proceso de confirmación
    CANCELADO,   // Cancelado por usuario
    REVERTIDO    // Compensación realizada
}