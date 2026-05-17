package com.tufondo.auth.application.usecase;

import com.tufondo.auth.application.dto.LoginRequestDTO;
import com.tufondo.auth.application.dto.LoginResponseDTO;
import com.tufondo.auth.application.dto.RefreshTokenRequestDTO;
import com.tufondo.auth.domain.exception.*;
import com.tufondo.auth.domain.model.Sesion;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.model.enums.TipoToken;
import com.tufondo.auth.domain.repository.SesionRepository;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.service.Argon2Hasher;
import com.tufondo.auth.infrastructure.service.JwtService;
import com.tufondo.auth.infrastructure.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUseCase {

    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final int MINUTOS_BLOQUEO = 30;

    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Argon2Hasher argon2Hasher;
    private final SecurityAuditService auditService;

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request, String clientIp) {
        log.info("Intento de login para identificador: {}", request.identificador());

        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorNombreUsuario(request.identificador());
        if (usuarioOpt.isEmpty()) {
            usuarioOpt = usuarioRepository.buscarPorCorreoElectronico(request.identificador());
        }

        Usuario usuario = usuarioOpt.orElseThrow(() -> {
            auditService.logLoginFallido(request.identificador(), clientIp, "usuario_no_encontrado");
            return new CredencialesInvalidasException("Credenciales inválidas");
        });

        if (!usuario.cuentaActiva()) {
            log.warn("Cuenta desactivada para usuario: {}", usuario.id());
            throw new CuentaDesactivadaException();
        }

        if (usuario.fechaBloqueo() != null && 
            usuario.fechaBloqueo().isAfter(Instant.now())) {
            log.warn("Cuenta bloqueada para usuario: {}", usuario.id());
            throw new CuentaBloqueadaException();
        }

        if (!passwordEncoder.matches(request.password(), usuario.passwordHash())) {
            manejarIntentoFallido(usuario, clientIp);
            throw new CredencialesInvalidasException("Credenciales inválidas");
        }

        usuarioRepository.resetearIntentosFallidos(usuario.id());
        auditService.logLoginExitoso(usuario.id().toString(), clientIp);

        return crearSesionYRespuesta(usuario, clientIp);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        return login(request, "unknown");
    }

    private void manejarIntentoFallido(Usuario usuario, String clientIp) {
        int intentos = usuario.intentosFallidos() + 1;
        Instant fechaBloqueo = null;

        if (intentos >= MAX_INTENTOS_FALLIDOS) {
            fechaBloqueo = Instant.now().plusSeconds(MINUTOS_BLOQUEO * 60L);
            log.warn("Usuario bloqueado por exceder intentos: {}", usuario.id());
            auditService.logCuentaBloqueada(usuario.id().toString(), clientIp);
        }

        usuarioRepository.actualizarIntentosFallidos(usuario.id(), intentos, fechaBloqueo);
        auditService.logLoginFallido(usuario.nombreUsuario(), clientIp, "password_incorrecto");
    }

    @Transactional
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO request, String clientIp) {
        log.info("Intento de refresh token");

        String refreshTokenHash = argon2Hasher.hash(request.refreshToken());

        Optional<Sesion> sesionOpt = sesionRepository.buscarPorRefreshTokenHash(refreshTokenHash);
        if (sesionOpt.isEmpty()) {
            log.warn("Refresh token no encontrado o sesión inactiva");
            throw new TokenInvalidoException("Token inválido");
        }

        Sesion sesion = sesionOpt.get();

        if (sesion.estaExpirado()) {
            log.warn("Sesión expirada: {}", sesion.id());
            throw new TokenExpiradoException("Sesión expirada");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorId(sesion.usuarioId());
        if (usuarioOpt.isEmpty() || !usuarioOpt.get().cuentaActiva()) {
            log.warn("Usuario no encontrado o inactivo para sesión: {}", sesion.id());
            throw new TokenInvalidoException("Token inválido");
        }

        Usuario usuario = usuarioOpt.get();

        // Token rotation: invalidar sesión anterior usando el refresh token
        sesionRepository.invalidarPorRefreshToken(refreshTokenHash);

        // Generar nuevos tokens
        String nuevoAccessToken = jwtService.generarAccessToken(usuario);
        String nuevoRefreshToken = jwtService.generarRefreshToken(usuario);
        String nuevoRefreshTokenHash = argon2Hasher.hash(nuevoRefreshToken);

        Instant accessTokenExpiracion = jwtService.extraerExpiracion(nuevoAccessToken);
        // FIX issue #178: el claim `exp` ya es un instante absoluto (segundos epoch),
        // no segundos relativos. Antes se sumaba a Instant.now() generando expiraciones
        // en el año ~3025 → sesiones efectivamente eternas.
        Instant refreshTokenExpiracion = jwtService.extraerExpiracion(nuevoRefreshToken);

        Sesion nuevaSesion = Sesion.desdeParametros(
                UUID.randomUUID(),
                usuario.id(),
                nuevoRefreshTokenHash,
                accessTokenExpiracion,
                refreshTokenExpiracion,
                true,
                TipoToken.REFRESH_TOKEN,
                Instant.now(),
                Instant.now()
        );

        sesionRepository.guardar(nuevaSesion);
        auditService.logTokenRefresh(usuario.id().toString(), clientIp);

        return new LoginResponseDTO(
                nuevoAccessToken,
                nuevoRefreshToken,
                "Bearer",
                accessTokenExpiracion.getEpochSecond(),
                new LoginResponseDTO.UsuarioDTO(
                        usuario.id().toString(),
                        usuario.nombreUsuario(),
                        usuario.correoElectronico(),
                        usuario.nombreCompleto(),
                        usuario.rol().name(),
                        usuario.socioId() != null ? usuario.socioId().toString() : null,
                        usuario.debeCambiarPassword()
                )
        );
    }

    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        return refreshToken(request, "unknown");
    }

    @Transactional
    public void logout(String authHeader, String clientIp) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Logout solicitado sin token válido");
            throw new TokenInvalidoException("Token requerido");
        }

        String token = authHeader.substring(7);

        if (!jwtService.esAccessTokenValido(token)) {
            log.warn("Intento de logout con token inválido");
            throw new TokenInvalidoException("Token inválido");
        }

        UUID usuarioId = jwtService.extraerUsuarioId(token);
        sesionRepository.invalidarTodasPorUsuario(usuarioId);
        auditService.logLogout(usuarioId.toString(), clientIp);

        log.info("Logout exitoso para usuario: {}", usuarioId);
    }

    public void logout(String authHeader) {
        logout(authHeader, "unknown");
    }

    private LoginResponseDTO crearSesionYRespuesta(Usuario usuario, String clientIp) {
        String accessToken = jwtService.generarAccessToken(usuario);
        String refreshToken = jwtService.generarRefreshToken(usuario);
        String refreshTokenHash = argon2Hasher.hash(refreshToken);

        Instant accessTokenExpiracion = jwtService.extraerExpiracion(accessToken);
        // FIX issue #178: el claim `exp` ya es un instante absoluto (segundos epoch),
        // no segundos relativos. Antes se sumaba a Instant.now() generando expiraciones
        // en el año ~3025 → sesiones efectivamente eternas.
        Instant refreshTokenExpiracion = jwtService.extraerExpiracion(refreshToken);

        Sesion sesion = Sesion.desdeParametros(
                UUID.randomUUID(),
                usuario.id(),
                refreshTokenHash,
                accessTokenExpiracion,
                refreshTokenExpiracion,
                true,
                TipoToken.REFRESH_TOKEN,
                Instant.now(),
                Instant.now()
        );

        sesionRepository.guardar(sesion);

        log.info("Sesión creada exitosamente para usuario: {}", usuario.id());

        return new LoginResponseDTO(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenExpiracion.getEpochSecond(),
                new LoginResponseDTO.UsuarioDTO(
                        usuario.id().toString(),
                        usuario.nombreUsuario(),
                        usuario.correoElectronico(),
                        usuario.nombreCompleto(),
                        usuario.rol().name(),
                        usuario.socioId() != null ? usuario.socioId().toString() : null,
                        usuario.debeCambiarPassword()
                )
        );
    }

}
