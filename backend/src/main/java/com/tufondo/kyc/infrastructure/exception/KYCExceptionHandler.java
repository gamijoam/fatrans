// com.tufondo.kyc.infrastructure.exception.KYCExceptionHandler
package com.tufondo.kyc.infrastructure.exception;

import com.tufondo.kyc.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Exception Handler para el módulo KYC.
 * Maneja las excepciones específicas del dominio de KYC.
 */
@RestControllerAdvice(basePackages = "com.tufondo.kyc")
public class KYCExceptionHandler {

    // ── NOT FOUND ───────────────────────────────────────────────────────────────

    @ExceptionHandler(VerificacionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleVerificacionNotFound(VerificacionNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "KYC_005", ex.getMessage());
    }

    @ExceptionHandler(DocumentoNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentoNotFound(DocumentoNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "KYC_006", ex.getMessage());
    }

    // ── CONFLICTS ──────────────────────────────────────────────────────────────

    @ExceptionHandler(KYCYaExisteException.class)
    public ResponseEntity<Map<String, Object>> handleKYCYаExiste(KYCYaExisteException ex) {
        return buildResponse(HttpStatus.CONFLICT, "KYC_001", ex.getMessage());
    }

    @ExceptionHandler(DocumentoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentoDuplicado(DocumentoDuplicadoException ex) {
        return buildResponse(HttpStatus.CONFLICT, "KYC_002", ex.getMessage());
    }

    // ── VALIDATION - DOCUMENTOS ────────────────────────────────────────────────

    @ExceptionHandler(DocumentosIncompletosException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentosIncompletos(DocumentosIncompletosException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "KYC_003", ex.getMessage());
    }

    @ExceptionHandler(TipoDocumentoNoPermitidoException.class)
    public ResponseEntity<Map<String, Object>> handleTipoDocumentoNoPermitido(TipoDocumentoNoPermitidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "KYC_004", ex.getMessage());
    }

    @ExceptionHandler(DocumentoFormatoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentoFormatoInvalido(DocumentoFormatoInvalidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "KYC_007", ex.getMessage());
    }

    @ExceptionHandler(DocumentoExcedeTamanoException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentoExcedeTamano(DocumentoExcedeTamanoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "KYC_008", ex.getMessage());
    }

    // ── ACCESS CONTROL ─────────────────────────────────────────────────────────

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<Map<String, Object>> handleAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "KYC_009", ex.getMessage());
    }

    @ExceptionHandler(VerificacionNoEditableException.class)
    public ResponseEntity<Map<String, Object>> handleVerificacionNoEditable(VerificacionNoEditableException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "KYC_010", ex.getMessage());
    }

    // ── DELETE VALIDATION ──────────────────────────────────────────────────────

    @ExceptionHandler(DocumentoNoEliminableException.class)
    public ResponseEntity<Map<String, Object>> handleDocumentoNoEliminable(DocumentoNoEliminableException ex) {
        return buildResponse(HttpStatus.CONFLICT, "KYC_011", ex.getMessage());
    }

    // ── FILE UPLOAD ─────────────────────────────────────────────────────────────

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "KYC_008", "El archivo excede el tamaño máximo permitido de 10MB");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ── GENERIC KYC ERROR ──────────────────────────────────────────────────────

    @ExceptionHandler(KYCException.class)
    public ResponseEntity<Map<String, Object>> handleKYCException(KYCException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getErrorCode(), ex.getMessage());
    }

    // ── HELPER ─────────────────────────────────────────────────────────────────

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
