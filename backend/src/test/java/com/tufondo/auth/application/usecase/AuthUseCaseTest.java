package com.tufondo.auth.application.usecase;

import com.tufondo.auth.application.dto.LoginRequestDTO;
import com.tufondo.auth.application.dto.LoginResponseDTO;
import com.tufondo.auth.application.dto.RefreshTokenRequestDTO;
import com.tufondo.auth.domain.exception.CredencialesInvalidasException;
import com.tufondo.auth.domain.exception.TokenInvalidoException;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.Rol;
import com.tufondo.auth.domain.repository.SesionRepository;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.service.Argon2Hasher;
import com.tufondo.auth.infrastructure.service.JwtService;
import com.tufondo.auth.infrastructure.service.SecurityAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SesionRepository sesionRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private Argon2Hasher argon2Hasher;

    @Mock
    private SecurityAuditService auditService;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthUseCase authUseCase;

    private Usuario usuarioTest;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        usuarioTest = Usuario.desdeParametros(
                UUID.randomUUID(),
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

        loginRequest = new LoginRequestDTO("admin_test", "password123");
    }

    @Test
    @DisplayName("Login exitoso devuelve usuario")
    void login_exitoso_devuelve_usuario() {
        when(usuarioRepository.buscarPorNombreUsuario("admin_test"))
                .thenReturn(Optional.of(usuarioTest));
        when(passwordEncoder.matches("password123", "passwordhash")).thenReturn(true);
        when(jwtService.generarAccessToken(any())).thenReturn("access_token");
        when(jwtService.generarRefreshToken(any())).thenReturn("refresh_token");
        when(jwtService.extraerExpiracionAccessToken(any())).thenReturn(Instant.now().plusSeconds(900));
        when(argon2Hasher.hash(any())).thenReturn("hash");

        var result = authUseCase.login(loginRequest);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("access_token");
        assertThat(result.refreshToken()).isEqualTo("refresh_token");
        assertThat(result.usuario().nombreUsuario()).isEqualTo("admin_test");
        verify(sesionRepository).guardar(any());
        verify(auditService).logLoginExitoso(any(), any());
    }

    @Test
    @DisplayName("Login con credenciales invalidas lanza excepción")
    void login_credenciales_invalidas_lanza_excepcion() {
        when(usuarioRepository.buscarPorNombreUsuario("admin_test"))
                .thenReturn(Optional.empty());
        when(usuarioRepository.buscarPorCorreoElectronico("admin_test"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authUseCase.login(loginRequest))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(auditService).logLoginFallido(any(), any(), any());
    }

    @Test
    @DisplayName("Refresh token invalido lanza excepción")
    void refresh_token_invalido_lanza_excepcion() {
        when(argon2Hasher.hash("invalid_token")).thenReturn("hash");
        when(sesionRepository.buscarPorRefreshTokenHash("hash")).thenReturn(Optional.empty());

        var request = new RefreshTokenRequestDTO("invalid_token");

        assertThatThrownBy(() -> authUseCase.refreshToken(request))
                .isInstanceOf(TokenInvalidoException.class);
    }
}