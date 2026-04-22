// com.tufondo.kyc.application.dto.request.IniciarKYCRequest
package com.tufondo.kyc.application.dto.request;

import com.tufondo.kyc.domain.model.enums.NivelVerificacion;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para iniciar un proceso KYC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IniciarKYCRequest {

    @NotNull(message = "nivel es requerido")
    private NivelVerificacion nivel;

    @NotNull(message = "consentimientoAceptado es requerido")
    private Boolean consentimientoAceptado;

    @NotNull(message = "versionPolitica es requerido")
    private String versionPolitica;

    private String ipCliente;

    private String userAgent;
}