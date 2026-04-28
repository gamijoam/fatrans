package com.tufondo.transporte.infrastructure.persistence.entity;

import com.tufondo.transporte.domain.model.enums.EstadoUnidad;
import com.tufondo.transporte.domain.model.enums.TipoUnidad;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "unidades_transporte",
    indexes = {
        @Index(name = "idx_unidades_transporte_socio_id", columnList = "socio_id"),
        @Index(name = "idx_unidades_transporte_placa", columnList = "placa", unique = true)
    })
public class UnidadTransporteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "socio_id", nullable = false)
    private UUID socioId;

    @Column(name = "placa", unique = true, nullable = false, length = 20)
    private String placa;

    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    @Column(name = "modelo", nullable = false, length = 50)
    private String modelo;

    @Column(name = "ano_vehiculo", nullable = false)
    private Integer ano;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_unidad", nullable = false, length = 20)
    private TipoUnidad tipoUnidad;

    @Column(name = "capacidad_pasajeros")
    private Integer capacidadPasajeros;

    @Column(name = "soat_vencimiento")
    private LocalDate soatVencimiento;

    @Column(name = "seguro_vencimiento")
    private LocalDate seguroVencimiento;

    @Column(name = "revision_tecnica_vencimiento")
    private LocalDate revisionTecnicaVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoUnidad estado;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    public UnidadTransporteEntity() {}

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSocioId() { return socioId; }
    public void setSocioId(UUID socioId) { this.socioId = socioId; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public TipoUnidad getTipoUnidad() { return tipoUnidad; }
    public void setTipoUnidad(TipoUnidad tipoUnidad) { this.tipoUnidad = tipoUnidad; }
    public Integer getCapacidadPasajeros() { return capacidadPasajeros; }
    public void setCapacidadPasajeros(Integer capacidadPasajeros) { this.capacidadPasajeros = capacidadPasajeros; }
    public LocalDate getSoatVencimiento() { return soatVencimiento; }
    public void setSoatVencimiento(LocalDate soatVencimiento) { this.soatVencimiento = soatVencimiento; }
    public LocalDate getSeguroVencimiento() { return seguroVencimiento; }
    public void setSeguroVencimiento(LocalDate seguroVencimiento) { this.seguroVencimiento = seguroVencimiento; }
    public LocalDate getRevisionTecnicaVencimiento() { return revisionTecnicaVencimiento; }
    public void setRevisionTecnicaVencimiento(LocalDate revisionTecnicaVencimiento) { this.revisionTecnicaVencimiento = revisionTecnicaVencimiento; }
    public EstadoUnidad getEstado() { return estado; }
    public void setEstado(EstadoUnidad estado) { this.estado = estado; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoUnidad.ACTIVA;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
