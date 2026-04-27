package com.tufondo.auth.infrastructure.persistence.adapter;

import com.tufondo.auth.domain.model.enums.Permiso;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.infrastructure.persistence.entity.RolPermisoEntity;
import com.tufondo.auth.infrastructure.persistence.jpa.RolPermisoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RolPermisoRepositoryImpl - Tests")
class RolPermisoRepositoryImplTest {

    @Mock
    private RolPermisoJpaRepository jpaRepository;

    @InjectMocks
    private RolPermisoRepositoryImpl repository;

    @Nested
    @DisplayName("obtenerPermisosPorRol")
    class ObtenerPermisosPorRolTests {

        @Test
        @DisplayName("Retorna permisos desde BD cuando existen")
        void retorna_desde_bd_cuando_existen() {
            RolPermisoEntity entity = new RolPermisoEntity(Rol.ADMIN, Permiso.VER_AUDITORIA);
            when(jpaRepository.findByRol(Rol.ADMIN)).thenReturn(List.of(entity));

            List<Permiso> result = repository.obtenerPermisosPorRol(Rol.ADMIN);

            assertThat(result).containsExactly(Permiso.VER_AUDITORIA);
        }

        @Test
        @DisplayName("Retorna permisos default cuando BD vacía")
        void retorna_default_cuando_vacia() {
            when(jpaRepository.findByRol(Rol.ADMIN)).thenReturn(List.of());

            List<Permiso> result = repository.obtenerPermisosPorRol(Rol.ADMIN);

            assertThat(result).isNotEmpty();
            assertThat(result).contains(Permiso.VER_AUDITORIA);
        }
    }

    @Nested
    @DisplayName("tienePermiso")
    class TienePermisoTests {

