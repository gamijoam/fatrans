// com/tufondo/ahorros/infrastructure/persistence/entity/MovimientoEntity.java
package com.tufondo.ahorros.infrastructure.persistence.entity;

import com.tufondo.ahorros.domain.model.enums.CanalOrigen;
import com.tufondo.ahorros.domain.model.enums.EstadoMovimiento;
import com.tufondo.ahorros.domain.model.enums.TipoMovimiento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity para Movimiento.
 * RN-006: Movimientos son INMUTABLES una vez creados.
 */
@Entity
@Table(name = "movimientos",
    indexes = {
        @Index(name = "idx_movimientos_cuenta_id", columnList = "cuenta_ahorro_id"),
        @Index(name = "idx_movimientos_socio_id", columnList = "socio_id"),
        @Index(name = "idx_movimientos_fecha", columnList = "fecha_movimiento"),
        @Index(name = "idx_movimientos_numero_operacion", columnList = "numero_operacion", unique = true),
        @Index(name = "idx_movimientos_estado", columnList = "estado")
    })
public class MovimientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_operacion", unique = true, nullable = false, length = 20)
    private String numeroOperacion;

    @Column(name = "cuenta_ahorro_id", nullable = false)
    private UUID cuentaAhorroId;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimiento tipo;

    @Column(name = "monto", precision = 19, scale = 4, nullable = false)
    private BigDecimal monto;

    @Column(name = "saldo_anterior", precision = 19, scale = 4)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", precision = 19, scale = 4)
    private BigDecimal saldoPosterior;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 100)
    private String referencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_origen", nullable = false, length = 20)
    private CanalOrigen canalOrigen;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(name = "request_id", length = 255)
    private String requestId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMovimiento estado;

    @Column(name = "fecha_movimiento", nullable = false, updatable = false)
    private LocalDateTime fechaMovimiento;

    @Column(name = "fecha_valor", nullable = false)
    private LocalDate fechaValor;

    public MovimientoEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public String getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(String v) { this.numeroOperacion = v; }
    public UUID getCuentaAhorroId() { return cuentaAhorroId; }
    public void setCuentaAhorroId(UUID v) { this.cuentaAhorroId = v; }
    public UUID getSocioId() { return socioId; }
    public void setSocioId(UUID v) { this.socioId = v; }
    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento v) { this.tipo = v; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal v) { this.monto = v; }
    public BigDecimal getSaldoAnterior() { return saldoAnterior; }
    public void setSaldoAnterior(BigDecimal v) { this.saldoAnterior = v; }
    public BigDecimal getSaldoPosterior() { return saldoPosterior; }
    public void setSaldoPosterior(BigDecimal v) { this.saldoPosterior = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String v) { this.referencia = v; }
    public CanalOrigen getCanalOrigen() { return canalOrigen; }
    public void setCanalOrigen(CanalOrigen v) { this.canalOrigen = v; }
    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String v) { this.ipOrigen = v; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String v) { this.sessionId = v; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String v) { this.requestId = v; }
    public EstadoMovimiento getEstado() { return estado; }
    public void setEstado(EstadoMovimiento v) { this.estado = v; }
    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime v) { this.fechaMovimiento = v; }
    public LocalDate getFechaValor() { return fechaValor; }
    public void setFechaValor(LocalDate v) { this.fechaValor = v; }

    public static MovimientoEntityBuilder builder() { return new MovimientoEntityBuilder(); }

    public static class MovimientoEntityBuilder {
        private MovimientoEntity e = new MovimientoEntity();
        public MovimientoEntityBuilder id(UUID v) { e.id = v; return this; }
        public MovimientoEntityBuilder numeroOperacion(String v) { e.numeroOperacion = v; return this; }
        public MovimientoEntityBuilder cuentaAhorroId(UUID v) { e.cuentaAhorroId = v; return this; }
        public MovimientoEntityBuilder socioId(UUID v) { e.socioId = v; return this; }
        public MovimientoEntityBuilder tipo(TipoMovimiento v) { e.tipo = v; return this; }
        public MovimientoEntityBuilder monto(BigDecimal v) { e.monto = v; return this; }
        public MovimientoEntityBuilder saldoAnterior(BigDecimal v) { e.saldoAnterior = v; return this; }
        public MovimientoEntityBuilder saldoPosterior(BigDecimal v) { e.saldoPosterior = v; return this; }
        public MovimientoEntityBuilder descripcion(String v) { e.descripcion = v; return this; }
        public MovimientoEntityBuilder referencia(String v) { e.referencia = v; return this; }
        public MovimientoEntityBuilder canalOrigen(CanalOrigen v) { e.canalOrigen = v; return this; }
        public MovimientoEntityBuilder ipOrigen(String v) { e.ipOrigen = v; return this; }
        public MovimientoEntityBuilder sessionId(String v) { e.sessionId = v; return this; }
        public MovimientoEntityBuilder requestId(String v) { e.requestId = v; return this; }
        public MovimientoEntityBuilder estado(EstadoMovimiento v) { e.estado = v; return this; }
        public MovimientoEntityBuilder fechaMovimiento(LocalDateTime v) { e.fechaMovimiento = v; return this; }
        public MovimientoEntityBuilder fechaValor(LocalDate v) { e.fechaValor = v; return this; }
        public MovimientoEntity build() { return e; }
    }
}