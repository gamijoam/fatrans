package com.tufondo.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación para Flutter Web (sin tokens, headers únicamente)")
public record LoginWebResponseDTO(
        @Schema(description = "ID del usuario")
        String id,

        @Schema(description = "Nombre de usuario")
        String nombreUsuario,

        @Schema(description = "Correo electrónico")
        String correoElectronico,

        @Schema(description = "Nombre completo")
        String nombreCompleto,

        @Schema(description = "Rol del usuario")
        String rol
) {}