package com.tufondo.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud para restablecer contraseña con token")
public record ResetPasswordRequestDTO(
        @Schema(description = "Token de recuperación", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "El token es requerido")
        String token,

        @Schema(description = "Nueva contraseña", example = "NuevaPassword123!")
        @NotBlank(message = "La nueva contraseña es requerida")
        @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial"
        )
        String nuevaPassword
) {}
