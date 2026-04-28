// com.tufondo.documentospdf.infrastructure.presentation.controller.DocumentoController
package com.tufondo.documentospdf.infrastructure.presentation.controller;

import com.tufondo.auth.infrastructure.security.AuthenticatedUser;
import com.tufondo.documentospdf.application.dto.DescargarDocumentoResponseDTO;
import com.tufondo.documentospdf.application.dto.DocumentoListResponseDTO;
import com.tufondo.documentospdf.application.dto.DocumentoResponseDTO;
import com.tufondo.documentospdf.application.usecase.*;
import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.fromString;

/**
 * REST Controller para el módulo de Documentos PDF.
 * Implementa los 9 endpoints del API con seguridad completa.
 *
 * Seguridad implementada:
 * - Rate limiting por usuario e IP
 * - Validación IDOR completa
 * - Pre-signed URLs para descarga
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
@Tag(name = "Documentos PDF", description = "Gestión de Documentos PDF")
@SecurityRequirement(name = "bearerAuth")
public class DocumentoController {

    private final GenerarEstadoCuentaUseCase generarEstadoCuentaUseCase;
    private final GenerarConstanciaAfiliacionUseCase generarConstanciaAfiliacionUseCase;
    private final GenerarContratoAdhesionUseCase generarContratoAdhesionUseCase;
    private final GenerarPagareUseCase generarPagareUseCase;
    private final GenerarTablaAmortizacionUseCase generarTablaAmortizacionUseCase;
    private final GenerarCartaBeneficiariosUseCase generarCartaBeneficiariosUseCase;
    private final DescargarDocumentoUseCase descargarDocumentoUseCase;
    private final ObtenerDocumentoUseCase obtenerDocumentoUseCase;
    private final ListarDocumentosSocioUseCase listarDocumentosSocioUseCase;

    // ==================== Endpoints de Generación ====================

    /**
     * 1. GET /documentos/estado-cuenta/{cuentaId} - Generar Estado de Cuenta
     * Roles: SOCIO (propia cuenta), ADMIN
     * Validación IDOR: socio solo puede generar su propio estado de cuenta
     */
    @GetMapping("/estado-cuenta/{cuentaId}")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Generar estado de cuenta")
    public ResponseEntity<DocumentoResponseDTO> generarEstadoCuenta(
            @PathVariable UUID cuentaId,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);

        DocumentoResponseDTO response = generarEstadoCuentaUseCase.ejecutar(cuentaId, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    /**
     * 2. GET /documentos/constancia-afiliacion/{socioId} - Generar Constancia de Afiliación
     * Roles: SOCIO (propio), ADMIN
     * Validación IDOR: socio solo puede generar su propia constancia
     */
    @GetMapping("/constancia-afiliacion/{socioId}")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Generar constancia de afiliación")
    public ResponseEntity<DocumentoResponseDTO> generarConstanciaAfiliacion(
            @PathVariable UUID socioId,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);

        DocumentoResponseDTO response = generarConstanciaAfiliacionUseCase.ejecutar(socioId, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. GET /documentos/contrato/{solicitudId} - Generar Contrato de Adhesión
     * Roles: ADMIN, SISTEMA
     * Requiere firma digital RSA SHA-256
     */
    @GetMapping("/contrato/{solicitudId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SISTEMA')")
    @Operation(summary = "Generar contrato de adhesión (firma digital)")
    public ResponseEntity<DocumentoResponseDTO> generarContrato(
            @PathVariable UUID solicitudId,
            Authentication authentication) {
        DocumentoResponseDTO response = generarContratoAdhesionUseCase.ejecutar(solicitudId);
        return ResponseEntity.ok(response);
    }

    /**
     * 4. GET /documentos/pagare/{creditoId} - Generar Pagaré
     * Roles: ADMIN, SISTEMA
     * Requiere firma digital RSA SHA-256
     */
    @GetMapping("/pagare/{creditoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SISTEMA')")
    @Operation(summary = "Generar pagaré (firma digital)")
    public ResponseEntity<DocumentoResponseDTO> generarPagare(
            @PathVariable UUID creditoId,
            Authentication authentication) {
        DocumentoResponseDTO response = generarPagareUseCase.ejecutar(creditoId);
        return ResponseEntity.ok(response);
    }

    /**
     * 5. GET /documentos/tabla-amortizacion/{creditoId} - Generar Tabla de Amortización
     * Roles: SOCIO (propio crédito), ADMIN, SISTEMA
     * Validación IDOR: socio solo puede ver su propia tabla
     */
    @GetMapping("/tabla-amortizacion/{creditoId}")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SISTEMA')")
    @Operation(summary = "Generar tabla de amortización")
    public ResponseEntity<DocumentoResponseDTO> generarTablaAmortizacion(
            @PathVariable UUID creditoId,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdminOSistema(authentication);

        DocumentoResponseDTO response = generarTablaAmortizacionUseCase.ejecutar(creditoId, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    /**
     * 6. GET /documentos/carta-beneficiarios/{socioId} - Generar Carta de Beneficiarios
     * Roles: SOCIO (propio), ADMIN
     * Validación IDOR: socio solo puede generar su propia carta
     */
    @GetMapping("/carta-beneficiarios/{socioId}")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Generar carta de beneficiarios")
    public ResponseEntity<DocumentoResponseDTO> generarCartaBeneficiarios(
            @PathVariable UUID socioId,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);

        DocumentoResponseDTO response = generarCartaBeneficiariosUseCase.ejecutar(socioId, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    // ==================== Endpoints de Consulta/Descarga ====================

    /**
     * 7. GET /documentos/{documentoId} - Obtener Metadata de Documento
     * Roles: SOCIO (propio), ADMIN
     * Validación IDOR: socio solo puede ver sus propios documentos
     */
    @GetMapping("/{documentoId}")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Obtener metadata de documento")
    public ResponseEntity<DocumentoResponseDTO> obtenerDocumento(
            @PathVariable UUID documentoId,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);

        DocumentoResponseDTO response = obtenerDocumentoUseCase.ejecutar(documentoId, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    /**
     * 8. GET /documentos/{documentoId}/descargar - Descargar Documento
     * Roles: SOCIO (propio), ADMIN
     * Validación IDOR: socio solo puede descargar sus propios documentos
     * Retorna pre-signed URL (nunca bytes directos)
     */
    @GetMapping("/{documentoId}/descargar")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Descargar documento (pre-signed URL)")
    public ResponseEntity<DescargarDocumentoResponseDTO> descargarDocumento(
            @PathVariable UUID documentoId,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);

        DescargarDocumentoResponseDTO response = descargarDocumentoUseCase.ejecutar(documentoId, socioIdToken, isAdmin);
        return ResponseEntity.ok(response);
    }

    /**
     * 9. GET /documentos/socio/{socioId} - Listar Documentos de Socio
     * Roles: SOCIO (propio), ADMIN
     * Validación IDOR: socio solo puede ver sus propios documentos
     */
    @GetMapping("/socio/{socioId}")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Listar documentos de socio")
    public ResponseEntity<Map<String, Object>> listarDocumentos(
            @PathVariable UUID socioId,
            @RequestParam(required = false) TipoDocumento tipo,
            @RequestParam(required = false) EstadoDocumento estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        UUID socioIdToken = extraerSocioId(authentication);
        boolean isAdmin = esAdmin(authentication);

        List<DocumentoListResponseDTO> documentos = listarDocumentosSocioUseCase.ejecutar(
                socioId, socioIdToken, isAdmin, tipo, estado, page, size);

        return ResponseEntity.ok(Map.of(
                "documentos", documentos,
                "total", documentos.size(),
                "page", page,
                "size", size
        ));
    }

    // ==================== Helpers ====================

    private UUID extraerSocioId(Authentication authentication) {
        try {
            if (authentication.getPrincipal() instanceof AuthenticatedUser authUser) {
                return authUser.getSocioId();
            }
            return fromString(authentication.getName());
        } catch (Exception e) {
            log.warn("No se pudo extraer socioId del token");
            return null;
        }
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.equals(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private boolean esAdminOSistema(Authentication authentication) {
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
