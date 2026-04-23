// com/tufondo/ahorros/api/controller/AhorroController.java
package com.tufondo.ahorros.api.controller;

import com.tufondo.ahorros.application.dto.*;
import com.tufondo.ahorros.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.fromString;

/**
 * REST Controller para el módulo de Ahorros.
 * Implementa los 12 endpoints del API.
 */
@RestController
@RequestMapping("/api/v1/cuentas")
@RequiredArgsConstructor
@Tag(name = "Ahorros", description = "Gestión de Cuentas de Ahorro")
public class AhorroController {

    private final CrearCuentaAhorroUseCase crearCuentaUseCase;
    private final ObtenerCuentaUseCase obtenerCuentaUseCase;
    private final ListarCuentasPorSocioUseCase listarCuentasPorSocioUseCase;
    private final ConsultarSaldoUseCase consultarSaldoUseCase;
    private final RealizarDepositoUseCase realizarDepositoUseCase;
    private final RealizarRetiroUseCase realizarRetiroUseCase;
    private final ListarMovimientosUseCase listarMovimientosUseCase;
    private final ObtenerMovimientoDetalleUseCase obtenerMovimientoDetalleUseCase;
    private final CalcularRendimientoUseCase calcularRendimientoUseCase;
    private final ListarRendimientosUseCase listarRendimientosUseCase;
    private final CerrarCuentaUseCase cerrarCuentaUseCase;
    private final CalcularRendimientosBatchUseCase calcularRendimientosBatchUseCase;

