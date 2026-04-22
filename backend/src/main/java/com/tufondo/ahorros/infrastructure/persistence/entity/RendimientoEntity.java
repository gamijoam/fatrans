// com/tufondo/ahorros/infrastructure/persistence/entity/RendimientoEntity.java
package com.tufondo.ahorros.infrastructure.persistence.entity;

import com.tufondo.ahorros.domain.model.enums.EstadoAplicacion;
import com.tufondo.ahorros.domain.model.enums.TipoRendimiento;
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
 * JPA Entity para Rendimiento.
 * RN-010: tasaAplicada debe estar en rango 0.0001 - 1.0 (CRÍTICO overflow).
 */
@Entity
@Table(name = "rendimientos",
    indexes = {
        @Index(name = "idx_rendimientos_cuenta_id", columnList = "cuenta_ahorro_id"),
        @Index(name = "idx_rendimientos_periodo", columnList = "periodo_inicio, periodo_fin"),
        @Index(name = "idx_rendimientos_estado", columnList = "estado_aplicacion")
    })
public class RendimientoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cuenta_ahorro_id", nullable = false)
    private UUID cuentaAhorroId;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @Column(name = "saldo_promedio_periodo", precision = 19, scale = 4)
    private BigDecimal saldoPromedioPeriodo;

    @Column(name = "tasa_aplicada", precision = 8, scale = 6, nullable = false)
    private BigDecimal tasaAplicada;

    @Column(name = "monto_rendimiento", precision = 19, scale = 4)
    private BigDecimal montoRendimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRendimiento tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_aplicacion", nullable = false, length = 20)
    private EstadoAplicacion estadoAplicacion;

    @Column(name = "fecha_calculo", nullable = false)
    private LocalDateTime fechaCalculo;

    public RendimientoEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public UUID getCuentaAhorroId() { return cuentaAhorroId; }
    public void setCuentaAhorroId(UUID v) { this.cuentaAhorroId = v; }
    public LocalDate getPeriodoInicio() { return periodoInicio; }
    public void setPeriodoInicio(LocalDate v) { this.periodoInicio = v; }
    public LocalDate getPeriodoFin() { return periodoFin; }
    public void setPeriodoFin(LocalDate v) { this.periodoFin = v; }
    public BigDecimal getSaldoPromedioPeriodo() { return saldoPromedioPeriodo; }
    public void setSaldoPromedioPeriodo(BigDecimal v) { this.saldoPromedioPeriodo = v; }
    public BigDecimal getTasaAplicada() { return tasaAplicada; }
    public void setTasaAplicada(BigDecimal v) { this.tasaAplicada = v; }
    public BigDecimal getMontoRendimiento() { return montoRendimiento; }
    public void setMontoRendimiento(BigDecimal v) { this.montoRendimiento = v; }
    public TipoRendimiento getTipo() { return tipo; }
    public void setTipo(TipoRendimiento v) { this.tipo = v; }
    public EstadoAplicacion getEstadoAplicacion() { return estadoAplicacion; }
    public void setEstadoAplicacion(EstadoAplicacion v) { this.estadoAplicacion = v; }
    public LocalDateTime getFechaCalculo() { return fechaCalculo; }
    public void setFechaCalculo(LocalDateTime v) { this.fechaCalculo = v; }

    public static RendimientoEntityBuilder builder() { return new RendimientoEntityBuilder(); }

    public static class RendimientoEntityBuilder {
        private RendimientoEntity e = new RendimientoEntity();
        public RendimientoEntityBuilder id(UUID v) { e.id = v; return this; }
        public RendimientoEntityBuilder cuentaAhorroId(UUID v) { e.cuentaAhorroId = v; return this; }
        public RendimientoEntityBuilder periodoInicio(LocalDate v) { e.periodoInicio = v; return this; }
        public RendimientoEntityBuilder periodoFin(LocalDate v) { e.periodoFin = v; return this; }
        public RendimientoEntityBuilder saldoPromedioPeriodo(BigDecimal v) { e.saldoPromedioPeriodo = v; return this; }
        public RendimientoEntityBuilder tasaAplicada(BigDecimal v) { e.tasaAplicada = v; return this; }
        public RendimientoEntityBuilder montoRendimiento(BigDecimal v) { e.montoRendimiento = v; return this; }
        public RendimientoEntityBuilder tipo(TipoRendimiento v) { e.tipo = v; return this; }
        public RendimientoEntityBuilder estadoAplicacion(EstadoAplicacion v) { e.estadoAplicacion = v; return this; }
        public RendimientoEntityBuilder fechaCalculo(LocalDateTime v) { e.fechaCalculo = v; return this; }
        public RendimientoEntity build() { return e; }
    }
}