// com/tufondo/beneficiarios/application/dto/DeleteBeneficiarioResponseDTO.java
package com.tufondo.beneficiarios.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO para la respuesta de eliminación de beneficiario.
 */
public record DeleteBeneficiarioResponseDTO(
        UUID id,
        UUID socioId,
        boolean activo,
        String mensaje,
        BigDecimal sumaPorcentajesRestantes,
        String warning
) {
    /**
     * Factory method cuando la suma restante no es 100%.
     */
    public static DeleteBeneficiarioResponseDTO withWarning(UUID id, UUID socioId, BigDecimal sumaRestante) {
        return new DeleteBeneficiarioResponseDTO(
                id,
                socioId,
                false,
                "Beneficiario eliminado exitosamente",
                sumaRestante,
                String.format("La suma de porcentajes restantes es %.2f%%. Considere agregar o redistribuir beneficiarios.", sumaRestante)
        );
    }

    /**
     * Factory method cuando la suma restante es exactamente 100%.
     */
    public static DeleteBeneficiarioResponseDTO success(UUID id, UUID socioId, BigDecimal sumaRestante) {
        return new DeleteBeneficiarioResponseDTO(
                id,
                socioId,
                false,
                "Beneficiario eliminado exitosamente",
                sumaRestante,
                null
        );
    }
}