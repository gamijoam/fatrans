package com.tufondo.tipocambio.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tipos_cambio")
public class TipoCambioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "fecha", nullable = false, unique = true)
    private LocalDate fecha;

    @Column(name = "tasa_compra", nullable = false, precision = 18, scale = 6)
    private BigDecimal tasaCompra;

    @Column(name = "tasa_venta", nullable = false, precision = 18, scale = 6)
    private BigDecimal tasaVenta;

    @Column(name = "fuente", length = 50)
    private String fuente;

    @Column(name = "creado_por")
    private UUID creadoPor;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public TipoCambioEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public BigDecimal getTasaCompra() { return tasaCompra; }
    public void setTasaCompra(BigDecimal tasaCompra) { this.tasaCompra = tasaCompra; }
    public BigDecimal getTasaVenta() { return tasaVenta; }
    public void setTasaVenta(BigDecimal tasaVenta) { this.tasaVenta = tasaVenta; }
    public String getFuente() { return fuente; }
    public void setFuente(String fuente) { this.fuente = fuente; }
    public UUID getCreadoPor() { return creadoPor; }
    public void setCreadoPor(UUID creadoPor) { this.creadoPor = creadoPor; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static TipoCambioEntityBuilder builder() {
        return new TipoCambioEntityBuilder();
    }

    public static class TipoCambioEntityBuilder {
        private final TipoCambioEntity entity = new TipoCambioEntity();

        public TipoCambioEntityBuilder id(UUID id) { entity.id = id; return this; }
        public TipoCambioEntityBuilder fecha(LocalDate fecha) { entity.fecha = fecha; return this; }
        public TipoCambioEntityBuilder tasaCompra(BigDecimal tasaCompra) { entity.tasaCompra = tasaCompra; return this; }
        public TipoCambioEntityBuilder tasaVenta(BigDecimal tasaVenta) { entity.tasaVenta = tasaVenta; return this; }
        public TipoCambioEntityBuilder fuente(String fuente) { entity.fuente = fuente; return this; }
        public TipoCambioEntityBuilder creadoPor(UUID creadoPor) { entity.creadoPor = creadoPor; return this; }
        public TipoCambioEntityBuilder createdAt(Instant createdAt) { entity.createdAt = createdAt; return this; }
        public TipoCambioEntity build() { return entity; }
    }
}