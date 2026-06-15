package com.tufondo.productos.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "productos_financiables",
    indexes = {
        @Index(name = "idx_productos_financiables_estado", columnList = "estado"),
        @Index(name = "idx_productos_financiables_categoria", columnList = "categoria"),
        @Index(name = "idx_productos_financiables_tipo_credito", columnList = "tipo_credito_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_productos_financiables_codigo", columnNames = "codigo"),
        @UniqueConstraint(name = "uk_productos_financiables_slug", columnNames = "slug")
    })
public class ProductoFinanciableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String slug;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false, length = 40)
    private String categoria;

    @Column(length = 120)
    private String proveedor;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal precio;

    @Column(nullable = false, length = 10)
    private String moneda;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "tipo_credito_id", nullable = false)
    private Long tipoCreditoId;

    @Column(name = "plazo_minimo_meses", nullable = false)
    private Integer plazoMinimoMeses;

    @Column(name = "plazo_maximo_meses", nullable = false)
    private Integer plazoMaximoMeses;

    @Column(name = "porcentaje_colateral", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeColateral;

    @Column(name = "requiere_aprobacion_manual", nullable = false)
    private Boolean requiereAprobacionManual;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "visible_desde")
    private LocalDateTime visibleDesde;

    @Column(name = "visible_hasta")
    private LocalDateTime visibleHasta;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
    public Long getTipoCreditoId() { return tipoCreditoId; }
    public void setTipoCreditoId(Long tipoCreditoId) { this.tipoCreditoId = tipoCreditoId; }
    public Integer getPlazoMinimoMeses() { return plazoMinimoMeses; }
    public void setPlazoMinimoMeses(Integer plazoMinimoMeses) { this.plazoMinimoMeses = plazoMinimoMeses; }
    public Integer getPlazoMaximoMeses() { return plazoMaximoMeses; }
    public void setPlazoMaximoMeses(Integer plazoMaximoMeses) { this.plazoMaximoMeses = plazoMaximoMeses; }
    public BigDecimal getPorcentajeColateral() { return porcentajeColateral; }
    public void setPorcentajeColateral(BigDecimal porcentajeColateral) { this.porcentajeColateral = porcentajeColateral; }
    public Boolean getRequiereAprobacionManual() { return requiereAprobacionManual; }
    public void setRequiereAprobacionManual(Boolean requiereAprobacionManual) { this.requiereAprobacionManual = requiereAprobacionManual; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getVisibleDesde() { return visibleDesde; }
    public void setVisibleDesde(LocalDateTime visibleDesde) { this.visibleDesde = visibleDesde; }
    public LocalDateTime getVisibleHasta() { return visibleHasta; }
    public void setVisibleHasta(LocalDateTime visibleHasta) { this.visibleHasta = visibleHasta; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
