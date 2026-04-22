// com.tufondo.kyc.infrastructure.persistence.entity.VerificacionKYCEntity
package com.tufondo.kyc.infrastructure.persistence.entity;

import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.model.enums.NivelVerificacion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity para VerificacionKYC.
 */
@Entity
@Table(name = "verificacion_kyc",
    indexes = {
        @Index(name = "idx_verificacion_socio_id", columnList = "socio_id"),
        @Index(name = "idx_verificacion_estado", columnList = "estado"),
        @Index(name = "idx_verificacion_fecha_inicio", columnList = "fecha_inicio")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificacionKYCEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false, length = 20)
    private NivelVerificacion nivel;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoVerificacion estado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_completado")
    private LocalDateTime fechaCompletado;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "datos_verificacion_automatica", columnDefinition = "TEXT")
    private String datosVerificacionAutomatica;

    @Column(name = "revisado_por", length = 100)
    private String revisadoPor;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    @Column(name = "comentarios_revision", columnDefinition = "TEXT")
    private String comentariosRevision;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}