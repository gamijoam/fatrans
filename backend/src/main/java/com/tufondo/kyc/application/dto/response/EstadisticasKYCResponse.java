// com.tufondo.kyc.application.dto.response.EstadisticasKYCResponse
package com.tufondo.kyc.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response para estadisticas KYC (Admin).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadisticasKYCResponse {
    private long totalVerificaciones;
    private EstadoActualResponse estadoActual;
    private MetricasResponse metricas;
    private Map<String, NivelEstadisticasResponse> porNivel;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EstadoActualResponse {
        private long pendientes;
        private long enRevision;
        private long aprobados;
        private long rechazados;
        private long expirados;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetricasResponse {
        private double tiempoPromedioRevisionHoras;
        private double tasaAprobacion;
        private double tasaRechazo;
        private long kycPorExpirarProximoMes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NivelEstadisticasResponse {
        private long total;
        private long aprobados;
        private long rechazados;
    }
}