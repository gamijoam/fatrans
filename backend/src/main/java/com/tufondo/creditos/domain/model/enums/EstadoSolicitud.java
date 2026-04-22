// com/tufondo/creditos/domain/model/enums/EstadoSolicitud.java
package com.tufondo.creditos.domain.model.enums;

/**
 * Estados de una solicitud de crédito.
 * Implementa transiciones validadas para prevenir estados inválidos.
 */
public enum EstadoSolicitud {
    PENDIENTE,
    EN_EVALUACION,
    APROBADA,
    RECHAZADA,
    CANCELADA,
    DESEMBOLSADO,
    COLATERAL_EJECUTADO;  // Estado final cuando se ejecuta el colateral

    /**
     * Valida si la transición al nuevo estado es válida.
     * Transiciones válidas:
     * - PENDIENTE → EN_EVALUACION
     * - EN_EVALUACION → APROBADA o RECHAZADA
     * - APROBADA → DESEMBOLSADO
     * - DESEMBOLSADO → COLATERAL_EJECUTADO (si se ejecuta colateral)
     * - RECHAZADA, CANCELADA, COLATERAL_EJECUTADO → (ninguna, son estados finales)
     */
    public boolean puedeTransicionarA(EstadoSolicitud nuevoEstado) {
        return switch (this) {
            case PENDIENTE -> nuevoEstado == EN_EVALUACION;
            case EN_EVALUACION -> nuevoEstado == APROBADA || nuevoEstado == RECHAZADA;
            case APROBADA -> nuevoEstado == DESEMBOLSADO;
            case DESEMBOLSADO -> nuevoEstado == COLATERAL_EJECUTADO;
            case RECHAZADA, CANCELADA, COLATERAL_EJECUTADO -> false;
        };
    }
}