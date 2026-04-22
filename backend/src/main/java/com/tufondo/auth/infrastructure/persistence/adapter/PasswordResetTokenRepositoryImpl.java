package com.tufondo.auth.infrastructure.persistence.adapter;

import com.tufondo.auth.domain.model.PasswordResetToken;
import com.tufondo.auth.domain.repository.PasswordResetTokenRepository;
import com.tufondo.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import com.tufondo.auth.infrastructure.persistence.jpa.PasswordResetTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del repositorio de tokens de recuperación de contraseña.
 */
@Component
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;

    @Override
    @Transactional
    public void guardar(PasswordResetToken token) {
        PasswordResetTokenEntity entity = PasswordResetTokenEntity.desdeDominio(token);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> buscarTokenValido(String token) {
        return jpaRepository.findByTokenAndNotUsedAndNotExpired(token, Instant.now())
                .map(PasswordResetTokenEntity::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PasswordResetToken> buscarPorToken(String token) {
        return jpaRepository.findByToken(token)
                .map(PasswordResetTokenEntity::aDominio);
    }

    @Override
    @Transactional
    public void marcarComoUsado(String token) {
        jpaRepository.marcarComoUsado(token);
    }

    @Override
    @Transactional
    public void eliminarTokensPorUsuario(UUID usuarioId) {
        jpaRepository.eliminarTokensPorUsuario(usuarioId);
    }

    @Override
    @Transactional
    public void limpiarTokensExpirados() {
        jpaRepository.eliminarTokensExpiradosYOUsados(Instant.now());
    }
}
