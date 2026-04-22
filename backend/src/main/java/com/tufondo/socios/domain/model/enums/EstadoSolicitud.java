// 📁 com/tufondo/socios/domain/model/enums/EstadoSolicitud.java
package com.tufondo.socios.domain.model.enums;

/**
 * Estados posibles de una Solicitud de Registro de Socio.
 * 
 * <h3>Transiciones válidas:</h3>
 * <pre>
 * ┌─────────────┬──────────────────┐
 * │ Estado      │ Siguiente        │
 * ├─────────────┼──────────────────┤
 * │ PENDIENTE   │ APROBADA, RECHAZADA │
 * │ APROBADA    │ (terminal)       │
 * │ RECHAZADA   │ (terminal)       │
 * └─────────────┴──────────────────┘
 * </pre>
 */
public enum EstadoSolicitud {
    
    /** Solicitud nueva, pendiente de revisión por administrador. */
    PENDIENTE,
    
    /** Solicitud aprobada, socio y usuario creados. */
    APROBADA,
    
    /** Solicitud rechazada por administrador. */
    RECHAZADA
}