package com.tufondo.transporte.presentation.controller;

import com.tufondo.transporte.application.dto.RegistrarUnidadRequestDTO;
import com.tufondo.transporte.application.dto.UnidadTransporteResponseDTO;
import com.tufondo.transporte.application.usecase.ListarUnidadesSocioUseCase;
import com.tufondo.transporte.application.usecase.RegistrarUnidadUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/socios/{socioId}/unidades")
@RequiredArgsConstructor
public class UnidadTransporteController {

    private final RegistrarUnidadUseCase registrarUnidadUseCase;
    private final ListarUnidadesSocioUseCase listarUnidadesSocioUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<UnidadTransporteResponseDTO> registrarUnidad(
            @PathVariable UUID socioId,
            @Valid @RequestBody RegistrarUnidadRequestDTO request) {

        UnidadTransporteResponseDTO response = registrarUnidadUseCase.ejecutar(request, socioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<UnidadTransporteResponseDTO>> listarUnidades(
            @PathVariable UUID socioId) {
        
        List<UnidadTransporteResponseDTO> response = listarUnidadesSocioUseCase.ejecutar(socioId);
        return ResponseEntity.ok(response);
    }
}
