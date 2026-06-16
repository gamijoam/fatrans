package com.tufondo.contabilidad.api.controller;

import com.tufondo.contabilidad.application.dto.BalanceGeneralFilter;
import com.tufondo.contabilidad.application.dto.BalanceGeneralResponse;
import com.tufondo.contabilidad.application.port.output.ContabilidadPdfPort;
import com.tufondo.contabilidad.application.usecase.GenerarBalanceGeneralUseCase;
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
 * Controller del Balance General (sub-issue #271).
 *
 * <p>Reporte VEN-NIF de la situación patrimonial a una fecha. Endpoints:</p>
 * <ul>
 *   <li>{@code GET /api/v1/contabilidad/balance-general}      — JSON</li>
 *   <li>{@code GET /api/v1/contabilidad/balance-general/pdf}  — descarga PDF</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/contabilidad/balance-general")
@RequiredArgsConstructor
@Tag(name = "Contabilidad - Balance General",
        description = "Reporte VEN-NIF de la situación patrimonial a una fecha")
public class BalanceGeneralController {

    private final GenerarBalanceGeneralUseCase useCase;
    private final ContabilidadPdfPort pdfPort;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','SISTEMA')")
    @Operation(summary = "Genera el Balance General en JSON",
            description = "Permisos: ADMIN/SUPER_ADMIN/SISTEMA. fechaCorte obligatoria. " +
                    "inicioEjercicio default = 1-enero del año del corte.")
    public ResponseEntity<BalanceGeneralResponse> generar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicioEjercicio,
            @RequestParam(defaultValue = "false") boolean incluirCeros,
            Authentication authentication) {

        UUID solicitanteId = extraerUsuarioId(authentication);
        BalanceGeneralFilter filter = new BalanceGeneralFilter(fechaCorte, inicioEjercicio, incluirCeros);
        return ResponseEntity.ok(useCase.ejecutar(filter, solicitanteId));
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','SISTEMA')")
    @Operation(summary = "Genera el Balance General en PDF para descarga")
    public ResponseEntity<byte[]> generarPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte,
            @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicioEjercicio,
            @RequestParam(defaultValue = "false") boolean incluirCeros,
            Authentication authentication) {

        UUID solicitanteId = extraerUsuarioId(authentication);
        BalanceGeneralFilter filter = new BalanceGeneralFilter(fechaCorte, inicioEjercicio, incluirCeros);
        BalanceGeneralResponse data = useCase.ejecutar(filter, solicitanteId);
        byte[] pdf = pdfPort.generarBalanceGeneralPdf(data);

        String filename = "BalanceGeneral_" + fechaCorte.format(DateTimeFormatter.BASIC_ISO_DATE) + ".pdf";
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
