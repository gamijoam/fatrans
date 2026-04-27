package com.tufondo.auth.domain.repository;

import com.tufondo.auth.domain.model.enums.Permiso;
import com.tufondo.auth.domain.model.enums.Rol;

import java.util.List;
import java.util.UUID;

public interface RolPermisoRepository {

    List<Permiso> obtenerPermisosPorRol(Rol rol);

    List<Permiso> obtenerPermisosPorRoles(List<Rol> roles);

    boolean tienePermiso(Rol rol, Permiso permiso);

    boolean tieneAlgunPermiso(Rol rol, List<Permiso> permisos);

    boolean tieneTodosLosPermisos(Rol rol, List<Permiso> permisos);

    void asignarPermiso(Rol rol, Permiso permiso);

    void quitarPermiso(Rol rol, Permiso permiso);

    void inicializarPermisosDefault();
}