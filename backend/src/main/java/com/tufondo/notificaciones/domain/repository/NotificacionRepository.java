package com.tufondo.notificaciones.domain.repository;

import com.tufondo.notificaciones.domain.model.Notificacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Port del dominio para persistencia de notificaciones (issue #214).
 */
public interface NotificacionRepository {

    Optional<Notificacion> buscarPorId(UUID id);

    /**
     * Lista notificaciones de un destinatario paginadas, ordenadas por fecha
     * descendente (más nuevas primero).
     */
    Page<Notificacion> listarPorDestinatario(UUID destinatarioId, Pageable pageable);

    /**
     * Variante con filtro: solo no-leídas.
     */
    Page<Notificacion> listarNoLeidasPorDestinatario(UUID destinatarioId, Pageable pageable);

    /**
     * Cuenta no-leídas. Usado por el badge del Bell (consulta más frecuente).
     */
    long contarNoLeidasPorDestinatario(UUID destinatarioId);

    void guardar(Notificacion notificacion);

    /**
     * Marca como leídas todas las notificaciones no leídas de un destinatario
     * en una sola operación atómica (más eficiente que iterar).
     *
     * @return cantidad de notificaciones actualizadas
     */
    int marcarTodasLeidas(UUID destinatarioId);
}
