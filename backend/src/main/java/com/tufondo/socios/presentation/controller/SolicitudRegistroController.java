// 📁 com/tufondo/socios/presentation/controller/SolicitudRegistroController.java
package com.tufondo.socios.presentation.controller;

import com.tufondo.socios.application.dto.*;
import com.tufondo.socios.application.usecase.*;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @Valid @RequestBody SolicitudRegistroRequestDTO request) {
        
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
    @PreAuthorize("hasRole('ADMIN')")
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
    
    @PostMapping("/solicitudes/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar una solicitud de registro (Admin)")
    public ResponseEntity<Map<String, Object>> aprobarSolicitud(
            @PathVariable UUID id,
            @RequestBody(required = false) AprobarSolicitudRequestDTO request,
            @RequestHeader("X-Admin-Id") String adminId) {
        
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Rechazar una solicitud de registro (Admin)")
    public ResponseEntity<Map<String, Object>> rechazarSolicitud(
            @PathVariable UUID id,
            @Valid @RequestBody RechazarSolicitudRequestDTO request,
            @RequestHeader("X-Admin-Id") String adminId) {
        
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