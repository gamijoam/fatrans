package com.tufondo.parametros.presentation.controller;

import com.tufondo.parametros.application.dto.ActualizarParametroRequest;
import com.tufondo.parametros.application.dto.ParametroResponse;
import com.tufondo.parametros.application.usecase.ParametroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/parametros")
@Tag(name = "Parámetros", description = "Gestión de parámetros del sistema")
public class ParametroController {

    private final ParametroService service;

    public ParametroController(ParametroService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los parámetros")
    public ResponseEntity<List<ParametroResponse>> listarTodos() {
        log.info("Listando todos los parámetros del sistema");
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/categoria/{categoria}")
    @Operation(summary = "Listar parámetros por categoría")
    public ResponseEntity<List<ParametroResponse>> listarPorCategoria(@PathVariable String categoria) {
        log.info("Listando parámetros de categoría: {}", categoria);
        return ResponseEntity.ok(service.listarPorCategoria(categoria));
    }

    @GetMapping("/{key}")
    @Operation(summary = "Obtener un parámetro específico")
    public ResponseEntity<ParametroResponse> obtenerPorKey(@PathVariable String key) {
        log.info("Obteniendo parámetro: {}", key);
        return ResponseEntity.ok(service.buscarPorKey(key));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Actualizar un parámetro (solo admins)")
    public ResponseEntity<ParametroResponse> actualizar(
            @PathVariable String key,
            @Valid @RequestBody ActualizarParametroRequest request,
            Authentication authentication) {

        UUID usuarioId = UUID.fromString(authentication.getName());
        log.info("Admin {} actualizando parámetro {} a valor {}",
                usuarioId, key, request.valor());

        return ResponseEntity.ok(service.actualizar(key, request, usuarioId));
    }
}