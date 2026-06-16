package com.tufondo.productos.presentation.controller;

import com.tufondo.auth.infrastructure.security.AuthenticatedUser;
import com.tufondo.creditos.application.dto.SolicitudCreditoResponse;
import com.tufondo.productos.application.dto.PrecalificacionProductoResponse;
import com.tufondo.productos.application.dto.ProductoFinanciableRequest;
import com.tufondo.productos.application.dto.ProductoFinanciableResponse;
import com.tufondo.productos.application.usecase.GestionarProductosFinanciablesUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductoFinanciableController {

    private final GestionarProductosFinanciablesUseCase useCase;

    @GetMapping("/productos")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> listarPublicados() {
        List<ProductoFinanciableResponse> productos = useCase.listarPublicados();
        return ResponseEntity.ok(Map.of(
            "productos", productos,
            "total", productos.size()
        ));
    }

    @GetMapping("/productos/{slug}")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoFinanciableResponse> obtenerPublicado(@PathVariable String slug) {
        return ResponseEntity.ok(useCase.obtenerPublicado(slug));
    }

    @PostMapping("/productos/{id}/precalificar")
    @PreAuthorize("hasRole('SOCIO')")
    public ResponseEntity<PrecalificacionProductoResponse> precalificar(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(useCase.precalificar(id, extraerSocioId(authentication)));
    }

    @PostMapping("/productos/{id}/solicitar-financiamiento")
    @PreAuthorize("hasRole('SOCIO')")
    public ResponseEntity<SolicitudCreditoResponse> solicitarFinanciamiento(
            @PathVariable Long id,
            Authentication authentication) {
        SolicitudCreditoResponse response = useCase.solicitarFinanciamiento(id, extraerSocioId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/productos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> listarAdmin() {
        List<ProductoFinanciableResponse> productos = useCase.listarAdmin();
        return ResponseEntity.ok(Map.of(
            "productos", productos,
            "total", productos.size()
        ));
    }

    @PostMapping("/admin/productos")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoFinanciableResponse> crear(
            @Valid @RequestBody ProductoFinanciableRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.crear(request, extraerUserId(authentication)));
    }

    @PutMapping("/admin/productos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoFinanciableResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoFinanciableRequest request) {
        return ResponseEntity.ok(useCase.actualizar(id, request));
    }

    @PostMapping("/admin/productos/{id}/publicar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoFinanciableResponse> publicar(@PathVariable Long id) {
        return ResponseEntity.ok(useCase.cambiarEstado(id, "PUBLICADO"));
    }

    @PostMapping("/admin/productos/{id}/pausar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoFinanciableResponse> pausar(@PathVariable Long id) {
        return ResponseEntity.ok(useCase.cambiarEstado(id, "PAUSADO"));
    }

    @PostMapping("/admin/productos/{id}/archivar")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoFinanciableResponse> archivar(@PathVariable Long id) {
        return ResponseEntity.ok(useCase.cambiarEstado(id, "ARCHIVADO"));
    }

    @ExceptionHandler(GestionarProductosFinanciablesUseCase.ProductoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> solicitudInvalida(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    private UUID extraerSocioId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authUser && authUser.getSocioId() != null) {
            return authUser.getSocioId();
        }
        if (esAdmin(authentication)) {
            return UUID.fromString(authentication.getName());
        }
        return UUID.fromString(authentication.getName());
    }

    private UUID extraerUserId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authUser) {
            return authUser.getUserId();
        }
        return UUID.fromString(authentication.getName());
    }

    private boolean esAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.equals(new SimpleGrantedAuthority("ROLE_ADMIN"))
                || a.equals(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
    }
}
