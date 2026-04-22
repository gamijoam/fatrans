// com.tufondo.kyc.application.dto.request.RevocarConsentimientoRequest
package com.tufondo.kyc.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para revocar consentimiento KYC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevocarConsentimientoRequest {

    @NotNull(message = "confirmacion es requerida")
    private Boolean confirmacion;

    private String motivoRevocacion;
}