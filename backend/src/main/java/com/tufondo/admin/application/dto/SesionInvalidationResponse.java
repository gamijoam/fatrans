package com.tufondo.admin.application.dto;

import java.util.UUID;

public record SesionInvalidationResponse(
        UUID usuarioId,
        int sesionesInvalidadas,
        String mensaje
) {
}