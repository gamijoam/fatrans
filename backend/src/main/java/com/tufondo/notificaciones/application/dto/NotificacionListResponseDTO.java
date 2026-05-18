package com.tufondo.notificaciones.application.dto;

import java.util.List;

/**
 * DTO de respuesta para listado paginado de notificaciones (issue #214).
 *
 * <p>Estructura idéntica al patrón {@code CuentasPorSocioResponse} —
 * envoltura con metadata de paginación. Frontend tiene
 * {@code parse-cuentas-response.ts} y similares para manejar este wrap.</p>
 */
public record NotificacionListResponseDTO(
        List<NotificacionResponseDTO> notificaciones,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas,
        long noLeidas
) {}
