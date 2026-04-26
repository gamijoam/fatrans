package com.tufondo.parametros.infrastructure.persistence.entity;

import com.tufondo.parametros.domain.model.ParametroSistema;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parametros_sistema")
public class ParametroEntity {

    @Id
    @Column(name = "param_key", length = 100)
    private String key;

    @Column(name = "valor", length = 500, nullable = false)
    private String valor;

    @Column(name = "tipo", length = 20, nullable = false)
    private String tipo;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "categoria", length = 50)
    private String categoria;

    @Column(name = "editable")
    private boolean editable = true;

    @Column(name = "fecha_actualizacion")
    private Instant fechaActualizacion;

    @Column(name = "actualizado_por")
    private UUID actualizadoPor;

    public ParametroEntity() {}

    public ParametroEntity(String key, String valor, String tipo, String descripcion, String categoria) {
        this.key = key;
        this.valor = valor;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.editable = true;
    }

    public static ParametroEntity desdeDominio(ParametroSistema param) {
        ParametroEntity entity = new ParametroEntity();
        entity.setKey(param.key());
        entity.setValor(param.valor());
        entity.setTipo(param.tipo().name());
        entity.setDescripcion(param.descripcion());
        entity.setCategoria(param.categoria());
        entity.setEditable(param.editable());
        entity.setFechaActualizacion(param.fechaActualizacion());
        entity.setActualizadoPor(param.actualizadoPor());
        return entity;
    }

    public ParametroSistema aDominio() {
        return ParametroSistema.desdeParametros(
                this.key,
                this.valor,
                ParametroSistema.TipoParametro.valueOf(this.tipo),
                this.descripcion,
                this.categoria,
                this.editable,
                this.fechaActualizacion,
                this.actualizadoPor
        );
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public boolean isEditable() { return editable; }
    public void setEditable(boolean editable) { this.editable = editable; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Instant fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
    public UUID getActualizadoPor() { return actualizadoPor; }
    public void setActualizadoPor(UUID actualizadoPor) { this.actualizadoPor = actualizadoPor; }
}