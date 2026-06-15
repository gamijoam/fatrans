// com/tufondo/creditos/application/usecase/CrearSolicitudCreditoUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.CrearSolicitudCreditoRequest;
import com.tufondo.creditos.application.dto.SolicitudCreditoResponse;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.exception.CreditoActivoExistenteException;
import com.tufondo.creditos.domain.exception.CreditoNoEncontradoException;
import com.tufondo.creditos.domain.model.PlanAmortizacion;
import com.tufondo.creditos.domain.model.SolicitudCredito;
import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import com.tufondo.creditos.domain.model.enums.FrecuenciaPago;
import com.tufondo.creditos.domain.repository.SolicitudCreditoRepository;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Caso de uso para crear una solicitud de crédito.
 * UC-CRE-01: Crear Solicitud de Crédito
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrearSolicitudCreditoUseCase {

    private final SolicitudCreditoRepository solicitudRepository;
    private final TipoCreditoRepository tipoCreditoRepository;
    private final CreditosDTOMapper mapper;

    @Transactional
    public SolicitudCreditoResponse ejecutar(CrearSolicitudCreditoRequest request, UUID socioId) {
        log.info("CrearSolicitudCreditoUseCase.execute: tipoCreditoId={}, monto={}, plazo={}",
            request.getTipoCreditoId(), request.getMontoSolicitado(), request.getPlazoMeses());

        // Validar que el socio no tenga un crédito activo
        if (solicitudRepository.existeCreditoActivoPorSocio(socioId)) {
            log.warn("Socio {} ya tiene un crédito activo", socioId);
            throw new CreditoActivoExistenteException(socioId);
        }

        // Obtener tipo de crédito
        TipoCredito tipoCredito = tipoCreditoRepository.buscarPorId(request.getTipoCreditoId())
            .orElseThrow(() -> {
                log.error("Tipo de crédito {} no encontrado", request.getTipoCreditoId());
                return new CreditoNoEncontradoException(request.getTipoCreditoId());
            });

        log.info("TipoCredito encontrado: {} (min={}, max={})",
            tipoCredito.getNombre(), tipoCredito.getMontoMinimo(), tipoCredito.getMontoMaximo());

        // Validar monto y plazo
        if (!tipoCredito.validaMonto(request.getMontoSolicitado())) {
            log.warn("Monto {} fuera de límites para tipo credito {}", request.getMontoSolicitado(), tipoCredito.getNombre());
            throw new IllegalArgumentException("Monto fuera de los límites del tipo de crédito");
        }
        if (!tipoCredito.validaPlazo(request.getPlazoMeses())) {
            log.warn("Plazo {} fuera de límites para tipo credito {}", request.getPlazoMeses(), tipoCredito.getNombre());
            throw new IllegalArgumentException("Plazo fuera de los límites del tipo de crédito");
        }

        // Calcular cuota estimada usando sistema francés
        BigDecimal cuotaEstimada = PlanAmortizacion.calcularCuotaFrances(
            request.getMontoSolicitado(),
            tipoCredito.getTasaInteresAnual(),
            request.getPlazoMeses()
        );

        // Generar número de solicitud no enumerable
        String numeroSolicitud;
        do {
            numeroSolicitud = SolicitudCredito.generarNumeroSolicitud();
        } while (solicitudRepository.existePorNumeroSolicitud(numeroSolicitud));

        // Crear solicitud (sin asignar ID - se genera en la DB)
        SolicitudCredito solicitud = SolicitudCredito.builder()
            .numeroSolicitud(numeroSolicitud)
            .socioId(socioId)
            .tipoCreditoId(request.getTipoCreditoId())
            .montoSolicitado(request.getMontoSolicitado())
            .plazoMeses(request.getPlazoMeses())
            .cuotaMensualEstimada(cuotaEstimada)
            .estado(EstadoSolicitud.PENDIENTE)
            .destinoCredito(request.getDestinoCredito())
            .productoFinanciableId(request.getProductoFinanciableId())
            .productoNombreSnapshot(request.getProductoNombreSnapshot())
            .productoPrecioSnapshot(request.getProductoPrecioSnapshot())
            .productoMonedaSnapshot(request.getProductoMonedaSnapshot())
            .productoColateralRequeridoSnapshot(request.getProductoColateralRequeridoSnapshot())
            .cuentaDestino(request.getCuentaDestino())
            .createdAt(LocalDateTime.now())
            .build();

        if (request.getColateralCuentaId() != null) {
            solicitud.setColateralCuentaId(UUID.fromString(request.getColateralCuentaId()));
        }
        if (request.getProductoColateralRequeridoSnapshot() != null) {
            solicitud.setColateralMontoRetenido(request.getProductoColateralRequeridoSnapshot());
        }

        solicitud = solicitudRepository.guardar(solicitud);
        log.info("Solicitud creada: {} para socio {}", numeroSolicitud, socioId);

        SolicitudCreditoResponse response = mapper.toResponse(solicitud);
        response.setTipoCreditoNombre(tipoCredito.getNombre());
        return response;
    }
}
