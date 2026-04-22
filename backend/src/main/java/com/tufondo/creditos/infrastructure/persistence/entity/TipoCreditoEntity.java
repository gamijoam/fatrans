// com/tufondo.creditos.infrastructure.persistence.entity.TipoCreditoEntity.java
package com.tufondo.creditos.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity para TipoCredito.
 */
@Entity
@Table(name = "tipos_credito",
    indexes = {
        @Index(name = "idx_tipos_credito_activo", columnList = "activo"),
        @Index(name = "idx_tipos_credito_codigo", columnList = "codigo", unique = true)
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_tipo_credito_codigo", columnNames = {"codigo"})
    })
public class TipoCreditoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", unique = true, nullable = false, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "tasa_interes_anual", precision = 8, scale = 4, nullable = false)
    private BigDecimal tasaInteresAnual;

    @Column(name = "plazo_minimo_meses", nullable = false)
    private Integer plazoMinimoMeses;

    @Column(name = "plazo_maximo_meses", nullable = false)
    private Integer plazoMaximoMeses;

    @Column(name = "monto_minimo", precision = 19, scale = 4, nullable = false)
    private BigDecimal montoMinimo;

    @Column(name = "monto_maximo", precision = 19, scale = 4, nullable = false)
    private BigDecimal montoMaximo;

    @Column(name = "porcentaje_requerimiento_colateral", precision = 5, scale = 2)
    private BigDecimal porcentajeRequerimientoColateral;

    @Column(name = "comision_apertura", precision = 5, scale = 4)
    private BigDecimal comisionApertura;

    @Column(name = "penalidad_mora_tasa", precision = 8, scale = 4)
    private BigDecimal penalidadMoraTasa;

    @Column(name = "dias_gracia", nullable = false)
    private Integer diasGracia;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public TipoCreditoEntity() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String v) { this.codigo = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public BigDecimal getTasaInteresAnual() { return tasaInteresAnual; }
    public void setTasaInteresAnual(BigDecimal v) { this.tasaInteresAnual = v; }
    public Integer getPlazoMinimoMeses() { return plazoMinimoMeses; }
    public void setPlazoMinimoMeses(Integer v) { this.plazoMinimoMeses = v; }
    public Integer getPlazoMaximoMeses() { return plazoMaximoMeses; }
    public void setPlazoMaximoMeses(Integer v) { this.plazoMaximoMeses = v; }
    public BigDecimal getMontoMinimo() { return montoMinimo; }
    public void setMontoMinimo(BigDecimal v) { this.montoMinimo = v; }
    public BigDecimal getMontoMaximo() { return montoMaximo; }
    public void setMontoMaximo(BigDecimal v) { this.montoMaximo = v; }
    public BigDecimal getPorcentajeRequerimientoColateral() { return porcentajeRequerimientoColateral; }
    public void setPorcentajeRequerimientoColateral(BigDecimal v) { this.porcentajeRequerimientoColateral = v; }
    public BigDecimal getComisionApertura() { return comisionApertura; }
    public void setComisionApertura(BigDecimal v) { this.comisionApertura = v; }
    public BigDecimal getPenalidadMoraTasa() { return penalidadMoraTasa; }
    public void setPenalidadMoraTasa(BigDecimal v) { this.penalidadMoraTasa = v; }
    public Integer getDiasGracia() { return diasGracia; }
    public void setDiasGracia(Integer v) { this.diasGracia = v; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean v) { this.activo = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
    public Long getVersion() { return version; }
    public void setVersion(Long v) { this.version = v; }

    public static TipoCreditoEntityBuilder builder() { return new TipoCreditoEntityBuilder(); }

    public static class TipoCreditoEntityBuilder {
        private TipoCreditoEntity e = new TipoCreditoEntity();
        public TipoCreditoEntityBuilder id(Long v) { e.id = v; return this; }
        public TipoCreditoEntityBuilder codigo(String v) { e.codigo = v; return this; }
        public TipoCreditoEntityBuilder nombre(String v) { e.nombre = v; return this; }
        public TipoCreditoEntityBuilder descripcion(String v) { e.descripcion = v; return this; }
        public TipoCreditoEntityBuilder tasaInteresAnual(BigDecimal v) { e.tasaInteresAnual = v; return this; }
        public TipoCreditoEntityBuilder plazoMinimoMeses(Integer v) { e.plazoMinimoMeses = v; return this; }
        public TipoCreditoEntityBuilder plazoMaximoMeses(Integer v) { e.plazoMaximoMeses = v; return this; }
        public TipoCreditoEntityBuilder montoMinimo(BigDecimal v) { e.montoMinimo = v; return this; }
        public TipoCreditoEntityBuilder montoMaximo(BigDecimal v) { e.montoMaximo = v; return this; }
        public TipoCreditoEntityBuilder porcentajeRequerimientoColateral(BigDecimal v) { e.porcentajeRequerimientoColateral = v; return this; }
        public TipoCreditoEntityBuilder comisionApertura(BigDecimal v) { e.comisionApertura = v; return this; }
        public TipoCreditoEntityBuilder penalidadMoraTasa(BigDecimal v) { e.penalidadMoraTasa = v; return this; }
        public TipoCreditoEntityBuilder diasGracia(Integer v) { e.diasGracia = v; return this; }
        public TipoCreditoEntityBuilder activo(Boolean v) { e.activo = v; return this; }
        public TipoCreditoEntityBuilder createdAt(LocalDateTime v) { e.createdAt = v; return this; }
        public TipoCreditoEntityBuilder updatedAt(LocalDateTime v) { e.updatedAt = v; return this; }
        public TipoCreditoEntityBuilder version(Long v) { e.version = v; return this; }
        public TipoCreditoEntity build() { return e; }
    }
}
