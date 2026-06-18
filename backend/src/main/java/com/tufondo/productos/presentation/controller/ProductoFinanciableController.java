package com.tufondo.productos.presentation.controller;

import com.tufondo.auth.infrastructure.security.AuthenticatedUser;
import com.tufondo.creditos.application.dto.SolicitudCreditoResponse;
import com.tufondo.productos.application.dto.PrecalificacionProductoResponse;
import com.tufondo.productos.application.dto.ProductoFinanciableRequest;
import com.tufondo.productos.application.dto.ProductoFinanciableResponse;
import com.tufondo.productos.application.usecase.GestionarProductosFinanciablesUseCase;
import com.tufondo.productos.infrastructure.storage.ProductoImagenStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductoFinanciableController {

    private final GestionarProductosFinanciablesUseCase useCase;
    private final ProductoImagenStorageService imagenStorageService;

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

    @PostMapping(value = "/admin/productos/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoFinanciableResponse> subirImagen(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        useCase.validarExiste(id);
        ProductoImagenStorageService.UploadProductoImagenResult imagen = imagenStorageService.subirImagen(id, file);
        return ResponseEntity.ok(useCase.actualizarImagen(id, imagen, extraerUserId(authentication)));
    }

    @PostMapping(value = "/admin/productos/{id}/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoFinanciableResponse> agregarImagen(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        useCase.validarExiste(id);
        ProductoImagenStorageService.UploadProductoImagenResult imagen = imagenStorageService.subirImagen(id, file);
        return ResponseEntity.ok(useCase.agregarImagen(id, imagen, false, extraerUserId(authentication)));
    }

    @PostMapping("/admin/productos/{id}/imagenes/{imagenId}/principal")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoFinanciableResponse> marcarImagenPrincipal(
            @PathVariable Long id,
            @PathVariable Long imagenId) {
        return ResponseEntity.ok(useCase.marcarImagenPrincipal(id, imagenId));
    }

    @DeleteMapping("/admin/productos/{id}/imagenes/{imagenId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ProductoFinanciableResponse> eliminarImagen(
            @PathVariable Long id,
            @PathVariable Long imagenId) {
        return ResponseEntity.ok(useCase.desactivarImagen(id, imagenId));
    }

    @GetMapping("/productos/imagenes/{fecha}/{fileName}")
    @PreAuthorize("hasAnyRole('SOCIO', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable String fecha, @PathVariable String fileName) {
        ProductoImagenStorageService.ImagenProducto imagen = imagenStorageService.descargar(fecha, fileName);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(imagen.contentType()))
            .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate())
            .body(imagen.data());
    }

    @ExceptionHandler(GestionarProductosFinanciablesUseCase.ProductoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ProductoImagenStorageService.ImagenNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> imagenNoEncontrada() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Imagen de producto no encontrada"));
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
