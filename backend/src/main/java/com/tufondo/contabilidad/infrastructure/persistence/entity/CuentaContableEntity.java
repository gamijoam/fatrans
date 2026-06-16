package com.tufondo.contabilidad.infrastructure.persistence.entity;

import com.tufondo.contabilidad.domain.model.CuentaContable;
import com.tufondo.contabilidad.domain.model.enums.NaturalezaSaldo;
import com.tufondo.contabilidad.domain.model.enums.TipoCuentaContable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity para el plan de cuentas contable.
 *
 * <p>Mapeada a {@code plan_cuentas}. Los enums se persisten como STRING para
 * que las migraciones legibles desde psql (no enteros). El campo {@code version}
 * habilita optimistic locking — relevante porque ediciones simultáneas desde
 * dos sesiones admin tienen que detectarse.</p>
 *
 * <p>El {@code cuenta_padre_id} es FK auto-referencial; en BD se valida con
 * trigger/check (ver migration V21) que el código de la hija respete la
 * jerarquía. Acá solo declaramos el lado Java.</p>
 */
@Entity
@Table(
        name = "plan_cuentas",
        indexes = {
                @Index(name = "idx_plan_cuentas_codigo", columnList = "codigo", unique = true),
                @Index(name = "idx_plan_cuentas_tipo", columnList = "tipo"),
                @Index(name = "idx_plan_cuentas_padre", columnList = "cuenta_padre_id"),
                @Index(name = "idx_plan_cuentas_activa_acepta", columnList = "activa,acepta_movimientos"),
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CuentaContableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoCuentaContable tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "naturaleza", nullable = false, length = 15)
    private NaturalezaSaldo naturaleza;

    @Column(name = "nivel", nullable = false)
    private Integer nivel;

    @Column(name = "cuenta_padre_id")
    private UUID cuentaPadreId;

    @Column(name = "acepta_movimientos", nullable = false)
    private Boolean aceptaMovimientos;

    @Column(name = "activa", nullable = false)
    private Boolean activa;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    /**
     * Mapeo entity → domain. Llama a {@link CuentaContable#reconstruir} que
     * valida invariantes — si la BD tiene datos inconsistentes (ej. nivel
     * que no coincide con el código), falla acá temprano.
     */
    public CuentaContable toDomain() {
        return CuentaContable.reconstruir(
                id, codigo, nombre, tipo, naturaleza, nivel,
                cuentaPadreId, aceptaMovimientos, activa,
                descripcion, createdAt, updatedAt, version);
    }

    /**
     * Mapeo domain → entity. Si el domain trae {@code createdAt=null} (el caso
     * típico cuando recién se creó), seteamos {@link Instant#now()} para que
     * la columna {@code created_at NOT NULL} no se rompa en el insert.
     */
    public static CuentaContableEntity fromDomain(CuentaContable cuenta) {
        return CuentaContableEntity.builder()
                .id(cuenta.getId())
                .codigo(cuenta.getCodigo())
                .nombre(cuenta.getNombre())
                .tipo(cuenta.getTipo())
                .naturaleza(cuenta.getNaturaleza())
                .nivel(cuenta.getNivel())
                .cuentaPadreId(cuenta.getCuentaPadreId())
                .aceptaMovimientos(cuenta.isAceptaMovimientos())
                .activa(cuenta.isActiva())
                .descripcion(cuenta.getDescripcion())
                .createdAt(cuenta.getCreatedAt() != null ? cuenta.getCreatedAt() : Instant.now())
                .updatedAt(cuenta.getUpdatedAt() != null ? cuenta.getUpdatedAt() : Instant.now())
                .version(cuenta.getVersion())
                .build();
    }
}
