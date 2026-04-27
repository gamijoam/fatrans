package com.tufondo.socios.application.usecase;

import com.tufondo.auth.domain.exception.CredencialesInvalidasException;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.service.SecurityAuditService;
import com.tufondo.auth.infrastructure.service.VerificacionService;
import com.tufondo.socios.application.dto.ConfirmarCodigoRequestDTO;
import com.tufondo.socios.application.dto.ConfirmarCodigoResponseDTO;
import com.tufondo.socios.application.dto.EnviarCodigoRequestDTO;
import com.tufondo.socios.application.dto.VerificarPasswordRequestDTO;
import com.tufondo.socios.application.dto.VerificarPasswordResponseDTO;
import com.tufondo.socios.domain.model.enums.TipoVerificacion;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VerificacionUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VerificacionService verificacionService;

    @Mock
    private SecurityAuditService auditService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private VerificacionUseCase verificacionUseCase;

    private UUID usuarioId;
    private Usuario usuario;
    private String passwordHash;
    private String ipAddress;
    private String userAgent;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        passwordHash = "$argon2hashpassword123";
        ipAddress = "192.168.1.1";
        userAgent = "Mozilla/5.0 Test";

        usuario = Usuario.desdeParametros(
                usuarioId,
                "usuario_test",
                "test@test.com",
                passwordHash,
                "Usuario Test",
                Rol.SOCIO,
                UUID.randomUUID(),
                true,
                Instant.now(),
                Instant.now(),
                0,
                null,
                false
        );

        lenient().when(httpRequest.getRemoteAddr()).thenReturn(ipAddress);
        lenient().when(httpRequest.getHeader("User-Agent")).thenReturn(userAgent);
    }

    @Nested
    @DisplayName("verificarPassword")
    class VerificarPassword {

        @Test
        @DisplayName("password correcto genera token de verificación")
        void passwordCorrecto_GeneraToken() {
            VerificarPasswordRequestDTO request = new VerificarPasswordRequestDTO("password123");
            String expectedToken = UUID.randomUUID().toString();

            when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
            when(verificacionService.verificarPasswordUsuario(
                    eq(usuarioId), eq("password123"), eq(passwordHash), eq(ipAddress), eq(userAgent)))
                    .thenReturn(true);
            when(verificacionService.generarTokenVerificacion(eq(usuarioId), eq(ipAddress), eq(userAgent)))
                    .thenReturn(expectedToken);

            VerificarPasswordResponseDTO response = verificacionUseCase.verificarPassword(
                    usuarioId, request, httpRequest);

            assertThat(response.isValido()).isTrue();
            assertThat(response.getTokenVerificacion()).isEqualTo(expectedToken);
            verify(verificacionService).generarTokenVerificacion(usuarioId, ipAddress, userAgent);
        }

        @Test
        @DisplayName("password incorrecto lanza excepción")
        void passwordIncorrecto_LanzaExcepcion() {
            VerificarPasswordRequestDTO request = new VerificarPasswordRequestDTO("wrongpassword");

            when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
            when(verificacionService.verificarPasswordUsuario(
                    eq(usuarioId), eq("wrongpassword"), eq(passwordHash), eq(ipAddress), eq(userAgent)))
                    .thenReturn(false);

            assertThatThrownBy(() -> verificacionUseCase.verificarPassword(usuarioId, request, httpRequest))
                    .isInstanceOf(CredencialesInvalidasException.class)
                    .hasMessage("Contraseña incorrecta");
        }

        @Test
        @DisplayName("usuario no encontrado lanza excepción")
        void usuarioNoEncontrado_LanzaExcepcion() {
            VerificarPasswordRequestDTO request = new VerificarPasswordRequestDTO("password123");

            when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> verificacionUseCase.verificarPassword(usuarioId, request, httpRequest))
                    .isInstanceOf(CredencialesInvalidasException.class)
                    .hasMessage("Usuario no encontrado");
        }
    }

    @Nested
    @DisplayName("enviarCodigo")
    class EnviarCodigo {

        @Test
        @DisplayName("envía código y retorna token")
        void enviaCodigo_RetornaToken() {
            EnviarCodigoRequestDTO request = new EnviarCodigoRequestDTO(TipoVerificacion.SMS, "04121234567");
            String expectedToken = UUID.randomUUID().toString();

            when(verificacionService.generarYCEnviarCodigo(
                    eq(usuarioId), eq(TipoVerificacion.SMS), eq("04121234567"),
                    eq(ipAddress), eq(userAgent), isNull()))
                    .thenReturn(expectedToken);

            String token = verificacionUseCase.enviarCodigo(usuarioId, request, httpRequest);

            assertThat(token).isEqualTo(expectedToken);
        }

        @Test
        @DisplayName("para EMAIL pasa el valor como emailDestino")
        void paraEMAIL_PasaValor() {
            EnviarCodigoRequestDTO request = new EnviarCodigoRequestDTO(TipoVerificacion.EMAIL, "test@test.com");
            String expectedToken = UUID.randomUUID().toString();

            when(verificacionService.generarYCEnviarCodigo(
                    eq(usuarioId), eq(TipoVerificacion.EMAIL), eq("test@test.com"),
                    eq(ipAddress), eq(userAgent), eq("test@test.com")))
                    .thenReturn(expectedToken);

            String token = verificacionUseCase.enviarCodigo(usuarioId, request, httpRequest);

            assertThat(token).isEqualTo(expectedToken);
        }
    }

    @Nested
    @DisplayName("confirmarCodigo")
    class ConfirmarCodigo {

        @Test
        @DisplayName("código válido retorna response con token")
        void codigoValido_RetornaResponse() {
            String token = UUID.randomUUID().toString();
            String codigo = "123456";
            ConfirmarCodigoRequestDTO request = ConfirmarCodigoRequestDTO.builder()
                    .tipo(TipoVerificacion.SMS)
                    .token(token)
                    .codigo(codigo)
                    .build();

            when(verificacionService.confirmarCodigo(usuarioId, token, codigo, ipAddress, userAgent))
                    .thenReturn(true);

            ConfirmarCodigoResponseDTO response = verificacionUseCase.confirmarCodigo(
                    usuarioId, request, httpRequest);

            assertThat(response.isValido()).isTrue();
            assertThat(response.getTokenVerificacion()).isEqualTo(token);
        }

        @Test
        @DisplayName("código inválido lanza excepción")
        void codigoInvalido_LanzaExcepcion() {
            String token = UUID.randomUUID().toString();
            String codigo = "wrong";
            ConfirmarCodigoRequestDTO request = ConfirmarCodigoRequestDTO.builder()
                    .tipo(TipoVerificacion.SMS)
                    .token(token)
                    .codigo(codigo)
                    .build();

            when(verificacionService.confirmarCodigo(usuarioId, token, codigo, ipAddress, userAgent))
                    .thenReturn(false);

            assertThatThrownBy(() -> verificacionUseCase.confirmarCodigo(usuarioId, request, httpRequest))
                    .isInstanceOf(VerificacionUseCase.CodigoInvalidoException.class)
                    .hasMessage("Código inválido o expirado");
        }
    }

    @Nested
    @DisplayName("validarToken")
    class ValidarToken {

        @Test
        @DisplayName("token válido retorna true")
        void tokenValido_RetornaTrue() {
            String token = UUID.randomUUID().toString();

            when(verificacionService.validarTokenVerificacion(usuarioId, token)).thenReturn(true);

            boolean result = verificacionUseCase.validarToken(usuarioId, token);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("token inválido retorna false")
        void tokenInvalido_RetornaFalse() {
            String token = UUID.randomUUID().toString();

            when(verificacionService.validarTokenVerificacion(usuarioId, token)).thenReturn(false);

            boolean result = verificacionUseCase.validarToken(usuarioId, token);

            assertThat(result).isFalse();
        }
    }
}