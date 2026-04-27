package com.tufondo.creditos.presentation.controller;

import com.tufondo.creditos.application.dto.TipoCreditoRequest;
import com.tufondo.creditos.application.dto.TipoCreditoResponse;
import com.tufondo.creditos.application.usecase.GestionarTipoCreditoUseCase;
import com.tufondo.creditos.application.usecase.GestionarTipoCreditoUseCase.CodigoTipoCreditoYaExisteException;
import com.tufondo.creditos.application.usecase.GestionarTipoCreditoUseCase.MontoInvalidoException;
import com.tufondo.creditos.application.usecase.GestionarTipoCreditoUseCase.PlazoInvalidoException;
import com.tufondo.creditos.application.usecase.GestionarTipoCreditoUseCase.TipoCreditoNoEncontradoException;
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
@RequestMapping("/api/v1/admin/tipos-credito")
@RequiredArgsConstructor
@Tag(name = "Admin Tipos de Crédito", description = "Gestión administrativa de tipos de crédito")
@SecurityRequirement(name = "bearerAuth")
public class AdminTipoCreditoController {

    private final GestionarTipoCreditoUseCase gestionarTipoCreditoUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Listar todos los tipos de crédito")
    public ResponseEntity<List<TipoCreditoResponse>> listarTodos() {
        List<TipoCreditoResponse> tipos = gestionarTipoCreditoUseCase.listarTodos();
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Obtener un tipo de crédito por ID")
    public ResponseEntity<TipoCreditoResponse> obtenerPorId(@PathVariable Long id) {
        TipoCreditoResponse tipo = gestionarTipoCreditoUseCase.obtenerPorId(id);
        return ResponseEntity.ok(tipo);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Crear un nuevo tipo de crédito")
    public ResponseEntity<TipoCreditoResponse> crear(
            @Valid @RequestBody TipoCreditoRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID adminId = parseAdminId(authentication);
        String ipAddress = getClientIp(httpRequest);
        TipoCreditoResponse tipo = gestionarTipoCreditoUseCase.crear(request, adminId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(tipo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Actualizar un tipo de crédito existente")
    public ResponseEntity<TipoCreditoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoCreditoRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID adminId = parseAdminId(authentication);
        String ipAddress = getClientIp(httpRequest);
        TipoCreditoResponse tipo = gestionarTipoCreditoUseCase.actualizar(id, request, adminId, ipAddress);
        return ResponseEntity.ok(tipo);
    }

    @PostMapping("/{id}/activar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Activar un tipo de crédito")
    public ResponseEntity<TipoCreditoResponse> activar(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID adminId = parseAdminId(authentication);
        String ipAddress = getClientIp(httpRequest);
        TipoCreditoResponse tipo = gestionarTipoCreditoUseCase.activar(id, adminId, ipAddress);
        return ResponseEntity.ok(tipo);
    }

    @PostMapping("/{id}/desactivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Desactivar un tipo de crédito")
    public ResponseEntity<TipoCreditoResponse> desactivar(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID adminId = parseAdminId(authentication);
        String ipAddress = getClientIp(httpRequest);
        TipoCreditoResponse tipo = gestionarTipoCreditoUseCase.desactivar(id, adminId, ipAddress);
        return ResponseEntity.ok(tipo);
    }

    @ExceptionHandler(TipoCreditoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleTipoCreditoNoEncontrado(TipoCreditoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CodigoTipoCreditoYaExisteException.class)
    public ResponseEntity<Map<String, String>> handleCodigoYaExiste(CodigoTipoCreditoYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(PlazoInvalidoException.class)
    public ResponseEntity<Map<String, String>> handlePlazoInvalido(PlazoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MontoInvalidoException.class)
    public ResponseEntity<Map<String, String>> handleMontoInvalido(MontoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
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

    @ExceptionHandler(SessioInvalidaException.class)
    public ResponseEntity<Map<String, String>> handleSesionInvalida(SessioInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Sesión inválida"));
    }

    public static class SessioInvalidaException extends RuntimeException {
    }
}