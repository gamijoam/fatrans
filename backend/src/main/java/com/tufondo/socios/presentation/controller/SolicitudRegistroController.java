// 📁 com/tufondo/socios/presentation/controller/SolicitudRegistroController.java
package com.tufondo.socios.presentation.controller;

import com.tufondo.socios.application.dto.*;
import com.tufondo.socios.application.usecase.*;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/socios")
@RequiredArgsConstructor
@Tag(name = "Solicitudes de Registro", description = "Gestión de solicitudes de registro de nuevos socios")
public class SolicitudRegistroController {
    
    private static final int MAX_PAGE_SIZE = 100;
    
    private final CrearSolicitudRegistroUseCase crearSolicitudUseCase;
    private final ListarSolicitudesUseCase listarSolicitudesUseCase;
    private final AprobarSolicitudUseCase aprobarSolicitudUseCase;
    private final RechazarSolicitudUseCase rechazarSolicitudUseCase;
    
    @PostMapping("/solicitud")
    @Operation(summary = "Crear una nueva solicitud de registro")
    public ResponseEntity<Map<String, Object>> crearSolicitud(
            @Valid @RequestBody SolicitudRegistroRequestDTO request,
            HttpServletRequest httpRequest) {

        // Auditoría LOPDP: priorizamos los valores ya capturados por el BFF
        // (que parsea correctamente x-forwarded-for) y caemos al request del
        // servlet como fallback. Nunca pisamos un valor explícito del BFF con
        // el remoteAddr crudo, porque éste puede ser la IP del proxy/Nginx
        // en producción.
        if (isBlank(request.getIpRegistro())) {
            request.setIpRegistro(resolveClientIp(httpRequest));
        }
        if (isBlank(request.getUserAgentRegistro())) {
            String ua = httpRequest.getHeader("User-Agent");
            if (ua != null && ua.length() > 500) {
                ua = ua.substring(0, 500);
            }
            request.setUserAgentRegistro(ua);
        }

        SolicitudRegistroResponseDTO response = crearSolicitudUseCase.ejecutar(request);
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Solicitud enviada exitosamente");
        body.put("data", Map.of(
            "solicitudId", response.getId(),
            "estado", response.getEstado()
        ));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }
    
    @GetMapping("/solicitudes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Listar solicitudes de registro (Admin)")
    public ResponseEntity<Map<String, Object>> listarSolicitudes(
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        int safeSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, safeSize);
        
        Page<SolicitudRegistroResponseDTO> solicitudes = listarSolicitudesUseCase.ejecutar(estado, pageable);
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("data", Map.of(
            "solicitudes", solicitudes.getContent(),
            "totalElementos", solicitudes.getTotalElements(),
            "totalPaginas", solicitudes.getTotalPages(),
            "paginaActual", solicitudes.getNumber()
        ));
        
        return ResponseEntity.ok(body);
    }
    
    /**
     * Resuelve la IP del cliente honrando proxies (X-Forwarded-For). Toma el
     * primer hop del header (el cliente original) y cae a remoteAddr cuando
     * el header no está presente.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            String first = (comma >= 0 ? xff.substring(0, comma) : xff).trim();
            if (!first.isEmpty()) {
                return truncate(first, 45);
            }
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return truncate(xRealIp.trim(), 45);
        }
        return truncate(request.getRemoteAddr(), 45);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    @PostMapping("/solicitudes/{id}/aprobar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Aprobar una solicitud de registro (Admin)")
    public ResponseEntity<Map<String, Object>> aprobarSolicitud(
            @PathVariable UUID id,
            @RequestBody(required = false) AprobarSolicitudRequestDTO request) {

        String adminId = SecurityContextHolder.getContext().getAuthentication().getName();
        SolicitudRegistroResponseDTO response = aprobarSolicitudUseCase.ejecutar(id, request, adminId);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Solicitud aprobada exitosamente");
        body.put("data", Map.of(
            "solicitudId", response.getId(),
            "estado", response.getEstado()
        ));

        return ResponseEntity.ok(body);
    }
    
    @PostMapping("/solicitudes/{id}/rechazar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Rechazar una solicitud de registro (Admin)")
    public ResponseEntity<Map<String, Object>> rechazarSolicitud(
            @PathVariable UUID id,
            @Valid @RequestBody RechazarSolicitudRequestDTO request) {

        String adminId = SecurityContextHolder.getContext().getAuthentication().getName();
        SolicitudRegistroResponseDTO response = rechazarSolicitudUseCase.ejecutar(id, request, adminId);

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Solicitud rechazada");
        body.put("data", Map.of(
            "solicitudId", response.getId(),
            "estado", response.getEstado()
        ));

        return ResponseEntity.ok(body);
    }
}