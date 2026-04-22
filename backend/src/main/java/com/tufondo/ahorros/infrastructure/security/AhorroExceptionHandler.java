// com/tufondo/ahorros/infrastructure/security/AhorroExceptionHandler.java
package com.tufondo.ahorros.infrastructure.security;

import com.tufondo.ahorros.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception Handler para el módulo de Ahorros.
 * Maneja todas las excepciones específicas del dominio.
 */
@RestControllerAdvice(basePackages = "com.tufondo.ahorros")
public class AhorroExceptionHandler {

    @ExceptionHandler(CuentaAhorroNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaNoEncontrada(CuentaAhorroNoEncontradaException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "CUENTA_NO_ENCONTRADA", ex.getMessage());
    }

    @ExceptionHandler(CuentaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaDuplicada(CuentaDuplicadaException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "CUENTA_DUPLICADA", ex.getMessage());
    }

    @ExceptionHandler(CuentaNoPermiteOperacionesException.class)
    public ResponseEntity<Map<String, Object>> handleCuentaNoPermiteOperaciones(CuentaNoPermiteOperacionesException ex) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "CUENTA_NO_PERMITE_OPERACIONES", ex.getMessage());
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> handleSaldoInsuficiente(SaldoInsuficienteException ex) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "SALDO_INSUFICIENTE", ex.getMessage());
    }

    @ExceptionHandler(SaldoNoCeroException.class)
    public ResponseEntity<Map<String, Object>> handleSaldoNoCero(SaldoNoCeroException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "SALDO_NO_CERO");
        body.put("mensaje", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MovimientoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleMovimientoNoEncontrado(MovimientoNoEncontradoException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "MOVIMIENTO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(MontoExcedeLimiteException.class)
    public ResponseEntity<Map<String, Object>> handleMontoExcedeLimite(MontoExcedeLimiteException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "MONTO_EXCEDE_LIMITE");
        body.put("mensaje", ex.getMessage());
        body.put("detalles", Map.of(
                "montoRecibido", ex.getMontoRecibido(),
                "limitePermitido", ex.getLimitePermitido()
        ));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RendimientoYaAplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleRendimientoYaAplicado(RendimientoYaAplicadoException ex) {
        return buildErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "RENDIMIENTO_YA_APLICADO", ex.getMessage());
    }

    @ExceptionHandler(AccesoCuentaAjenaException.class)
    public ResponseEntity<Map<String, Object>> handleAccesoCuentaAjena(AccesoCuentaAjenaException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "ACCESO_CUENTA_AJENA", ex.getMessage());
    }

    @ExceptionHandler(TasaInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleTasaInvalida(TasaInvalidaException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "TASA_INVALIDA", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("mensaje", message);
        return new ResponseEntity<>(body, status);
    }
}