package com.tufondo.auth.infrastructure.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.security.Principal;
import java.util.Collection;
import java.util.UUID;

@Getter
public class AuthenticatedUser implements Principal {
    private final UUID userId;
    private final String nombreUsuario;
    private final UUID socioId;
    private final String rol;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(UUID userId, String nombreUsuario, UUID socioId, String rol,
                             Collection<? extends GrantedAuthority> authorities) {
        this.userId = userId;
        this.nombreUsuario = nombreUsuario;
        this.socioId = socioId;
        this.rol = rol;
        this.authorities = authorities;
    }

    @Override
    public String getName() {
        return userId.toString();
    }
}