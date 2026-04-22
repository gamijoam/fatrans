// com.tufondo.kyc.api.controller.AnalistaKYCController
package com.tufondo.kyc.api.controller;

import com.tufondo.kyc.application.dto.request.AprobarVerificacionRequest;
import com.tufondo.kyc.application.dto.request.RechazarVerificacionRequest;
import com.tufondo.kyc.application.dto.request.SolicitarInfoRequest;
import com.tufondo.kyc.application.dto.response.RevisionDecisionResponse;
import com.tufondo.kyc.application.dto.response.RevisionResponse;
import com.tufondo.kyc.application.usecase.RevisarDocumentosUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller para KYC - Endpoints de Analista.
 */
@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC Analista", description = "Endpoints de revision para analistas KYC")
@SecurityRequirement(name = "bearerAuth")
public class AnalistaKYCController {

    private final RevisarDocumentosUseCase revisarDocumentosUseCase;

    /**
     * GET /kyc/revision/{verificacionId} - Detalle de Revision
     */
    @GetMapping("/revision/{verificacionId}")
    @PreAuthorize("hasAnyRole('ANALISTA_KYC', 'ADMIN')")
    @Operation(summary = "Obtener detalle de verificacion para revision")
    public ResponseEntity<RevisionResponse> obtenerDetalleRevision(
            @PathVariable UUID verificacionId) {

        RevisionResponse response = revisarDocumentosUseCase.obtenerDetalle(verificacionId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /kyc/revision/{verificacionId}/aprobar - Aprobar Verificacion
     */
    @PostMapping("/revision/{verificacionId}/aprobar")
    @PreAuthorize("hasAnyRole('ANALISTA_KYC', 'ADMIN')")
    @Operation(summary = "Aprobar verificacion KYC")
    public ResponseEntity<RevisionDecisionResponse> aprobarVerificacion(
            @PathVariable UUID verificacionId,
            @Valid @RequestBody(required = false) AprobarVerificacionRequest request,
            Authentication authentication) {

        if (request == null) {
            request = new AprobarVerificacionRequest();
        }

        String analistaId = authentication.getName();
        RevisionDecisionResponse response = revisarDocumentosUseCase.aprobar(verificacionId, request, analistaId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /kyc/revision/{verificacionId}/rechazar - Rechazar Verificacion
     */
    @PostMapping("/revision/{verificacionId}/rechazar")
    @PreAuthorize("hasAnyRole('ANALISTA_KYC', 'ADMIN')")
    @Operation(summary = "Rechazar verificacion KYC")
    public ResponseEntity<RevisionDecisionResponse> rechazarVerificacion(
            @PathVariable UUID verificacionId,
            @Valid @RequestBody RechazarVerificacionRequest request,
            Authentication authentication) {

        String analistaId = authentication.getName();
        RevisionDecisionResponse response = revisarDocumentosUseCase.rechazar(verificacionId, request, analistaId);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /kyc/revision/{verificacionId}/solicitar-info - Solicitar Mas Informacion
     */
    @PostMapping("/revision/{verificacionId}/solicitar-info")
    @PreAuthorize("hasAnyRole('ANALISTA_KYC', 'ADMIN')")
    @Operation(summary = "Solicitar informacion adicional")
    public ResponseEntity<RevisionDecisionResponse> solicitarInfo(
            @PathVariable UUID verificacionId,
            @Valid @RequestBody SolicitarInfoRequest request,
            Authentication authentication) {

        String analistaId = authentication.getName();
        RevisionDecisionResponse response = revisarDocumentosUseCase.solicitarInfo(verificacionId, request, analistaId);
        return ResponseEntity.ok(response);
    }
}