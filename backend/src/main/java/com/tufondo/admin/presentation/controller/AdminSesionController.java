package com.tufondo.admin.presentation.controller;

import com.tufondo.admin.application.dto.SesionInfoResponse;
import com.tufondo.admin.application.dto.SesionInvalidationResponse;
import com.tufondo.auth.domain.model.Sesion;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.repository.SesionRepository;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.service.EmailService;
import com.tufondo.auth.infrastructure.service.SecurityAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/sesiones")
@RequiredArgsConstructor
@Tag(name = "Admin Sesiones", description = "Endpoints para gestión de sesiones de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class AdminSesionController {

    private final SesionRepository sesionRepository;
    private final UsuarioRepository usuarioRepository;
    private final SecurityAuditService auditService;
    private final EmailService emailService;

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Listar sesiones activas de un usuario")
    public ResponseEntity<List<SesionInfoResponse>> listarSesionesActivas(
            @PathVariable UUID usuarioId) {

        List<Sesion> sesiones = sesionRepository.buscarSesionesActivasPorUsuario(usuarioId);

        List<SesionInfoResponse> response = sesiones.stream()
                .map(this::toSesionInfoResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/usuario/{usuarioId}/invalidar-todas")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Invalidar todas las sesiones de un usuario")
    public ResponseEntity<SesionInvalidationResponse> invalidarTodasLasSesiones(
            @PathVariable UUID usuarioId,
            HttpServletRequest request,
            Authentication authentication) {

        String adminId = authentication.getName();
        String clientIp = getClientIp(request);

        List<Sesion> sesionesActivas = sesionRepository.buscarSesionesActivasPorUsuario(usuarioId);
        int cantidad = sesionesActivas.size();

        sesionRepository.invalidarTodasPorUsuario(usuarioId);

        auditService.logSesionesInvalidadas(usuarioId, clientIp, UUID.fromString(adminId), cantidad);

        usuarioRepository.buscarPorId(usuarioId).ifPresent(usuario -> {
            emailService.enviarNotificacionSesionesInvalidadas(
                    usuario.correoElectronico(),
                    usuario.nombreUsuario(),
                    cantidad,
                    clientIp
            );
        });

        log.info("Sesiones invalidadas para usuario {} por admin {} - cantidad: {}",
                usuarioId, adminId, cantidad);

        return ResponseEntity.ok(new SesionInvalidationResponse(
                usuarioId,
                cantidad,
                "Todas las sesiones han sido invalidadas exitosamente"
        ));
    }

    @PostMapping("/{sesionId}/invalidar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Invalidar una sesión específica")
    public ResponseEntity<Void> invalidarSesion(
            @PathVariable String sesionId,
            HttpServletRequest request,
            Authentication authentication) {

        String adminId = authentication.getName();
        String clientIp = getClientIp(request);

        sesionRepository.invalidarPorTokenId(sesionId);

        auditService.logSesionIndividualInvalidadas(
                null,
                clientIp,
                UUID.fromString(adminId),
                sesionId
        );

        log.info("Sesión {} invalidada por admin {} desde IP {}",
                sesionId, adminId, clientIp);

        return ResponseEntity.noContent().build();
    }

    private SesionInfoResponse toSesionInfoResponse(Sesion sesion) {
        return new SesionInfoResponse(
                sesion.id(),
                null,
                null,
                sesion.ultimaActividad(),
                sesion.fechaCreacion(),
                sesion.refreshTokenExpiracion(),
                sesion.activa()
        );
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