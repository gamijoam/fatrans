// com/tufondo/creditos/domain/model/enums/EstadoPlanAmortizacion.java
package com.tufondo.creditos.domain.model.enums;

/**
 * Estados de un plan de amortización.
 */
public enum EstadoPlanAmortizacion {
    ACTIVO,       // En curso, con cuotas pendientes
    CANCELADO,    // Cancelado anticipadamente por prepago
    FINALIZADO,   // Todas las cuotas pagadas
    VENCIDO       // Con cuotas en mora > 90 días
}
