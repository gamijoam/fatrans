package com.tufondo.productos.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PrecalificacionProductoResponse {
    private Long productoId;
    private boolean elegible;
    private BigDecimal saldoDisponible;
    private BigDecimal colateralRequerido;
    private BigDecimal montoFaltante;
    private UUID cuentaColateralId;
    private String mensaje;

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public boolean isElegible() { return elegible; }
    public void setElegible(boolean elegible) { this.elegible = elegible; }
    public BigDecimal getSaldoDisponible() { return saldoDisponible; }
    public void setSaldoDisponible(BigDecimal saldoDisponible) { this.saldoDisponible = saldoDisponible; }
    public BigDecimal getColateralRequerido() { return colateralRequerido; }
    public void setColateralRequerido(BigDecimal colateralRequerido) { this.colateralRequerido = colateralRequerido; }
    public BigDecimal getMontoFaltante() { return montoFaltante; }
    public void setMontoFaltante(BigDecimal montoFaltante) { this.montoFaltante = montoFaltante; }
    public UUID getCuentaColateralId() { return cuentaColateralId; }
    public void setCuentaColateralId(UUID cuentaColateralId) { this.cuentaColateralId = cuentaColateralId; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
