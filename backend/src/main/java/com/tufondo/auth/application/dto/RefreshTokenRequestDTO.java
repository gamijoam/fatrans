package com.tufondo.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Solicitud de refresh de token")
public record RefreshTokenRequestDTO(
        @Schema(description = "Refresh token actual", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @NotBlank(message = "El refresh token es requerido")
        String refreshToken
) {}