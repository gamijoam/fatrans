package com.tufondo.auth.infrastructure.persistence.jpa;

import com.tufondo.auth.domain.model.enums.Permiso;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.infrastructure.persistence.entity.RolPermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RolPermisoJpaRepository extends JpaRepository<RolPermisoEntity, UUID> {

    List<RolPermisoEntity> findByRol(Rol rol);

    List<RolPermisoEntity> findByRolIn(List<Rol> roles);

    @Query("SELECT rp.permiso FROM RolPermisoEntity rp WHERE rp.rol = :rol")
    List<Permiso> findPermisosByRol(@Param("rol") Rol rol);

    @Query("SELECT rp.permiso FROM RolPermisoEntity rp WHERE rp.rol IN :roles")
    List<Permiso> findPermisosByRolIn(@Param("roles") List<Rol> roles);

    @Query("SELECT CASE WHEN COUNT(rp) > 0 THEN true ELSE false END FROM RolPermisoEntity rp WHERE rp.rol = :rol AND rp.permiso = :permiso")
    boolean existsByRolAndPermiso(@Param("rol") Rol rol, @Param("permiso") Permiso permiso);

    void deleteByRol(Rol rol);
}