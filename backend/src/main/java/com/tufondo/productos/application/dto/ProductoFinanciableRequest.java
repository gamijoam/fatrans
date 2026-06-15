package com.tufondo.productos.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ProductoFinanciableRequest {
    @NotBlank
    @Size(max = 30)
    private String codigo;

    @NotBlank
    @Size(max = 120)
    private String nombre;

    @Size(max = 1000)
    private String descripcion;

    @NotBlank
    @Size(max = 40)
    private String categoria;

    @Size(max = 120)
    private String proveedor;

    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal precio;

    @NotBlank
    @Size(max = 10)
    private String moneda;

    @Size(max = 500)
    private String imagenUrl;

    @NotNull
    private Long tipoCreditoId;

    @NotNull
    @Min(1)
    private Integer plazoMinimoMeses;

    @NotNull
    @Min(1)
    private Integer plazoMaximoMeses;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    private BigDecimal porcentajeColateral;

    private Boolean requiereAprobacionManual = true;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
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
}
