// 📁 backend/src/main/java/com/tufondo/presentation/exception/GlobalExceptionHandler.java
package com.tufondo.presentation.exception;

import com.tufondo.socios.domain.exception.SocioNoEncontradoException;
import com.tufondo.socios.domain.exception.NumeroSocioYaRegistradoException;
import com.tufondo.socios.domain.exception.NumeroDocumentoYaRegistradoException;
import com.tufondo.socios.domain.exception.CorreoYaRegistradoException;
import com.tufondo.socios.domain.exception.FechaNacimientoInvalidaException;
import com.tufondo.socios.domain.exception.EstadoSocioInvalidoException;
import com.tufondo.socios.domain.exception.FormatoCorreoInvalidoException;
import com.tufondo.socios.domain.exception.FormatoTelefonoInvalidoException;
import com.tufondo.socios.domain.exception.CedulaDuplicadaException;
import com.tufondo.socios.domain.exception.CorreoDuplicadoException;
import com.tufondo.auth.domain.exception.*;
import com.tufondo.beneficiarios.domain.exception.BeneficiarioNoEncontradoException;
import com.tufondo.beneficiarios.domain.exception.MaximoBeneficiariosExcedidoException;
import com.tufondo.beneficiarios.domain.exception.PorcentajeSumExcedidoException;
import com.tufondo.beneficiarios.domain.exception.DocumentoIgualAlTitularException;
import com.tufondo.beneficiarios.domain.exception.BeneficiarioDuplicadoException;
import com.tufondo.beneficiarios.domain.exception.PorcentajeInvalidoException;
import com.tufondo.beneficiarios.domain.exception.AccesoNoAutorizadoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── SOCIOS EXCEPTIONS ──────────────────────────────────────────────────────

    @ExceptionHandler(SocioNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleSocioNoEncontrado(SocioNoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NumeroSocioYaRegistradoException.class)
    public ResponseEntity<Map<String, Object>> handleNumeroSocioYaRegistrado(NumeroSocioYaRegistradoException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(NumeroDocumentoYaRegistradoException.class)
    public ResponseEntity<Map<String, Object>> handleNumeroDocumentoYaRegistrado(NumeroDocumentoYaRegistradoException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CorreoYaRegistradoException.class)
    public ResponseEntity<Map<String, Object>> handleCorreoYaRegistrado(CorreoYaRegistradoException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(FechaNacimientoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleFechaNacimientoInvalida(FechaNacimientoInvalidaException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EstadoSocioInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleEstadoSocioInvalido(EstadoSocioInvalidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(FormatoCorreoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleFormatoCorreoInvalido(FormatoCorreoInvalidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(FormatoTelefonoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleFormatoTelefonoInvalido(FormatoTelefonoInvalidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CedulaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleCedulaDuplicada(CedulaDuplicadaException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleCorreoDuplicado(CorreoDuplicadoException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // ── BENEFICIARIOS EXCEPTIONS ───────────────────────────────────────────────

    @ExceptionHandler(BeneficiarioNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleBeneficiarioNoEncontrado(BeneficiarioNoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MaximoBeneficiariosExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handleMaximoBeneficiariosExcedido(MaximoBeneficiariosExcedidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PorcentajeSumExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handlePorcentajeSumExcedido(PorcentajeSumExcedidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DocumentoIgualAlTitularException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentoIgualAlTitular(DocumentoIgualAlTitularException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(BeneficiarioDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleBeneficiarioDuplicado(BeneficiarioDuplicadoException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(PorcentajeInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handlePorcentajeInvalido(PorcentajeInvalidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<Map<String, Object>> handleAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(com.tufondo.beneficiarios.domain.exception.SocioNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleBeneficiarioSocioNoEncontrado(com.tufondo.beneficiarios.domain.exception.SocioNoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── AUTH EXCEPTIONS ────────────────────────────────────────────────────────

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(CuentaDesactivadaException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaDesactivada(CuentaDesactivadaException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(CuentaBloqueadaException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaBloqueada(CuentaBloqueadaException ex) {
        return buildResponse(HttpStatus.LOCKED, ex.getMessage());
    }

    @ExceptionHandler(TokenExpiradoException.class)
    public ResponseEntity<Map<String, Object>> handleTokenExpirado(TokenExpiradoException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleTokenInvalido(TokenInvalidoException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(SesionNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleSesionNoEncontrada(SesionNoEncontradaException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── VALIDATION FALLBACK ────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Parámetro '%s' con valor '%s' tiene formato inválido. Expected type: %s",
                ex.getName(), ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        log.warn("Error de validación: {}", errors);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validación fallida");
        body.put("details", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ── GENERIC FALLBACK ───────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDACION_ERROR: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno del servidor: " + ex.getMessage());
    }

    // ── HELPER ─────────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
