package com.tufondo.notificaciones.application.usecase;

import com.tufondo.notificaciones.application.dto.NotificacionListResponseDTO;
import com.tufondo.notificaciones.application.dto.NotificacionResponseDTO;
import com.tufondo.notificaciones.domain.model.Notificacion;
import com.tufondo.notificaciones.domain.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lista notificaciones del destinatario autenticado (issue #214).
 *
 * <p>Filtro opcional {@code soloNoLeidas} para que el dropdown del Bell
 * pueda pedir solo las pendientes (UX más limpia que mostrar el histórico
 * completo en el dropdown).</p>
 */
@Service
@RequiredArgsConstructor
public class ListarNotificacionesUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final NotificacionRepository notificacionRepository;

    @Transactional(readOnly = true)
    public NotificacionListResponseDTO ejecutar(
            UUID destinatarioId,
            int page,
            int size,
            boolean soloNoLeidas
    ) {
        // Defensive: limitar size para prevenir abuso (mismo patrón que SocioController)
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Notificacion> pageResult = soloNoLeidas
                ? notificacionRepository.listarNoLeidasPorDestinatario(destinatarioId, pageable)
                : notificacionRepository.listarPorDestinatario(destinatarioId, pageable);

        List<NotificacionResponseDTO> items = pageResult.getContent().stream()
                .map(NotificacionResponseDTO::desde)
                .toList();

        // Contador de no-leídas: siempre se devuelve para el badge del Bell.
        // No depende del filtro `soloNoLeidas` — el badge muestra el total real.
        long noLeidas = notificacionRepository.contarNoLeidasPorDestinatario(destinatarioId);

        return new NotificacionListResponseDTO(
                items,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                noLeidas
        );
    }
}
