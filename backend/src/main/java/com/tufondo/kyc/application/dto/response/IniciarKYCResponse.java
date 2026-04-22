// com.tufondo.kyc.application.dto.response.IniciarKYCResponse
package com.tufondo.kyc.application.dto.response;

import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.model.enums.NivelVerificacion;
import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response para iniciar KYC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IniciarKYCResponse {
    private UUID verificacionId;
    private NivelVerificacion nivel;
    private EstadoVerificacion estado;
    private List<TipoDocumentoKYC> documentosRequeridos;
    private String mensaje;
}