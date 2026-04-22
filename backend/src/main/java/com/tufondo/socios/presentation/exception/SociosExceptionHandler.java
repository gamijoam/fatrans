// 📁 com/tufondo/socios/presentation/exception/SociosExceptionHandler.java
package com.tufondo.socios.presentation.exception;

import com.tufondo.socios.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.tufondo.socios")
public class SociosExceptionHandler {
    
    @ExceptionHandler(SolicitudNoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handleSolicitudNoEncontrada(SolicitudNoEncontradaException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "SOLICITUD_NOT_FOUND", ex.getMessage());
    }
    
    @ExceptionHandler(SolicitudNoEditableException.class)
    public ResponseEntity<Map<String, Object>> handleSolicitudNoEditable(SolicitudNoEditableException ex) {
        return buildResponse(HttpStatus.CONFLICT, "SOLICITUD_NO_EDITABLE", ex.getMessage());
    }
    
    @ExceptionHandler(CedulaDuplicadaException.class)
    public ResponseEntity<Map<String, Object>> handleCedulaDuplicada(CedulaDuplicadaException ex) {
        return buildResponse(HttpStatus.CONFLICT, "CEDULA_DUPLICADA", ex.getMessage());
    }
    
    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleCorreoDuplicado(CorreoDuplicadoException ex) {
        return buildResponse(HttpStatus.CONFLICT, "EMAIL_DUPLICADO", ex.getMessage());
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