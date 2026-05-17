package com.tufondo.notificaciones.infrastructure.persistence.jpa;

import com.tufondo.notificaciones.infrastructure.persistence.entity.NotificacionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface NotificacionJpaRepository extends JpaRepository<NotificacionEntity, UUID> {

    Page<NotificacionEntity> findByDestinatarioIdOrderByCreatedAtDesc(
            UUID destinatarioId, Pageable pageable);

    Page<NotificacionEntity> findByDestinatarioIdAndLeidaFalseOrderByCreatedAtDesc(
            UUID destinatarioId, Pageable pageable);

    long countByDestinatarioIdAndLeidaFalse(UUID destinatarioId);

    /**
     * UPDATE bulk: marca como leídas TODAS las no-leídas del destinatario
     * en una sola query. Mucho más eficiente que cargar+iterar+save.
     */
    @Modifying
    @Query("UPDATE NotificacionEntity n " +
           "SET n.leida = true, n.fechaLectura = :ahora " +
           "WHERE n.destinatarioId = :destinatarioId AND n.leida = false")
    int marcarTodasLeidas(@Param("destinatarioId") UUID destinatarioId,
                          @Param("ahora") Instant ahora);
}
