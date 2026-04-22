// com/tufondo/creditos/infrastructure/persistence/entity/PlanAmortizacionEntity.java
package com.tufondo.creditos.infrastructure.persistence.entity;

import com.tufondo.creditos.domain.model.enums.EstadoPlanAmortizacion;
import com.tufondo.creditos.domain.model.enums.FrecuenciaPago;
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
 * JPA Entity para PlanAmortizacion.
 */
@Entity
@Table(name = "planes_amortizacion",
    indexes = {
        @Index(name = "idx_plan_solicitud_id", columnList = "solicitud_id", unique = true),
        @Index(name = "idx_plan_estado", columnList = "estado"),
        @Index(name = "idx_plan_fecha_inicio", columnList = "fecha_inicio")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_plan_solicitud", columnNames = {"solicitud_id"})
    })
public class PlanAmortizacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "solicitud_id", nullable = false, unique = true)
    private UUID solicitudId;

    @Column(name = "monto_principal", precision = 19, scale = 4, nullable = false)
    private BigDecimal montoPrincipal;

    @Column(name = "tasa_interes", precision = 8, scale = 4, nullable = false)
    private BigDecimal tasaInteres;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Enumerated(EnumType.STRING)
    @Column(name = "frecuencia_pago", nullable = false, length = 20)
    private FrecuenciaPago frecuenciaPago;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "total_intereses", precision = 19, scale = 4)
    private BigDecimal totalIntereses;

    @Column(name = "total_pagado", precision = 19, scale = 4)
    private BigDecimal totalPagado;

    @Column(name = "saldo_pendiente", precision = 19, scale = 4)
    private BigDecimal saldoPendiente;

    @Column(name = "numero_cuotas", nullable = false)
    private Integer numeroCuotas;

    @Column(name = "cuota_mensual", precision = 19, scale = 4)
    private BigDecimal cuotaMensual;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPlanAmortizacion estado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public PlanAmortizacionEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public UUID getSolicitudId() { return solicitudId; }
    public void setSolicitudId(UUID v) { this.solicitudId = v; }
    public BigDecimal getMontoPrincipal() { return montoPrincipal; }
    public void setMontoPrincipal(BigDecimal v) { this.montoPrincipal = v; }
    public BigDecimal getTasaInteres() { return tasaInteres; }
    public void setTasaInteres(BigDecimal v) { this.tasaInteres = v; }
    public Integer getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(Integer v) { this.plazoMeses = v; }
    public FrecuenciaPago getFrecuenciaPago() { return frecuenciaPago; }
    public void setFrecuenciaPago(FrecuenciaPago v) { this.frecuenciaPago = v; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate v) { this.fechaInicio = v; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate v) { this.fechaFin = v; }
    public BigDecimal getTotalIntereses() { return totalIntereses; }
    public void setTotalIntereses(BigDecimal v) { this.totalIntereses = v; }
    public BigDecimal getTotalPagado() { return totalPagado; }
    public void setTotalPagado(BigDecimal v) { this.totalPagado = v; }
    public BigDecimal getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(BigDecimal v) { this.saldoPendiente = v; }
    public Integer getNumeroCuotas() { return numeroCuotas; }
    public void setNumeroCuotas(Integer v) { this.numeroCuotas = v; }
    public BigDecimal getCuotaMensual() { return cuotaMensual; }
    public void setCuotaMensual(BigDecimal v) { this.cuotaMensual = v; }
    public EstadoPlanAmortizacion getEstado() { return estado; }
    public void setEstado(EstadoPlanAmortizacion v) { this.estado = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
    public Long getVersion() { return version; }
    public void setVersion(Long v) { this.version = v; }

    public static PlanAmortizacionEntityBuilder builder() { return new PlanAmortizacionEntityBuilder(); }

    public static class PlanAmortizacionEntityBuilder {
        private PlanAmortizacionEntity e = new PlanAmortizacionEntity();
        public PlanAmortizacionEntityBuilder id(UUID v) { e.id = v; return this; }
        public PlanAmortizacionEntityBuilder solicitudId(UUID v) { e.solicitudId = v; return this; }
        public PlanAmortizacionEntityBuilder montoPrincipal(BigDecimal v) { e.montoPrincipal = v; return this; }
        public PlanAmortizacionEntityBuilder tasaInteres(BigDecimal v) { e.tasaInteres = v; return this; }
        public PlanAmortizacionEntityBuilder plazoMeses(Integer v) { e.plazoMeses = v; return this; }
        public PlanAmortizacionEntityBuilder frecuenciaPago(FrecuenciaPago v) { e.frecuenciaPago = v; return this; }
        public PlanAmortizacionEntityBuilder fechaInicio(LocalDate v) { e.fechaInicio = v; return this; }
        public PlanAmortizacionEntityBuilder fechaFin(LocalDate v) { e.fechaFin = v; return this; }
        public PlanAmortizacionEntityBuilder totalIntereses(BigDecimal v) { e.totalIntereses = v; return this; }
        public PlanAmortizacionEntityBuilder totalPagado(BigDecimal v) { e.totalPagado = v; return this; }
        public PlanAmortizacionEntityBuilder saldoPendiente(BigDecimal v) { e.saldoPendiente = v; return this; }
        public PlanAmortizacionEntityBuilder numeroCuotas(Integer v) { e.numeroCuotas = v; return this; }
        public PlanAmortizacionEntityBuilder cuotaMensual(BigDecimal v) { e.cuotaMensual = v; return this; }
        public PlanAmortizacionEntityBuilder estado(EstadoPlanAmortizacion v) { e.estado = v; return this; }
        public PlanAmortizacionEntityBuilder createdAt(LocalDateTime v) { e.createdAt = v; return this; }
        public PlanAmortizacionEntityBuilder updatedAt(LocalDateTime v) { e.updatedAt = v; return this; }
        public PlanAmortizacionEntityBuilder version(Long v) { e.version = v; return this; }
        public PlanAmortizacionEntity build() { return e; }
    }
}
