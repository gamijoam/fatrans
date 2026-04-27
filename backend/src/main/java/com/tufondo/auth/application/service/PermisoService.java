package com.tufondo.auth.application.service;

import com.tufondo.auth.domain.model.enums.Permiso;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.domain.repository.RolPermisoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service("permisoService")
@RequiredArgsConstructor
@Slf4j
public class PermisoService {

    private final RolPermisoRepository rolPermisoRepository;

    public boolean tienePermiso(Authentication authentication, Permiso permiso) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Rol rol = extraerRol(authentication);
        if (rol == null) {
            return false;
        }
        return rolPermisoRepository.tienePermiso(rol, permiso);
    }

    public boolean tieneAlgunPermiso(Authentication authentication, Permiso... permisos) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Rol rol = extraerRol(authentication);
        if (rol == null) {
            return false;
        }
        return rolPermisoRepository.tieneAlgunPermiso(rol, List.of(permisos));
    }

    public boolean tieneTodosLosPermisos(Authentication authentication, Permiso... permisos) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Rol rol = extraerRol(authentication);
        if (rol == null) {
            return false;
        }
        return rolPermisoRepository.tieneTodosLosPermisos(rol, List.of(permisos));
    }

    public boolean tieneRol(Authentication authentication, Rol... roles) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (Rol rol : roles) {
            if (authorities.stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol.name()))) {
                return true;
            }
        }
        return false;
    }

    public boolean esAdmin(Authentication authentication) {
        return tieneRol(authentication, Rol.ADMIN, Rol.SUPER_ADMIN);
    }

    public boolean esSuperAdmin(Authentication authentication) {
        return tieneRol(authentication, Rol.SUPER_ADMIN);
    }

    public boolean esSocio(Authentication authentication) {
        return tieneRol(authentication, Rol.SOCIO);
    }

    public boolean esCajero(Authentication authentication) {
        return tieneRol(authentication, Rol.CAJERO);
    }

    public boolean esAnalistaKyc(Authentication authentication) {
        return tieneRol(authentication, Rol.ANALISTA_KYC);
    }

    public boolean esSistema(Authentication authentication) {
        return tieneRol(authentication, Rol.SISTEMA);
    }

    public List<Permiso> obtenerPermisosDelUsuario(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return List.of();
        }
        Rol rol = extraerRol(authentication);
        if (rol == null) {
            return List.of();
        }
        return rolPermisoRepository.obtenerPermisosPorRol(rol);
    }

    public boolean puedeAccederSocio(Authentication authentication, UUID socioId) {
        if (authentication == null) {
            return false;
        }
        if (esAdmin(authentication) || esSuperAdmin(authentication)) {
            return true;
        }
        return false;
    }

    private Rol extraerRol(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            String auth = authority.getAuthority();
            if (auth.startsWith("ROLE_")) {
                String rolName = auth.substring(5);
                try {
                    return Rol.valueOf(rolName);
                } catch (IllegalArgumentException e) {
                    log.warn("Rol desconocido en token: {}", rolName);
                }
            }
        }
        return null;
    }
}