package com.tufondo.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud de inicio de sesión")
public record LoginRequestDTO(
        @Schema(description = "Nombre de usuario o correo electrónico", example = "usuario@ejemplo.com")
        @NotBlank(message = "El identificador es requerido")
        @Size(min = 3, max = 100, message = "El identificador debe tener entre 3 y 100 caracteres")
        String identificador,

        @Schema(description = "Contraseña", example = "ContraseñaSegura123!")
        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial"
        )
        String password
) {}