package com.tufondo.auth.infrastructure.persistence.jpa;

import com.tufondo.auth.domain.model.enums.TipoToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.tufondo.auth.infrastructure.persistence.entity.SesionEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SesionJpaRepository extends JpaRepository<SesionEntity, UUID> {

    Optional<SesionEntity> findByIdAndActivoTrue(UUID id);

    Optional<SesionEntity> findByRefreshTokenHashAndActivoTrue(String refreshTokenHash);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM SesionEntity s WHERE s.usuarioId = :usuarioId AND s.tipoToken = :tipoToken AND s.activo = true")
    Optional<SesionEntity> findActivaPorUsuarioYTipo(UUID usuarioId, String tipoToken);

    List<SesionEntity> findByUsuarioIdAndActivoTrue(UUID usuarioId);

    boolean existsByIdAndActivoTrue(UUID id);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE SesionEntity s SET s.activo = false WHERE s.id = :id")
    void invalidarPorId(UUID id);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE SesionEntity s SET s.activo = false WHERE s.refreshTokenHash = :refreshTokenHash")
    void invalidarPorRefreshToken(String refreshTokenHash);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE SesionEntity s SET s.activo = false WHERE s.usuarioId = :usuarioId")
    void invalidarTodasPorUsuario(UUID usuarioId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE SesionEntity s SET s.activo = false WHERE s.usuarioId = :usuarioId AND s.tipoToken = :tipoToken AND s.fechaCreacion < :fechaLimite")
    void invalidarSesionesAnteriores(UUID usuarioId, TipoToken tipo, Instant fechaLimite);

    int countByUsuarioIdAndActivoTrue(UUID usuarioId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM SesionEntity s WHERE s.refreshTokenExpiracion < :ahora")
    void limpiarSesionesExpiradas(Instant ahora);
}
