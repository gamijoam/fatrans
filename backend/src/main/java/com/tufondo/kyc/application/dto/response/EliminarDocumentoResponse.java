// com.tufondo.kyc.application.dto.response.EliminarDocumentoResponse
package com.tufondo.kyc.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response para eliminar documento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EliminarDocumentoResponse {
    private boolean eliminado;
    private String mensaje;
}