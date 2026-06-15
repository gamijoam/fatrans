// com/tufondo/creditos/api/controller/CreditoController.java
package com.tufondo.creditos.api.controller;

import com.tufondo.creditos.application.dto.*;
import com.tufondo.creditos.application.usecase.*;
import com.tufondo.creditos.domain.model.enums.EstadoAmortizacion;
import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.fromString;

/**
 * REST Controller para el módulo de Créditos.
 * Implementa los 14 endpoints del API.
 * 
 * Seguridad implementada:
 * - Rate limiting por IP para /simulador
 * - Validación IDOR para endpoints de socio
 * - Optimistic locking en pagos
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Créditos", description = "Gestión de Solicitudes y Créditos")
public class CreditoController {

    // Use cases inyectados
    private final CrearSolicitudCreditoUseCase crearSolicitudUseCase;
    private final ObtenerSolicitudUseCase obtenerSolicitudUseCase;
    private final ListarSolicitudesPorSocioUseCase listarSolicitudesPorSocioUseCase;
    private final ListarSolicitudesCreditoAdminUseCase listarSolicitudesCreditoAdminUseCase;
    private final ListarTiposCreditoUseCase listarTiposCreditoUseCase;
    private final ObtenerTipoCreditoUseCase obtenerTipoCreditoUseCase;
    private final EvaluarSolicitudUseCase evaluarSolicitudUseCase;
    private final AprobarSolicitudCreditoUseCase aprobarSolicitudUseCase;
    private final RechazarSolicitudCreditoUseCase rechazarSolicitudUseCase;
    private final ObtenerPlanAmortizacionUseCase obtenerPlanAmortizacionUseCase;
    private final DesembolsaCreditoUseCase desembolsarCreditoUseCase;
    private final ListarCuotasUseCase listarCuotasUseCase;
    private final RegistrarPagoCuotaUseCase registrarPagoCuotaUseCase;
    private final ObtenerEstadoCreditoUseCase obtenerEstadoCreditoUseCase;
    private final SimularCreditoUseCase simularCreditoUseCase;

    // Endpoints de Solicitud de Crédito

    /**
     * 1. POST /creditos/solicitudes - Crear Solicitud de Crédito
     * Roles: SOCIO, ADMIN
     */
    @PostMapping("/creditos/solicitudes")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Crear solicitud de crédito")
    public ResponseEntity<SolicitudCreditoResponse> crearSolicitud(
            @Valid @RequestBody CrearSolicitudCreditoRequest request,
            Authentication authentication) {
        UUID socioId = extraerSocioId(authentication);
        SolicitudCreditoResponse response = crearSolicitudUseCase.ejecutar(request, socioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2. GET /creditos/solicitudes/{numeroSolicitud} - Consultar Solicitud
     * Validación IDOR: socio solo puede ver sus propias solicitudes
     * Roles: SOCIO (propias), ADMIN (todas)
     */
    @GetMapping("/creditos/solicitudes/{numeroSolicitud}")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Consultar solicitud de crédito")
    public ResponseEntity<SolicitudCreditoResponse> obtenerSolicitud(
            @PathVariable String numeroSolicitud,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        SolicitudCreditoResponse response = obtenerSolicitudUseCase.ejecutar(numeroSolicitud, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. GET /creditos/solicitudes/socio/{socioId} - Listar Solicitudes por Socio
     * Validación IDOR: socio solo puede ver sus propias solicitudes
     * Roles: SOCIO (propias), ADMIN (todas)
     */
    @GetMapping("/creditos/solicitudes/socio/{socioId}")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Listar solicitudes de crédito por socio")
    public ResponseEntity<Map<String, Object>> listarSolicitudesPorSocio(
            @PathVariable UUID socioId,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        List<SolicitudCreditoResponse> solicitudes = listarSolicitudesPorSocioUseCase.ejecutar(socioId, socioIdToken, isAdmin);
        return ResponseEntity.ok(Map.of(
            "socioId", socioId,
            "totalSolicitudes", solicitudes.size(),
            "solicitudes", solicitudes
        ));
    }

    /**
     * 3b. GET /admin/creditos/solicitudes - Listar TODAS las solicitudes (Admin)
     * Roles: ADMIN
     * Filtros: estado, fechaDesde, fechaHasta, montoMin, montoMax
     * Paginación: page, size, sort
     */
    @GetMapping("/admin/creditos/solicitudes")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Listar todas las solicitudes de crédito (Admin)")
    public ResponseEntity<Page<SolicitudCreditoAdminResponse>> listarTodasSolicitudes(
            @RequestParam(required = false) EstadoSolicitud estado,
            @RequestParam(required = false) LocalDateTime fechaDesde,
            @RequestParam(required = false) LocalDateTime fechaHasta,
            @RequestParam(required = false) BigDecimal montoMin,
            @RequestParam(required = false) BigDecimal montoMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            Authentication authentication) {

        String adminUser = authentication.getName();
        log.info("AUDIT: Admin {} consultando solicitudes - estado:{}, fechaDesde:{}, montoMin:{}",
                adminUser, estado, fechaDesde, montoMin);

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SolicitudCreditoAdminResponse> result = listarSolicitudesCreditoAdminUseCase.ejecutar(
                estado, fechaDesde, fechaHasta, montoMin, montoMax, pageable);

        log.info("AUDIT: Admin {} obtuve {} solicitudes", adminUser, result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    // Endpoints de Tipos de Crédito

    /**
     * 4. GET /creditos/tipos-credito - Listar Tipos de Crédito
     * Roles: SOCIO, ADMIN
     * Usa TipoCreditoPublicResponse para no exponer campos sensibles
     */
    @GetMapping("/creditos/tipos-credito")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Listar tipos de crédito disponibles")
    public ResponseEntity<Map<String, Object>> listarTiposCredito(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Map<String, Object> result = listarTiposCreditoUseCase.ejecutar(page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * 5. GET /creditos/tipos-credito/{id} - Consultar Tipo de Crédito
     * Roles: SOCIO, ADMIN
     */
    @GetMapping("/creditos/tipos-credito/{id}")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Consultar tipo de crédito específico")
    public ResponseEntity<TipoCreditoPublicResponse> obtenerTipoCredito(
            @PathVariable Long id,
            Authentication authentication) {
        TipoCreditoPublicResponse tipo = obtenerTipoCreditoUseCase.ejecutar(id);
        return ResponseEntity.ok(tipo);
    }

    // Endpoints de Evaluación

    /**
     * 6. POST /creditos/solicitudes/{numeroSolicitud}/evaluar - Evaluar Solicitud
     * Roles: ADMIN, SISTEMA
     */
    @PostMapping("/creditos/solicitudes/{numeroSolicitud}/evaluar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SISTEMA')")
    @Operation(summary = "Evaluar solicitud de crédito")
    public ResponseEntity<EvaluacionResponse> evaluarSolicitud(
            @PathVariable String numeroSolicitud,
            @Valid @RequestBody EvaluarSolicitudRequest request,
            Authentication authentication) {
        String evaluador = authentication.getName();
        EvaluacionResponse response = evaluarSolicitudUseCase.ejecutar(numeroSolicitud, request, evaluador);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 7. POST /creditos/solicitudes/{numeroSolicitud}/aprobar - Aprobar Crédito
     * Roles: ADMIN
     */
    @PostMapping("/creditos/solicitudes/{numeroSolicitud}/aprobar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Aprobar solicitud de crédito")
    public ResponseEntity<Map<String, Object>> aprobarSolicitud(
            @PathVariable String numeroSolicitud,
            @Valid @RequestBody AprobarRechazarRequest request,
            Authentication authentication) {
        Map<String, Object> response = aprobarSolicitudUseCase.ejecutar(numeroSolicitud, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 8. POST /creditos/solicitudes/{numeroSolicitud}/rechazar - Rechazar Crédito
     * Roles: ADMIN
     */
    @PostMapping("/creditos/solicitudes/{numeroSolicitud}/rechazar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Rechazar solicitud de crédito")
    public ResponseEntity<Map<String, Object>> rechazarSolicitud(
            @PathVariable String numeroSolicitud,
            @Valid @RequestBody AprobarRechazarRequest request,
            Authentication authentication) {
        Map<String, Object> response = rechazarSolicitudUseCase.ejecutar(numeroSolicitud, request);
        return ResponseEntity.ok(response);
    }

    // Endpoints de Plan de Amortización

    /**
     * 9. GET /creditos/solicitudes/{numeroSolicitud}/plan - Consultar Plan de Amortización
     * Validación IDOR: socio solo puede ver sus propios planes
     * Roles: SOCIO (propias), ADMIN (todas)
     */
    @GetMapping("/creditos/solicitudes/{numeroSolicitud}/plan")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Consultar plan de amortización")
    public ResponseEntity<PlanAmortizacionResponse> obtenerPlanAmortizacion(
            @PathVariable String numeroSolicitud,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        PlanAmortizacionResponse response = obtenerPlanAmortizacionUseCase.ejecutar(numeroSolicitud, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    // Endpoints de Desembolso

    /**
     * 10. POST /creditos/solicitudes/{numeroSolicitud}/desembolso - Desembolsar Crédito
     * Roles: ADMIN, SISTEMA
     */
    @PostMapping("/creditos/solicitudes/{numeroSolicitud}/desembolso")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SISTEMA')")
    @Operation(summary = "Desembolsar crédito")
    public ResponseEntity<Map<String, Object>> desembolsarCredito(
            @PathVariable String numeroSolicitud,
            @Valid @RequestBody DesembolsaRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        String ipOrigen = getClientIp(httpRequest);
        Map<String, Object> response = desembolsarCreditoUseCase.ejecutar(numeroSolicitud, request, ipOrigen);
        return ResponseEntity.ok(response);
    }

    // Endpoints de Pagos (Cuotas)

    /**
     * 11. GET /creditos/{numeroSolicitud}/cuotas - Listar Cuotas
     * Validación IDOR: socio solo puede ver sus propias cuotas
     * Roles: SOCIO (propias), ADMIN (todas)
     */
    @GetMapping("/creditos/{numeroSolicitud}/cuotas")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Listar cuotas del plan de amortización")
    public ResponseEntity<Map<String, Object>> listarCuotas(
            @PathVariable String numeroSolicitud,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) EstadoAmortizacion estado,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        Map<String, Object> response = listarCuotasUseCase.ejecutar(numeroSolicitud, socioIdToken, isAdmin, page, size, estado);
        return ResponseEntity.ok(response);
    }

    /**
     * 12. POST /creditos/cuotas/{cuotaId}/pago - Registrar Pago de Cuota
     * Implementa idempotencia y optimistic locking
     * Roles: CAJERO, ADMIN, SOCIO (auto-pago)
     */
    @PostMapping("/creditos/cuotas/{cuotaId}/pago")
    @PreAuthorize("hasAnyRole('CAJERO', 'ADMIN', 'SOCIO')")
    @Operation(summary = "Registrar pago de cuota")
    public ResponseEntity<PagoCuotaResponse> registrarPagoCuota(
            @PathVariable UUID cuotaId,
            @Valid @RequestBody PagoCuotaRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        boolean isCajero = esCajero(authentication);
        String ipOrigen = getClientIp(httpRequest);

        PagoCuotaResponse response = registrarPagoCuotaUseCase.ejecutar(
            cuotaId, request, socioIdToken, isAdmin, isCajero, ipOrigen);
        return ResponseEntity.ok(response);
    }

    // Endpoints de Estado de Crédito

    /**
     * 13. GET /creditos/{numeroSolicitud} - Consultar Estado de Crédito
     * Validación IDOR: socio solo puede ver sus propios créditos
     * Roles: SOCIO (propias), ADMIN (todas)
     */
    @GetMapping("/creditos/{numeroSolicitud}")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Consultar estado de crédito")
    public ResponseEntity<CreditoResponse> obtenerEstadoCredito(
            @PathVariable String numeroSolicitud,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);
        CreditoResponse response = obtenerEstadoCreditoUseCase.ejecutar(numeroSolicitud, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    // Endpoint de Simulación

    /**
     * 14. POST /simulador - Simular Crédito
     * Rate limiting: 10 requests/min por IP
     * Roles: SOCIO, ADMIN
     */
    @PostMapping("/simulador")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Simular crédito (rate limited)")
    public ResponseEntity<SimulacionResponse> simularCredito(
            @Valid @RequestBody SimulacionRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        String ipOrigen = getClientIp(httpRequest);
        SimulacionResponse response = simularCreditoUseCase.ejecutar(request, ipOrigen);
        return ResponseEntity.ok(response);
    }

    // ==================== Métodos helper ====================

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
                              a.equals(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")) ||
                              a.equals(new SimpleGrantedAuthority("ROLE_SISTEMA")));
    }

    private boolean esCajero(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.equals(new SimpleGrantedAuthority("ROLE_CAJERO")));
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
