// com/tufondo/creditos/domain/model/enums/EstadoAmortizacion.java
package com.tufondo.creditos.domain.model.enums;

/**
 * Estados de una cuota (amortización).
 * Ciclo de vida: PENDIENTE → PAGADA/VENCIDA → CURSO_MORA/EJECUTADA/CANCELADA
 */
public enum EstadoAmortizacion {
    PENDIENTE,       // Cuota pendiente de pago, vigente
    PAGADA,          // Pagada exitosamente
    VENCIDA,         // Pasó fechaVencimiento sin pago (después de grace period)
    CURSO_MORA,      // Con más de 30 días en mora
    CANCELADA,       // Cancelada por prepago o refinanciamiento
    EJECUTADA        // Colateral ejecutada por incumplimiento
}
