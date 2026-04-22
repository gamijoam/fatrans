// com.tufondo.kyc.application.dto.response.RevisionDecisionResponse
package com.tufondo.kyc.application.dto.response;

import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response generico para decisiones de revision.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevisionDecisionResponse {
    private UUID verificacionId;
    private EstadoVerificacion estadoAnterior;
    private EstadoVerificacion estadoNuevo;
    private String mensaje;
}