package com.tufondo.auth.domain.repository;

import com.tufondo.auth.domain.model.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto (interface) para el repositorio de tokens de recuperación de contraseña.
 */
public interface PasswordResetTokenRepository {

    /**
     * Guarda un token de recuperación.
     */
    void guardar(PasswordResetToken token);

    /**
     * Busca un token válido (no usado y no expirado).
     */
    Optional<PasswordResetToken> buscarTokenValido(String token);

    /**
     * Busca un token por su valor.
     */
    Optional<PasswordResetToken> buscarPorToken(String token);

    /**
     * Marca un token como usado.
     */
    void marcarComoUsado(String token);

    /**
     * Elimina todos los tokens de un usuario.
     */
    void eliminarTokensPorUsuario(UUID usuarioId);

    /**
     * Limpia tokens expirados y usados (mantenimiento).
     */
    void limpiarTokensExpirados();
}
