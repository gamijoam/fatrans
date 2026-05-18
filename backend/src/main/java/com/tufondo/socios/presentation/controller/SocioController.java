// com/tufondo/socios/presentation/controller/SocioController.java
// 🔧 SECURITY FIX: Limitar size de paginación a máximo 100 para prevenir DoS
package com.tufondo.socios.presentation.controller;

import com.tufondo.socios.application.dto.ActualizarSocioDTO;
import com.tufondo.socios.application.dto.CrearSocioRequestDTO;
import com.tufondo.socios.application.dto.SocioResponseDTO;
import com.tufondo.socios.application.usecase.*;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.repository.SocioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/socios")
@RequiredArgsConstructor
@Tag(name = "Socios", description = "Gestión de Socios del Fondo de Ahorro")
public class SocioController {

    // SECURITY FIX: Límites de paginación para prevenir DoS
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MIN_PAGE_SIZE = 1;
    
    // SECURITY FIX: Rate limiting para prevenir ataques de enumeración y DoS
    private static final long MAX_REQUESTS_PER_MINUTE = 60;
    private static final Map<String, Long> requestCounts = new ConcurrentHashMap<>();
    private static final Map<String, Long> requestTimestamps = new ConcurrentHashMap<>();

    private final CrearSocioUseCase crearSocioUseCase;
    private final ObtenerSocioUseCase obtenerSocioUseCase;
    private final ListarSociosUseCase listarSociosUseCase;
    private final BuscarSocioUseCase buscarSocioUseCase;
    private final ActualizarSocioUseCase actualizarSocioUseCase;
    private final ActivarSocioUseCase activarSocioUseCase;
    private final DesactivarSocioUseCase desactivarSocioUseCase;
    private final EliminarSocioUseCase eliminarSocioUseCase;
    private final SocioDTOMapper socioDTOMapper;

    // Issue #179: solo ADMIN puede crear socios directamente. El flujo público
    // de auto-registro va por /api/v1/socios/solicitud (que sigue siendo permitAll).
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un nuevo socio (solo admin)")
    public ResponseEntity<SocioResponseDTO> crearSocio(
            @Valid @RequestBody CrearSocioRequestDTO request) {
        Socio socio = crearSocioUseCase.ejecutar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(socioDTOMapper.toResponseDTO(socio));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un socio por ID")
    public ResponseEntity<SocioResponseDTO> obtenerSocio(
            @PathVariable UUID id,
            HttpServletRequest request) {
        checkRateLimit(request);
        SocioResponseDTO response = obtenerSocioUseCase.ejecutar(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Listar socios con paginación")
    public ResponseEntity<Page<SocioResponseDTO>> listarSocios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaRegistro") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            HttpServletRequest request) {
        checkRateLimit(request);
        // SECURITY FIX: Limitar size para prevenir DoS
        int safeSize = Math.min(Math.max(size, MIN_PAGE_SIZE), MAX_PAGE_SIZE);
        
        Sort sort = direction.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, safeSize, sort);
        Page<Socio> socios = listarSociosUseCase.ejecutar(pageable);
        return ResponseEntity.ok(socios.map(socioDTOMapper::toResponseDTO));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar socios por criterios")
    public ResponseEntity<Page<SocioResponseDTO>> buscarSocios(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String apellido,
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) String numeroSocio,
            @RequestParam(required = false) String correo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        checkRateLimit(request);
        // SECURITY FIX: Limitar size para prevenir DoS
        int safeSize = Math.min(Math.max(size, MIN_PAGE_SIZE), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, safeSize);
        Page<Socio> socios = buscarSocioUseCase.ejecutar(
                nombre, apellido, numeroDocumento, numeroSocio, correo, pageable);
        return ResponseEntity.ok(socios.map(socioDTOMapper::toResponseDTO));
    }

    // Issue #179: solo ADMIN puede actualizar datos de cualquier socio. La actualización
    // de perfil propio del socio va por otro endpoint (/perfil) que sí valida ownership.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un socio (solo admin)")
    public ResponseEntity<SocioResponseDTO> actualizarSocio(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarSocioDTO request,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        SocioResponseDTO response = actualizarSocioUseCase.ejecutar(id, request, ip, userAgent);
        return ResponseEntity.ok(response);
    }

    // Issue #179: operaciones administrativas
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar un socio (solo admin)")
    public ResponseEntity<SocioResponseDTO> activarSocio(@PathVariable UUID id) {
        SocioResponseDTO response = activarSocioUseCase.ejecutar(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar un socio (solo admin)")
    public ResponseEntity<SocioResponseDTO> desactivarSocio(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivo) {
        SocioResponseDTO response = desactivarSocioUseCase.ejecutar(id, motivo);
        return ResponseEntity.ok(response);
    }

    // Issue #179: eliminación es operación crítica, solo admin.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un socio (soft delete, solo admin)")
    public ResponseEntity<Map<String, String>> eliminarSocio(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivo) {
        eliminarSocioUseCase.ejecutar(id, motivo);
        return ResponseEntity.ok(Map.of("mensaje", "Socio eliminado correctamente"));
    }
    
    /**
     * SECURITY: Rate limiting para prevenir ataques de enumeración y DoS.
     * Limita a MAX_REQUESTS_PER_MINUTE requests por IP.
     */
    private void checkRateLimit(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        long currentMinute = System.currentTimeMillis() / 1000 / 60;
        
        Long lastTimestamp = requestTimestamps.get(clientIp);
        if (lastTimestamp != null && !lastTimestamp.equals(currentMinute)) {
            // Nueva minuto, resetear contador
            requestCounts.put(clientIp, 1L);
            requestTimestamps.put(clientIp, currentMinute);
        } else {
            // Mismo minuto, incrementar
            Long count = requestCounts.merge(clientIp, 1L, (old, n) -> old + 1);
            if (count > MAX_REQUESTS_PER_MINUTE) {
                throw new TooManyRequestsException();
            }
            if (lastTimestamp == null) {
                requestTimestamps.put(clientIp, currentMinute);
            }
        }
    }
    
    /**
     * Obtiene la IP del cliente, considerando headers de proxy.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
    
    /**
     * Excepción personalizada para rate limit excedido.
     */
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS)
    public static class TooManyRequestsException extends RuntimeException {
        public TooManyRequestsException() {
            super("Demasiadas solicitudes. Por favor, intente nuevamente más tarde.");
        }
    }
}
