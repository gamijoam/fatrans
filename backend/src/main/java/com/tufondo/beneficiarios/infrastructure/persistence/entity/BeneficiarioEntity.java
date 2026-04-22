// com/tufondo/beneficiarios/infrastructure/persistence/entity/BeneficiarioEntity.java
package com.tufondo.beneficiarios.infrastructure.persistence.entity;

import com.tufondo.beneficiarios.domain.model.enums.Parentesco;
import com.tufondo.beneficiarios.domain.model.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "beneficiaries",
    indexes = {
        @Index(name = "idx_beneficiaries_socio_id", columnList = "socio_id"),
        @Index(name = "idx_beneficiaries_socio_activo", columnList = "socio_id, activo"),
        @Index(name = "idx_beneficiaries_numero_documento", columnList = "numero_documento"),
        @Index(name = "idx_beneficiaries_activo", columnList = "activo")
    })
public class BeneficiarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Column(name = "nombre_completo", nullable = false, length = 200)
    private String nombreCompleto;

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 30)
    private TipoDocumento tipoDocumento;

    @Enumerated(EnumType.STRING)
    @Column(name = "parentesco", nullable = false, length = 20)
    private Parentesco parentesco;

    @Column(name = "porcentaje", precision = 5, scale = 2, nullable = false)
    private BigDecimal porcentaje;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "activo", nullable = false)
    private boolean activo;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private Instant fechaRegistro;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;

    public BeneficiarioEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID v) { this.id = v; }
    public UUID getSocioId() { return socioId; }
    public void setSocioId(UUID v) { this.socioId = v; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String v) { this.nombreCompleto = v; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String v) { this.numeroDocumento = v; }
    public TipoDocumento getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumento v) { this.tipoDocumento = v; }
    public Parentesco getParentesco() { return parentesco; }
    public void setParentesco(Parentesco v) { this.parentesco = v; }
    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal v) { this.porcentaje = v; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { this.telefono = v; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean v) { this.activo = v; }
    public Instant getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Instant v) { this.fechaRegistro = v; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Instant v) { this.fechaActualizacion = v; }

    public static BeneficiarioEntityBuilder builder() { return new BeneficiarioEntityBuilder(); }

    public static class BeneficiarioEntityBuilder {
        private BeneficiarioEntity e = new BeneficiarioEntity();
        public BeneficiarioEntityBuilder id(UUID v) { e.id = v; return this; }
        public BeneficiarioEntityBuilder socioId(UUID v) { e.socioId = v; return this; }
        public BeneficiarioEntityBuilder nombreCompleto(String v) { e.nombreCompleto = v; return this; }
        public BeneficiarioEntityBuilder numeroDocumento(String v) { e.numeroDocumento = v; return this; }
        public BeneficiarioEntityBuilder tipoDocumento(TipoDocumento v) { e.tipoDocumento = v; return this; }
        public BeneficiarioEntityBuilder parentesco(Parentesco v) { e.parentesco = v; return this; }
        public BeneficiarioEntityBuilder porcentaje(BigDecimal v) { e.porcentaje = v; return this; }
        public BeneficiarioEntityBuilder telefono(String v) { e.telefono = v; return this; }
        public BeneficiarioEntityBuilder activo(boolean v) { e.activo = v; return this; }
        public BeneficiarioEntityBuilder fechaRegistro(Instant v) { e.fechaRegistro = v; return this; }
        public BeneficiarioEntityBuilder fechaActualizacion(Instant v) { e.fechaActualizacion = v; return this; }
        public BeneficiarioEntity build() { return e; }
    }
}