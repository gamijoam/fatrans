package com.tufondo.auth.domain.repository;

import com.tufondo.auth.domain.model.Sesion;
import com.tufondo.auth.domain.model.enums.TipoToken;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SesionRepository {

    Optional<Sesion> buscarPorTokenId(String tokenId);

    Optional<Sesion> buscarPorRefreshToken(String refreshToken);

    Optional<Sesion> buscarPorRefreshTokenHash(String refreshTokenHash);

    Optional<Sesion> buscarActivaPorUsuarioYTipo(UUID usuarioId, TipoToken tipo);

    List<Sesion> buscarSesionesActivasPorUsuario(UUID usuarioId);

    boolean estaActiva(String tokenId);

    void guardar(Sesion sesion);

    void invalidarPorTokenId(String tokenId);

    void invalidarPorRefreshToken(String refreshToken);

    void invalidarTodasPorUsuario(UUID usuarioId);

    void invalidarSesionesAnteriores(UUID usuarioId, TipoToken tipo);

    int contarSesionesActivasPorUsuario(UUID usuarioId);

    void limpiarSesionesExpiradas();
}
