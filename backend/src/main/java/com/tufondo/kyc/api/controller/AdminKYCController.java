// com.tufondo.kyc.api.controller.AdminKYCController
package com.tufondo.kyc.api.controller;

import com.tufondo.kyc.application.dto.response.ColaRevisionResponse;
import com.tufondo.kyc.application.dto.response.EstadisticasKYCResponse;
import com.tufondo.kyc.application.dto.response.HistorialKYCResponse;
import com.tufondo.kyc.domain.model.VerificacionKYC;
import com.tufondo.kyc.domain.model.enums.EstadoVerificacion;
import com.tufondo.kyc.domain.model.enums.NivelVerificacion;
import com.tufondo.kyc.domain.repository.VerificacionKYCRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller para KYC - Endpoints de Admin.
 */
@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
@Tag(name = "KYC Admin", description = "Endpoints administrativos de KYC")
@SecurityRequirement(name = "bearerAuth")
public class AdminKYCController {

    private final VerificacionKYCRepository verificacionRepository;

    /**
     * GET /kyc/cola-revision - Cola de Revision
     */
    @GetMapping("/cola-revision")
    @PreAuthorize("hasAnyRole('ANALISTA_KYC', 'ADMIN')")
    @Operation(summary = "Obtener cola de revision de verificaciones")
    public ResponseEntity<ColaRevisionResponse> obtenerColaRevision(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) NivelVerificacion nivel,
            @RequestParam(defaultValue = "EN_REVISION") EstadoVerificacion estado) {

        List<VerificacionKYC> verificaciones;

        if (nivel != null) {
            verificaciones = verificacionRepository.findByEstado(estado).stream()
                .filter(v -> v.getNivel() == nivel)
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        } else {
            verificaciones = verificacionRepository.findByRevisionPendienteOrderByFechaAsc().stream()
                .skip((long) page * size)
                .limit(size)
                .collect(Collectors.toList());
        }

        List<ColaRevisionResponse.ColaItemResponse> cola = verificaciones.stream()
            .map(this::toColaItem)
            .collect(Collectors.toList());

        long total = verificacionRepository.countByEstado(estado);

        ColaRevisionResponse response = ColaRevisionResponse.builder()
            .pagina(page)
            .tamanio(size)
            .totalElementos(total)
            .totalPaginas((int) Math.ceil((double) total / size))
            .cola(cola)
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /kyc/admin/socio/{socioId} - Obtener KYC de un socio específico (Admin)
     */
    @GetMapping("/admin/socio/{socioId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener verificación KYC actual de un socio")
    public ResponseEntity<?> obtenerKycPorSocio(@PathVariable UUID socioId) {
        return verificacionRepository.findBySocioId(socioId)
            .map(verificacion -> {
                HistorialKYCResponse.HistorialItemResponse item = toHistorialItem(verificacion);
                return ResponseEntity.ok(Map.of(
                    "verificacionId", verificacion.getId(),
                    "socioId", socioId,
                    "nivel", verificacion.getNivel(),
                    "estado", verificacion.getEstado(),
                    "fechaInicio", verificacion.getFechaInicio(),
                    "fechaCompletado", verificacion.getFechaCompletado() != null ? verificacion.getFechaCompletado() : "",
                    "fechaExpiracion", verificacion.getFechaExpiracion() != null ? verificacion.getFechaExpiracion() : "",
                    "revisadoPor", verificacion.getRevisadoPor() != null ? verificacion.getRevisadoPor() : "",
                    "motivoRechazo", verificacion.getMotivoRechazo() != null ? verificacion.getMotivoRechazo() : ""
                ));
            })
            .orElse(ResponseEntity.ok(Map.of(
                "socioId", socioId,
                "estado", "SIN_KYC",
                "mensaje", "El socio no tiene verificaciones KYC"
            )));
    }

    /**
     * GET /kyc/historial - Historial de Verificaciones
     */
    @GetMapping("/historial")
    @PreAuthorize("hasRole('SOCIO')")
    @Operation(summary = "Obtener historial de verificaciones del socio")
    public ResponseEntity<HistorialKYCResponse> obtenerHistorial(Authentication authentication) {
        UUID socioId = UUID.fromString(authentication.getName());

        List<VerificacionKYC> verificaciones = verificacionRepository.findBySocioId(socioId).stream()
            .sorted((a, b) -> b.getFechaInicio().compareTo(a.getFechaInicio()))
            .collect(Collectors.toList());

        List<HistorialKYCResponse.HistorialItemResponse> historial = verificaciones.stream()
            .map(this::toHistorialItem)
            .collect(Collectors.toList());

        HistorialKYCResponse response = HistorialKYCResponse.builder()
            .socioId(socioId)
            .totalVerificaciones(verificaciones.size())
            .historial(historial)
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /kyc/admin/estadisticas - Estadisticas
     */
    @GetMapping("/admin/estadisticas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener estadisticas de KYC")
    public ResponseEntity<EstadisticasKYCResponse> obtenerEstadisticas() {

        long pendientes = verificacionRepository.countByEstado(EstadoVerificacion.PENDIENTE);
        long enRevision = verificacionRepository.countByEstado(EstadoVerificacion.EN_REVISION);
        long aprobados = verificacionRepository.countByEstado(EstadoVerificacion.APROBADO);
        long rechazados = verificacionRepository.countByEstado(EstadoVerificacion.RECHAZADO);
        long expirados = verificacionRepository.countByEstado(EstadoVerificacion.EXPIRADO);

        long total = pendientes + enRevision + aprobados + rechazados + expirados;

        double tasaAprobacion = total > 0 ? (double) aprobados / total : 0;
        double tasaRechazo = total > 0 ? (double) rechazados / total : 0;

        Double tiempoPromedioHoras = verificacionRepository.calculateTiempoPromedioRevisionHoras();
        Long kycPorExpirar = verificacionRepository.countPorExpirarEntreFechas(
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(30)
        );

        EstadisticasKYCResponse.MetricasResponse metricas = EstadisticasKYCResponse.MetricasResponse.builder()
            .tiempoPromedioRevisionHoras(tiempoPromedioHoras != null ? tiempoPromedioHoras : 0.0)
            .tasaAprobacion(tasaAprobacion)
            .tasaRechazo(tasaRechazo)
            .kycPorExpirarProximoMes(kycPorExpirar != null ? kycPorExpirar : 0L)
            .build();

        EstadisticasKYCResponse.EstadoActualResponse estadoActual = EstadisticasKYCResponse.EstadoActualResponse.builder()
            .pendientes(pendientes)
            .enRevision(enRevision)
            .aprobados(aprobados)
            .rechazados(rechazados)
            .expirados(expirados)
            .build();

        EstadisticasKYCResponse response = EstadisticasKYCResponse.builder()
            .totalVerificaciones(total)
            .estadoActual(estadoActual)
            .metricas(metricas)
            .build();

        return ResponseEntity.ok(response);
    }

    private ColaRevisionResponse.ColaItemResponse toColaItem(VerificacionKYC v) {
        String tiempoEnCola = calcularTiempoEnCola(v.getUpdatedAt());

        return ColaRevisionResponse.ColaItemResponse.builder()
            .verificacionId(v.getId())
            .socioId(v.getSocioId())
            .nivel(v.getNivel())
            .estado(v.getEstado())
            .fechaEnvio(v.getUpdatedAt())
            .tiempoEnCola(tiempoEnCola)
            .build();
    }

    private HistorialKYCResponse.HistorialItemResponse toHistorialItem(VerificacionKYC v) {
        Integer diasRestantes = null;
        if (v.getFechaExpiracion() != null) {
            long dias = ChronoUnit.DAYS.between(LocalDateTime.now(), v.getFechaExpiracion());
            diasRestantes = dias > 0 ? (int) dias : 0;
        }

        return HistorialKYCResponse.HistorialItemResponse.builder()
            .verificacionId(v.getId())
            .nivel(v.getNivel())
            .estado(v.getEstado())
            .fechaInicio(v.getFechaInicio())
            .fechaCompletado(v.getFechaCompletado())
            .fechaExpiracion(v.getFechaExpiracion())
            .diasRestantes(diasRestantes)
            .revisadoPor(v.getRevisadoPor())
            .motivoRechazo(v.getMotivoRechazo())
            .build();
    }

    private String calcularTiempoEnCola(LocalDateTime fechaEnvio) {
        if (fechaEnvio == null) return "0 minutos";

        long minutos = ChronoUnit.MINUTES.between(fechaEnvio, LocalDateTime.now());

        if (minutos < 60) {
            return minutos + " minutos";
        } else if (minutos < 1440) {
            return (minutos / 60) + " horas";
        } else {
            return (minutos / 1440) + " dias";
        }
    }
}