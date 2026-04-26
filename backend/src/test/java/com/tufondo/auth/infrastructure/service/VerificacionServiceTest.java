package com.tufondo.auth.infrastructure.service;

import com.tufondo.auth.infrastructure.persistence.entity.VerificacionTokenEntity;
import com.tufondo.auth.infrastructure.persistence.repository.VerificacionTokenRepository;
import com.tufondo.socios.domain.model.enums.TipoVerificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VerificacionServiceTest {

    @Mock
    private VerificacionTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private SecurityAuditService auditService;

    @InjectMocks
    private VerificacionService verificacionService;

    private UUID usuarioId;
    private String ipAddress;
    private String userAgent;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        ipAddress = "192.168.1.1";
        userAgent = "Mozilla/5.0 Test";
    }

    @Nested
    @DisplayName("verificarPasswordUsuario")
    class VerificarPasswordUsuario {

        @Test
        @DisplayName("password correcto retorna true")
        void passwordCorrecto_RetornaTrue() {
            String password = "password123";
            String hashedPassword = "$argon2hash";

            when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);

            boolean result = verificacionService.verificarPasswordUsuario(usuarioId, password, hashedPassword);

            assertThat(result).isTrue();
            verify(passwordEncoder).matches(password, hashedPassword);
        }

        @Test
        @DisplayName("password incorrecto retorna false")
        void passwordIncorrecto_RetornaFalse() {
            String password = "wrongpassword";
            String hashedPassword = "$argon2hash";

            when(passwordEncoder.matches(password, hashedPassword)).thenReturn(false);

            boolean result = verificacionService.verificarPasswordUsuario(usuarioId, password, hashedPassword);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("generarTokenVerificacion")
    class GenerarTokenVerificacion {

        @Test
        @DisplayName("genera token y guarda en repository")
        void generaToken_GuardaEnRepository() {
            when(tokenRepository.save(any(VerificacionTokenEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            String token = verificacionService.generarTokenVerificacion(usuarioId, ipAddress, userAgent);

            assertThat(token).isNotNull();
            assertThat(token).matches("[a-f0-9-]{36}");

            ArgumentCaptor<VerificacionTokenEntity> captor = ArgumentCaptor.forClass(VerificacionTokenEntity.class);
            verify(tokenRepository).save(captor.capture());

            VerificacionTokenEntity saved = captor.getValue();
            assertThat(saved.getUsuarioId()).isEqualTo(usuarioId);
            assertThat(saved.getToken()).isEqualTo(token);
            assertThat(saved.getTipo()).isEqualTo(TipoVerificacion.EMAIL);
            assertThat(saved.isUsed()).isFalse();
            assertThat(saved.getIntentos()).isZero();
            assertThat(saved.getIpAddress()).isEqualTo(ipAddress);
            assertThat(saved.getUserAgent()).isEqualTo(userAgent);
        }

        @Test
        @DisplayName("token tiene TTL de 5 minutos")
        void tokenTieneTTLD5Minutos() {
            Instant before = Instant.now().plus(5, ChronoUnit.MINUTES).minusSeconds(1);
            Instant after = Instant.now().plus(5, ChronoUnit.MINUTES).plusSeconds(1);

            when(tokenRepository.save(any(VerificacionTokenEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            String token = verificacionService.generarTokenVerificacion(usuarioId, ipAddress, userAgent);

            ArgumentCaptor<VerificacionTokenEntity> captor = ArgumentCaptor.forClass(VerificacionTokenEntity.class);
            verify(tokenRepository).save(captor.capture());

            VerificacionTokenEntity saved = captor.getValue();
            assertThat(saved.getExpiresAt()).isAfter(before);
            assertThat(saved.getExpiresAt()).isBefore(after);
        }

        @Test
        @DisplayName("registra audit log")
        void registraAuditLog() {
            when(tokenRepository.save(any(VerificacionTokenEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            verificacionService.generarTokenVerificacion(usuarioId, ipAddress, userAgent);

            verify(auditService).registrarIntentoVerificacion(usuarioId, "PASSWORD_VERIFIED", true, ipAddress);
        }
    }

    @Nested
    @DisplayName("generarYCEnviarCodigo")
    class GenerarYCEnviarCodigo {

        @Test
        @DisplayName("genera código de 6 dígitos")
        void generaCodigo6Digitos() {
            when(tokenRepository.findByUsuarioIdAndTipoAndUsedFalseAndExpiresAtAfter(
                    any(UUID.class), any(TipoVerificacion.class), any(Instant.class)))
                    .thenReturn(Optional.empty());
            when(tokenRepository.save(any(VerificacionTokenEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            String result = verificacionService.generarYCEnviarCodigo(
                    usuarioId, TipoVerificacion.SMS, "04121234567",
                    ipAddress, userAgent, "test@test.com");

            assertThat(result).isNotNull();

            ArgumentCaptor<VerificacionTokenEntity> captor = ArgumentCaptor.forClass(VerificacionTokenEntity.class);
            verify(tokenRepository).save(captor.capture());

            VerificacionTokenEntity saved = captor.getValue();
            assertThat(saved.getCodigo()).hasSize(6);
            assertThat(saved.getCodigo()).matches("\\d{6}");
        }

        @Test
        @DisplayName("elimina códigos existentes del mismo usuario y tipo")
        void eliminaCodigosExistentes() {
            VerificacionTokenEntity existente = VerificacionTokenEntity.builder()
                    .token("old-token")
                    .usuarioId(usuarioId)
                    .tipo(TipoVerificacion.SMS)
                    .build();

            when(tokenRepository.findByUsuarioIdAndTipoAndUsedFalseAndExpiresAtAfter(
                    eq(usuarioId), eq(TipoVerificacion.SMS), any(Instant.class)))
                    .thenReturn(Optional.of(existente));

            verificacionService.generarYCEnviarCodigo(
                    usuarioId, TipoVerificacion.SMS, "04121234567",
                    ipAddress, userAgent, null);

            verify(tokenRepository).delete(existente);
        }

        @Test
        @DisplayName("envía email con código cuando tipo es EMAIL")
        void enviaEmailCuandoTipoEMAIL() {
            when(tokenRepository.findByUsuarioIdAndTipoAndUsedFalseAndExpiresAtAfter(
                    any(), any(), any()))
                    .thenReturn(Optional.empty());
            when(tokenRepository.save(any(VerificacionTokenEntity.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            verificacionService.generarYCEnviarCodigo(
                    usuarioId, TipoVerificacion.EMAIL, "test@test.com",
                    ipAddress, userAgent, "test@test.com");

            verify(emailService).enviarCodigoVerificacion(eq("test@test.com"), anyString());
        }
    }

    @Nested
    @DisplayName("confirmarCodigo")
    class ConfirmarCodigo {

        @Test
        @DisplayName("código correcto retorna true y marca como usado")
        void codigoCorrecto_RetornaTrue() {
            String token = "valid-token";
            String codigo = "123456";

            VerificacionTokenEntity entity = VerificacionTokenEntity.builder()
                    .token(token)
                    .usuarioId(usuarioId)
                    .tipo(TipoVerificacion.SMS)
                    .codigo(codigo)
                    .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                    .used(false)
                    .intentos(0)
                    .build();

            when(tokenRepository.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(entity));
            when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            boolean result = verificacionService.confirmarCodigo(usuarioId, token, codigo, ipAddress, userAgent);

            assertThat(result).isTrue();
            assertThat(entity.isUsed()).isTrue();
        }

        @Test
        @DisplayName("token no encontrado retorna false")
        void tokenNoEncontrado_RetornaFalse() {
            when(tokenRepository.findByTokenAndUsedFalse("invalid-token")).thenReturn(Optional.empty());

            boolean result = verificacionService.confirmarCodigo(
                    usuarioId, "invalid-token", "123456", ipAddress, userAgent);

            assertThat(result).isFalse();
            verify(auditService).registrarIntentoVerificacion(
                    usuarioId, "CODIGO_CONFIRM_FAIL_NO_TOKEN", false, ipAddress);
        }

        @Test
        @DisplayName("usuario diferente retorna false")
        void usuarioDiferente_RetornaFalse() {
            String token = "valid-token";
            UUID otroUsuarioId = UUID.randomUUID();

            VerificacionTokenEntity entity = VerificacionTokenEntity.builder()
                    .token(token)
                    .usuarioId(otroUsuarioId)
                    .tipo(TipoVerificacion.SMS)
                    .codigo("123456")
                    .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                    .used(false)
                    .intentos(0)
                    .build();

            when(tokenRepository.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(entity));

            boolean result = verificacionService.confirmarCodigo(usuarioId, token, "123456", ipAddress, userAgent);

            assertThat(result).isFalse();
            verify(auditService).registrarIntentoVerificacion(
                    usuarioId, "CODIGO_CONFIRM_FAIL_USER_MISMATCH", false, ipAddress);
        }

        @Test
        @DisplayName("token expirado retorna false")
        void tokenExpirado_RetornaFalse() {
            String token = "expired-token";

            VerificacionTokenEntity entity = VerificacionTokenEntity.builder()
                    .token(token)
                    .usuarioId(usuarioId)
                    .tipo(TipoVerificacion.SMS)
                    .codigo("123456")
                    .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                    .used(false)
                    .intentos(0)
                    .build();

            when(tokenRepository.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(entity));

            boolean result = verificacionService.confirmarCodigo(usuarioId, token, "123456", ipAddress, userAgent);

            assertThat(result).isFalse();
            verify(auditService).registrarIntentoVerificacion(
                    usuarioId, "CODIGO_CONFIRM_FAIL_EXPIRED", false, ipAddress);
        }

        @Test
        @DisplayName("código incorrecto incrementa intentos")
        void codigoIncorrecto_IncrementaIntentos() {
            String token = "valid-token";

            VerificacionTokenEntity entity = VerificacionTokenEntity.builder()
                    .token(token)
                    .usuarioId(usuarioId)
                    .tipo(TipoVerificacion.SMS)
                    .codigo("123456")
                    .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                    .used(false)
                    .intentos(1)
                    .build();

            when(tokenRepository.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(entity));
            when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            boolean result = verificacionService.confirmarCodigo(usuarioId, token, "wrong", ipAddress, userAgent);

            assertThat(result).isFalse();
            assertThat(entity.getIntentos()).isEqualTo(2);
        }

        @Test
        @DisplayName("superar máximo intentos lanza excepción")
        void superarMaximoIntentos_LanzaExcepcion() {
            String token = "max-attempts-token";

            VerificacionTokenEntity entity = VerificacionTokenEntity.builder()
                    .token(token)
                    .usuarioId(usuarioId)
                    .tipo(TipoVerificacion.SMS)
                    .codigo("123456")
                    .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                    .used(false)
                    .intentos(3)
                    .build();

            when(tokenRepository.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> verificacionService.confirmarCodigo(
                    usuarioId, token, "123456", ipAddress, userAgent))
                    .isInstanceOf(VerificacionService.ExcesoIntentosException.class)
                    .hasMessage("Superaste el número máximo de intentos");
        }
    }

    @Nested
    @DisplayName("validarTokenVerificacion")
    class ValidarTokenVerificacion {

        @Test
        @DisplayName("token válido y no expirado retorna true")
        void tokenValidoNoExpirado_RetornaTrue() {
            String token = "valid-token";

            VerificacionTokenEntity entity = VerificacionTokenEntity.builder()
                    .token(token)
                    .usuarioId(usuarioId)
                    .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                    .used(false)
                    .build();

            when(tokenRepository.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(entity));

            boolean result = verificacionService.validarTokenVerificacion(usuarioId, token);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("token no encontrado retorna false")
        void tokenNoEncontrado_RetornaFalse() {
            when(tokenRepository.findByTokenAndUsedFalse("invalid")).thenReturn(Optional.empty());

            boolean result = verificacionService.validarTokenVerificacion(usuarioId, "invalid");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("token de otro usuario retorna false")
        void tokenOtroUsuario_RetornaFalse() {
            String token = "valid-token";
            UUID otroUsuarioId = UUID.randomUUID();

            VerificacionTokenEntity entity = VerificacionTokenEntity.builder()
                    .token(token)
                    .usuarioId(otroUsuarioId)
                    .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                    .used(false)
                    .build();

            when(tokenRepository.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(entity));

            boolean result = verificacionService.validarTokenVerificacion(usuarioId, token);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("token expirado retorna false")
        void tokenExpirado_RetornaFalse() {
            String token = "expired-token";

            VerificacionTokenEntity entity = VerificacionTokenEntity.builder()
                    .token(token)
                    .usuarioId(usuarioId)
                    .expiresAt(Instant.now().minus(1, ChronoUnit.MINUTES))
                    .used(false)
                    .build();

            when(tokenRepository.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(entity));

            boolean result = verificacionService.validarTokenVerificacion(usuarioId, token);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("invalidarToken")
    class InvalidarToken {

        @Test
        @DisplayName("marca token como usado")
        void marcaTokenComoUsado() {
            String token = "token-to-invalidate";

            VerificacionTokenEntity entity = VerificacionTokenEntity.builder()
                    .token(token)
                    .usuarioId(usuarioId)
                    .used(false)
                    .build();

            when(tokenRepository.findByTokenAndUsedFalse(token)).thenReturn(Optional.of(entity));
            when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            verificacionService.invalidarToken(usuarioId, token);

            assertThat(entity.isUsed()).isTrue();
            verify(tokenRepository).save(entity);
        }

        @Test
        @DisplayName("no hace nada si token no existe")
        void noHaceNadaSiTokenNoExiste() {
            when(tokenRepository.findByTokenAndUsedFalse("invalid")).thenReturn(Optional.empty());

            verificacionService.invalidarToken(usuarioId, "invalid");

            verify(tokenRepository, never()).save(any());
        }
    }
}