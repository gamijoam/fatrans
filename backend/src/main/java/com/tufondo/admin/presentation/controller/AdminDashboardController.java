package com.tufondo.admin.presentation.controller;

import com.tufondo.admin.application.dto.DashboardEstadisticasResponse;
import com.tufondo.admin.application.usecase.ObtenerDashboardEstadisticasUseCase;
import com.tufondo.auth.infrastructure.service.SecurityAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Endpoints del dashboard administrativo")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final ObtenerDashboardEstadisticasUseCase obtenerDashboardEstadisticasUseCase;
    private final SecurityAuditService auditService;

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Obtener estadísticas completas del dashboard")
    public ResponseEntity<DashboardEstadisticasResponse> obtenerEstadisticas(
            HttpServletRequest request,
            Authentication authentication) {

        String userId = authentication.getName();
        String clientIp = getClientIp(request);
        String rol = authentication.getAuthorities().iterator().next().toString();

        auditService.logDashboardAcceso(userId, clientIp, rol);

        log.info("Estadísticas de dashboard solicitadas por usuario: {} - Rol: {}",
                userId, rol);

        DashboardEstadisticasResponse estadisticas = obtenerDashboardEstadisticasUseCase.ejecutar();
        return ResponseEntity.ok(estadisticas);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}