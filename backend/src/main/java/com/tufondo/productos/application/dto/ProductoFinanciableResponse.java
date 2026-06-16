package com.tufondo.productos.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductoFinanciableResponse {
    private Long id;
    private String codigo;
    private String slug;
    private String nombre;
    private String descripcion;
    private String categoria;
    private String proveedor;
    private BigDecimal precio;
    private String moneda;
    private String imagenUrl;
    private Long tipoCreditoId;
    private Integer plazoMinimoMeses;
    private Integer plazoMaximoMeses;
    private BigDecimal porcentajeColateral;
    private BigDecimal colateralRequerido;
    private Boolean requiereAprobacionManual;
    private String estado;
    private LocalDateTime updatedAt;

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
    public BigDecimal getColateralRequerido() { return colateralRequerido; }
    public void setColateralRequerido(BigDecimal colateralRequerido) { this.colateralRequerido = colateralRequerido; }
    public Boolean getRequiereAprobacionManual() { return requiereAprobacionManual; }
    public void setRequiereAprobacionManual(Boolean requiereAprobacionManual) { this.requiereAprobacionManual = requiereAprobacionManual; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
