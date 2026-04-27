package com.tufondo.tipocambio.presentation.controller;

import com.tufondo.tipocambio.application.dto.TipoCambioRequest;
import com.tufondo.tipocambio.application.dto.TipoCambioResponse;
import com.tufondo.tipocambio.application.usecase.ConsultarTipoCambioUseCase;
import com.tufondo.tipocambio.application.usecase.GestionarTipoCambioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/tipos-cambio")
@RequiredArgsConstructor
@Tag(name = "Tipos de Cambio", description = "Gestión de tasas VES/USD")
@SecurityRequirement(name = "bearerAuth")
public class TipoCambioController {

    private final ConsultarTipoCambioUseCase consultarUseCase;
    private final GestionarTipoCambioUseCase gestionarUseCase;

    @GetMapping("/actual")
    @Operation(summary = "Obtener tasa de cambio actual")
    public ResponseEntity<TipoCambioResponse> obtenerTasaActual() {
        Optional<TipoCambioResponse> tasa = consultarUseCase.obtenerTasaActual();
        return tasa.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/fecha/{fecha}")
    @Operation(summary = "Obtener tasa de cambio por fecha")
    public ResponseEntity<TipoCambioResponse> obtenerPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        Optional<TipoCambioResponse> tasa = consultarUseCase.obtenerPorFecha(fecha);
        return tasa.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/historial")
    @Operation(summary = "Listar historial de tasas de cambio")
    public ResponseEntity<List<TipoCambioResponse>> listarHistorial(
            @RequestParam(defaultValue = "30") int limit) {
        List<TipoCambioResponse> historial = consultarUseCase.listarHistorial(limit);
        return ResponseEntity.ok(historial);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Listar todas las tasas de cambio")
    public ResponseEntity<List<TipoCambioResponse>> listarTodos() {
        List<TipoCambioResponse> tasas = consultarUseCase.listarTodos();
        return ResponseEntity.ok(tasas);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Obtener tipo de cambio por ID")
    public ResponseEntity<TipoCambioResponse> obtenerPorId(@PathVariable UUID id) {
        Optional<TipoCambioResponse> tasa = gestionarUseCase.obtenerPorId(id);
        return tasa.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Crear nuevo tipo de cambio")
    public ResponseEntity<TipoCambioResponse> crear(
            @Valid @RequestBody TipoCambioRequest request,
            Authentication authentication,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        try {
            UUID adminId = parseAdminId(authentication);
            TipoCambioResponse response = gestionarUseCase.crear(request, adminId, ipAddress);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (GestionarTipoCambioUseCase.TipoCambioYaExisteException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (GestionarTipoCambioUseCase.TasaInvalidaException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Actualizar tipo de cambio")
    public ResponseEntity<TipoCambioResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody TipoCambioRequest request,
            Authentication authentication,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        try {
            UUID adminId = parseAdminId(authentication);
            TipoCambioResponse response = gestionarUseCase.actualizar(id, request, adminId, ipAddress);
            return ResponseEntity.ok(response);
        } catch (GestionarTipoCambioUseCase.TipoCambioNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        } catch (GestionarTipoCambioUseCase.TipoCambioYaExisteException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (GestionarTipoCambioUseCase.TasaInvalidaException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Eliminar tipo de cambio")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            Authentication authentication,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
        try {
            UUID adminId = parseAdminId(authentication);
            gestionarUseCase.eliminar(id, adminId, ipAddress);
            return ResponseEntity.noContent().build();
        } catch (GestionarTipoCambioUseCase.TipoCambioNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private UUID parseAdminId(Authentication authentication) {
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("ID de administrador inválido en token");
        }
    }
}