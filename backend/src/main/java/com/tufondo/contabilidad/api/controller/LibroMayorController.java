package com.tufondo.contabilidad.api.controller;

import com.tufondo.contabilidad.application.dto.LibroMayorFilter;
import com.tufondo.contabilidad.application.dto.LibroMayorResponse;
import com.tufondo.contabilidad.application.port.output.ContabilidadPdfPort;
import com.tufondo.contabilidad.application.usecase.GenerarLibroMayorUseCase;
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
 * Controller del Libro Mayor (sub-issue #270).
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li>{@code GET /api/v1/contabilidad/libro-mayor}     — JSON</li>
 *   <li>{@code GET /api/v1/contabilidad/libro-mayor/pdf} — descarga A4 horizontal</li>
 * </ul>
 *
 * <p>Solo {@code ROLE_ADMIN / SUPER_ADMIN / SISTEMA}. Falta agregar rol
 * {@code CONTADOR} dedicado — ver {@code _pendientes-criticos.md}.</p>
 */
@RestController
@RequestMapping("/api/v1/contabilidad/libro-mayor")
@RequiredArgsConstructor
@Tag(name = "Contabilidad - Libro Mayor",
        description = "Reporte SUDECA con saldos y movimientos por cuenta")
public class LibroMayorController {

    private final GenerarLibroMayorUseCase useCase;
    private final ContabilidadPdfPort pdfPort;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','SISTEMA')")
    @Operation(summary = "Genera el Libro Mayor en JSON",
            description = "Rango ≤ 1 año fiscal. Filtros opcionales: cuenta específica, incluir sin movimientos, incluir totalizadoras.")
    public ResponseEntity<LibroMayorResponse> generar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String codigoCuenta,
            @RequestParam(defaultValue = "false") boolean incluirSinMovimientos,
            @RequestParam(defaultValue = "false") boolean incluirTotalizadoras,
            Authentication authentication) {

        UUID solicitanteId = extraerUsuarioId(authentication);
        LibroMayorFilter filter = new LibroMayorFilter(
                desde, hasta, codigoCuenta, incluirSinMovimientos, incluirTotalizadoras);
        LibroMayorResponse response = useCase.ejecutar(filter, solicitanteId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','SISTEMA')")
    @Operation(summary = "Genera el Libro Mayor en PDF para descarga")
    public ResponseEntity<byte[]> generarPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String codigoCuenta,
            @RequestParam(defaultValue = "false") boolean incluirSinMovimientos,
            @RequestParam(defaultValue = "false") boolean incluirTotalizadoras,
            Authentication authentication) {

        UUID solicitanteId = extraerUsuarioId(authentication);
        LibroMayorFilter filter = new LibroMayorFilter(
                desde, hasta, codigoCuenta, incluirSinMovimientos, incluirTotalizadoras);
        LibroMayorResponse data = useCase.ejecutar(filter, solicitanteId);
        byte[] pdf = pdfPort.generarLibroMayorPdf(data);

        String filename = String.format("LibroMayor_%s_a_%s%s.pdf",
                desde.format(DateTimeFormatter.BASIC_ISO_DATE),
                hasta.format(DateTimeFormatter.BASIC_ISO_DATE),
                codigoCuenta != null && !codigoCuenta.isBlank() ? "_" + codigoCuenta : "");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL,
                        "no-store, no-cache, must-revalidate, private")
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
