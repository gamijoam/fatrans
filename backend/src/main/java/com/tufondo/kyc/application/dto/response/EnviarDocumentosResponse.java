// com.tufondo.kyc.application.dto.response.EnviarDocumentosResponse
package com.tufondo.kyc.application.dto.response;

import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response para enviar documentos a revision.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnviarDocumentosResponse {
    private UUID verificacionId;
    private EstadoVerificacion estado;
    private int documentosEnviados;
    private String mensaje;
}