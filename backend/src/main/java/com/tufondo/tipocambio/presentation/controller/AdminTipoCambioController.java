package com.tufondo.tipocambio.presentation.controller;

import com.tufondo.tipocambio.infrastructure.scraper.BcvScraperService;
import com.tufondo.tipocambio.infrastructure.scraper.BcvSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoints administrativos del módulo de tipo de cambio (issue #231).
 *
 * <p>Separado del {@code TipoCambioController} público porque estos endpoints
 * son exclusivos de administradores y tocan el job de sincronización BCV.</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/tipos-cambio")
@RequiredArgsConstructor
@Tag(name = "Admin - Tipos de Cambio", description = "Sincronización BCV (solo admin)")
public class AdminTipoCambioController {

    private final BcvSyncService bcvSyncService;

    /**
     * Fuerza una sincronización inmediata con el BCV (sin esperar al cron diario).
     * Útil cuando el BCV publicó una tasa de emergencia o el job falló.
     */
    @PostMapping("/sync-bcv")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Sincronizar tasa BCV manualmente",
               description = "Scrapea bcv.org.ve, extrae la tasa USD del día y la persiste en BD. " +
                             "Idempotente: si ya existe la tasa para esa fecha, no la duplica. " +
                             "Solo ADMIN.")
    public ResponseEntity<Map<String, Object>> sincronizarManual() {
        log.info("Sincronización BCV manual disparada por admin");
        try {
            BcvSyncService.SincronizacionResultado r = bcvSyncService.sincronizarDesdeBcv();
            return ResponseEntity.ok(Map.of(
                    "exito", true,
                    "insertada", r.insertada(),
                    "fecha", r.fecha().toString(),
                    "tasa", r.tasa(),
                    "detalle", r.detalle()
            ));
        } catch (BcvScraperService.BcvScrapingException e) {
            log.error("Sync BCV manual falló (red/SSL/parsing): {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "exito", false,
                    "error", "BCV_UNREACHABLE_OR_INVALID_HTML",
                    "mensaje", e.getMessage()
            ));
        }
    }
}
