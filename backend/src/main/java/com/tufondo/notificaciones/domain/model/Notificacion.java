package com.tufondo.notificaciones.domain.model;

import com.tufondo.notificaciones.domain.model.enums.PrioridadNotificacion;
import com.tufondo.notificaciones.domain.model.enums.TipoNotificacion;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de dominio de una notificación in-app (issue #214).
 *
 * <p>Immutable. Las operaciones de cambio de estado (marcar leída) crean
 * una nueva instancia vía métodos {@code conXxx}.</p>
 */
@Getter
@Builder
public class Notificacion {

    private final UUID id;
    private final UUID destinatarioId;
    private final TipoNotificacion tipo;
    private final String titulo;
    private final String mensaje;
    /** Link relativo donde llevar al usuario si hace click. Puede ser null. */
    private final String linkAccion;
    private final boolean leida;
    /** Solo presente si {@code leida == true}. Invariante validada en DB. */
    private final Instant fechaLectura;
    private final PrioridadNotificacion prioridad;
    /** Metadata libre JSON (BLOB serializado). Puede ser null. */
    private final String metadata;
    private final Instant createdAt;

    /**
     * Retorna una nueva instancia marcada como leída, con la fecha de lectura
     * registrada en este momento. Si ya estaba leída, retorna {@code this}
     * sin cambios (idempotencia — evita actualizar fechaLectura por error
     * cuando el cliente marca dos veces).
     */
    public Notificacion marcarLeida() {
        if (this.leida) return this;
        return Notificacion.builder()
                .id(this.id)
                .destinatarioId(this.destinatarioId)
                .tipo(this.tipo)
                .titulo(this.titulo)
                .mensaje(this.mensaje)
                .linkAccion(this.linkAccion)
                .leida(true)
                .fechaLectura(Instant.now())
                .prioridad(this.prioridad)
                .metadata(this.metadata)
                .createdAt(this.createdAt)
                .build();
    }

    /**
     * Verifica que esta notificación pertenezca al destinatario indicado.
     * Usado en use cases para protección anti-IDOR antes de mostrar/modificar.
     */
    public boolean perteneceA(UUID usuarioId) {
        return this.destinatarioId != null && this.destinatarioId.equals(usuarioId);
    }
}
