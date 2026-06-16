package com.tufondo.contabilidad.infrastructure.persistence.entity;

import com.tufondo.contabilidad.domain.model.PartidaAsiento;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity para una partida de asiento contable.
 *
 * <p>Mapeada a {@code partidas_asientos}. La FK al asiento se mapea con
 * {@code @ManyToOne} (lazy) pero usamos solo el ID para evitar cargar la
 * cabecera cuando no se necesita.</p>
 *
 * <p>El check constraint {@code chk_partida_debe_xor_haber} vive en BD —
 * acá no se duplica con @Check porque Hibernate puede o no respetar esos
 * hints según el dialecto. Confiamos en el constraint de la migration V22.</p>
 */
@Entity
@Table(
        name = "partidas_asientos",
        indexes = {
                @Index(name = "idx_partidas_asiento_jpa", columnList = "asiento_id"),
                @Index(name = "idx_partidas_cuenta_jpa", columnList = "cuenta_id"),
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PartidaAsientoEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "asiento_id", nullable = false)
    private UUID asientoId;

    @Column(name = "cuenta_id", nullable = false)
    private UUID cuentaId;

    @Column(name = "debe", nullable = false, precision = 18, scale = 4)
    private BigDecimal debe;

    @Column(name = "haber", nullable = false, precision = 18, scale = 4)
    private BigDecimal haber;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "glosa", length = 300)
    private String glosa;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public PartidaAsiento toDomain() {
        return PartidaAsiento.reconstruir(
                id, cuentaId, debe, haber, orden, glosa, createdAt);
    }

    public static PartidaAsientoEntity fromDomain(PartidaAsiento p, UUID asientoId) {
        return PartidaAsientoEntity.builder()
                .id(p.getId())
                .asientoId(asientoId)
                .cuentaId(p.getCuentaId())
                .debe(p.getDebe())
                .haber(p.getHaber())
                .orden(p.getOrden())
                .glosa(p.getGlosa())
                .createdAt(p.getCreatedAt() != null ? p.getCreatedAt() : Instant.now())
                .build();
    }
}
