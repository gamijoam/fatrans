package com.tufondo.notificaciones.domain.model.enums;

/**
 * Prioridad visual de una notificación (issue #214).
 *
 * <p>Determina el ordenamiento y el estilo (color) en la UI:
 * <ul>
 *   <li>{@code URGENTE} — rojo, aparece arriba aunque sea más viejo.
 *       Ej: fraude detectado, cuota vencida.</li>
 *   <li>{@code NORMAL} — azul, default. Ej: KYC aprobado, depósito recibido.</li>
 *   <li>{@code BAJA} — gris, informativa. Ej: nueva versión disponible.</li>
 * </ul>
 */
public enum PrioridadNotificacion {
    URGENTE,
    NORMAL,
    BAJA
}
