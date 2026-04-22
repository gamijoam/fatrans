package com.tufondo.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Solicitud de recuperación de contraseña")
public record RecuperarPasswordRequestDTO(
        @Schema(description = "Email o nombre de usuario delaccount", example = "juan@empresa.com")
        @NotBlank(message = "El identificador es requerido")
        @Size(min = 3, max = 100, message = "El identificador debe tener entre 3 y 100 caracteres")
        String identificador
) {}
