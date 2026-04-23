package com.tufondo.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud para cambiar contraseña del usuario autenticado")
public record CambiarPasswordRequestDTO(
        @Schema(description = "Contraseña actual", example = "ActualPassword123!")
        @NotBlank(message = "La contraseña actual es requerida")
        String passwordActual,

        @Schema(description = "Nueva contraseña", example = "NuevaPassword123!")
        @NotBlank(message = "La nueva contraseña es requerida")
        @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "La contraseña debe contener al menos una mayúscula, una minúscula, un número y un carácter especial"
        )
        String nuevoPassword
) {}