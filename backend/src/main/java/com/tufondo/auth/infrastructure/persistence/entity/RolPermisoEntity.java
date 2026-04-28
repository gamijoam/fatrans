package com.tufondo.auth.infrastructure.persistence.entity;

import com.tufondo.auth.domain.model.enums.Permiso;
import com.tufondo.auth.domain.model.enums.Rol;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "rol_permiso")
public class RolPermisoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "permiso", nullable = false)
    private Permiso permiso;

    public RolPermisoEntity() {}

    public RolPermisoEntity(Rol rol, Permiso permiso) {
        this.rol = rol;
        this.permiso = permiso;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public Permiso getPermiso() { return permiso; }
    public void setPermiso(Permiso permiso) { this.permiso = permiso; }
}