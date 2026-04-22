// com.tufondo.kyc.application.dto.response.SubirDocumentoResponse
package com.tufondo.kyc.application.dto.response;

import com.tufondo.kyc.domain.model.enums.EstadoDocumento;
import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response para subir documento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubirDocumentoResponse {
    private UUID documentoId;
    private TipoDocumentoKYC tipoDocumento;
    private String nombreOriginal;
    private EstadoDocumento estado;
    private String mensaje;
}