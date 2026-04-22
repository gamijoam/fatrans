package com.tufondo.auth.infrastructure.presentation.exception;

import com.tufondo.auth.domain.exception.*;
import com.tufondo.socios.domain.exception.SocioNoEncontradoException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice(basePackages = "com.tufondo.auth")
@Schema(description = "Manejo centralizado de excepciones")
public class AuthExceptionHandler {

    @ExceptionHandler(CredencialesInvalidasException.class)
    @Schema(description = "Error cuando las credenciales son incorrectas")
    public ResponseEntity<ErrorResponse> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        log.warn("Credenciales inválidas: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("CREDENCIALES_INVALIDAS", ex.getMessage()));
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    @Schema(description = "Error cuando el usuario no existe")
    public ResponseEntity<ErrorResponse> manejarUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        log.warn("Usuario no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("USUARIO_NO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    @Schema(description = "Error cuando la cuenta está bloqueada")
    public ResponseEntity<ErrorResponse> manejarCuentaBloqueada(CuentaBloqueadaException ex) {
        log.warn("Cuenta bloqueada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("CUENTA_BLOQUEADA", ex.getMessage()));
    }

    @ExceptionHandler(CuentaDesactivadaException.class)
    @Schema(description = "Error cuando la cuenta está desactivada")
    public ResponseEntity<ErrorResponse> manejarCuentaDesactivada(CuentaDesactivadaException ex) {
        log.warn("Cuenta desactivada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("CUENTA_DESACTIVADA", ex.getMessage()));
    }

    @ExceptionHandler(TokenExpiradoException.class)
    @Schema(description = "Error cuando el token ha expirado")
    public ResponseEntity<ErrorResponse> manejarTokenExpirado(TokenExpiradoException ex) {
        log.warn("Token expirado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("TOKEN_EXPIRADO", ex.getMessage()));
    }

    @ExceptionHandler(TokenInvalidoException.class)
    @Schema(description = "Error cuando el token es inválido")
    public ResponseEntity<ErrorResponse> manejarTokenInvalido(TokenInvalidoException ex) {
        log.warn("Token inválido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("TOKEN_INVALIDO", ex.getMessage()));
    }

    @ExceptionHandler(SesionNoEncontradaException.class)
    @Schema(description = "Error cuando la sesión no existe")
    public ResponseEntity<ErrorResponse> manejarSesionNoEncontrada(SesionNoEncontradaException ex) {
        log.warn("Sesión no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("SESION_NO_ENCONTRADA", ex.getMessage()));
    }

    @ExceptionHandler(SocioNoEncontradoException.class)
    @Schema(description = "Error cuando el socio no existe")
    public ResponseEntity<ErrorResponse> manejarSocioNoEncontrado(SocioNoEncontradoException ex) {
        log.warn("Socio no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("SOCIO_NO_ENCONTRADO", ex.getMessage()));
    }

    @ExceptionHandler(NombreUsuarioYaExisteException.class)
    @Schema(description = "Error cuando el nombre de usuario ya existe")
    public ResponseEntity<ErrorResponse> manejarNombreUsuarioYaExiste(NombreUsuarioYaExisteException ex) {
        log.warn("Nombre de usuario ya existe: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("NOMBRE_USUARIO_YA_EXISTE", ex.getMessage()));
    }

    @ExceptionHandler(SocioYaTieneUsuarioException.class)
    @Schema(description = "Error cuando el socio ya tiene usuario vinculado")
    public ResponseEntity<ErrorResponse> manejarSocioYaTieneUsuario(SocioYaTieneUsuarioException ex) {
        log.warn("Socio ya tiene usuario vinculado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("SOCIO_YA_TIENE_USUARIO", ex.getMessage()));
    }

    @ExceptionHandler(TokenRecuperacionInvalidoException.class)
    @Schema(description = "Error cuando el token de recuperación es inválido o expirado")
    public ResponseEntity<ErrorResponse> manejarTokenRecuperacionInvalido(TokenRecuperacionInvalidoException ex) {
        log.warn("Token de recuperación inválido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("TOKEN_RECUPERACION_INVALIDO", ex.getMessage()));
    }

    @ExceptionHandler(PasswordNoCumpleRequisitosException.class)
    @Schema(description = "Error cuando la password no cumple los requisitos de seguridad")
    public ResponseEntity<ErrorResponse> manejarPasswordNoCumpleRequisitos(PasswordNoCumpleRequisitosException ex) {
        log.warn("Password no cumple requisitos: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("PASSWORD_NO_CUMPLE_REQUISITOS", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @Schema(description = "Error interno del servidor")
    public ResponseEntity<ErrorResponse> manejarExcepcionGeneral(Exception ex) {
        log.error("Error interno en módulo auth", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERROR_INTERNO", "Error interno del servidor"));
    }

    @Schema(description = "Estructura estándar de error")
    public record ErrorResponse(
            @Schema(description = "Código de error")
            String codigo,

            @Schema(description = "Mensaje descriptivo")
            String mensaje,

            @Schema(description = "Timestamp del error")
            Instant timestamp
    ) {
        public ErrorResponse(String codigo, String mensaje) {
            this(codigo, mensaje, Instant.now());
        }
    }
}