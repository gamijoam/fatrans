package com.tufondo.auth.infrastructure.presentation.controller;

import com.tufondo.auth.application.dto.*;
import com.tufondo.auth.application.usecase.AuthUseCase;
import com.tufondo.auth.application.usecase.CambiarPasswordUseCase;
import com.tufondo.auth.application.usecase.CrearUsuarioManualUseCase;
import com.tufondo.auth.application.usecase.ObtenerUsuarioActualUseCase;
import com.tufondo.auth.application.usecase.RestablecerPasswordUseCase;
import com.tufondo.auth.application.usecase.SolicitarRecuperacionPasswordUseCase;
import com.tufondo.auth.application.usecase.ValidarTokenUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticación y gestión de sesiones")
public class AuthController {

    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final int ACCESS_TOKEN_MAX_AGE = 900; // 15 minutos
    private static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60; // 7 días

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthUseCase authUseCase;
    private final ObtenerUsuarioActualUseCase obtenerUsuarioActualUseCase;
    private final ValidarTokenUseCase validarTokenUseCase;
    private final CrearUsuarioManualUseCase crearUsuarioManualUseCase;
    private final SolicitarRecuperacionPasswordUseCase solicitarRecuperacionPasswordUseCase;
    private final RestablecerPasswordUseCase restablecerPasswordUseCase;
    private final CambiarPasswordUseCase cambiarPasswordUseCase;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y devuelve tokens JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "403", description = "Cuenta bloqueada o desactivada"),
            @ApiResponse(responseCode = "429", description = "Demasiadas solicitudes")
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authUseCase.login(request));
    }

    @PostMapping("/login-web")
    @Operation(summary = "Iniciar sesión desde Flutter Web", description = "Autentica y devuelve tokens en cookies httpOnly para Flutter Web")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso con cookies"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas"),
            @ApiResponse(responseCode = "403", description = "Cuenta bloqueada o desactivada")
    })
    public ResponseEntity<LoginWebResponseDTO> loginWeb(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String clientIp = extractClientIp(httpRequest);
        LoginResponseDTO login = authUseCase.login(request, clientIp);
        boolean isSecure = isSecureCookie(httpRequest);

        // Crear cookie de access token (httpOnly, secure, sameSite)
        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, login.accessToken())
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .maxAge(ACCESS_TOKEN_MAX_AGE)
                .sameSite("Strict")
                .build();
        httpResponse.addHeader("Set-Cookie", accessCookie.toString());

        // Crear cookie de refresh token (httpOnly, secure, sameSite)
        // Path apunta a /refresh-web que es el endpoint que usa este cookie
        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, login.refreshToken())
                .httpOnly(true)
                .secure(isSecure)
                .path("/api/v1/auth/refresh-web")
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .sameSite("Strict")
                .build();
        httpResponse.addHeader("Set-Cookie", refreshCookie.toString());

        log.info("Login web exitoso para usuario: {}", login.usuario().id());

        return ResponseEntity.ok()
                .header("X-User-Id", login.usuario().id())
                .header("X-User-Rol", login.usuario().rol())
                .body(new LoginWebResponseDTO(
                        login.usuario().id(),
                        login.usuario().nombreUsuario(),
                        login.usuario().correoElectronico(),
                        login.usuario().nombreCompleto(),
                        login.usuario().rol()
                ));
    }

    @PostMapping("/logout-web")
    @Operation(summary = "Cerrar sesión desde Flutter Web", description = "Invalida la sesión y limpia las cookies httpOnly")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesión cerrada y cookies eliminadas"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<Map<String, String>> logoutWeb(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String clientIp = extractClientIp(httpRequest);
        boolean isSecure = isSecureCookie(httpRequest);

        // Extraer token desde cookie o header
        String token = extractTokenFromRequest(httpRequest);

        if (token != null) {
            try {
                authUseCase.logout("Bearer " + token, clientIp);
            } catch (Exception e) {
                log.warn("Error en logout web: {}", e.getMessage());
            }
        }

        // Limpiar cookies
        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        httpResponse.addHeader("Set-Cookie", accessCookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(isSecure)
                .path("/api/v1/auth/refresh-web")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        httpResponse.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token", description = "Obtiene nuevos tokens usando un refresh token válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    })
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(authUseCase.refreshToken(request));
    }

    @PostMapping("/refresh-web")
    @Operation(summary = "Refrescar token desde Flutter Web", description = "Usa cookie httpOnly para refresh y devuelve nuevas cookies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tokens actualizados"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    })
    public ResponseEntity<LoginWebResponseDTO> refreshTokenWeb(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        String clientIp = extractClientIp(httpRequest);
        boolean isSecure = isSecureCookie(httpRequest);

        // Extraer refresh token desde cookie
        String refreshToken = extractRefreshTokenFromCookie(httpRequest);
        if (refreshToken == null) {
            throw new com.tufondo.auth.domain.exception.TokenInvalidoException("Refresh token requerido");
        }

        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO(refreshToken);
        LoginResponseDTO login = authUseCase.refreshToken(request, clientIp);

        // Actualizar cookies con nuevos tokens
        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, login.accessToken())
                .httpOnly(true)
                .secure(isSecure)
                .path("/")
                .maxAge(ACCESS_TOKEN_MAX_AGE)
                .sameSite("Strict")
                .build();
        httpResponse.addHeader("Set-Cookie", accessCookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, login.refreshToken())
                .httpOnly(true)
                .secure(isSecure)
                .path("/api/v1/auth/refresh-web")
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .sameSite("Strict")
                .build();
        httpResponse.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok()
                .header("X-User-Id", login.usuario().id())
                .header("X-User-Rol", login.usuario().rol())
                .body(new LoginWebResponseDTO(
                        login.usuario().id(),
                        login.usuario().nombreUsuario(),
                        login.usuario().correoElectronico(),
                        login.usuario().nombreCompleto(),
                        login.usuario().rol()
                ));
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Invalida la sesión actual del usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        authUseCase.logout(authHeader);
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener usuario actual", description = "Devuelve la información del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<LoginResponseDTO.UsuarioDTO> getUsuarioActual() {
        return ResponseEntity.ok(obtenerUsuarioActualUseCase.ejecutar());
    }

    @PostMapping("/validar")
    @Operation(summary = "Validar token", description = "Verifica si un token JWT es válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token válido"),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    public ResponseEntity<TokenValidacionDTO> validarToken(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(validarTokenUseCase.ejecutar(token));
    }

    @PostMapping("/crear-usuario")
    @Operation(summary = "Crear usuario vinculado a socio",
               description = "Vincula un Socio existente con credenciales de acceso (Usuario)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
                    content = @Content(schema = @Schema(implementation = CrearUsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Socio no encontrado"),
            @ApiResponse(responseCode = "409", description = "Nombre de usuario ya existe o socio ya tiene usuario vinculado")
    })
    public ResponseEntity<CrearUsuarioResponseDTO> crearUsuario(
            @Valid @RequestBody CrearUsuarioRequestDTO request) {
        CrearUsuarioResponseDTO response = crearUsuarioManualUseCase.ejecutar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/recuperar-password")
    @Operation(summary = "Solicitar recuperación de contraseña",
               description = "Genera token de recuperación y envía email con enlace")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Si el email existe, se ha enviado un enlace de recuperación"),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    public ResponseEntity<MensajeResponseDTO> recuperarPassword(
            @Valid @RequestBody RecuperarPasswordRequestDTO request) {
        MensajeResponseDTO response = solicitarRecuperacionPasswordUseCase.ejecutar(request.identificador());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña",
               description = "Restablece la contraseña usando un token de recuperación válido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Token inválido, expirado o password no cumple requisitos")
    })
    public ResponseEntity<MensajeResponseDTO> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request) {
        MensajeResponseDTO response = restablecerPasswordUseCase.ejecutar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cambiar-password")
    @Operation(summary = "Cambiar contraseña",
               description = "Permite al usuario autenticado cambiar su contraseña actual por una nueva")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Password no cumple requisitos"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas o no autorizado"),
            @ApiResponse(responseCode = "403", description = "Cuenta bloqueada o desactivada")
    })
    public ResponseEntity<MensajeResponseDTO> cambiarPassword(
            @Valid @RequestBody CambiarPasswordRequestDTO request) {
        MensajeResponseDTO response = cambiarPasswordUseCase.ejecutar(request);
        return ResponseEntity.ok(response);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        // Primero intentar desde cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // Luego desde header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isSecureCookie(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin != null) {
            return !origin.contains("localhost") && !origin.contains("127.0.0.1");
        }
        String referer = request.getHeader("Referer");
        if (referer != null) {
            return !referer.contains("localhost") && !referer.contains("127.0.0.1");
        }
        return true;
    }
}
