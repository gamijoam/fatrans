// com/tufondo/ahorros/infrastructure/persistence/entity/CuentaAhorroEntity.java
package com.tufondo.ahorros.infrastructure.persistence.entity;

import com.tufondo.ahorros.domain.model.enums.EstadoCuenta;
import com.tufondo.ahorros.domain.model.enums.Moneda;
import com.tufondo.ahorros.domain.model.enums.TipoCuenta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity para CuentaAhorro.
 * Incluye @Version para optimistic locking.
 */
@Entity
@Table(name = "cuentas_ahorro", 
    indexes = {
        @Index(name = "idx_cuentas_ahorro_socio_id", columnList = "socio_id"),
        @Index(name = "idx_cuentas_ahorro_estado", columnList = "estado"),
        @Index(name = "idx_cuentas_ahorro_numero_cuenta", columnList = "numero_cuenta", unique = true)
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_socio_tipo_cuenta", columnNames = {"socio_id", "tipo_cuenta"})
    })
public class CuentaAhorroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_cuenta", unique = true, nullable = false, length = 20)
    private String numeroCuenta;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Column(name = "saldo_actual", precision = 19, scale = 4, nullable = false)
    private BigDecimal saldoActual = BigDecimal.ZERO;

    @Column(name = "saldo_retenido", precision = 19, scale = 4, nullable = false)
    private BigDecimal saldoRetenido = BigDecimal.ZERO;

    @Column(name = "tasa_interes", precision = 8, scale = 6)
    private BigDecimal tasaInteres;

    @Column(name = "monto_minimo_requerido", precision = 19, scale = 4)
    private BigDecimal montoMinimoRequerido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCuenta estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    private TipoCuenta tipoCuenta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Moneda moneda;

    @Column(name = "fecha_apertura", nullable = false, updatable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_ultima_operacion")
    private LocalDateTime fechaUltimaOperacion;

    @Version
    private Long version;

    public CuentaAhorroEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String v) { this.numeroCuenta = v; }
    public UUID getSocioId() { return socioId; }
    public void setSocioId(UUID v) { this.socioId = v; }
    public BigDecimal getSaldoActual() { return saldoActual; }
    public void setSaldoActual(BigDecimal v) { this.saldoActual = v; }
    public BigDecimal getSaldoRetenido() { return saldoRetenido; }
    public void setSaldoRetenido(BigDecimal v) { this.saldoRetenido = v; }
    public BigDecimal getTasaInteres() { return tasaInteres; }
    public void setTasaInteres(BigDecimal v) { this.tasaInteres = v; }
    public BigDecimal getMontoMinimoRequerido() { return montoMinimoRequerido; }
    public void setMontoMinimoRequerido(BigDecimal v) { this.montoMinimoRequerido = v; }
    public EstadoCuenta getEstado() { return estado; }
    public void setEstado(EstadoCuenta v) { this.estado = v; }
    public TipoCuenta getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(TipoCuenta v) { this.tipoCuenta = v; }
    public Moneda getMoneda() { return moneda; }
    public void setMoneda(Moneda v) { this.moneda = v; }
    public LocalDateTime getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(LocalDateTime v) { this.fechaApertura = v; }
    public LocalDateTime getFechaUltimaOperacion() { return fechaUltimaOperacion; }
    public void setFechaUltimaOperacion(LocalDateTime v) { this.fechaUltimaOperacion = v; }
    public Long getVersion() { return version; }
    public void setVersion(Long v) { this.version = v; }

    /**
     * Calcula saldo disponible.
     */
    public BigDecimal getSaldoDisponible() {
        if (saldoActual == null) return BigDecimal.ZERO;
        if (saldoRetenido == null) return saldoActual;
        return saldoActual.subtract(saldoRetenido);
    }

    public static CuentaAhorroEntityBuilder builder() { return new CuentaAhorroEntityBuilder(); }

    public static class CuentaAhorroEntityBuilder {
        private CuentaAhorroEntity e = new CuentaAhorroEntity();
        public CuentaAhorroEntityBuilder id(UUID v) { e.id = v; return this; }
        public CuentaAhorroEntityBuilder numeroCuenta(String v) { e.numeroCuenta = v; return this; }
        public CuentaAhorroEntityBuilder socioId(UUID v) { e.socioId = v; return this; }
        public CuentaAhorroEntityBuilder saldoActual(BigDecimal v) { e.saldoActual = v; return this; }
        public CuentaAhorroEntityBuilder saldoRetenido(BigDecimal v) { e.saldoRetenido = v; return this; }
        public CuentaAhorroEntityBuilder tasaInteres(BigDecimal v) { e.tasaInteres = v; return this; }
        public CuentaAhorroEntityBuilder montoMinimoRequerido(BigDecimal v) { e.montoMinimoRequerido = v; return this; }
        public CuentaAhorroEntityBuilder estado(EstadoCuenta v) { e.estado = v; return this; }
        public CuentaAhorroEntityBuilder tipoCuenta(TipoCuenta v) { e.tipoCuenta = v; return this; }
        public CuentaAhorroEntityBuilder moneda(Moneda v) { e.moneda = v; return this; }
        public CuentaAhorroEntityBuilder fechaApertura(LocalDateTime v) { e.fechaApertura = v; return this; }
        public CuentaAhorroEntityBuilder fechaUltimaOperacion(LocalDateTime v) { e.fechaUltimaOperacion = v; return this; }
        public CuentaAhorroEntityBuilder version(Long v) { e.version = v; return this; }
        public CuentaAhorroEntity build() { return e; }
    }
}