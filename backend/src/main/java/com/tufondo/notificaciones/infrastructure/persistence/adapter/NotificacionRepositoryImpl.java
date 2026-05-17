package com.tufondo.notificaciones.infrastructure.persistence.adapter;

import com.tufondo.notificaciones.domain.model.Notificacion;
import com.tufondo.notificaciones.domain.repository.NotificacionRepository;
import com.tufondo.notificaciones.infrastructure.persistence.entity.NotificacionEntity;
import com.tufondo.notificaciones.infrastructure.persistence.jpa.NotificacionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador JPA del port {@link NotificacionRepository} (issue #214).
 */
@Component
@RequiredArgsConstructor
public class NotificacionRepositoryImpl implements NotificacionRepository {

    private final NotificacionJpaRepository jpa;

    @Override
    public Optional<Notificacion> buscarPorId(UUID id) {
        return jpa.findById(id).map(NotificacionEntity::aDominio);
    }

    @Override
    public Page<Notificacion> listarPorDestinatario(UUID destinatarioId, Pageable pageable) {
        return jpa.findByDestinatarioIdOrderByCreatedAtDesc(destinatarioId, pageable)
                .map(NotificacionEntity::aDominio);
    }

    @Override
    public Page<Notificacion> listarNoLeidasPorDestinatario(UUID destinatarioId, Pageable pageable) {
        return jpa.findByDestinatarioIdAndLeidaFalseOrderByCreatedAtDesc(destinatarioId, pageable)
                .map(NotificacionEntity::aDominio);
    }

    @Override
    public long contarNoLeidasPorDestinatario(UUID destinatarioId) {
        return jpa.countByDestinatarioIdAndLeidaFalse(destinatarioId);
    }

    @Override
    public void guardar(Notificacion notificacion) {
        jpa.save(NotificacionEntity.desdeDominio(notificacion));
    }

    @Override
    public int marcarTodasLeidas(UUID destinatarioId) {
        return jpa.marcarTodasLeidas(destinatarioId, Instant.now());
    }
}
