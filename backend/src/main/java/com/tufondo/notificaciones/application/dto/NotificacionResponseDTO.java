package com.tufondo.notificaciones.application.dto;

import com.tufondo.notificaciones.domain.model.Notificacion;
import com.tufondo.notificaciones.domain.model.enums.PrioridadNotificacion;
import com.tufondo.notificaciones.domain.model.enums.TipoNotificacion;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta para una notificación (issue #214).
 * Record por simplicidad — no muta tras crearse.
 */
public record NotificacionResponseDTO(
        UUID id,
        TipoNotificacion tipo,
        String titulo,
        String mensaje,
        String linkAccion,
        boolean leida,
        Instant fechaLectura,
        PrioridadNotificacion prioridad,
        Instant createdAt
) {
    public static NotificacionResponseDTO desde(Notificacion n) {
        return new NotificacionResponseDTO(
                n.getId(),
                n.getTipo(),
                n.getTitulo(),
                n.getMensaje(),
                n.getLinkAccion(),
                n.isLeida(),
                n.getFechaLectura(),
                n.getPrioridad(),
                n.getCreatedAt()
        );
    }
}
