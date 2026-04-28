package com.tufondo.admin.presentation.controller;

import com.tufondo.admin.application.dto.AuditLogResponse;
import com.tufondo.admin.application.usecase.ConsultarAuditoriaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/auditoria")
@RequiredArgsConstructor
@Tag(name = "Admin Auditoría", description = "Consultar logs de auditoría del sistema")
@SecurityRequirement(name = "bearerAuth")
public class AdminAuditoriaController {

    private final ConsultarAuditoriaUseCase consultarAuditoriaUseCase;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or @permisoService.tienePermiso(authentication, T(com.tufondo.auth.domain.model.enums.Permiso).VER_AUDITORIA)")
    @Operation(summary = "Listar logs de auditoría con filtros")
    public ResponseEntity<Map<String, Object>> listarAuditoria(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID usuarioId,
            @RequestParam(required = false) String tipoEvento,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {

        Instant fechaInicioInstant = fechaInicio != null
                ? fechaInicio.atStartOfDay(ZoneId.systemDefault()).toInstant()
                : null;
        Instant fechaFinInstant = fechaFin != null
                ? fechaFin.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
                : null;

        Map<String, Object> result = consultarAuditoriaUseCase.listarAuditoria(
                page, size, usuarioId, tipoEvento, fechaInicioInstant, fechaFinInstant);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/recientes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or @permisoService.tienePermiso(authentication, T(com.tufondo.auth.domain.model.enums.Permiso).VER_AUDITORIA)")
    @Operation(summary = "Listar últimos 100 eventos de auditoría")
    public ResponseEntity<List<AuditLogResponse>> listarRecientes(
            @RequestParam(defaultValue = "50") int limit) {

        List<AuditLogResponse> recientes = consultarAuditoriaUseCase.listarRecientes(limit);
        return ResponseEntity.ok(recientes);
    }

    @GetMapping("/tipos-evento")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Listar tipos de eventos disponibles")
    public ResponseEntity<List<String>> listarTiposEventos() {
        List<String> tipos = consultarAuditoriaUseCase.listarTiposEventos();
        return ResponseEntity.ok(tipos);
    }
}