        @Test
        @DisplayName("Retorna true cuando rol tiene permiso")
        void retorna_true_cuando_tiene() {
            when(jpaRepository.findByRol(Rol.ADMIN)).thenReturn(List.of(
                    new RolPermisoEntity(Rol.ADMIN, Permiso.VER_AUDITORIA)
            ));

            boolean result = repository.tienePermiso(Rol.ADMIN, Permiso.VER_AUDITORIA);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Retorna false cuando rol no tiene permiso")
        void retorna_false_cuando_no_tiene() {
            when(jpaRepository.findByRol(Rol.SOCIO)).thenReturn(List.of());

            boolean result = repository.tienePermiso(Rol.SOCIO, Permiso.VER_AUDITORIA);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("tieneAlgunPermiso")
    class TieneAlgunPermisoTests {

        @Test
        @DisplayName("Retorna true cuando tiene al menos uno")
        void retorna_true_cuando_tiene_al_menos_uno() {
            when(jpaRepository.findByRol(Rol.ADMIN)).thenReturn(List.of(
                    new RolPermisoEntity(Rol.ADMIN, Permiso.VER_AUDITORIA)
            ));

            boolean result = repository.tieneAlgunPermiso(Rol.ADMIN,
                    List.of(Permiso.VER_AUDITORIA, Permiso.GESTIONAR_USUARIOS));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Retorna false cuando no tiene ninguno")
        void retorna_false_cuando_no_tiene_ninguno() {
            when(jpaRepository.findByRol(Rol.SOCIO)).thenReturn(List.of());

            boolean result = repository.tieneAlgunPermiso(Rol.SOCIO,
                    List.of(Permiso.VER_AUDITORIA, Permiso.GESTIONAR_USUARIOS));

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("tieneTodosLosPermisos")
    class TieneTodosLosPermisosTests {

        @Test
        @DisplayName("Retorna true cuando tiene todos")
        void retorna_true_cuando_tiene_todos() {
            when(jpaRepository.findByRol(Rol.ADMIN)).thenReturn(List.of(
                    new RolPermisoEntity(Rol.ADMIN, Permiso.VER_AUDITORIA),
                    new RolPermisoEntity(Rol.ADMIN, Permiso.GESTIONAR_USUARIOS)
            ));

            boolean result = repository.tieneTodosLosPermisos(Rol.ADMIN,
                    List.of(Permiso.VER_AUDITORIA, Permiso.GESTIONAR_USUARIOS));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Retorna false cuando falta alguno")
        void retorna_false_cuando_falta_alguno() {
            when(jpaRepository.findByRol(Rol.ADMIN)).thenReturn(List.of(
                    new RolPermisoEntity(Rol.ADMIN, Permiso.VER_AUDITORIA)
            ));

            boolean result = repository.tieneTodosLosPermisos(Rol.ADMIN,
                    List.of(Permiso.VER_AUDITORIA, Permiso.GESTIONAR_USUARIOS));

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("asignarPermiso")
    class AsignarPermisoTests {

        @Test
        @DisplayName("Guarda permiso cuando no existe")
        void guarda_cuando_no_existe() {
            when(jpaRepository.existsByRolAndPermiso(Rol.ADMIN, Permiso.GESTIONAR_USUARIOS))
                    .thenReturn(false);

            repository.asignarPermiso(Rol.ADMIN, Permiso.GESTIONAR_USUARIOS);

            verify(jpaRepository).save(any(RolPermisoEntity.class));
        }

        @Test
        @DisplayName("No guarda cuando permiso ya existe")
        void no_guarda_cuando_ya_existe() {
            when(jpaRepository.existsByRolAndPermiso(Rol.ADMIN, Permiso.GESTIONAR_USUARIOS))
                    .thenReturn(true);

            repository.asignarPermiso(Rol.ADMIN, Permiso.GESTIONAR_USUARIOS);

            verify(jpaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("quitarPermiso")
    class QuitarPermisoTests {

        @Test
        @DisplayName("Elimina permiso existente")
        void elimina_permiso_existente() {
            RolPermisoEntity entity = new RolPermisoEntity(Rol.ADMIN, Permiso.GESTIONAR_USUARIOS);
            when(jpaRepository.findByRol(Rol.ADMIN)).thenReturn(List.of(entity));

            repository.quitarPermiso(Rol.ADMIN, Permiso.GESTIONAR_USUARIOS);

            verify(jpaRepository).delete(entity);
        }
    }

    @Nested
    @DisplayName("inicializarPermisosDefault")
    class InicializarPermisosDefaultTests {

        @Test
        @DisplayName("Inicializa permisos para todos los roles")
        void inicializa_todos_los_roles() {
            when(jpaRepository.existsByRolAndPermiso(any(), any())).thenReturn(false);

            repository.inicializarPermisosDefault();

            verify(jpaRepository, atLeastOnce()).save(any(RolPermisoEntity.class));
        }
    }

    @Nested
    @DisplayName("obtenerPermisosPorRoles")
    class ObtenerPermisosPorRolesTests {

        @Test
        @DisplayName("Combina permisos de múltiples roles sin duplicados")
        void combina_permisos_sin_duplicados() {
            when(jpaRepository.findByRol(Rol.ADMIN)).thenReturn(List.of(
                    new RolPermisoEntity(Rol.ADMIN, Permiso.VER_AUDITORIA)
            ));
            when(jpaRepository.findByRol(Rol.SUPER_ADMIN)).thenReturn(List.of(
                    new RolPermisoEntity(Rol.SUPER_ADMIN, Permiso.VER_AUDITORIA),
                    new RolPermisoEntity(Rol.SUPER_ADMIN, Permiso.GESTIONAR_USUARIOS)
            ));

            List<Permiso> result = repository.obtenerPermisosPorRoles(List.of(Rol.ADMIN, Rol.SUPER_ADMIN));

            assertThat(result).contains(Permiso.VER_AUDITORIA);
            assertThat(result).contains(Permiso.GESTIONAR_USUARIOS);
        }
    }
}