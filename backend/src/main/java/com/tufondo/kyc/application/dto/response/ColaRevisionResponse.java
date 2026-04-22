// com.tufondo.kyc.application.dto.response.ColaRevisionResponse
package com.tufondo.kyc.application.dto.response;

import com.tufondo.kyc.domain.model.enums.EstadoDocumento;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.model.enums.NivelVerificacion;
import com.tufondo.kyc.domain.model.enums.TipoDocumentoKYC;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response para cola de revision (analista).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ColaRevisionResponse {
    private int pagina;
    private int tamanio;
    private long totalElementos;
    private int totalPaginas;
    private List<ColaItemResponse> cola;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ColaItemResponse {
        private UUID verificacionId;
        private UUID socioId;
        private NivelVerificacion nivel;
        private EstadoVerificacion estado;
        private LocalDateTime fechaEnvio;
        private String tiempoEnCola;
        private List<DocumentoResumenResponse> documentos;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentoResumenResponse {
        private TipoDocumentoKYC tipo;
        private EstadoDocumento estado;
        private String nombreOriginal;
    }
}