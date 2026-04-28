// com.tufondo.kyc.api.controller.KYCController
package com.tufondo.kyc.api.controller;

import com.tufondo.auth.infrastructure.security.AuthenticatedUser;
import com.tufondo.kyc.application.dto.request.RevocarConsentimientoRequest;
import com.tufondo.kyc.application.dto.request.EnviarDocumentosRequest;
import com.tufondo.kyc.application.dto.request.IniciarKYCRequest;
import com.tufondo.kyc.application.dto.request.SubirDocumentoRequest;
import com.tufondo.kyc.application.dto.response.*;
import com.tufondo.kyc.application.usecase.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * REST Controller para KYC - Endpoints de Socio.
 */
@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC", description = "Modulo de Verificacion de Identidad KYC")
@SecurityRequirement(name = "bearerAuth")
public class KYCController {

    private final IniciarKYCUseCase iniciarKYCUseCase;
    private final ConsultarEstadoKYCUseCase consultarEstadoKYCUseCase;
    private final SubirDocumentoUseCase subirDocumentoUseCase;
    private final EliminarDocumentoUseCase eliminarDocumentoUseCase;
    private final EnviarDocumentosValidacionUseCase enviarDocumentosValidacionUseCase;
    private final RevocarConsentimientoUseCase revocarConsentimientoUseCase;

    /**
     * POST /kyc/iniciar - Iniciar Proceso KYC
     */
    @PostMapping("/iniciar")
    @PreAuthorize("hasRole('SOCIO')")
    @Operation(summary = "Iniciar proceso KYC")
    public ResponseEntity<IniciarKYCResponse> iniciarKYC(
            @Valid @RequestBody IniciarKYCRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        UUID socioId = extraerSocioId(authentication);

        // Capturar IP del cliente
        if (request.getIpCliente() == null) {
            request.setIpCliente(getClientIp(httpRequest));
        }
        if (request.getUserAgent() == null) {
            request.setUserAgent(httpRequest.getHeader("User-Agent"));
        }

        IniciarKYCResponse response = iniciarKYCUseCase.ejecutar(request, socioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /kyc/estado - Consultar Estado KYC
     */
    @GetMapping("/estado")
    @PreAuthorize("hasRole('SOCIO')")
    @Operation(summary = "Consultar estado del KYC")
    public ResponseEntity<EstadoKYCResponse> consultarEstado(Authentication authentication) {
        UUID socioId = extraerSocioId(authentication);
        EstadoKYCResponse response = consultarEstadoKYCUseCase.ejecutar(socioId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /kyc/documentos - Subir Documento
     */
    @PostMapping("/documentos")
    @PreAuthorize("hasRole('SOCIO')")
    @Operation(summary = "Subir documento de identidad")
    public ResponseEntity<SubirDocumentoResponse> subirDocumento(
            @Valid @RequestBody SubirDocumentoRequest request,
            Authentication authentication) {

        UUID socioId = extraerSocioId(authentication);
        SubirDocumentoResponse response = subirDocumentoUseCase.ejecutar(request, socioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /kyc/documentos/{documentoId} - Eliminar Documento
     */
    @DeleteMapping("/documentos/{documentoId}")
    @PreAuthorize("hasRole('SOCIO')")
    @Operation(summary = "Eliminar documento")
    public ResponseEntity<EliminarDocumentoResponse> eliminarDocumento(
            @PathVariable UUID documentoId,
            Authentication authentication) {

        UUID socioId = extraerSocioId(authentication);
        EliminarDocumentoResponse response = eliminarDocumentoUseCase.ejecutar(documentoId, socioId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /kyc/enviar - Enviar Documentos para Revision
     */
    @PostMapping("/enviar")
    @PreAuthorize("hasRole('SOCIO')")
    @Operation(summary = "Enviar documentos para revision")
    public ResponseEntity<EnviarDocumentosResponse> enviarDocumentos(
            @Valid @RequestBody EnviarDocumentosRequest request,
            Authentication authentication) {

        UUID socioId = extraerSocioId(authentication);
        EnviarDocumentosResponse response = enviarDocumentosValidacionUseCase.ejecutar(request, socioId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /kyc/revocar-consentimiento - Revocar Consentimiento (LOPDP Art. 7)
     */
    @PostMapping("/revocar-consentimiento")
    @PreAuthorize("hasRole('SOCIO')")
    @Operation(summary = "Revocar consentimiento para tratamiento de datos")
    public ResponseEntity<RevocarConsentimientoResponse> revocarConsentimiento(
            @Valid @RequestBody RevocarConsentimientoRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        UUID socioId = extraerSocioId(authentication);
        String ipCliente = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        RevocarConsentimientoResponse response = revocarConsentimientoUseCase.ejecutar(
            request, socioId, ipCliente, userAgent);
        return ResponseEntity.ok(response);
    }

    private UUID extraerSocioId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof AuthenticatedUser authUser) {
            return authUser.getSocioId();
        }
        return UUID.fromString(authentication.getName());
    }

    private static final Pattern IPV4_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    private static final Pattern IPV6_PATTERN = Pattern.compile(
        "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::$|^([0-9a-fA-F]{1,4}:){1,7}:$|^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|^([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}$|^([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}$|^([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}$|^([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}$|^[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})$|^:((:[0-9a-fA-F]{1,4}){1,7}|:)$|^fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}$|^::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])$|^([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])$");

    private static final Pattern IPV4_MAPPED_IPV6_PATTERN = Pattern.compile(
        "^::ffff:(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For puede contener varias IPs: client, proxy1, proxy2...
            String clientIp = xForwardedFor.split(",")[0].trim();
            if (isValidIp(clientIp)) {
                return clientIp;
            }
        }
        return request.getRemoteAddr();
    }

    private boolean isValidIp(String ip) {
        return IPV4_PATTERN.matcher(ip).matches()
            || IPV6_PATTERN.matcher(ip.toLowerCase()).matches()
            || IPV4_MAPPED_IPV6_PATTERN.matcher(ip.toLowerCase()).matches();
    }
}