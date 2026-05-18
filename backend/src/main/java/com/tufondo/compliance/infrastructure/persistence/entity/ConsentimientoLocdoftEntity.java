package com.tufondo.compliance.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity para `consentimiento_locdoft_operacion` (#218 PR-C).
 * Schema en V20__locdoft_operacion_grande.sql.
 */
@Entity
@Table(name = "consentimiento_locdoft_operacion",
    indexes = {
        @Index(name = "idx_locdoft_op_socio_fecha", columnList = "socio_id, created_at DESC"),
        @Index(name = "idx_locdoft_op_movimiento", columnList = "movimiento_id")
    })
public class ConsentimientoLocdoftEntity {

    @Id
    private UUID id;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Column(name = "cuenta_ahorro_id", nullable = false)
    private UUID cuentaAhorroId;

    @Column(name = "movimiento_id")
    private UUID movimientoId;

    @Column(name = "tipo_operacion", nullable = false, length = 20)
    private String tipoOperacion;

    @Column(name = "monto", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Column(name = "umbral_aplicado", nullable = false, precision = 18, scale = 2)
    private BigDecimal umbralAplicado;

    @Column(name = "acepta_origen_licito", nullable = false)
    private boolean aceptaOrigenLicito;

    @Column(name = "origen_fondos", columnDefinition = "TEXT")
    private String origenFondos;

    @Column(name = "ip_origen", length = 45)
    private String ipOrigen;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "request_id", length = 64)
    private String requestId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ConsentimientoLocdoftEntity() {}

    // Getters / setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSocioId() { return socioId; }
    public void setSocioId(UUID v) { this.socioId = v; }
    public UUID getCuentaAhorroId() { return cuentaAhorroId; }
    public void setCuentaAhorroId(UUID v) { this.cuentaAhorroId = v; }
    public UUID getMovimientoId() { return movimientoId; }
    public void setMovimientoId(UUID v) { this.movimientoId = v; }
    public String getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(String v) { this.tipoOperacion = v; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal v) { this.monto = v; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String v) { this.moneda = v; }
    public BigDecimal getUmbralAplicado() { return umbralAplicado; }
    public void setUmbralAplicado(BigDecimal v) { this.umbralAplicado = v; }
    public boolean isAceptaOrigenLicito() { return aceptaOrigenLicito; }
    public void setAceptaOrigenLicito(boolean v) { this.aceptaOrigenLicito = v; }
    public String getOrigenFondos() { return origenFondos; }
    public void setOrigenFondos(String v) { this.origenFondos = v; }
    public String getIpOrigen() { return ipOrigen; }
    public void setIpOrigen(String v) { this.ipOrigen = v; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String v) { this.userAgent = v; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String v) { this.sessionId = v; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String v) { this.requestId = v; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant v) { this.createdAt = v; }
}
