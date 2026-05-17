package com.tufondo.notificaciones.application.usecase;

import com.tufondo.notificaciones.domain.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Marca todas las notificaciones no leídas del usuario como leídas (issue #214).
 *
 * <p>Operación atómica en BD (UPDATE con WHERE leida=false) — más eficiente
 * que cargarlas y guardarlas una por una.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarcarTodasLeidasUseCase {

    private final NotificacionRepository notificacionRepository;

    @Transactional
    public int ejecutar(UUID destinatarioId) {
        int actualizadas = notificacionRepository.marcarTodasLeidas(destinatarioId);
        log.info("Marcadas {} notificaciones como leídas para usuario {}",
                actualizadas, destinatarioId);
        return actualizadas;
    }
}
