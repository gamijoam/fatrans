package com.tufondo.contabilidad.api.controller;

import com.tufondo.contabilidad.application.dto.EstadoResultadosFilter;
import com.tufondo.contabilidad.application.dto.EstadoResultadosResponse;
import com.tufondo.contabilidad.application.port.output.ContabilidadPdfPort;
import com.tufondo.contabilidad.application.usecase.GenerarEstadoResultadosUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Controller del Estado de Resultados (sub-issue #271).
 *
 * <p>Reporte VEN-NIF del excedente/déficit del período.</p>
 */
@RestController
@RequestMapping("/api/v1/contabilidad/estado-resultados")
@RequiredArgsConstructor
@Tag(name = "Contabilidad - Estado de Resultados",
        description = "Reporte VEN-NIF del excedente/déficit del período")
public class EstadoResultadosController {

    private final GenerarEstadoResultadosUseCase useCase;
    private final ContabilidadPdfPort pdfPort;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','SISTEMA')")
    @Operation(summary = "Genera el Estado de Resultados en JSON",
            description = "Rango ≤ 1 año fiscal. Permisos: ADMIN/SUPER_ADMIN/SISTEMA.")
    public ResponseEntity<EstadoResultadosResponse> generar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "false") boolean incluirCeros,
            Authentication authentication) {

        UUID solicitanteId = extraerUsuarioId(authentication);
        EstadoResultadosFilter filter = new EstadoResultadosFilter(desde, hasta, incluirCeros);
        return ResponseEntity.ok(useCase.ejecutar(filter, solicitanteId));
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','SISTEMA')")
    @Operation(summary = "Genera el Estado de Resultados en PDF para descarga")
    public ResponseEntity<byte[]> generarPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "false") boolean incluirCeros,
            Authentication authentication) {

        UUID solicitanteId = extraerUsuarioId(authentication);
        EstadoResultadosFilter filter = new EstadoResultadosFilter(desde, hasta, incluirCeros);
        EstadoResultadosResponse data = useCase.ejecutar(filter, solicitanteId);
        byte[] pdf = pdfPort.generarEstadoResultadosPdf(data);

        String filename = String.format("EstadoResultados_%s_a_%s.pdf",
                desde.format(DateTimeFormatter.BASIC_ISO_DATE),
                hasta.format(DateTimeFormatter.BASIC_ISO_DATE));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, private")
                .body(pdf);
    }

    private UUID extraerUsuarioId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.tufondo.auth.infrastructure.security.AuthenticatedUser authUser) {
            UUID socioId = authUser.getSocioId();
            if (socioId != null) return socioId;
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
