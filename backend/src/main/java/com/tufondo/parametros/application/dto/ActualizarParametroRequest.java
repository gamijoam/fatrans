package com.tufondo.parametros.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ActualizarParametroRequest(
        @NotBlank(message = "El valor es requerido")
        String valor
) {}