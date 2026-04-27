package com.tufondo.auth.application.service;

import com.tufondo.auth.domain.model.enums.Permiso;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.domain.repository.RolPermisoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PermisoService - Tests")
class PermisoServiceTest {

    @Mock
    private RolPermisoRepository rolPermisoRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PermisoService permisoService;

    private Collection<? extends GrantedAuthority> authorities(String... roles) {
        return List.of(roles).stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .map(a -> (GrantedAuthority) a)
                .toList();
    }

    @Nested
    @DisplayName("tienePermiso")
    class TienePermisoTests {

        @Test
        @DisplayName("Retorna true cuando rol tiene el permiso")
        void retorna_true_cuando_tiene_permiso() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("ADMIN")).when(authentication).getAuthorities();
            when(rolPermisoRepository.tienePermiso(Rol.ADMIN, Permiso.VER_AUDITORIA))
                    .thenReturn(true);

            boolean result = permisoService.tienePermiso(authentication, Permiso.VER_AUDITORIA);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Retorna false cuando rol no tiene el permiso")
        void retorna_false_cuando_no_tiene_permiso() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("SOCIO")).when(authentication).getAuthorities();
            when(rolPermisoRepository.tienePermiso(Rol.SOCIO, Permiso.VER_AUDITORIA))
                    .thenReturn(false);

            boolean result = permisoService.tienePermiso(authentication, Permiso.VER_AUDITORIA);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Retorna false cuando authentication es null")
        void retorna_false_cuando_auth_null() {
            boolean result = permisoService.tienePermiso(null, Permiso.VER_AUDITORIA);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Retorna false cuando no está autenticado")
        void retorna_false_cuando_no_autenticado() {
            doReturn(false).when(authentication).isAuthenticated();

            boolean result = permisoService.tienePermiso(authentication, Permiso.VER_AUDITORIA);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("tieneRol")
    class TieneRolTests {

        @Test
        @DisplayName("Retorna true cuando tiene el rol")
        void retorna_true_cuando_tiene_rol() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("ADMIN")).when(authentication).getAuthorities();

            boolean result = permisoService.tieneRol(authentication, Rol.ADMIN);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Retorna false cuando no tiene el rol")
        void retorna_false_cuando_no_tiene_rol() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("SOCIO")).when(authentication).getAuthorities();

            boolean result = permisoService.tieneRol(authentication, Rol.ADMIN);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Retorna true cuando tiene alguno de los roles")
        void retorna_true_cuando_tiene_algun_rol() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("CAJERO")).when(authentication).getAuthorities();

            boolean result = permisoService.tieneRol(authentication, Rol.ADMIN, Rol.CAJERO);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Metodos de ayuda")
    class MetodosAyudaTests {

        @Test
        @DisplayName("esAdmin retorna true para ADMIN")
        void es_admin_retorna_true() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("ADMIN")).when(authentication).getAuthorities();

            assertThat(permisoService.esAdmin(authentication)).isTrue();
        }

        @Test
        @DisplayName("esAdmin retorna true para SUPER_ADMIN")
        void es_admin_retorna_true_super_admin() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("SUPER_ADMIN")).when(authentication).getAuthorities();

            assertThat(permisoService.esAdmin(authentication)).isTrue();
        }

        @Test
        @DisplayName("esSuperAdmin retorna true solo para SUPER_ADMIN")
        void es_super_admin_retorna_true() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("SUPER_ADMIN")).when(authentication).getAuthorities();

            assertThat(permisoService.esSuperAdmin(authentication)).isTrue();
        }

        @Test
        @DisplayName("esSocio retorna true para SOCIO")
        void es_socio_retorna_true() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("SOCIO")).when(authentication).getAuthorities();

            assertThat(permisoService.esSocio(authentication)).isTrue();
        }

        @Test
        @DisplayName("esCajero retorna true para CAJERO")
        void es_cajero_retorna_true() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("CAJERO")).when(authentication).getAuthorities();

            assertThat(permisoService.esCajero(authentication)).isTrue();
        }

        @Test
        @DisplayName("esAnalistaKyc retorna true para ANALISTA_KYC")
        void es_analista_kyc_retorna_true() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("ANALISTA_KYC")).when(authentication).getAuthorities();

            assertThat(permisoService.esAnalistaKyc(authentication)).isTrue();
        }

        @Test
        @DisplayName("Retorna lista vacía cuando auth null")
        void retorna_lista_vacia_cuando_auth_null() {
            List<Permiso> result = permisoService.obtenerPermisosDelUsuario(null);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("tieneAlgunPermiso")
    class TieneAlgunPermisoTests {

        @Test
        @DisplayName("Retorna true cuando tiene al menos un permiso")
        void retorna_true_cuando_tiene_al_menos_uno() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("ADMIN")).when(authentication).getAuthorities();
            when(rolPermisoRepository.tieneAlgunPermiso(eq(Rol.ADMIN), any()))
                    .thenReturn(true);

            boolean result = permisoService.tieneAlgunPermiso(authentication,
                    Permiso.VER_AUDITORIA, Permiso.GESTIONAR_USUARIOS);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Retorna false cuando no tiene ninguno de los permisos")
        void retorna_false_cuando_no_tiene_ninguno() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("SOCIO")).when(authentication).getAuthorities();
            when(rolPermisoRepository.tieneAlgunPermiso(eq(Rol.SOCIO), any()))
                    .thenReturn(false);

            boolean result = permisoService.tieneAlgunPermiso(authentication,
                    Permiso.VER_AUDITORIA, Permiso.GESTIONAR_USUARIOS);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("obtenerPermisosDelUsuario")
    class ObtenerPermisosTests {

        @Test
        @DisplayName("Retorna permisos del rol")
        void retorna_permisos_del_rol() {
            doReturn(true).when(authentication).isAuthenticated();
            doReturn(authorities("ADMIN")).when(authentication).getAuthorities();
            when(rolPermisoRepository.obtenerPermisosPorRol(Rol.ADMIN))
                    .thenReturn(List.of(Permiso.VER_AUDITORIA, Permiso.GESTIONAR_USUARIOS));

            List<Permiso> result = permisoService.obtenerPermisosDelUsuario(authentication);

            assertThat(result).containsExactlyInAnyOrder(Permiso.VER_AUDITORIA, Permiso.GESTIONAR_USUARIOS);
        }
    }
}