package com.tufondo.auth.infrastructure.presentation.exception;

import com.tufondo.auth.domain.exception.CredencialesInvalidasException;
import com.tufondo.auth.domain.exception.SesionNoEncontradaException;
import com.tufondo.auth.domain.exception.UsuarioNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests para issue #206: verificar que distintas excepciones de identidad
 * producen exactamente la misma respuesta al cliente (anti-enumeración).
 */
class AuthExceptionHandlerTest {

    private final AuthExceptionHandler handler = new AuthExceptionHandler();

    @Test
    @DisplayName("Issue #206: CredencialesInvalidasException produce 401 con código y mensaje genéricos")
    void credencialesInvalidas_produce_response_generico() {
        var ex = new CredencialesInvalidasException("usuario tal-y-cual está mal");

        ResponseEntity<AuthExceptionHandler.ErrorResponse> response =
                handler.manejarFalloCredenciales(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().codigo()).isEqualTo("CREDENCIALES_INVALIDAS");
        assertThat(response.getBody().mensaje())
                .as("El mensaje NO debe contener detalles internos (no propaga ex.getMessage())")
                .doesNotContain("tal-y-cual")
                .isEqualTo("Credenciales inválidas. Verifica tu usuario y contraseña.");
    }

    @Test
    @DisplayName("Issue #206: UsuarioNoEncontradoException produce EXACTAMENTE el mismo response que CredencialesInvalidas")
    void usuarioNoEncontrado_produce_mismo_response_que_credenciales_invalidas() {
        var exUsuario = new UsuarioNoEncontradoException("Usuario 'admin' no existe en BD");
        var exCreds = new CredencialesInvalidasException("password mala");

        var responseUsuario = handler.manejarFalloCredenciales(exUsuario);
        var responseCreds = handler.manejarFalloCredenciales(exCreds);

        // El cliente debe recibir respuestas indistinguibles en status, código y mensaje
        assertThat(responseUsuario.getStatusCode()).isEqualTo(responseCreds.getStatusCode());
        assertThat(responseUsuario.getBody().codigo()).isEqualTo(responseCreds.getBody().codigo());
        assertThat(responseUsuario.getBody().mensaje()).isEqualTo(responseCreds.getBody().mensaje());

        // Y específicamente NO debe ser el 404 + "USUARIO_NO_ENCONTRADO" del bug
        assertThat(responseUsuario.getStatusCode()).isNotEqualTo(HttpStatus.NOT_FOUND);
        assertThat(responseUsuario.getBody().codigo()).isNotEqualTo("USUARIO_NO_ENCONTRADO");
    }

    @Test
    @DisplayName("Issue #206: SesionNoEncontradaException también queda unificado en el mismo response genérico")
    void sesionNoEncontrada_produce_mismo_response_que_credenciales_invalidas() {
        var exSesion = new SesionNoEncontradaException("Sesión abc-123 no encontrada");
        var exCreds = new CredencialesInvalidasException("password mala");

        var responseSesion = handler.manejarFalloCredenciales(exSesion);
        var responseCreds = handler.manejarFalloCredenciales(exCreds);

        assertThat(responseSesion.getStatusCode()).isEqualTo(responseCreds.getStatusCode());
        assertThat(responseSesion.getBody().codigo()).isEqualTo(responseCreds.getBody().codigo());
        assertThat(responseSesion.getBody().mensaje()).isEqualTo(responseCreds.getBody().mensaje());

        // No debe filtrar el ID de sesión
        assertThat(responseSesion.getBody().mensaje()).doesNotContain("abc-123");
    }
}
