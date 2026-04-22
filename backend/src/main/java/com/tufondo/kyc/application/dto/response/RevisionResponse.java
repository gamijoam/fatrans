// com.tufondo.kyc.application.dto.response.RevisionResponse
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
 * Response para detalle de revision (analista).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevisionResponse {
    private UUID verificacionId;
    private UUID socioId;
    private NivelVerificacion nivel;
    private EstadoVerificacion estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaEnvio;
    private List<DocumentoRevisionResponse> documentos;
    private ConsentimientoResponse consentimiento;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentoRevisionResponse {
        private UUID id;
        private TipoDocumentoKYC tipo;
        private String descripcion;
        private EstadoDocumento estado;
        private String urlVisualizacion;
        private String nombreOriginal;
        private Long tamanoBytes;
        private LocalDateTime fechaSubida;
        private String metadatosValidacion;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConsentimientoResponse {
        private boolean aceptado;
        private LocalDateTime fechaConsentimiento;
    }
}