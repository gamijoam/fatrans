package com.tufondo.admin.application.dto;

import java.time.Instant;
import java.util.UUID;

public record SesionInfoResponse(
        UUID id,
        String ipAddress,
        String userAgent,
        Instant ultimoAcceso,
        Instant fechaCreacion,
        Instant expiraAt,
        boolean activa
) {
}