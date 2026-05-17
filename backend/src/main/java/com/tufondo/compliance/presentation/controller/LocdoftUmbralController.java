package com.tufondo.compliance.presentation.controller;

import com.tufondo.compliance.application.service.LocdoftOperacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint público (autenticado) para consultar el umbral LOCDOFT vigente
 * (#218 PR-C). El frontend lo llama antes de mostrar el modal o
 * pre-validar si el monto de la operación lo excede.
 *
 * <p>Solo lectura — el endpoint de modificación vive en el módulo
 * `parametros` con permisos de admin.</p>
 */
@RestController
@RequestMapping("/api/v1/compliance/locdoft")
@RequiredArgsConstructor
@Tag(name = "Compliance", description = "Umbrales y consentimientos LOCDOFT")
public class LocdoftUmbralController {

    private final LocdoftOperacionService service;

    @GetMapping("/umbral")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consulta el umbral LOCDOFT vigente por moneda")
    public ResponseEntity<Map<String, Object>> obtenerUmbral(
            @RequestParam(defaultValue = "VES") String moneda
    ) {
        BigDecimal umbral = service.obtenerUmbral(moneda);
        Map<String, Object> body = new HashMap<>();
        body.put("moneda", moneda.toUpperCase());
        body.put("umbral", umbral);  // puede ser null (fail-open) → frontend trata como "sin umbral"
        return ResponseEntity.ok(body);
    }
}
