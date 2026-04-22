// com.tufondo.kyc.application.dto.response.HistorialKYCResponse
package com.tufondo.kyc.application.dto.response;

import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.model.enums.NivelVerificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response para historial KYC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialKYCResponse {
    private UUID socioId;
    private int totalVerificaciones;
    private List<HistorialItemResponse> historial;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistorialItemResponse {
        private UUID verificacionId;
        private NivelVerificacion nivel;
        private EstadoVerificacion estado;
        private LocalDateTime fechaInicio;
        private LocalDateTime fechaCompletado;
        private LocalDateTime fechaExpiracion;
        private Integer diasRestantes;
        private String revisadoPor;
        private String motivoRechazo;
    }
}