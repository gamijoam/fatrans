package com.tufondo.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación")
public record LoginResponseDTO(
        @Schema(description = "Access token JWT")
        String accessToken,

        @Schema(description = "Refresh token JWT")
        String refreshToken,

        @Schema(description = "Tipo de token", example = "Bearer")
        String tokenType,

        @Schema(description = "Tiempo de expiración del access token en segundos", example = "900")
        long expiresIn,

        @Schema(description = "Información del usuario")
        UsuarioDTO usuario
) {
    @Schema(description = "Información del usuario autenticado")
    public record UsuarioDTO(
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
}