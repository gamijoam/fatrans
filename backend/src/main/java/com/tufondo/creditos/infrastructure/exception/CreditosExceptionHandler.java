// com/tufondo/creditos/infrastructure/exception/CreditosExceptionHandler.java
package com.tufondo.creditos.infrastructure.exception;

import com.tufondo.creditos.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception Handler para el módulo de Créditos.
 * Maneja las excepciones específicas del dominio de créditos.
 */
@RestControllerAdvice(basePackages = "com.tufondo.creditos")
public class CreditosExceptionHandler {

    // ── CRÉDITO NOT FOUND ──────────────────────────────────────────────────────

    @ExceptionHandler(CreditoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleCreditoNoEncontrado(CreditoNoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "CREDITO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(CuotaNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleCuotaNoEncontrada(CuotaNoEncontradaException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "CUOTA_NO_ENCONTRADA", ex.getMessage());
    }

    @ExceptionHandler(ColateralInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleColateralInsuficiente(ColateralInsuficienteException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "COLATERAL_INSUFICIENTE", ex.getMessage());
    }

    // ── STATUS CONFLICTS ────────────────────────────────────────────────────────

    @ExceptionHandler(CreditoActivoExistenteException.class)
    public ResponseEntity<Map<String, Object>> handleCreditoActivoExistente(CreditoActivoExistenteException ex) {
        return buildResponse(HttpStatus.CONFLICT, "CREDITO_ACTIVO_EXISTENTE", ex.getMessage());
    }

    @ExceptionHandler(EstadoCreditoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleEstadoCreditoInvalido(EstadoCreditoInvalidoException ex) {
        return buildResponse(HttpStatus.CONFLICT, "ESTADO_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(CuotaYaPagadaException.class)
    public ResponseEntity<Map<String, Object>> handleCuotaYaPagada(CuotaYaPagadaException ex) {
        return buildResponse(HttpStatus.CONFLICT, "CUOTA_YA_PAGADA", ex.getMessage());
    }

    @ExceptionHandler(PagoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handlePagoDuplicado(PagoDuplicadoException ex) {
        return buildResponse(HttpStatus.CONFLICT, "PAGO_DUPLICADO", ex.getMessage());
    }

    // ── ACCESS CONTROL ─────────────────────────────────────────────────────────

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<Map<String, Object>> handleAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "ACCESO_NO_AUTORIZADO", ex.getMessage());
    }

    // ── SCORE / EVALUATION ──────────────────────────────────────────────────────

    @ExceptionHandler(ScoreInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleScoreInsuficiente(ScoreInsuficienteException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "SCORE_INSUFICIENTE", ex.getMessage());
    }

    // ── AMOUNT VALIDATION ───────────────────────────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDACION_ERROR", ex.getMessage());
    }

    @ExceptionHandler(MontoInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleMontoInsuficiente(MontoInsuficienteException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "MONTO_INSUFICIENTE", ex.getMessage());
    }

    // ── RATE LIMITING ───────────────────────────────────────────────────────────

    @ExceptionHandler(SimulacionRateLimitException.class)
    public ResponseEntity<Map<String, Object>> handleSimulacionRateLimit(SimulacionRateLimitException ex) {
        Map<String, Object> body = buildBody(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", ex.getMessage());
        body.put("retryAfter", ex.getRetryAfterSeconds());
        return new ResponseEntity<>(body, HttpStatus.TOO_MANY_REQUESTS);
    }

    // ── HELPER ─────────────────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof org.springframework.validation.FieldError
                ? ((org.springframework.validation.FieldError) error).getField()
                : error.getObjectName();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });
        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "VALIDACION_ERROR", "Error de validación");
        body.put("details", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String errorCode, String message) {
        return new ResponseEntity<>(buildBody(status, errorCode, message), status);
    }

    private Map<String, Object> buildBody(HttpStatus status, String errorCode, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", errorCode);
        body.put("mensaje", message);
        return body;
    }
}