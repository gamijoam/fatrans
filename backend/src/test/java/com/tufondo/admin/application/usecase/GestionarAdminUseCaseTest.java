package com.tufondo.admin.application.usecase;

import com.tufondo.admin.application.dto.AdminUsuarioRequest;
import com.tufondo.admin.application.dto.AdminUsuarioResponse;
import com.tufondo.creditos.infrastructure.security.XssSanitizer;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.service.Argon2Hasher;
import com.tufondo.auth.infrastructure.service.SecurityAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GestionarAdminUseCase - Tests Completos")
class GestionarAdminUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private Argon2Hasher argon2Hasher;

    @Mock
    private SecurityAuditService auditService;

    private XssSanitizer xssSanitizer;

    private GestionarAdminUseCase useCase;

    private UUID adminId;
    private UUID superAdminId;
    private String ipAddress;
    private Usuario adminUsuario;
    private Usuario superAdminUsuario;

    @BeforeEach
    void setUp() {
        xssSanitizer = new XssSanitizer();
        useCase = new GestionarAdminUseCase(usuarioRepository, argon2Hasher, xssSanitizer, auditService);

        adminId = UUID.randomUUID();
        superAdminId = UUID.randomUUID();
        ipAddress = "192.168.1.1";

        adminUsuario = Usuario.desdeParametros(
                adminId,
                "admin_test",
                "admin@test.com",
                "passwordhash",
                "Admin Test",
                Rol.ADMIN,
                null,
                true,
                Instant.now(),
                Instant.now(),
                0,
                null,
                false
        );

        superAdminUsuario = Usuario.desdeParametros(
                superAdminId,
                "super_admin_test",
                "superadmin@test.com",
                "passwordhash",
                "Super Admin Test",
                Rol.SUPER_ADMIN,
                null,
                true,
                Instant.now(),
                Instant.now(),
                0,
                null,
                false
        );
    }

    @Nested
    @DisplayName("listarAdmins")
    class ListarAdminsTests {

        @Test
        @DisplayName("Lista solo usuarios con rol ADMIN")
        void lista_solo_admins() {
            when(usuarioRepository.listarPorRol(Rol.ADMIN)).thenReturn(List.of(adminUsuario));

            List<AdminUsuarioResponse> result = useCase.listarAdmins();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNombreUsuario()).isEqualTo("admin_test");
            assertThat(result.get(0).getRol()).isEqualTo("ADMIN");
            verify(usuarioRepository).listarPorRol(Rol.ADMIN);
        }

        @Test
        @DisplayName("Lista admins vacíos cuando no hay ninguno")
        void lista_admins_vacios() {
            when(usuarioRepository.listarPorRol(Rol.ADMIN)).thenReturn(List.of());

            List<AdminUsuarioResponse> result = useCase.listarAdmins();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listarTodos")
    class ListarTodosTests {

        @Test
        @DisplayName("Lista todos los usuarios")
        void lista_todos_los_usuarios() {
            when(usuarioRepository.listarTodos()).thenReturn(List.of(adminUsuario, superAdminUsuario));

            List<AdminUsuarioResponse> result = useCase.listarTodos();

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Lista usuarios vacíos")
        void lista_usuarios_vacios() {
            when(usuarioRepository.listarTodos()).thenReturn(List.of());

            List<AdminUsuarioResponse> result = useCase.listarTodos();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("obtenerPorId")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("Obtiene usuario por ID")
        void obtiene_por_id() {
            when(usuarioRepository.buscarPorId(adminId)).thenReturn(Optional.of(adminUsuario));

            AdminUsuarioResponse result = useCase.obtenerPorId(adminId);

            assertThat(result.getId()).isEqualTo(adminId);
            assertThat(result.getNombreUsuario()).isEqualTo("admin_test");
        }

        @Test
        @DisplayName("Lanza excepción para ID no existente")
        void lanza_excepcion_id_no_existente() {
            UUID nonExistentId = UUID.randomUUID();
            when(usuarioRepository.buscarPorId(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.obtenerPorId(nonExistentId))
                    .isInstanceOf(GestionarAdminUseCase.UsuarioNoEncontradoException.class)
                    .hasMessageContaining(nonExistentId.toString());
        }
    }

    @Nested
    @DisplayName("crear")
    class CrearTests {

        private AdminUsuarioRequest crearRequest;
        private AdminUsuarioRequest crearSuperAdminRequest;

        @BeforeEach
        void setUp() {
            crearRequest = AdminUsuarioRequest.builder()
                    .nombreUsuario("nuevo_admin")
                    .correoElectronico("nuevo@test.com")
                    .nombreCompleto("Nuevo Admin")
                    .password("password123")
                    .rol("ADMIN")
                    .cuentaActiva(true)
                    .build();

            crearSuperAdminRequest = AdminUsuarioRequest.builder()
                    .nombreUsuario("nuevo_super_admin")
                    .correoElectronico("nuevosuper@test.com")
                    .nombreCompleto("Nuevo Super Admin")
                    .password("password123")
                    .rol("SUPER_ADMIN")
                    .cuentaActiva(true)
                    .build();
        }

        @Test
        @DisplayName("Crea admin exitosamente")
        void crea_admin_exitosamente() {
            when(usuarioRepository.existePorNombreUsuario("nuevo_admin")).thenReturn(false);
            when(usuarioRepository.existePorCorreoElectronico("nuevo@test.com")).thenReturn(false);
            when(argon2Hasher.hash("password123")).thenReturn("hashed_password");
            doAnswer(invocation -> {
                Usuario u = invocation.getArgument(0);
                return null;
            }).when(usuarioRepository).guardar(any(Usuario.class));

            AdminUsuarioResponse result = useCase.crear(crearRequest, adminId, ipAddress);

            assertThat(result.getNombreUsuario()).isEqualTo("nuevo_admin");
            assertThat(result.getCorreoElectronico()).isEqualTo("nuevo@test.com");
            assertThat(result.getRol()).isEqualTo("ADMIN");
            verify(usuarioRepository).guardar(any(Usuario.class));
        }

        @Test
        @DisplayName("Crea SUPER_ADMIN exitosamente")
        void crea_super_admin_exitosamente() {
            when(usuarioRepository.existePorNombreUsuario("nuevo_super_admin")).thenReturn(false);
            when(usuarioRepository.existePorCorreoElectronico("nuevosuper@test.com")).thenReturn(false);
            when(argon2Hasher.hash("password123")).thenReturn("hashed_password");
            doAnswer(invocation -> null).when(usuarioRepository).guardar(any(Usuario.class));

            AdminUsuarioResponse result = useCase.crear(crearSuperAdminRequest, superAdminId, ipAddress);

            assertThat(result.getRol()).isEqualTo("SUPER_ADMIN");
        }

        @Test
        @DisplayName("Lanza excepción por nombre de usuario duplicado")
        void lanza_excepcion_nombre_usuario_duplicado() {
            when(usuarioRepository.existePorNombreUsuario("nuevo_admin")).thenReturn(true);

            assertThatThrownBy(() -> useCase.crear(crearRequest, adminId, ipAddress))
                    .isInstanceOf(GestionarAdminUseCase.NombreUsuarioYaExisteException.class)
                    .hasMessageContaining("nuevo_admin");
        }

        @Test
        @DisplayName("Lanza excepción por correo duplicado")
        void lanza_excepcion_correo_duplicado() {
            when(usuarioRepository.existePorNombreUsuario("nuevo_admin")).thenReturn(false);
            when(usuarioRepository.existePorCorreoElectronico("nuevo@test.com")).thenReturn(true);

            assertThatThrownBy(() -> useCase.crear(crearRequest, adminId, ipAddress))
                    .isInstanceOf(GestionarAdminUseCase.CorreoYaExisteException.class)
                    .hasMessageContaining("nuevo@test.com");
        }

        @Test
        @DisplayName("Lanza excepción por rol inválido")
        void lanza_excepcion_rol_invalido() {
            AdminUsuarioRequest requestInvalido = AdminUsuarioRequest.builder()
                    .nombreUsuario("usuario_invalido")
                    .correoElectronico("invalido@test.com")
                    .nombreCompleto("Usuario Invalido")
                    .password("password123")
                    .rol("SOCIO")
                    .build();

            assertThatThrownBy(() -> useCase.crear(requestInvalido, adminId, ipAddress))
                    .isInstanceOf(GestionarAdminUseCase.RolInvalidoException.class);
        }

        @Test
        @DisplayName("Sanitiza inputs de usuario")
        void sanitiza_inputs() {
            AdminUsuarioRequest requestConXSS = AdminUsuarioRequest.builder()
                    .nombreUsuario("  <script>alert('xss')</script>ADMIN  ")
                    .correoElectronico("  <b>TEST@TEST.COM</b>  ")
                    .nombreCompleto("  <script>alert('xss')</script>Admin XSS  ")
                    .password("password123")
                    .rol("ADMIN")
                    .build();

            when(usuarioRepository.existePorNombreUsuario(any())).thenReturn(false);
            when(usuarioRepository.existePorCorreoElectronico(any())).thenReturn(false);
            when(argon2Hasher.hash(any())).thenReturn("hashed_password");
            doAnswer(invocation -> null).when(usuarioRepository).guardar(any(Usuario.class));

            useCase.crear(requestConXSS, adminId, ipAddress);

            verify(usuarioRepository).existePorNombreUsuario("admin");
            verify(usuarioRepository).existePorCorreoElectronico("test@test.com");
        }

        @Test
        @DisplayName("Convierte correo a minúsculas")
        void convierte_correo_a_minusculas() {
            AdminUsuarioRequest requestMayus = AdminUsuarioRequest.builder()
                    .nombreUsuario("nuevo_admin2")
                    .correoElectronico("MAYUSCULAS@TEST.COM")
                    .nombreCompleto("Nuevo Admin")
                    .password("password123")
                    .rol("ADMIN")
                    .build();

            when(usuarioRepository.existePorNombreUsuario("nuevo_admin2")).thenReturn(false);
            when(usuarioRepository.existePorCorreoElectronico("mayusculas@test.com")).thenReturn(false);
            when(argon2Hasher.hash(any())).thenReturn("hashed_password");
            doAnswer(invocation -> null).when(usuarioRepository).guardar(any(Usuario.class));

            useCase.crear(requestMayus, adminId, ipAddress);

            verify(usuarioRepository).existePorCorreoElectronico("mayusculas@test.com");
        }
    }

    @Nested
    @DisplayName("actualizar")
    class ActualizarTests {

        private AdminUsuarioRequest actualizarRequest;

        @BeforeEach
        void setUp() {
            actualizarRequest = AdminUsuarioRequest.builder()
                    .nombreUsuario("admin_actualizado")
                    .correoElectronico("actualizado@test.com")
                    .nombreCompleto("Admin Actualizado")
                    .password(null)
                    .rol("ADMIN")
                    .cuentaActiva(true)
                    .build();
        }

        @Test
        @DisplayName("Actualiza admin exitosamente sin cambiar password")
        void actualiza_admin_sin_password() {
            when(usuarioRepository.buscarPorId(adminId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepository.existePorNombreUsuario("admin_actualizado")).thenReturn(false);
            when(usuarioRepository.existePorCorreoElectronico("actualizado@test.com")).thenReturn(false);
            doAnswer(invocation -> null).when(usuarioRepository).actualizar(any(Usuario.class));

            AdminUsuarioResponse result = useCase.actualizar(adminId, actualizarRequest, superAdminId, ipAddress);

            assertThat(result.getNombreUsuario()).isEqualTo("admin_actualizado");
            assertThat(result.getCorreoElectronico()).isEqualTo("actualizado@test.com");
            verify(usuarioRepository).actualizar(any(Usuario.class));
            verify(argon2Hasher, never()).hash(any());
        }

        @Test
        @DisplayName("Actualiza admin con nuevo password")
        void actualiza_admin_con_password() {
            AdminUsuarioRequest requestConPassword = AdminUsuarioRequest.builder()
                    .nombreUsuario("admin_test")
                    .correoElectronico("admin@test.com")
                    .nombreCompleto("Admin Test")
                    .password("newpassword123")
                    .rol("ADMIN")
                    .build();

            when(usuarioRepository.buscarPorId(adminId)).thenReturn(Optional.of(adminUsuario));
            when(argon2Hasher.hash("newpassword123")).thenReturn("new_hashed_password");
            doAnswer(invocation -> null).when(usuarioRepository).actualizar(any(Usuario.class));

            useCase.actualizar(adminId, requestConPassword, superAdminId, ipAddress);

            verify(argon2Hasher).hash("newpassword123");
        }

        @Test
        @DisplayName("Lanza excepción al actualizar usuario no existente")
        void lanza_excepcion_usuario_no_existente() {
            UUID nonExistentId = UUID.randomUUID();
            when(usuarioRepository.buscarPorId(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.actualizar(nonExistentId, actualizarRequest, superAdminId, ipAddress))
                    .isInstanceOf(GestionarAdminUseCase.UsuarioNoEncontradoException.class);
        }

        @Test
        @DisplayName("Lanza excepción por nombre de usuario duplicado al actualizar")
        void lanza_excepcion_nombre_duplicado_al_actualizar() {
            UUID otroId = UUID.randomUUID();
            Usuario otroUsuario = Usuario.desdeParametros(
                    otroId, "otro_admin", "otro@test.com", "hash", "Otro Admin",
                    Rol.ADMIN, null, true, Instant.now(), Instant.now(), 0, null, false
            );
            when(usuarioRepository.buscarPorId(adminId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepository.existePorNombreUsuario("admin_actualizado")).thenReturn(true);

            assertThatThrownBy(() -> useCase.actualizar(adminId, actualizarRequest, superAdminId, ipAddress))
                    .isInstanceOf(GestionarAdminUseCase.NombreUsuarioYaExisteException.class);
        }

        @Test
        @DisplayName("Lanza excepción por correo duplicado al actualizar")
        void lanza_excepcion_correo_duplicado_al_actualizar() {
            UUID otroId = UUID.randomUUID();
            Usuario otroUsuario = Usuario.desdeParametros(
                    otroId, "otro_admin", "actualizado@test.com", "hash", "Otro Admin",
                    Rol.ADMIN, null, true, Instant.now(), Instant.now(), 0, null, false
            );
            when(usuarioRepository.buscarPorId(adminId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepository.existePorNombreUsuario("admin_actualizado")).thenReturn(false);
            when(usuarioRepository.existePorCorreoElectronico("actualizado@test.com")).thenReturn(true);

            assertThatThrownBy(() -> useCase.actualizar(adminId, actualizarRequest, superAdminId, ipAddress))
                    .isInstanceOf(GestionarAdminUseCase.CorreoYaExisteException.class);
        }

        @Test
        @DisplayName("Sanitiza inputs al actualizar")
        void sanitiza_inputs_al_actualizar() {
            AdminUsuarioRequest requestXSS = AdminUsuarioRequest.builder()
                    .nombreUsuario("  <script>xss</script>admin  ")
                    .correoElectronico("  <b>test@test.com</b>  ")
                    .nombreCompleto("  <script>xss</script>Nombre XSS  ")
                    .password(null)
                    .rol("ADMIN")
                    .build();

            when(usuarioRepository.buscarPorId(adminId)).thenReturn(Optional.of(adminUsuario));
            when(usuarioRepository.existePorNombreUsuario(any())).thenReturn(false);
            when(usuarioRepository.existePorCorreoElectronico(any())).thenReturn(false);
            doAnswer(invocation -> null).when(usuarioRepository).actualizar(any(Usuario.class));

            useCase.actualizar(adminId, requestXSS, superAdminId, ipAddress);

            verify(usuarioRepository).existePorNombreUsuario("admin");
        }
    }

    @Nested
    @DisplayName("activar")
    class ActivarTests {

        @Test
        @DisplayName("Activa usuario exitosamente")
        void activa_usuario_exitosamente() {
            Usuario usuarioInactivo = Usuario.desdeParametros(
                    adminId, "admin_test", "admin@test.com", "hash", "Admin Test",
                    Rol.ADMIN, null, false, Instant.now(), Instant.now(), 0, null, false
            );
            when(usuarioRepository.buscarPorId(adminId)).thenReturn(Optional.of(usuarioInactivo));
            doAnswer(invocation -> null).when(usuarioRepository).actualizar(any(Usuario.class));

            AdminUsuarioResponse result = useCase.activar(adminId, superAdminId, ipAddress);

            assertThat(result.isCuentaActiva()).isTrue();
        }

        @Test
        @DisplayName("Lanza excepción al activar usuario no existente")
        void lanza_excepcion_activar_no_existente() {
            UUID nonExistentId = UUID.randomUUID();
            when(usuarioRepository.buscarPorId(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.activar(nonExistentId, superAdminId, ipAddress))
                    .isInstanceOf(GestionarAdminUseCase.UsuarioNoEncontradoException.class);
        }

        @Test
        @DisplayName("Resetea intentos fallidos al activar")
        void resetea_intentos_fallidos_al_activar() {
            Usuario usuarioBloqueado = Usuario.desdeParametros(
                    adminId, "admin_test", "admin@test.com", "hash", "Admin Test",
                    Rol.ADMIN, null, false, Instant.now(), Instant.now(), 5, Instant.now(), false
            );
            when(usuarioRepository.buscarPorId(adminId)).thenReturn(Optional.of(usuarioBloqueado));
            doAnswer(invocation -> {
                Usuario u = invocation.getArgument(0);
                assertThat(u.intentosFallidos()).isEqualTo(0);
                return null;
            }).when(usuarioRepository).actualizar(any(Usuario.class));

            useCase.activar(adminId, superAdminId, ipAddress);

            verify(usuarioRepository).actualizar(any(Usuario.class));
        }
    }

    @Nested
    @DisplayName("desactivar")
    class DesactivarTests {

        @Test
        @DisplayName("Desactiva admin exitosamente")
        void desactivar_admin_exitosamente() {
            when(usuarioRepository.buscarPorId(adminId)).thenReturn(Optional.of(adminUsuario));
            doAnswer(invocation -> null).when(usuarioRepository).actualizar(any(Usuario.class));

            AdminUsuarioResponse result = useCase.desactivar(adminId, superAdminId, ipAddress);

            assertThat(result.isCuentaActiva()).isFalse();
        }

        @Test
        @DisplayName("Lanza excepción al desactivar SUPER_ADMIN")
        void lanza_excepcion_desactivar_super_admin() {
            when(usuarioRepository.buscarPorId(superAdminId)).thenReturn(Optional.of(superAdminUsuario));

            assertThatThrownBy(() -> useCase.desactivar(superAdminId, adminId, ipAddress))
                    .isInstanceOf(GestionarAdminUseCase.NoSePuedeDesactivarSuperAdminException.class);
        }

        @Test
        @DisplayName("Lanza excepción al desactivar usuario no existente")
        void lanza_excepcion_desactivar_no_existente() {
            UUID nonExistentId = UUID.randomUUID();
            when(usuarioRepository.buscarPorId(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.desactivar(nonExistentId, superAdminId, ipAddress))
                    .isInstanceOf(GestionarAdminUseCase.UsuarioNoEncontradoException.class);
        }
    }
}