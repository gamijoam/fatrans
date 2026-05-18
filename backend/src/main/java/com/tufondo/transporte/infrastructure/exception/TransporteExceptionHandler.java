package com.tufondo.transporte.infrastructure.exception;

import com.tufondo.transporte.domain.exception.PlacaDuplicadaException;
import com.tufondo.transporte.domain.exception.UnidadNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.tufondo.transporte.presentation.controller")
public class TransporteExceptionHandler {

    @ExceptionHandler(PlacaDuplicadaException.class)
    public ResponseEntity<Map<String, String>> handlePlacaDuplicada(PlacaDuplicadaException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "PLACA_DUPLICADA");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UnidadNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> handleUnidadNoEncontrada(UnidadNoEncontradaException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "UNIDAD_NO_ENCONTRADA");
        response.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}
