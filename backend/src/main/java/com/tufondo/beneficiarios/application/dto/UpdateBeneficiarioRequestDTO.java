// com/tufondo/beneficiarios/application/dto/UpdateBeneficiarioRequestDTO.java
package com.tufondo.beneficiarios.application.dto;

import com.tufondo.beneficiarios.domain.model.enums.Parentesco;
import com.tufondo.beneficiarios.domain.model.enums.TipoDocumento;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * DTO para actualizar un beneficiario existente.
 */
public record UpdateBeneficiarioRequestDTO(
        @NotBlank(message = "nombreCompleto es requerido")
        @Size(max = 200, message = "nombreCompleto debe tener máximo 200 caracteres")
        String nombreCompleto,

        @NotBlank(message = "numeroDocumento es requerido")
        @Size(max = 20, message = "numeroDocumento debe tener máximo 20 caracteres")
        String numeroDocumento,

        @NotNull(message = "tipoDocumento es requerido")
        TipoDocumento tipoDocumento,

        @NotNull(message = "parentesco es requerido")
        Parentesco parentesco,

        @NotNull(message = "porcentaje es requerido")
        @DecimalMin(value = "0.01", message = "porcentaje debe ser >= 0.01")
        @DecimalMax(value = "100.00", message = "porcentaje debe ser <= 100.00")
        BigDecimal porcentaje,

        @Size(max = 20, message = "telefono debe tener máximo 20 caracteres")
        String telefono
) {}