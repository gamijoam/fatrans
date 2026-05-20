package com.tufondo.contabilidad.infrastructure.persistence.entity;

import com.tufondo.contabilidad.domain.model.AsientoContable;
import com.tufondo.contabilidad.domain.model.PartidaAsiento;
import com.tufondo.contabilidad.domain.model.enums.EstadoAsiento;
import com.tufondo.contabilidad.domain.model.enums.OrigenAsiento;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity para la cabecera del asiento contable.
 *
 * <p>Mapeado a {@code asientos_contables}. Las partidas viven en tabla
 * separada y se mapean explícitamente en el adapter (no usamos
 * {@code @OneToMany} para tener control fino sobre el orden de inserts
 * y evitar el N+1 típico).</p>
 *
 * <p>El campo {@code numero} se asigna en BD vía secuencia
 * {@code seq_asiento_numero} desde el adapter. El entity solo lo refleja.</p>
 */
@Entity
@Table(
        name = "asientos_contables",
        indexes = {
                @Index(name = "idx_asientos_fecha_jpa", columnList = "fecha_contable"),
                @Index(name = "idx_asientos_origen_jpa", columnList = "origen"),
                @Index(name = "idx_asientos_estado_jpa", columnList = "estado"),
                @Index(name = "idx_asientos_referencia_jpa", columnList = "referencia_externa"),
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AsientoContableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "numero", nullable = false, unique = true, updatable = false)
    private Long numero;

    @Column(name = "fecha_contable", nullable = false)
    private LocalDate fechaContable;

    @Column(name = "glosa", nullable = false, length = 500)
    private String glosa;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen", nullable = false, length = 30)
    private OrigenAsiento origen;

    @Column(name = "referencia_externa", length = 100)
    private String referenciaExterna;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoAsiento estado;

    @Column(name = "creado_por_usuario_id")
    private UUID creadoPorUsuarioId;

    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    @Column(name = "asiento_reversa_id")
    private UUID asientoReversaId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Convierte a domain. Las partidas se pasan por parámetro porque viven
     * en tabla separada y el adapter es quien las junta para construir el
     * agregado completo.
     */
    public AsientoContable toDomain(List<PartidaAsientoEntity> partidasEntities) {
        List<PartidaAsiento> partidas = partidasEntities.stream()
                .sorted(Comparator.comparingInt(PartidaAsientoEntity::getOrden))
                .map(PartidaAsientoEntity::toDomain)
                .toList();
        return AsientoContable.reconstruir(
                id, numero, fechaContable, glosa, origen, referenciaExterna,
                estado, creadoPorUsuarioId, motivoAnulacion, asientoReversaId,
                partidas, createdAt, updatedAt, version);
    }

    /**
     * Construye el entity desde domain. NO incluye las partidas — esas se
     * persisten por separado para preservar el orden de inserts y el control
     * transaccional explícito en el adapter.
     */
    public static AsientoContableEntity fromDomain(AsientoContable a) {
        return AsientoContableEntity.builder()
                .id(a.getId())
                .numero(a.getNumero())  // puede ser null si no se asignó
                .fechaContable(a.getFechaContable())
                .glosa(a.getGlosa())
                .origen(a.getOrigen())
                .referenciaExterna(a.getReferenciaExterna())
                .estado(a.getEstado())
                .creadoPorUsuarioId(a.getCreadoPorUsuarioId())
                .motivoAnulacion(a.getMotivoAnulacion())
                .asientoReversaId(a.getAsientoReversaId())
                .createdAt(a.getCreatedAt() != null ? a.getCreatedAt() : Instant.now())
                .updatedAt(a.getUpdatedAt() != null ? a.getUpdatedAt() : Instant.now())
                .version(a.getVersion())
                .build();
    }
}
