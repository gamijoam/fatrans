package com.tufondo.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta tras crear un usuario exitosamente")
public record CrearUsuarioResponseDTO(
        @Schema(description = "ID del usuario creado", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Nombre de usuario creado", example = "juan.perez")
        String nombreUsuario,

        @Schema(description = "Mensaje de confirmación", example = "Usuario creado exitosamente")
        String mensaje
) {}
