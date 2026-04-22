package com.tufondo.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud para crear un usuario vinculado a un socio")
public record CrearUsuarioRequestDTO(
        @Schema(description = "ID del socio existente", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "El ID del socio es requerido")
        String socioId,

        @Schema(description = "Nombre de usuario para login", example = "juan.perez")
        @NotBlank(message = "El nombre de usuario es requerido")
        @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
        @Pattern(
                regexp = "^[a-zA-Z0-9._-]+$",
                message = "El nombre de usuario solo puede contener letras, números, puntos, guiones bajos y guiones"
        )
        String nombreUsuario,

        @Schema(description = "Contraseña temporal", example = "TempPassword123!")
        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial"
        )
        String password
) {}