    // 1. POST /cuentas - Crear Cuenta
    @PostMapping
    @Operation(summary = "Crear cuenta de ahorro")
    public ResponseEntity<CuentaAhorroResponse> crearCuenta(
            @Valid @RequestBody CreateCuentaAhorroRequest request,
            Authentication authentication) {
        CuentaAhorroResponse response = crearCuentaUseCase.ejecutar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. GET /cuentas/{numeroCuenta} - Consultar Cuenta
    @GetMapping("/{numeroCuenta}")
    @Operation(summary = "Consultar cuenta por número")
    public ResponseEntity<CuentaAhorroResponse> obtenerCuenta(
            @PathVariable String numeroCuenta,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        CuentaAhorroResponse response = obtenerCuentaUseCase.ejecutar(numeroCuenta, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    // 3. GET /cuentas/socio/{socioId} - Listar Cuentas por Socio
    @GetMapping("/socio/{socioId}")
    @Operation(summary = "Listar cuentas por socio")
    public ResponseEntity<CuentasPorSocioResponse> listarCuentasPorSocio(
            @PathVariable UUID socioId,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        CuentasPorSocioResponse response = listarCuentasPorSocioUseCase.ejecutar(socioId, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    // 4. GET /cuentas/{numeroCuenta}/saldo - Consultar Saldo
    @GetMapping("/{numeroCuenta}/saldo")
    @Operation(summary = "Consultar saldo de cuenta")
    public ResponseEntity<SaldoResponse> consultarSaldo(
            @PathVariable String numeroCuenta,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        SaldoResponse response = consultarSaldoUseCase.ejecutar(numeroCuenta, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    // 5. POST /cuentas/{numeroCuenta}/depositos - Realizar Depósito
    @PostMapping("/{numeroCuenta}/depositos")
    @Operation(summary = "Realizar depósito")
    public ResponseEntity<MovimientoResponse> realizarDeposito(
            @PathVariable String numeroCuenta,
            @Valid @RequestBody DepositoRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        String ipOrigen = getClientIp(httpRequest);
        String sessionId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();

        MovimientoResponse response = realizarDepositoUseCase.ejecutar(
                numeroCuenta, request, socioIdToken, isAdmin, ipOrigen, sessionId, requestId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 6. POST /cuentas/{numeroCuenta}/retiros - Realizar Retiro
    @PostMapping("/{numeroCuenta}/retiros")
    @Operation(summary = "Realizar retiro")
    public ResponseEntity<MovimientoResponse> realizarRetiro(
            @PathVariable String numeroCuenta,
            @Valid @RequestBody RetiroRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        String ipOrigen = getClientIp(httpRequest);
        String sessionId = UUID.randomUUID().toString();
        String requestId = UUID.randomUUID().toString();
        
        MovimientoResponse response = realizarRetiroUseCase.ejecutar(
                numeroCuenta, request, socioIdToken, isAdmin, ipOrigen, sessionId, requestId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 7. GET /cuentas/{numeroCuenta}/movimientos - Listar Movimientos
    @GetMapping("/{numeroCuenta}/movimientos")
    @Operation(summary = "Listar movimientos de cuenta")
    public ResponseEntity<MovimientosListResponse> listarMovimientos(
            @PathVariable String numeroCuenta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        
        MovimientosListResponse response = listarMovimientosUseCase.ejecutar(
                numeroCuenta, socioIdToken, isAdmin, page, size, fechaInicio, fechaFin, null);
        return ResponseEntity.ok(response);
    }

    // 8. GET /cuentas/{numeroCuenta}/movimientos/{numeroOperacion} - Detalle Movimiento
    @GetMapping("/{numeroCuenta}/movimientos/{numeroOperacion}")
    @Operation(summary = "Obtener detalle de movimiento")
    public ResponseEntity<MovimientoResponse> obtenerMovimientoDetalle(
            @PathVariable String numeroCuenta,
            @PathVariable String numeroOperacion,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        MovimientoResponse response = obtenerMovimientoDetalleUseCase.ejecutar(
                numeroCuenta, numeroOperacion, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    // 9. POST /cuentas/{numeroCuenta}/rendimientos/calcular - Calcular Rendimiento
    @PostMapping("/{numeroCuenta}/rendimientos/calcular")
    @Operation(summary = "Calcular rendimiento de cuenta")
    public ResponseEntity<RendimientoResponse> calcularRendimiento(
            @PathVariable String numeroCuenta,
            @Valid @RequestBody CalcularRendimientoRequest request,
            Authentication authentication) {
        RendimientoResponse response = calcularRendimientoUseCase.ejecutar(numeroCuenta, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 10. GET /cuentas/{numeroCuenta}/rendimientos - Listar Rendimientos
    @GetMapping("/{numeroCuenta}/rendimientos")
    @Operation(summary = "Listar rendimientos de cuenta")
    public ResponseEntity<RendimientosListResponse> listarRendimientos(
            @PathVariable String numeroCuenta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        RendimientosListResponse response = listarRendimientosUseCase.ejecutar(
                numeroCuenta, socioIdToken, isAdmin, page, size);
        return ResponseEntity.ok(response);
    }

    // 11. DELETE /cuentas/{numeroCuenta} - Cerrar Cuenta
    @DeleteMapping("/{numeroCuenta}")
    @Operation(summary = "Cerrar cuenta de ahorro")
    public ResponseEntity<CerrarCuentaResponse> cerrarCuenta(
            @PathVariable String numeroCuenta,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        CerrarCuentaResponse response = cerrarCuentaUseCase.ejecutar(numeroCuenta, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    // 12. POST /rendimientos/calcular-batch - Calcular Batch
    @PostMapping("/rendimientos/calcular-batch")
    @Operation(summary = "Calcular rendimientos en batch")
    public ResponseEntity<CalcularBatchResponse> calcularRendimientosBatch(
            @Valid @RequestBody CalcularBatchRequest request,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        CalcularBatchResponse response = calcularRendimientosBatchUseCase.ejecutar(request, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    // Helper methods
    private UUID extraerSocioId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.tufondo.auth.infrastructure.security.AuthenticatedUser authUser) {
            UUID socioId = authUser.getSocioId();
            if (socioId != null) {
                return socioId;
            }
        }
        return fromString(authentication.getName());
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.equals(new SimpleGrantedAuthority("ROLE_ADMIN")) ||
                              a.equals(new SimpleGrantedAuthority("ROLE_SISTEMA")));
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