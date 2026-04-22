package com.tufondo.auth.infrastructure.persistence.jpa;

import com.tufondo.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio JPA para tokens de recuperación de contraseña.
 */
@Repository
public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    /**
     * Busca un token válido (no usado y no expirado).
     */
    @Query("SELECT t FROM PasswordResetTokenEntity t WHERE t.token = :token AND t.used = false AND t.expiresAt > :now")
    Optional<PasswordResetTokenEntity> findByTokenAndNotUsedAndNotExpired(
            @Param("token") String token,
            @Param("now") Instant now);

    /**
     * Busca un token por su valor.
     */
    Optional<PasswordResetTokenEntity> findByToken(String token);

    /**
     * Marca un token como usado.
     */
    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.used = true WHERE t.token = :token")
    void marcarComoUsado(@Param("token") String token);

    /**
     * Elimina tokens expirados (limpieza periódica).
     */
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.expiresAt < :now OR t.used = true")
    void eliminarTokensExpiradosYOUsados(@Param("now") Instant now);

    /**
     * Elimina todos los tokens de un usuario.
     */
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.usuarioId = :usuarioId")
    void eliminarTokensPorUsuario(@Param("usuarioId") UUID usuarioId);
}
