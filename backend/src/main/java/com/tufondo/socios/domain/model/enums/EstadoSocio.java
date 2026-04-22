// 📁 com/tufondo/socios/domain/model/enums/EstadoSocio.java
// 🔧 AUDITORÍA MEJORA: Documentar transiciones válidas
package com.tufondo.socios.domain.model.enums;

/**
 * Estados posibles de un Socio en el sistema de Fondo de Ahorro.
 * 
 * <h3>Transiciones válidas de estado:</h3>
 * <pre>
 * ┌─────────────────────┬──────────────────┐
 * │ Estado Actual       │ Estados Sig.     │
 * ├─────────────────────┼──────────────────┤
 * │ PENDIENTE_APROBACION│ ACTIVO, INACTIVO │
 * │ ACTIVO              │ INACTIVO         │
 * │ INACTIVO            │ ACTIVO           │
 * └─────────────────────┴──────────────────┘
 * </pre>
 * 
 * <h3>Ciclos de vida:</h3>
 * <ul>
 *   <li><b>Alta Normal:</b> PENDIENTE → ACTIVO (aprobar)</li>
 *   <li><b>Desactivación:</b> ACTIVO → INACTIVO (desactivar)</li>
 *   <li><b>Reactivación:</b> INACTIVO → ACTIVO (activar)</li>
 *   <li><b>Rechazo:</b> PENDIENTE → INACTIVO (rechazar)</li>
 * </ul>
 */
public enum EstadoSocio {
    
    /** Socio nuevo, pendiente de aprobación por administrador. */
    PENDIENTE_APROBACION,
    
    /** Socio activo, puede realizar operaciones financieras. */
    ACTIVO,
    
    /** Socio inactivo, no puede realizar operaciones. */
    INACTIVO,

    /** Socio eliminado logicalmente del sistema. */
    ELIMINADO
}
