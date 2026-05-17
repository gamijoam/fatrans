package com.tufondo.notificaciones.application.usecase;

import com.tufondo.notificaciones.domain.model.Notificacion;
import com.tufondo.notificaciones.domain.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Marca una notificación específica como leída (issue #214).
 *
 * <p>Crítico: valida ownership antes de modificar — un usuario NO puede
 * marcar leída una notificación de OTRO usuario (anti-IDOR, lección del
 * issue #179).</p>
 *
 * <p>Idempotente: si la notificación ya está leída, no hace nada y retorna
 * sin error (cliente puede marcar dos veces sin consecuencias).</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarcarLeidaUseCase {

    private final NotificacionRepository notificacionRepository;

    @Transactional
    public void ejecutar(UUID notificacionId, UUID usuarioId) {
        Notificacion notificacion = notificacionRepository.buscarPorId(notificacionId)
                .orElseThrow(() -> new NotificacionNoEncontradaException(notificacionId));

        // Anti-IDOR: el usuario solo puede marcar SUS propias notificaciones.
        // Sin este check, conocer un UUID válido de notificación permitía
        // marcar como leídas las de otros.
        if (!notificacion.perteneceA(usuarioId)) {
            log.warn("Intento de marcar leída notificación ajena: notifId={}, usuarioActual={}",
                    notificacionId, usuarioId);
            throw new AccessDeniedException(
                    "No tienes permisos para modificar esta notificación");
        }

        // Idempotente: si ya está leída, no hace nada
        Notificacion actualizada = notificacion.marcarLeida();
        if (actualizada != notificacion) {
            notificacionRepository.guardar(actualizada);
        }
    }

    public static class NotificacionNoEncontradaException extends RuntimeException {
        public NotificacionNoEncontradaException(UUID id) {
            super("Notificación no encontrada: " + id);
        }
    }
}
