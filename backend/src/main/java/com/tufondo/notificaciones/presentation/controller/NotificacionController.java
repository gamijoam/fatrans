package com.tufondo.notificaciones.presentation.controller;

import com.tufondo.notificaciones.application.dto.NotificacionListResponseDTO;
import com.tufondo.notificaciones.application.usecase.ListarNotificacionesUseCase;
import com.tufondo.notificaciones.application.usecase.MarcarLeidaUseCase;
import com.tufondo.notificaciones.application.usecase.MarcarTodasLeidasUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Endpoints REST de notificaciones in-app (issue #214, PR-A).
 *
 * <p>Todos los endpoints son del usuario autenticado — un usuario NUNCA
 * puede leer/modificar notificaciones de otro. La autorización viene del
 * JWT (extraerUsuarioId), no de query params (anti-IDOR).</p>
 */
@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Notificaciones in-app del usuario")
public class NotificacionController {

    private final ListarNotificacionesUseCase listarUseCase;
    private final MarcarLeidaUseCase marcarLeidaUseCase;
    private final MarcarTodasLeidasUseCase marcarTodasUseCase;

    /**
     * GET /notificaciones?page=0&size=20&soloNoLeidas=false
     *
     * Lista las notificaciones del usuario autenticado, paginadas.
     * Incluye en el response el contador total de no-leídas (para el badge).
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar mis notificaciones",
               description = "Paginado. Filtro opcional soloNoLeidas=true para el dropdown del Bell.")
    public ResponseEntity<NotificacionListResponseDTO> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean soloNoLeidas,
            Authentication authentication) {
        UUID usuarioId = extraerUsuarioId(authentication);
        return ResponseEntity.ok(listarUseCase.ejecutar(usuarioId, page, size, soloNoLeidas));
    }

    /**
     * GET /notificaciones/count
     *
     * Endpoint ligero solo para el contador del badge — evita transferir
     * la lista completa cuando solo necesitamos el número. Polling cada 30s
     * desde el frontend lo llamará constantemente.
     */
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Contar mis notificaciones no leídas",
               description = "Endpoint ligero para el badge del Bell. Optimizado con índice parcial en BD.")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(Authentication authentication) {
        UUID usuarioId = extraerUsuarioId(authentication);
        NotificacionListResponseDTO r = listarUseCase.ejecutar(usuarioId, 0, 1, true);
        return ResponseEntity.ok(Map.of("noLeidas", r.noLeidas()));
    }

    /**
     * PATCH /notificaciones/{id}/leida
     *
     * Marca UNA notificación específica como leída. Anti-IDOR: el use case
     * valida que la notificación pertenezca al usuario autenticado.
     */
    @PatchMapping("/{id}/leida")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Marcar una notificación como leída")
    public ResponseEntity<Void> marcarLeida(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID usuarioId = extraerUsuarioId(authentication);
        marcarLeidaUseCase.ejecutar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /notificaciones/marcar-todas-leidas
     *
     * Marca como leídas TODAS las notificaciones no leídas del usuario
     * autenticado. Útil para el botón "Marcar todas como leídas" del
     * dropdown.
     */
    @PostMapping("/marcar-todas-leidas")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Marcar todas mis notificaciones como leídas")
    public ResponseEntity<Map<String, Integer>> marcarTodasLeidas(Authentication authentication) {
        UUID usuarioId = extraerUsuarioId(authentication);
        int actualizadas = marcarTodasUseCase.ejecutar(usuarioId);
        return ResponseEntity.ok(Map.of("actualizadas", actualizadas));
    }

    /**
     * Extrae el userId del JWT, NUNCA de query params.
     * Patrón ya usado en otros controllers (auth, ahorros).
     */
    private UUID extraerUsuarioId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.tufondo.auth.infrastructure.security.AuthenticatedUser authUser) {
            return authUser.getUserId();
        }
        return UUID.fromString(authentication.getName());
    }
}
