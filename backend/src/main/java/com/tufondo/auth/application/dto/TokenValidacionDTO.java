package com.tufondo.auth.application.dto;

import java.time.Instant;
import java.util.UUID;

public record TokenValidacionDTO(
        UUID usuarioId,
        String nombreUsuario,
        String correoElectronico,
        String rol,
        Instant expiracion,
        boolean valido
) {}
