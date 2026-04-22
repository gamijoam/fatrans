// com/tufondo/beneficiarios/presentation/exception/BeneficiarioExceptionHandler.java
package com.tufondo.beneficiarios.presentation.exception;

import com.tufondo.beneficiarios.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.tufondo.beneficiarios")
public class BeneficiarioExceptionHandler {

    @ExceptionHandler(BeneficiarioNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleBeneficiarioNoEncontrado(BeneficiarioNoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "BENEFICIARIO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(BeneficiarioDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleBeneficiarioDuplicado(BeneficiarioDuplicadoException ex) {
        return buildResponse(HttpStatus.CONFLICT, "BENEFICIARIO_DUPLICADO", ex.getMessage());
    }

    @ExceptionHandler(PorcentajeInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handlePorcentajeInvalido(PorcentajeInvalidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "PORCENTAJE_INVALIDO", ex.getMessage());
    }

    @ExceptionHandler(PorcentajeSumExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handlePorcentajeSumExcedido(PorcentajeSumExcedidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "PORCENTAJE_SUM_EXCEDIDO", ex.getMessage());
    }

    @ExceptionHandler(MaximoBeneficiariosExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handleMaximoBeneficiariosExcedido(MaximoBeneficiariosExcedidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "MAXIMO_BENEFICIARIOS_EXCEDIDO", ex.getMessage());
    }

    @ExceptionHandler(DocumentoIgualAlTitularException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentoIgualAlTitular(DocumentoIgualAlTitularException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "DOCUMENTO_IGUAL_TITULAR", ex.getMessage());
    }

    @ExceptionHandler(SocioNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleSocioNoEncontrado(SocioNoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "SOCIO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<Map<String, Object>> handleAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "Acceso denegado");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "VALIDATION_ERROR");
        body.put("errors", errors);
        body.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", message);
        body.put("code", code);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}