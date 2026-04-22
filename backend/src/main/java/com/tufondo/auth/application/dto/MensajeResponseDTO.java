package com.tufondo.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta genérica con mensaje")
public record MensajeResponseDTO(
        @Schema(description = "Mensaje descriptivo", example = "Operación realizada exitosamente")
        String mensaje
) {
    public MensajeResponseDTO(String mensaje) {
        this.mensaje = mensaje;
    }
}
