package com.tufondo.parametros.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class ParametroSistema {

    private final String key;
    private final String valor;
    private final TipoParametro tipo;
    private final String descripcion;
    private final String categoria;
    private final boolean editable;
    private final Instant fechaActualizacion;
    private final UUID actualizadoPor;

    public enum TipoParametro {
        STRING, NUMERIC, BOOLEAN, DATE, PERCENTAGE, CURRENCY
    }

    private ParametroSistema(
            String key,
            String valor,
            TipoParametro tipo,
            String descripcion,
            String categoria,
            boolean editable,
            Instant fechaActualizacion,
            UUID actualizadoPor
    ) {
        this.key = key;
        this.valor = valor;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.editable = editable;
        this.fechaActualizacion = fechaActualizacion;
        this.actualizadoPor = actualizadoPor;
    }

    public static ParametroSistema crear(
            String key,
            String valor,
            TipoParametro tipo,
            String descripcion,
            String categoria
    ) {
        return new ParametroSistema(
                key,
                valor,
                tipo,
                descripcion,
                categoria,
                true,
                Instant.now(),
                null
        );
    }

    public static ParametroSistema desdeParametros(
            String key,
            String valor,
            TipoParametro tipo,
            String descripcion,
            String categoria,
            boolean editable,
            Instant fechaActualizacion,
            UUID actualizadoPor
    ) {
        return new ParametroSistema(
                key,
                valor,
                tipo,
                descripcion,
                categoria,
                editable,
                fechaActualizacion,
                actualizadoPor
        );
    }

    public ParametroSistema conValor(String nuevoValor, UUID usuarioId) {
        return new ParametroSistema(
                this.key,
                nuevoValor,
                this.tipo,
                this.descripcion,
                this.categoria,
                this.editable,
                Instant.now(),
                usuarioId
        );
    }

    public String getValorComoString() {
        return valor;
    }

    public BigDecimal getValorComoNumeric() {
        return valor != null ? new BigDecimal(valor) : BigDecimal.ZERO;
    }

    public Boolean getValorComoBoolean() {
        return valor != null ? Boolean.parseBoolean(valor) : false;
    }

    public boolean isEditable() {
        return editable;
    }

    public String key() { return key; }
    public String valor() { return valor; }
    public TipoParametro tipo() { return tipo; }
    public String descripcion() { return descripcion; }
    public String categoria() { return categoria; }
    public boolean editable() { return editable; }
    public Instant fechaActualizacion() { return fechaActualizacion; }
    public UUID actualizadoPor() { return actualizadoPor; }
}