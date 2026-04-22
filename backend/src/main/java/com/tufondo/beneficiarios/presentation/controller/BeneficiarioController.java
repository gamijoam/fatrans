// com/tufondo/beneficiarios/presentation/controller/BeneficiarioController.java
package com.tufondo.beneficiarios.presentation.controller;

import com.tufondo.beneficiarios.application.dto.*;
import com.tufondo.beneficiarios.application.usecase.*;
import com.tufondo.beneficiarios.domain.exception.BeneficiarioNoEncontradoException;
import com.tufondo.beneficiarios.domain.exception.AccesoNoAutorizadoException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller para gestionar beneficiarios.
 * 🔒 SECURITY: Implementa validación IDOR - socio solo puede acceder a sus propios beneficiarios.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/socios/{socioId}/beneficiarios")
@RequiredArgsConstructor
@Tag(name = "Beneficiarios", description = "Gestión de Beneficiarios de Socios")
public class BeneficiarioController {

    private final CreateBeneficiarioUseCase createBeneficiarioUseCase;
    private final GetBeneficiariosBySocioUseCase getBeneficiariosBySocioUseCase;
    private final GetBeneficiarioByIdUseCase getBeneficiarioByIdUseCase;
    private final UpdateBeneficiarioUseCase updateBeneficiarioUseCase;
    private final DeleteBeneficiarioUseCase deleteBeneficiarioUseCase;

    @PostMapping
    @Operation(summary = "Crear un nuevo beneficiario")
    public ResponseEntity<BeneficiarioResponseDTO> crearBeneficiario(
            @PathVariable UUID socioId,
            @Valid @RequestBody CreateBeneficiarioRequestDTO request,
            HttpServletRequest httpRequest) {
        validarAccesoSocio(socioId, httpRequest);
        BeneficiarioResponseDTO response = createBeneficiarioUseCase.ejecutar(socioId, request, httpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar beneficiarios activos de un socio")
    public ResponseEntity<BeneficiarioListResponseDTO> listarBeneficiarios(
            @PathVariable UUID socioId,
            HttpServletRequest httpRequest) {
        validarAccesoSocio(socioId, httpRequest);
        BeneficiarioListResponseDTO response = getBeneficiariosBySocioUseCase.ejecutar(socioId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un beneficiario por ID")
    public ResponseEntity<BeneficiarioResponseDTO> obtenerBeneficiario(
            @PathVariable UUID socioId,
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        validarAccesoSocio(socioId, httpRequest);
        BeneficiarioResponseDTO response = getBeneficiarioByIdUseCase.ejecutar(socioId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un beneficiario")
    public ResponseEntity<BeneficiarioResponseDTO> actualizarBeneficiario(
            @PathVariable UUID socioId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBeneficiarioRequestDTO request,
            HttpServletRequest httpRequest) {
        validarAccesoSocio(socioId, httpRequest);
        BeneficiarioResponseDTO response = updateBeneficiarioUseCase.ejecutar(socioId, id, request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un beneficiario (soft delete)")
    public ResponseEntity<DeleteBeneficiarioResponseDTO> eliminarBeneficiario(
            @PathVariable UUID socioId,
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        validarAccesoSocio(socioId, httpRequest);
        DeleteBeneficiarioResponseDTO response = deleteBeneficiarioUseCase.ejecutar(socioId, id, httpRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * 🔒 SECURITY: Valida que el usuario autenticado tenga acceso al socio especificado.
     * Admin puede acceder a cualquier socio. Socio solo a sus propios datos.
     */
    private void validarAccesoSocio(UUID socioId, HttpServletRequest request) {
        String usuarioIdAutenticado = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        var auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            try {
                UUID socioIdUsuario = UUID.fromString(usuarioIdAutenticado);
                if (!socioIdUsuario.equals(socioId)) {
                    log.warn("IDOR attempt - Usuario {} intentó acceder a beneficiarios del socio {}",
                            usuarioIdAutenticado, socioId);
                    throw new BeneficiarioNoEncontradoException(socioId);
                }
            } catch (IllegalArgumentException e) {
                log.warn("IDOR attempt - usuarioId no es UUID válido: {}", usuarioIdAutenticado);
                throw new AccesoNoAutorizadoException();
            }
        }
    }
}