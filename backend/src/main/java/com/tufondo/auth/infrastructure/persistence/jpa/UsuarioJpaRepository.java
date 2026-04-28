package com.tufondo.auth.infrastructure.persistence.jpa;

import com.tufondo.auth.domain.model.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.tufondo.auth.infrastructure.persistence.entity.UsuarioEntity;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    Optional<UsuarioEntity> findByCorreoElectronico(String correoElectronico);

    Optional<UsuarioEntity> findByNombreUsuario(String nombreUsuario);

    boolean existsByCorreoElectronico(String correoElectronico);

    boolean existsByNombreUsuario(String nombreUsuario);

    // Métodos para búsqueda de cuentas activas
    Optional<UsuarioEntity> findByIdAndCuentaActivaTrue(UUID id);

    Optional<UsuarioEntity> findByNombreUsuarioAndCuentaActivaTrue(String nombreUsuario);

    Optional<UsuarioEntity> findByCorreoElectronicoAndCuentaActivaTrue(String correoElectronico);

    Optional<UsuarioEntity> findBySocioIdAndCuentaActivaTrue(UUID socioId);

    boolean existsByNombreUsuarioAndCuentaActivaTrue(String nombreUsuario);

    boolean existsByCorreoElectronicoAndCuentaActivaTrue(String correoElectronico);

    boolean existsBySocioId(UUID socioId);

    // Búsqueda por Rol
    @Query("SELECT u FROM UsuarioEntity u WHERE u.rol = :rol AND u.cuentaActiva = true")
    List<UsuarioEntity> findByRolAndCuentaActivaTrue(@Param("rol") Rol rol);

    @Query("SELECT u FROM UsuarioEntity u WHERE u.socioId = :socioId AND u.rol = :rol AND u.cuentaActiva = true")
    Optional<UsuarioEntity> findBySocioIdAndRolAndCuentaActivaTrue(@Param("socioId") UUID socioId, @Param("rol") Rol rol);

    // Auditoría e Intentos
    @Modifying
    @Query("UPDATE UsuarioEntity u SET u.intentosFallidos = :intentos, u.fechaBloqueo = :fechaBloqueo, u.ultimaModificacion = :ahora WHERE u.id = :id")
    void actualizarIntentosFallidos(@Param("id") UUID id, @Param("intentos") int intentos, @Param("fechaBloqueo") Instant fechaBloqueo, @Param("ahora") Instant ahora);

    @Modifying
    @Query("UPDATE UsuarioEntity u SET u.intentosFallidos = 0, u.fechaBloqueo = null, u.ultimaModificacion = :ahora WHERE u.id = :id")
    void resetearIntentosFallidos(@Param("id") UUID id, @Param("ahora") Instant ahora);

    List<UsuarioEntity> findByRol(Rol rol);
}
