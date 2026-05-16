// com.tufondo.kyc.application.dto.response.EstadoKYCResponse
package com.tufondo.kyc.application.dto.response;

import com.tufondo.kyc.domain.model.enums.EstadoBiometria;
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
 * Response para consultar estado KYC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoKYCResponse {
    private UUID verificacionId;
    private UUID socioId;
    private NivelVerificacion nivel;
    private EstadoVerificacion estado;
    private String descripcionEstado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaExpiracion;
    private int diasRestantes;
    private int documentosRequeridos;
    private int documentosValidos;
    private List<DocumentoEstadoResponse> documentos;
    private String comentarioRevision;
    private String motivoRechazo;

    /**
     * Cache del resultado biométrico (Didit). Se expone al frontend para que la
     * UI sepa si el socio ya pasó la captura de selfie + cédula sin tener que
     * consultar otro endpoint. Si el adapter biométrico está deshabilitado,
     * queda en {@code NO_INICIADA}.
     */
    private EstadoBiometria estadoBiometria;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocumentoEstadoResponse {
        private UUID id;
        private TipoDocumentoKYC tipo;
        private String descripcion;
        private EstadoDocumento estado;
        private String nombreOriginal;
        private LocalDateTime fechaSubida;
        private String motivoRechazo;
    }
}