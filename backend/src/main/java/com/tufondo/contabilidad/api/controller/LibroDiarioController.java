package com.tufondo.contabilidad.api.controller;

import com.tufondo.contabilidad.application.dto.LibroDiarioFilter;
import com.tufondo.contabilidad.application.dto.LibroDiarioResponse;
import com.tufondo.contabilidad.application.port.output.ContabilidadPdfPort;
import com.tufondo.contabilidad.application.usecase.GenerarLibroDiarioUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Controller del Libro Diario (sub-issue #269).
 *
 * <p>Expone GET con dos formatos:</p>
 * <ul>
 *   <li>{@code .../libro-diario} — JSON estructurado (default)</li>
 *   <li>{@code .../libro-diario/pdf} — PDF descargable</li>
 * </ul>
 *
 * <p>Solo accesible para roles ADMIN / SUPER_ADMIN / SISTEMA. Ideal sería
 * un rol CONTADOR dedicado — ver pendiente "Crear rol CONTADOR" en
 * {@code docs/modulos/contabilidad/_pendientes-criticos.md}.</p>
 */
@RestController
@RequestMapping("/api/v1/contabilidad/libro-diario")
@RequiredArgsConstructor
@Tag(name = "Contabilidad - Libro Diario",
        description = "Reporte SUDECA con todos los asientos del período")
public class LibroDiarioController {

    private final GenerarLibroDiarioUseCase useCase;
    private final ContabilidadPdfPort pdfPort;

    /**
     * Libro Diario en JSON.
     *
     * <p>Devuelve la estructura completa con encabezado, asientos y totales.
     * El frontend admin puede mostrar la tabla o re-descargar como PDF.</p>
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','SISTEMA')")
    @Operation(summary = "Genera el Libro Diario en JSON",
            description = "Solo ADMIN/SUPER_ADMIN/SISTEMA. Rango ≤ 1 año fiscal.")
    public ResponseEntity<LibroDiarioResponse> generar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "true") boolean incluirAnulados,
            Authentication authentication) {

        UUID solicitanteId = extraerUsuarioId(authentication);
        LibroDiarioFilter filter = new LibroDiarioFilter(desde, hasta, incluirAnulados);
        LibroDiarioResponse response = useCase.ejecutar(filter, solicitanteId);
        return ResponseEntity.ok(response);
    }

    /**
     * Libro Diario en PDF. Mismo contenido que el endpoint JSON pero renderizado
     * para impresión / archivo.
     */
    @GetMapping("/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','SISTEMA')")
    @Operation(summary = "Genera el Libro Diario en PDF para descarga",
            description = "Mismo dataset que JSON, formateado tabular SUDECA.")
    public ResponseEntity<byte[]> generarPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "true") boolean incluirAnulados,
            Authentication authentication) {

        UUID solicitanteId = extraerUsuarioId(authentication);
        LibroDiarioFilter filter = new LibroDiarioFilter(desde, hasta, incluirAnulados);
        LibroDiarioResponse data = useCase.ejecutar(filter, solicitanteId);
        byte[] pdf = pdfPort.generarLibroDiarioPdf(data);

        String filename = String.format("LibroDiario_%s_a_%s.pdf",
                desde.format(DateTimeFormatter.BASIC_ISO_DATE),
                hasta.format(DateTimeFormatter.BASIC_ISO_DATE));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CACHE_CONTROL,
                        "no-store, no-cache, must-revalidate, private")
                .body(pdf);
    }

    /** Helper: extrae socioId o usuarioId del token JWT. */
    private UUID extraerUsuarioId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.tufondo.auth.infrastructure.security.AuthenticatedUser authUser) {
            UUID socioId = authUser.getSocioId();
            if (socioId != null) return socioId;
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            return null; // usuarios admin sin UUID en name — auditoría queda con null
        }
    }
}
