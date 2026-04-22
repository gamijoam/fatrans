// com/tufondo/creditos/infrastructure/persistence/entity/AmortizacionEntity.java
package com.tufondo.creditos.infrastructure.persistence.entity;

import com.tufondo.creditos.domain.model.enums.EstadoAmortizacion;
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
 * JPA Entity para Amortizacion.
 * Incluye @Version para optimistic locking y referenciaPago única para idempotencia.
 */
@Entity
@Table(name = "amortizaciones",
    indexes = {
        @Index(name = "idx_amortizacion_plan_id", columnList = "plan_id"),
        @Index(name = "idx_amortizacion_numero_cuota", columnList = "plan_id, numero_cuota"),
        @Index(name = "idx_amortizacion_estado", columnList = "estado"),
        @Index(name = "idx_amortizacion_fecha_venc", columnList = "fecha_vencimiento"),
        @Index(name = "idx_amortizacion_referencia", columnList = "referencia_pago", unique = true)
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_referencia_pago", columnNames = {"referencia_pago"})
    })
public class AmortizacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "plan_id", nullable = false)
    private UUID planId;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @Column(name = "capital", precision = 19, scale = 4, nullable = false)
    private BigDecimal capital;

    @Column(name = "interes", precision = 19, scale = 4, nullable = false)
    private BigDecimal interes;

    @Column(name = "seguros", precision = 19, scale = 4)
    private BigDecimal seguros;

    @Column(name = "comisiones", precision = 19, scale = 4)
    private BigDecimal comisiones;

    @Column(name = "monto_cuota", precision = 19, scale = 4, nullable = false)
    private BigDecimal montoCuota;

    @Column(name = "saldo_insoluto", precision = 19, scale = 4)
    private BigDecimal saldoInsoluto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoAmortizacion estado;

    @Column(name = "dias_mora")
    private Integer diasMora;

    @Column(name = "interes_mora", precision = 19, scale = 4)
    private BigDecimal interesMora;

    @Column(name = "monto_pagado", precision = 19, scale = 4)
    private BigDecimal montoPagado;

    @Column(name = "referencia_pago", unique = true, length = 100)
    private String referenciaPago;

    @Column(name = "colateral_ejecutada", nullable = false)
    private Boolean colateralEjecutada = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;  // CRÍTICO: Previene double-payment

    public AmortizacionEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public UUID getPlanId() { return planId; }
    public void setPlanId(UUID v) { this.planId = v; }
    public Integer getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(Integer v) { this.numeroCuota = v; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate v) { this.fechaVencimiento = v; }
    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate v) { this.fechaPago = v; }
    public BigDecimal getCapital() { return capital; }
    public void setCapital(BigDecimal v) { this.capital = v; }
    public BigDecimal getInteres() { return interes; }
    public void setInteres(BigDecimal v) { this.interes = v; }
    public BigDecimal getSeguros() { return seguros; }
    public void setSeguros(BigDecimal v) { this.seguros = v; }
    public BigDecimal getComisiones() { return comisiones; }
    public void setComisiones(BigDecimal v) { this.comisiones = v; }
    public BigDecimal getMontoCuota() { return montoCuota; }
    public void setMontoCuota(BigDecimal v) { this.montoCuota = v; }
    public BigDecimal getSaldoInsoluto() { return saldoInsoluto; }
    public void setSaldoInsoluto(BigDecimal v) { this.saldoInsoluto = v; }
    public EstadoAmortizacion getEstado() { return estado; }
    public void setEstado(EstadoAmortizacion v) { this.estado = v; }
    public Integer getDiasMora() { return diasMora; }
    public void setDiasMora(Integer v) { this.diasMora = v; }
    public BigDecimal getInteresMora() { return interesMora; }
    public void setInteresMora(BigDecimal v) { this.interesMora = v; }
    public BigDecimal getMontoPagado() { return montoPagado; }
    public void setMontoPagado(BigDecimal v) { this.montoPagado = v; }
    public String getReferenciaPago() { return referenciaPago; }
    public void setReferenciaPago(String v) { this.referenciaPago = v; }
    public Boolean getColateralEjecutada() { return colateralEjecutada; }
    public void setColateralEjecutada(Boolean v) { this.colateralEjecutada = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
    public Long getVersion() { return version; }
    public void setVersion(Long v) { this.version = v; }

    // Alias para compatibilidad con CreditoQueryPortAdapter
    public BigDecimal getCuota() { return montoCuota; }
    public BigDecimal getSaldoRestante() { return saldoInsoluto; }

    public static AmortizacionEntityBuilder builder() { return new AmortizacionEntityBuilder(); }

    public static class AmortizacionEntityBuilder {
        private AmortizacionEntity e = new AmortizacionEntity();
        public AmortizacionEntityBuilder id(UUID v) { e.id = v; return this; }
        public AmortizacionEntityBuilder planId(UUID v) { e.planId = v; return this; }
        public AmortizacionEntityBuilder numeroCuota(Integer v) { e.numeroCuota = v; return this; }
        public AmortizacionEntityBuilder fechaVencimiento(LocalDate v) { e.fechaVencimiento = v; return this; }
        public AmortizacionEntityBuilder fechaPago(LocalDate v) { e.fechaPago = v; return this; }
        public AmortizacionEntityBuilder capital(BigDecimal v) { e.capital = v; return this; }
        public AmortizacionEntityBuilder interes(BigDecimal v) { e.interes = v; return this; }
        public AmortizacionEntityBuilder seguros(BigDecimal v) { e.seguros = v; return this; }
        public AmortizacionEntityBuilder comisiones(BigDecimal v) { e.comisiones = v; return this; }
        public AmortizacionEntityBuilder montoCuota(BigDecimal v) { e.montoCuota = v; return this; }
        public AmortizacionEntityBuilder saldoInsoluto(BigDecimal v) { e.saldoInsoluto = v; return this; }
        public AmortizacionEntityBuilder estado(EstadoAmortizacion v) { e.estado = v; return this; }
        public AmortizacionEntityBuilder diasMora(Integer v) { e.diasMora = v; return this; }
        public AmortizacionEntityBuilder interesMora(BigDecimal v) { e.interesMora = v; return this; }
        public AmortizacionEntityBuilder montoPagado(BigDecimal v) { e.montoPagado = v; return this; }
        public AmortizacionEntityBuilder referenciaPago(String v) { e.referenciaPago = v; return this; }
        public AmortizacionEntityBuilder colateralEjecutada(Boolean v) { e.colateralEjecutada = v; return this; }
        public AmortizacionEntityBuilder createdAt(LocalDateTime v) { e.createdAt = v; return this; }
        public AmortizacionEntityBuilder updatedAt(LocalDateTime v) { e.updatedAt = v; return this; }
        public AmortizacionEntityBuilder version(Long v) { e.version = v; return this; }
        public AmortizacionEntity build() { return e; }
    }
}
