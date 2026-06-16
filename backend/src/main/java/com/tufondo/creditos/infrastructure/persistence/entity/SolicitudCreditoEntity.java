// com/tufondo/creditos/infrastructure/persistence/entity/SolicitudCreditoEntity.java
package com.tufondo.creditos.infrastructure.persistence.entity;

import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity para SolicitudCredito.
 */
@Entity
@Table(name = "solicitudes_credito",
    indexes = {
        @Index(name = "idx_solicitudes_socio_id", columnList = "socio_id"),
        @Index(name = "idx_solicitudes_estado", columnList = "estado"),
        @Index(name = "idx_solicitudes_numero", columnList = "numero_solicitud", unique = true),
        @Index(name = "idx_solicitudes_fecha", columnList = "created_at"),
        @Index(name = "idx_solicitudes_tipo_credito", columnList = "tipo_credito_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_numero_solicitud", columnNames = {"numero_solicitud"})
    })
public class SolicitudCreditoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "numero_solicitud", unique = true, nullable = false, length = 25)
    private String numeroSolicitud;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Column(name = "tipo_credito_id", nullable = false)
    private Long tipoCreditoId;

    @Column(name = "monto_solicitado", precision = 19, scale = 4, nullable = false)
    private BigDecimal montoSolicitado;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "tasa_interes_aplicada", precision = 8, scale = 4)
    private BigDecimal tasaInteresAplicada;

    @Column(name = "cuota_mensual_estimada", precision = 19, scale = 4)
    private BigDecimal cuotaMensualEstimada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(name = "colateral_cuenta_id")
    private UUID colateralCuentaId;

    @Column(name = "colateral_monto_retenido", precision = 19, scale = 4)
    private BigDecimal colateralMontoRetenido;

    @Column(name = "destino_credito", length = 500)
    private String destinoCredito;

    @Column(name = "producto_financiable_id")
    private Long productoFinanciableId;

    @Column(name = "producto_nombre_snapshot", length = 120)
    private String productoNombreSnapshot;

    @Column(name = "producto_precio_snapshot", precision = 19, scale = 4)
    private BigDecimal productoPrecioSnapshot;

    @Column(name = "producto_moneda_snapshot", length = 10)
    private String productoMonedaSnapshot;

    @Column(name = "producto_colateral_requerido_snapshot", precision = 19, scale = 4)
    private BigDecimal productoColateralRequeridoSnapshot;

    @Column(name = "evaluacion_id")
    private UUID evaluacionId;

    @Column(name = "plan_amortizacion_id")
    private UUID planAmortizacionId;

    @Column(name = "referencia_desembolso", length = 100)
    private String referenciaDesembolso;

    @Column(name = "cuenta_destino", length = 34)
    private String cuentaDestino;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "fecha_rechazo")
    private LocalDateTime fechaRechazo;

    @Column(name = "fecha_desembolso")
    private LocalDateTime fechaDesembolso;

    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public SolicitudCreditoEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public String getNumeroSolicitud() { return numeroSolicitud; }
    public void setNumeroSolicitud(String v) { this.numeroSolicitud = v; }
    public UUID getSocioId() { return socioId; }
    public void setSocioId(UUID v) { this.socioId = v; }
    public Long getTipoCreditoId() { return tipoCreditoId; }
    public void setTipoCreditoId(Long v) { this.tipoCreditoId = v; }
    public BigDecimal getMontoSolicitado() { return montoSolicitado; }
    public void setMontoSolicitado(BigDecimal v) { this.montoSolicitado = v; }
    public Integer getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(Integer v) { this.plazoMeses = v; }
    public BigDecimal getTasaInteresAplicada() { return tasaInteresAplicada; }
    public void setTasaInteresAplicada(BigDecimal v) { this.tasaInteresAplicada = v; }
    public BigDecimal getCuotaMensualEstimada() { return cuotaMensualEstimada; }
    public void setCuotaMensualEstimada(BigDecimal v) { this.cuotaMensualEstimada = v; }
    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud v) { this.estado = v; }
    public UUID getColateralCuentaId() { return colateralCuentaId; }
    public void setColateralCuentaId(UUID v) { this.colateralCuentaId = v; }
    public BigDecimal getColateralMontoRetenido() { return colateralMontoRetenido; }
    public void setColateralMontoRetenido(BigDecimal v) { this.colateralMontoRetenido = v; }
    public String getDestinoCredito() { return destinoCredito; }
    public void setDestinoCredito(String v) { this.destinoCredito = v; }
    public Long getProductoFinanciableId() { return productoFinanciableId; }
    public void setProductoFinanciableId(Long v) { this.productoFinanciableId = v; }
    public String getProductoNombreSnapshot() { return productoNombreSnapshot; }
    public void setProductoNombreSnapshot(String v) { this.productoNombreSnapshot = v; }
    public BigDecimal getProductoPrecioSnapshot() { return productoPrecioSnapshot; }
    public void setProductoPrecioSnapshot(BigDecimal v) { this.productoPrecioSnapshot = v; }
    public String getProductoMonedaSnapshot() { return productoMonedaSnapshot; }
    public void setProductoMonedaSnapshot(String v) { this.productoMonedaSnapshot = v; }
    public BigDecimal getProductoColateralRequeridoSnapshot() { return productoColateralRequeridoSnapshot; }
    public void setProductoColateralRequeridoSnapshot(BigDecimal v) { this.productoColateralRequeridoSnapshot = v; }
    public UUID getEvaluacionId() { return evaluacionId; }
    public void setEvaluacionId(UUID v) { this.evaluacionId = v; }
    public UUID getPlanAmortizacionId() { return planAmortizacionId; }
    public void setPlanAmortizacionId(UUID v) { this.planAmortizacionId = v; }
    public String getReferenciaDesembolso() { return referenciaDesembolso; }
    public void setReferenciaDesembolso(String v) { this.referenciaDesembolso = v; }
    public String getCuentaDestino() { return cuentaDestino; }
    public void setCuentaDestino(String v) { this.cuentaDestino = v; }
    public String getNotas() { return notas; }
    public void setNotas(String v) { this.notas = v; }
    public LocalDateTime getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(LocalDateTime v) { this.fechaAprobacion = v; }
    public LocalDateTime getFechaRechazo() { return fechaRechazo; }
    public void setFechaRechazo(LocalDateTime v) { this.fechaRechazo = v; }
    public LocalDateTime getFechaDesembolso() { return fechaDesembolso; }
    public void setFechaDesembolso(LocalDateTime v) { this.fechaDesembolso = v; }
    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String v) { this.motivoRechazo = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
    public Long getVersion() { return version; }
    public void setVersion(Long v) { this.version = v; }

    public static SolicitudCreditoEntityBuilder builder() { return new SolicitudCreditoEntityBuilder(); }

    public static class SolicitudCreditoEntityBuilder {
        private SolicitudCreditoEntity e = new SolicitudCreditoEntity();
        public SolicitudCreditoEntityBuilder id(UUID v) { e.id = v; return this; }
        public SolicitudCreditoEntityBuilder numeroSolicitud(String v) { e.numeroSolicitud = v; return this; }
        public SolicitudCreditoEntityBuilder socioId(UUID v) { e.socioId = v; return this; }
        public SolicitudCreditoEntityBuilder tipoCreditoId(Long v) { e.tipoCreditoId = v; return this; }
        public SolicitudCreditoEntityBuilder montoSolicitado(BigDecimal v) { e.montoSolicitado = v; return this; }
        public SolicitudCreditoEntityBuilder plazoMeses(Integer v) { e.plazoMeses = v; return this; }
        public SolicitudCreditoEntityBuilder tasaInteresAplicada(BigDecimal v) { e.tasaInteresAplicada = v; return this; }
        public SolicitudCreditoEntityBuilder cuotaMensualEstimada(BigDecimal v) { e.cuotaMensualEstimada = v; return this; }
        public SolicitudCreditoEntityBuilder estado(EstadoSolicitud v) { e.estado = v; return this; }
        public SolicitudCreditoEntityBuilder colateralCuentaId(UUID v) { e.colateralCuentaId = v; return this; }
        public SolicitudCreditoEntityBuilder colateralMontoRetenido(BigDecimal v) { e.colateralMontoRetenido = v; return this; }
        public SolicitudCreditoEntityBuilder destinoCredito(String v) { e.destinoCredito = v; return this; }
        public SolicitudCreditoEntityBuilder productoFinanciableId(Long v) { e.productoFinanciableId = v; return this; }
        public SolicitudCreditoEntityBuilder productoNombreSnapshot(String v) { e.productoNombreSnapshot = v; return this; }
        public SolicitudCreditoEntityBuilder productoPrecioSnapshot(BigDecimal v) { e.productoPrecioSnapshot = v; return this; }
        public SolicitudCreditoEntityBuilder productoMonedaSnapshot(String v) { e.productoMonedaSnapshot = v; return this; }
        public SolicitudCreditoEntityBuilder productoColateralRequeridoSnapshot(BigDecimal v) { e.productoColateralRequeridoSnapshot = v; return this; }
        public SolicitudCreditoEntityBuilder evaluacionId(UUID v) { e.evaluacionId = v; return this; }
        public SolicitudCreditoEntityBuilder planAmortizacionId(UUID v) { e.planAmortizacionId = v; return this; }
        public SolicitudCreditoEntityBuilder referenciaDesembolso(String v) { e.referenciaDesembolso = v; return this; }
        public SolicitudCreditoEntityBuilder cuentaDestino(String v) { e.cuentaDestino = v; return this; }
        public SolicitudCreditoEntityBuilder notas(String v) { e.notas = v; return this; }
        public SolicitudCreditoEntityBuilder fechaAprobacion(LocalDateTime v) { e.fechaAprobacion = v; return this; }
        public SolicitudCreditoEntityBuilder fechaRechazo(LocalDateTime v) { e.fechaRechazo = v; return this; }
        public SolicitudCreditoEntityBuilder fechaDesembolso(LocalDateTime v) { e.fechaDesembolso = v; return this; }
        public SolicitudCreditoEntityBuilder motivoRechazo(String v) { e.motivoRechazo = v; return this; }
        public SolicitudCreditoEntityBuilder createdAt(LocalDateTime v) { e.createdAt = v; return this; }
        public SolicitudCreditoEntityBuilder updatedAt(LocalDateTime v) { e.updatedAt = v; return this; }
        public SolicitudCreditoEntityBuilder version(Long v) { e.version = v; return this; }
        public SolicitudCreditoEntity build() { return e; }
    }
}
