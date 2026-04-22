package com.tufondo.auth.infrastructure.persistence.adapter;

import com.tufondo.auth.domain.exception.UsuarioNoEncontradoException;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.persistence.entity.UsuarioEntity;
import com.tufondo.auth.infrastructure.persistence.jpa.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(UUID id) {
        return jpaRepository.findByIdAndCuentaActivaTrue(id)
                .map(UsuarioEntity::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        return jpaRepository.findByNombreUsuarioAndCuentaActivaTrue(nombreUsuario)
                .map(UsuarioEntity::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorCorreoElectronico(String correo) {
        return jpaRepository.findByCorreoElectronicoAndCuentaActivaTrue(correo.toLowerCase().trim())
                .map(UsuarioEntity::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorSocioId(UUID socioId) {
        return jpaRepository.findBySocioIdAndCuentaActivaTrue(socioId)
                .map(UsuarioEntity::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNombreUsuario(String nombreUsuario) {
        return jpaRepository.existsByNombreUsuarioAndCuentaActivaTrue(nombreUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCorreoElectronico(String correo) {
        return jpaRepository.existsByCorreoElectronicoAndCuentaActivaTrue(correo.toLowerCase().trim());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorSocioId(UUID socioId) {
        return jpaRepository.existsBySocioId(socioId);
    }

    @Override
    @Transactional
    public void guardar(Usuario usuario) {
        UsuarioEntity entity = UsuarioEntity.desdeDominio(usuario);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public void actualizar(Usuario usuario) {
        UsuarioEntity entity = jpaRepository.findById(usuario.id())
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "No se encontró usuario con ID: " + usuario.id()));
        
        entity.setNombreUsuario(usuario.nombreUsuario());
        entity.setCorreoElectronico(usuario.correoElectronico());
        entity.setPasswordHash(usuario.passwordHash());
        entity.setNombreCompleto(usuario.nombreCompleto());
        entity.setRol(usuario.rol());
        entity.setSocioId(usuario.socioId());
        entity.setCuentaActiva(usuario.cuentaActiva());
        entity.setUltimaModificacion(Instant.now());
        entity.setIntentosFallidos(usuario.intentosFallidos());
        entity.setFechaBloqueo(usuario.fechaBloqueo());
        
        jpaRepository.save(entity);
    }

    @Override
    @Transactional
    public void actualizarIntentosFallidos(UUID usuarioId, int intentos, Instant fechaBloqueo) {
        jpaRepository.actualizarIntentosFallidos(usuarioId, intentos, fechaBloqueo, Instant.now());
    }

    @Override
    @Transactional
    public void resetearIntentosFallidos(UUID usuarioId) {
        jpaRepository.resetearIntentosFallidos(usuarioId, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarAdminActivo() {
        return jpaRepository.findByRolAndCuentaActivaTrue(
                com.tufondo.auth.domain.model.enums.Rol.ADMIN)
                .stream()
                .findFirst()
                .map(UsuarioEntity::aDominio);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarSocioActivoPorSocioId(UUID socioId) {
        return jpaRepository.findBySocioIdAndRolAndCuentaActivaTrue(socioId,
                com.tufondo.auth.domain.model.enums.Rol.SOCIO)
                .map(UsuarioEntity::aDominio);
    }
}