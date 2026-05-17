package com.tufondo.notificaciones.infrastructure.persistence.entity;

import com.tufondo.notificaciones.domain.model.Notificacion;
import com.tufondo.notificaciones.domain.model.enums.PrioridadNotificacion;
import com.tufondo.notificaciones.domain.model.enums.TipoNotificacion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "destinatario_id", nullable = false)
    private UUID destinatarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 50)
    private TipoNotificacion tipo;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "link_accion", length = 500)
    private String linkAccion;

    @Column(name = "leida", nullable = false)
    private boolean leida;

    @Column(name = "fecha_lectura")
    private Instant fechaLectura;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false, length = 20)
    private PrioridadNotificacion prioridad;

    /**
     * JSONB en Postgres, mapeado como String (el dominio no lo procesa,
     * solo lo almacena/serializa el caller).
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onPrePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (prioridad == null) prioridad = PrioridadNotificacion.NORMAL;
    }

    public Notificacion aDominio() {
        return Notificacion.builder()
                .id(this.id)
                .destinatarioId(this.destinatarioId)
                .tipo(this.tipo)
                .titulo(this.titulo)
                .mensaje(this.mensaje)
                .linkAccion(this.linkAccion)
                .leida(this.leida)
                .fechaLectura(this.fechaLectura)
                .prioridad(this.prioridad)
                .metadata(this.metadata)
                .createdAt(this.createdAt)
                .build();
    }

    public static NotificacionEntity desdeDominio(Notificacion n) {
        return NotificacionEntity.builder()
                .id(n.getId())
                .destinatarioId(n.getDestinatarioId())
                .tipo(n.getTipo())
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .linkAccion(n.getLinkAccion())
                .leida(n.isLeida())
                .fechaLectura(n.getFechaLectura())
                .prioridad(n.getPrioridad())
                .metadata(n.getMetadata())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
