package com.tufondo.parametros.application.dto;

import com.tufondo.parametros.domain.model.ParametroSistema;
import java.time.Instant;
import java.util.UUID;

public record ParametroResponse(
        String key,
        String valor,
        String tipo,
        String descripcion,
        String categoria,
        boolean editable,
        Instant fechaActualizacion,
        UUID actualizadoPor
) {
    public static ParametroResponse fromDomain(ParametroSistema param) {
        return new ParametroResponse(
                param.key(),
                param.valor(),
                param.tipo().name(),
                param.descripcion(),
                param.categoria(),
                param.editable(),
                param.fechaActualizacion(),
                param.actualizadoPor()
        );
    }
}