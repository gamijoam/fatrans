package com.tufondo.admin.presentation.controller;

import com.tufondo.admin.application.dto.AdminUsuarioRequest;
import com.tufondo.admin.application.dto.AdminUsuarioResponse;
import com.tufondo.admin.application.usecase.GestionarAdminUseCase;
import com.tufondo.admin.application.usecase.GestionarAdminUseCase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/usuarios")
@RequiredArgsConstructor
@Tag(name = "Admin Gestión de Usuarios", description = "Gestión de usuarios administradores del sistema")
@SecurityRequirement(name = "bearerAuth")
public class AdminUsuarioController {

    private final GestionarAdminUseCase gestionarAdminUseCase;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<List<AdminUsuarioResponse>> listarTodos() {
        List<AdminUsuarioResponse> usuarios = gestionarAdminUseCase.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/admins")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Listar solo administradores")
    public ResponseEntity<List<AdminUsuarioResponse>> listarAdmins() {
        List<AdminUsuarioResponse> admins = gestionarAdminUseCase.listarAdmins();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Obtener un usuario por ID")
    public ResponseEntity<AdminUsuarioResponse> obtenerPorId(@PathVariable UUID id) {
        AdminUsuarioResponse usuario = gestionarAdminUseCase.obtenerPorId(id);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Crear un nuevo usuario administrador")
    public ResponseEntity<AdminUsuarioResponse> crear(
            @Valid @RequestBody AdminUsuarioRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID adminCreadorId = parseAdminId(authentication);
        String ipAddress = getClientIp(httpRequest);
        AdminUsuarioResponse usuario = gestionarAdminUseCase.crear(request, adminCreadorId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Actualizar un usuario existente")
    public ResponseEntity<AdminUsuarioResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AdminUsuarioRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID adminActualizadorId = parseAdminId(authentication);
        String ipAddress = getClientIp(httpRequest);
        AdminUsuarioResponse usuario = gestionarAdminUseCase.actualizar(id, request, adminActualizadorId, ipAddress);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/{id}/activar")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Activar un usuario")
    public ResponseEntity<AdminUsuarioResponse> activar(
            @PathVariable UUID id,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID adminActivadorId = parseAdminId(authentication);
        String ipAddress = getClientIp(httpRequest);
        AdminUsuarioResponse usuario = gestionarAdminUseCase.activar(id, adminActivadorId, ipAddress);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Desactivar un usuario")
    public ResponseEntity<AdminUsuarioResponse> desactivar(
            @PathVariable UUID id,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID adminDesactivadorId = parseAdminId(authentication);
        String ipAddress = getClientIp(httpRequest);
        AdminUsuarioResponse usuario = gestionarAdminUseCase.desactivar(id, adminDesactivadorId, ipAddress);
        return ResponseEntity.ok(usuario);
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NombreUsuarioYaExisteException.class)
    public ResponseEntity<Map<String, String>> handleNombreUsuarioYaExiste(NombreUsuarioYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CorreoYaExisteException.class)
    public ResponseEntity<Map<String, String>> handleCorreoYaExiste(CorreoYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RolInvalidoException.class)
    public ResponseEntity<Map<String, String>> handleRolInvalido(RolInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoSePuedeDesactivarSuperAdminException.class)
    public ResponseEntity<Map<String, String>> handleNoSePuedeDesactivarSuperAdmin(NoSePuedeDesactivarSuperAdminException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(SessioInvalidaException.class)
    public ResponseEntity<Map<String, String>> handleSesionInvalida(SessioInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Sesión inválida"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private UUID parseAdminId(Authentication authentication) {
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new SessioInvalidaException();
        }
    }

    public static class SessioInvalidaException extends RuntimeException {
    }
}