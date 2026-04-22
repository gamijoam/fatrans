package com.tufondo.auth.infrastructure.persistence.adapter;

import com.tufondo.auth.domain.model.Sesion;
import com.tufondo.auth.domain.model.enums.TipoToken;
import com.tufondo.auth.domain.repository.SesionRepository;
import com.tufondo.auth.infrastructure.persistence.entity.SesionEntity;
import com.tufondo.auth.infrastructure.persistence.jpa.SesionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SesionRepositoryImpl implements SesionRepository {

    private final SesionJpaRepository jpaRepository;

    @Value("${jwt.sesion.ventana-inicializacion-segundos:60}")
    private int ventanaInicializacionSegundos;

    @Override
    @Transactional(readOnly = true)
    public Optional<Sesion> buscarPorTokenId(String tokenId) {
        try {
            UUID id = UUID.fromString(tokenId);
            return jpaRepository.findByIdAndActivoTrue(id)
                    .map(SesionEntity::aDominio);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sesion> buscarPorRefreshToken(String refreshToken) {
        return jpaRepository.findByRefreshTokenHashAndActivoTrue(refreshToken)
                .map(SesionEntity::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sesion> buscarPorRefreshTokenHash(String refreshTokenHash) {
        return jpaRepository.findByRefreshTokenHashAndActivoTrue(refreshTokenHash)
                .map(SesionEntity::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sesion> buscarActivaPorUsuarioYTipo(UUID usuarioId, TipoToken tipo) {
        return jpaRepository.findActivaPorUsuarioYTipo(usuarioId, tipo.name())
                .map(SesionEntity::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sesion> buscarSesionesActivasPorUsuario(UUID usuarioId) {
        List<SesionEntity> entidades = jpaRepository.findByUsuarioIdAndActivoTrue(usuarioId);
        if (entidades == null) {
            return List.of();
        }
        return entidades.stream()
                .map(SesionEntity::aDominio)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaActiva(String tokenId) {
        try {
            return jpaRepository.existsByIdAndActivoTrue(UUID.fromString(tokenId));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    @Transactional
    public void guardar(Sesion sesion) {
        SesionEntity entity = SesionEntity.desdeDominio(sesion);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public void invalidarPorTokenId(String tokenId) {
        try {
            jpaRepository.invalidarPorId(UUID.fromString(tokenId));
        } catch (IllegalArgumentException e) {
            // Log ignored
        }
    }

    @Override
    @Transactional
    public void invalidarPorRefreshToken(String refreshToken) {
        jpaRepository.invalidarPorRefreshToken(refreshToken);
    }

    @Override
    @Transactional
    public void invalidarTodasPorUsuario(UUID usuarioId) {
        jpaRepository.invalidarTodasPorUsuario(usuarioId);
    }

    @Override
    @Transactional
    public void invalidarSesionesAnteriores(UUID usuarioId, TipoToken tipo) {
        Instant fechaLimite = Instant.now().minusSeconds(ventanaInicializacionSegundos);
        jpaRepository.invalidarSesionesAnteriores(usuarioId, tipo, fechaLimite);
    }

    @Override
    @Transactional(readOnly = true)
    public int contarSesionesActivasPorUsuario(UUID usuarioId) {
        return jpaRepository.countByUsuarioIdAndActivoTrue(usuarioId);
    }

    @Override
    @Transactional
    public void limpiarSesionesExpiradas() {
        jpaRepository.limpiarSesionesExpiradas(Instant.now());
    }
}