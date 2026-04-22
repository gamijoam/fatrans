// com.tufondo.kyc.application.dto.response.RevocarConsentimientoResponse
package com.tufondo.kyc.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response para revocación de consentimiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevocarConsentimientoResponse {

    private UUID consentimientoId;
    private String mensaje;
    private LocalDateTime fechaRevocacion;
    private Boolean revocacionExitosa;
}