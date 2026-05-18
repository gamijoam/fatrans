package com.tufondo.kyc.api.controller;

import com.tufondo.auth.infrastructure.security.AuthenticatedUser;
import com.tufondo.kyc.application.dto.request.AceptarConsentimientoBiometricoRequest;
import com.tufondo.kyc.application.dto.response.IniciarBiometriaResponse;
import com.tufondo.kyc.application.usecase.IniciarVerificacionBiometricaUseCase;
import com.tufondo.kyc.application.usecase.ProcesarWebhookBiometricoUseCase;
import com.tufondo.kyc.application.usecase.RegistrarConsentimientoBiometricoUseCase;
import com.tufondo.kyc.application.usecase.RevocarConsentimientoBiometricoUseCase;
import com.tufondo.kyc.domain.model.port.BiometricVerificatorPort;
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

import java.io.IOException;
import java.util.UUID;

/**
 * Endpoints del flujo biométrico:
 *  - POST /api/v1/kyc/biometric/consentimiento   → socio acepta política LOPDP biométrica
 *  - POST /api/v1/kyc/biometric/iniciar          → socio inicia sesión con el proveedor
 *  - POST /api/v1/kyc/biometric/webhook          → callback público del proveedor (sin auth, valida HMAC)
 *  - POST /api/v1/kyc/biometric/revocar          → socio revoca consentimiento (Art. 7 LOPDP)
 *
 * El webhook es público (sin JWT) pero está protegido por HMAC + ventana de timestamp
 * verificada dentro del adapter del puerto. NO logueamos el body completo (PII potencial).
 */
@RestController
@RequestMapping("/api/v1/kyc/biometric")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "KYC Biométrico", description = "Verificación facial + liveness vía proveedor externo")
@SecurityRequirement(name = "bearerAuth")
public class BiometricController {

    private final RegistrarConsentimientoBiometricoUseCase registrarConsentimientoUseCase;
    private final IniciarVerificacionBiometricaUseCase iniciarBiometriaUseCase;
    private final ProcesarWebhookBiometricoUseCase procesarWebhookUseCase;
    private final RevocarConsentimientoBiometricoUseCase revocarConsentimientoUseCase;
    private final BiometricVerificatorPort biometricPort;

    @PostMapping("/consentimiento")
    @PreAuthorize("hasRole('SOCIO')")
    @Operation(summary = "Aceptar consentimiento biométrico (LOPDP)")
    public ResponseEntity<Void> aceptarConsentimiento(
            @Valid @RequestBody AceptarConsentimientoBiometricoRequest request,
            Authentication auth,
            HttpServletRequest http) {
        UUID socioId = extraerSocioId(auth);
        registrarConsentimientoUseCase.ejecutar(socioId, request.getVersionPolitica(),
                clientIp(http), http.getHeader("User-Agent"));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/iniciar")
    @PreAuthorize("hasRole('SOCIO')")
    @Operation(summary = "Iniciar sesión biométrica con el proveedor")
    public ResponseEntity<IniciarBiometriaResponse> iniciar(
            Authentication auth,
            HttpServletRequest http) {
        UUID socioId = extraerSocioId(auth);

        IniciarVerificacionBiometricaUseCase.Resultado r = iniciarBiometriaUseCase.ejecutar(
                socioId, clientIp(http), http.getHeader("User-Agent"));

        return ResponseEntity.ok(IniciarBiometriaResponse.builder()
                .intentoId(r.intentoId())
                .sessionId(r.sessionId())
                .widgetUrl(r.widgetUrl())
                .widgetToken(r.widgetToken())
                .proveedor(biometricPort.getProveedor())
                .build());
    }

    /**
     * Webhook público de Didit. La autenticación es por HMAC, no por JWT (de ahí
     * la anotación de seguridad explícita y la ausencia de @PreAuthorize).
     */
    @PostMapping(value = "/webhook", consumes = "application/json")
    @Operation(summary = "Webhook del proveedor biométrico (HMAC firmado)")
    public ResponseEntity<Void> webhook(HttpServletRequest http) throws IOException {
        byte[] rawBody = http.getInputStream().readAllBytes();
        String timestamp = http.getHeader("x-timestamp");

        // Didit envía tres firmas. Preferimos V2 (HMAC sobre JSON canonicalizado:
        // sort recursivo de keys + floats whole-number como int + re-encode sin
        // escape Unicode). V2 sobrevive a middleware que re-encoda JSON (Cloudflare,
        // proxies). X-Signature simple es fallback frágil con caracteres especiales.
        String sigV2 = http.getHeader("x-signature-v2");
        String sig;
        if (sigV2 != null && !sigV2.isBlank()) {
            sig = "v2:" + sigV2;
        } else {
            sig = http.getHeader("x-signature");
        }

        // Solo logueamos metadatos (length y prefijo del sessionId si quisiéramos),
        // nunca el body completo — Didit envía datos sensibles (nombre, cédula, foto).
        log.info("Webhook biométrico recibido: bytes={} sigType={}",
                rawBody.length, sigV2 != null ? "v2" : "simple");

        procesarWebhookUseCase.ejecutar(rawBody, sig, timestamp);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/revocar")
    @PreAuthorize("hasRole('SOCIO')")
    @Operation(summary = "Revocar consentimiento biométrico (LOPDP Art. 7)")
    public ResponseEntity<Void> revocar(Authentication auth) {
        UUID socioId = extraerSocioId(auth);
        revocarConsentimientoUseCase.ejecutar(socioId);
        return ResponseEntity.noContent().build();
    }

    // ---- helpers ----

    private UUID extraerSocioId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof AuthenticatedUser authUser) {
            return authUser.getSocioId();
        }
        return UUID.fromString(authentication.getName());
    }

    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        return xRealIp != null && !xRealIp.isBlank() ? xRealIp : request.getRemoteAddr();
    }
}
