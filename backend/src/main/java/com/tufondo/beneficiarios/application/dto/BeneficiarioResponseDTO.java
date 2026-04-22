// com/tufondo/beneficiarios/application/dto/BeneficiarioResponseDTO.java
package com.tufondo.beneficiarios.application.dto;

import com.tufondo.beneficiarios.domain.model.Beneficiario;
import com.tufondo.beneficiarios.domain.model.enums.Parentesco;
import com.tufondo.beneficiarios.domain.model.enums.TipoDocumento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO de respuesta para un beneficiario.
 * 🔒 SECURITY: Enmascara el número de documento para proteger datos sensibles.
 */
public record BeneficiarioResponseDTO(
        UUID id,
        UUID socioId,
        String nombreCompleto,
        String numeroDocumento,
        TipoDocumento tipoDocumento,
        Parentesco parentesco,
        BigDecimal porcentaje,
        String telefono,
        boolean activo,
        Instant fechaRegistro,
        Instant fechaActualizacion
) {
    private static final int DIGITOS_VISIBLE_DOCUMENTO = 4;

    /**
     * Factory method para crear desde la entidad de dominio.
     * 🔒 SECURITY: Enmascara el número de documento (muestra solo últimos 4 dígitos).
     */
    public static BeneficiarioResponseDTO fromDomain(Beneficiario beneficiario) {
        String documentoEnmascarado = enmascararDocumento(beneficiario.getNumeroDocumento());

        return new BeneficiarioResponseDTO(
                beneficiario.getId(),
                beneficiario.getSocioId(),
                beneficiario.getNombreCompleto(),
                documentoEnmascarado,
                beneficiario.getTipoDocumento(),
                beneficiario.getParentesco(),
                beneficiario.getPorcentaje(),
                beneficiario.getTelefono(),
                beneficiario.isActivo(),
                beneficiario.getFechaRegistro(),
                beneficiario.getFechaActualizacion()
        );
    }

    /**
     * 🔒 SECURITY: Enmascara documento mostrando solo últimos 4 dígitos.
     * Ejemplo: V-12345678 → ****5678
     */
    private static String enmascararDocumento(String documento) {
        if (documento == null || documento.length() <= DIGITOS_VISIBLE_DOCUMENTO) {
            return "****";
        }
        int length = documento.length();
        return "*".repeat(length - DIGITOS_VISIBLE_DOCUMENTO) + documento.substring(length - DIGITOS_VISIBLE_DOCUMENTO);
    }
}