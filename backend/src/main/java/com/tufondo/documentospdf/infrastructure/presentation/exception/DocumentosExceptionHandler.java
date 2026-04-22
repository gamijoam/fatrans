// com.tufondo.documentospdf.infrastructure.presentation.exception.DocumentosExceptionHandler
package com.tufondo.documentospdf.infrastructure.presentation.exception;

import com.tufondo.documentospdf.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception Handler para el módulo de Documentos PDF.
 * Maneja las excepciones específicas del dominio.
 */
@RestControllerAdvice(basePackages = "com.tufondo.documentospdf")
public class DocumentosExceptionHandler {

    // ── DOCUMENTO NOT FOUND ───────────────────────────────────────────────────

    @ExceptionHandler(DocumentoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentoNoEncontrado(DocumentoNoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getCodigo(), ex.getMessage());
    }

    // ── ESTADO DOCUMENTO ─────────────────────────────────────────────────────

    @ExceptionHandler(DocumentoExpiradoException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentoExpirado(DocumentoExpiradoException ex) {
        return buildResponse(HttpStatus.GONE, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(DocumentoRevocadoException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentoRevocado(DocumentoRevocadoException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getCodigo(), ex.getMessage());
    }

    // ── ACCESO / IDOR ─────────────────────────────────────────────────────────

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<Map<String, Object>> handleAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getCodigo(), ex.getMessage());
    }

    // ── GENERACIÓN / FIRMA ───────────────────────────────────────────────────

    @ExceptionHandler(GeneracionPDFException.class)
    public ResponseEntity<Map<String, Object>> handleGeneracionPdf(GeneracionPDFException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getCodigo(), ex.getMessage());
    }

    @ExceptionHandler(FirmaDigitalException.class)
    public ResponseEntity<Map<String, Object>> handleFirmaDigital(FirmaDigitalException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getCodigo(), ex.getMessage());
    }

    // ── MALWARE ───────────────────────────────────────────────────────────────

    @ExceptionHandler(EscaneoMalwareException.class)
    public ResponseEntity<Map<String, Object>> handleEscaneoMalware(EscaneoMalwareException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getCodigo(), ex.getMessage());
    }

    // ── VALIDACIÓN ───────────────────────────────────────────────────────────

    @ExceptionHandler(TipoDocumentoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleTipoDocumentoInvalido(TipoDocumentoInvalidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getCodigo(), ex.getMessage());
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String errorCode, String message) {
        Map<String, Object> body = buildBody(status, errorCode, message);
        return new ResponseEntity<>(body, status);
    }

    private Map<String, Object> buildBody(HttpStatus status, String errorCode, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("codigo", errorCode);
        body.put("mensaje", message);
        return body;
    }
}
