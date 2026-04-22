// com/tufondo/socios/infrastructure/persistence/jpa/SocioJpaRepository.java
package com.tufondo.socios.infrastructure.persistence.jpa;

import com.tufondo.socios.infrastructure.persistence.entity.SocioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SocioJpaRepository extends JpaRepository<SocioEntity, UUID>, JpaSpecificationExecutor<SocioEntity> {

    boolean existsByNumeroSocio(String numeroSocio);

    boolean existsByNumeroDocumento(String numeroDocumento);

    @Query("SELECT COUNT(s) > 0 FROM SocioEntity s WHERE s.correoElectronico = :correo")
    boolean existsByCorreo(@Param("correo") String correo);

    java.util.Optional<SocioEntity> findByCorreoElectronico(String correo);

    List<SocioEntity> findByIdIn(List<UUID> ids);

    @Query("SELECT s FROM SocioEntity s WHERE s.estado <> 'ELIMINADO' AND " +
           "(:nombre IS NULL OR LOWER(s.primerNombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:apellido IS NULL OR LOWER(s.primerApellido) LIKE LOWER(CONCAT('%', :apellido, '%'))) AND " +
           "(:numeroDocumento IS NULL OR s.numeroDocumento LIKE CONCAT('%', :numeroDocumento, '%')) AND " +
           "(:numeroSocio IS NULL OR s.numeroSocio LIKE CONCAT('%', :numeroSocio, '%')) AND " +
           "(:correo IS NULL OR LOWER(s.correoElectronico) LIKE LOWER(CONCAT('%', :correo, '%')))")
    Page<SocioEntity> buscarPorCriterios(
            @Param("nombre") String nombre,
            @Param("apellido") String apellido,
            @Param("numeroDocumento") String numeroDocumento,
            @Param("numeroSocio") String numeroSocio,
            @Param("correo") String correo,
            Pageable pageable);

    long count();

    @Query("SELECT COUNT(s) FROM SocioEntity s WHERE s.estado = :estado")
    long countByEstado(@Param("estado") com.tufondo.socios.domain.model.enums.EstadoSocio estado);

    @Query("SELECT COUNT(s) FROM SocioEntity s WHERE s.fechaRegistro BETWEEN :inicio AND :fin")
    long countByFechaRegistroBetween(@Param("inicio") java.time.LocalDateTime inicio, @Param("fin") java.time.LocalDateTime fin);
}
