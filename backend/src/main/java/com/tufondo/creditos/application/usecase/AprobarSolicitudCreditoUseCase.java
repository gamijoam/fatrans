// com/tufondo/creditos/application/usecase/AprobarSolicitudCreditoUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.AprobarRechazarRequest;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.exception.ColateralInsuficienteException;
import com.tufondo.creditos.domain.exception.CreditoNoEncontradoException;
import com.tufondo.creditos.domain.exception.EstadoCreditoInvalidoException;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import com.tufondo.creditos.domain.repository.CuentaGarantiaRepository;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Caso de uso para aprobar una solicitud de crédito.
 * Validación de colateral requerida antes de aprobar.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AprobarSolicitudCreditoUseCase {

    private final SolicitudCreditoRepository solicitudRepository;
    private final TipoCreditoRepository tipoCreditoRepository;
    private final CuentaGarantiaRepository cuentaGarantiaRepository;
    private final CreditosDTOMapper mapper;

    @Transactional
    public Map<String, Object> ejecutar(String numeroSolicitud, AprobarRechazarRequest request) {
        SolicitudCredito solicitud = solicitudRepository.buscarPorNumeroSolicitud(numeroSolicitud)
            .orElseThrow(() -> new CreditoNoEncontradoException(numeroSolicitud));

        // Validar estado
        if (solicitud.getEstado() != EstadoSolicitud.EN_EVALUACION) {
            throw new EstadoCreditoInvalidoException(numeroSolicitud, solicitud.getEstado(), "aprobación");
        }

        // Obtener tipo de crédito para calcular colateral
        TipoCredito tipoCredito = tipoCreditoRepository.buscarPorId(solicitud.getTipoCreditoId())
            .orElseThrow(() -> new CreditoNoEncontradoException(solicitud.getTipoCreditoId()));

        // Calcular colateral requerido
        BigDecimal colateralRequerido = solicitud.getMontoSolicitado()
            .multiply(tipoCredito.getPorcentajeRequerimientoColateral());

        // HALLLAGO 2 FIX: Si el tipo de crédito requiere colateral pero no hay cuenta asignada, rechazar
        boolean requiereColateral = tipoCredito.getPorcentajeRequerimientoColateral()
            .compareTo(BigDecimal.ZERO) > 0;
        
        if (requiereColateral && solicitud.getColateralCuentaId() == null) {
            log.warn("Aprobación denegada: tipo de crédito {} requiere colateral {}% pero no hay cuenta de garantía asignada",
                tipoCredito.getCodigo(), tipoCredito.getPorcentajeRequerimientoColateral());
            throw new ColateralInsuficienteException(colateralRequerido, BigDecimal.ZERO);
        }

        // HALLLAGO 2 FIX: Validar saldo disponible ANTES de aprobar
        if (solicitud.getColateralCuentaId() != null) {
            boolean tieneSaldoSuficiente = cuentaGarantiaRepository
                .verificarSaldoParaColateral(solicitud.getColateralCuentaId(), colateralRequerido);
            
            if (!tieneSaldoSuficiente) {
                log.warn("Aprobación denegada: saldo insuficiente para colateral. Solicitud: {}, Requerido: {}", 
                    numeroSolicitud, colateralRequerido);
                throw new ColateralInsuficienteException(colateralRequerido, BigDecimal.ZERO);
            }
            
            // Retener el colateral
            cuentaGarantiaRepository.retenerSaldo(solicitud.getColateralCuentaId(), colateralRequerido);
            log.info("Colateral retenido: {} para solicitud {}", colateralRequerido, numeroSolicitud);
        }

        // Aplicar override de tasa si existe
        BigDecimal tasaInteres = request.getTasaInteresOverride() != null 
            ? request.getTasaInteresOverride() 
            : solicitud.getTasaInteresAplicada();

        // Transicionar estado
        solicitud.setTasaInteresAplicada(tasaInteres);
        solicitud.setColateralMontoRetenido(colateralRequerido);
        solicitud.transicionarA(EstadoSolicitud.APROBADA);
        solicitud.setUpdatedAt(LocalDateTime.now());
        solicitudRepository.guardar(solicitud);

        log.info("Solicitud {} aprobada - Colateral: {} - Tasa: {}", 
            numeroSolicitud, colateralRequerido, tasaInteres);

        Map<String, Object> response = new HashMap<>();
        response.put("id", solicitud.getId().toString());
        response.put("numeroSolicitud", numeroSolicitud);
        response.put("estado", EstadoSolicitud.APROBADA.name());
        response.put("tasaInteresAplicada", tasaInteres);
        response.put("colateralMontoRetenido", colateralRequerido);
        response.put("mensaje", "Crédito aprobado. Colateral retenido de cuenta de ahorro.");
        response.put("fechaAprobacion", LocalDateTime.now().toString());
        return response;
    }
}