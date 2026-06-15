// com/tufondo/creditos/application/usecase/ListarSolicitudesCreditoAdminUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.SolicitudCreditoAdminResponse;
import com.tufondo.creditos.application.mapper.SocioAdminMapper;
import com.tufondo.creditos.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.repository.SocioRepository;
import com.tufondo.creditos.infrastructure.persistence.entity.SolicitudCreditoEntity;
import com.tufondo.creditos.infrastructure.persistence.jpa.SolicitudCreditoJpaRepository;
import com.tufondo.creditos.infrastructure.persistence.jpa.SolicitudCreditoSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Caso de uso para listar todas las solicitudes de crédito (para Admin).
 * Soporta filtros por estado, fecha y monto con paginación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListarSolicitudesCreditoAdminUseCase {

    private final SolicitudCreditoJpaRepository jpaRepository;
    private final SocioRepository socioRepository;
    private final SocioAdminMapper socioAdminMapper;

    @Transactional(readOnly = true)
    public Page<SolicitudCreditoAdminResponse> ejecutar(
            EstadoSolicitud estado,
            LocalDateTime fechaDesde,
            LocalDateTime fechaHasta,
            BigDecimal montoMin,
            BigDecimal montoMax,
            Pageable pageable) {

        log.info("Listando solicitudes admin - estado:{}, fechaDesde:{}, fechaHasta:{}, montoMin:{}, montoMax:{}",
                estado, fechaDesde, fechaHasta, montoMin, montoMax);

        var spec = SolicitudCreditoSpecification.conFiltros(estado, fechaDesde, fechaHasta, montoMin, montoMax);
        Page<SolicitudCreditoEntity> entityPage = jpaRepository.findAll(spec, pageable);

        log.info("Encontradas {} solicitudes (pagina {} de {})",
                entityPage.getNumberOfElements(), pageable.getPageNumber(), entityPage.getTotalPages());

        List<SolicitudCreditoEntity> entities = entityPage.getContent();
        Map<UUID, Socio> sociosMap = buildSociosMap(entities);

        return entities.stream()
                .map(entity -> toAdminResponse(entity, sociosMap.get(entity.getSocioId())))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> new org.springframework.data.domain.PageImpl<>(list, pageable, entityPage.getTotalElements())
                ));
    }

    private Map<UUID, Socio> buildSociosMap(List<SolicitudCreditoEntity> entities) {
        List<UUID> socioIds = entities.stream()
                .map(SolicitudCreditoEntity::getSocioId)
                .distinct()
                .collect(Collectors.toList());

        if (socioIds.isEmpty()) {
            return Map.of();
        }

        List<Socio> socios = socioRepository.buscarPorIdIn(socioIds);
        return socios.stream().collect(Collectors.toMap(Socio::getId, s -> s));
    }

    private SolicitudCreditoAdminResponse toAdminResponse(SolicitudCreditoEntity entity, Socio socio) {
        return SolicitudCreditoAdminResponse.builder()
                .id(entity.getId().toString())
                .numeroSolicitud(entity.getNumeroSolicitud())
                .socioId(entity.getSocioId())
                .socioNombre(socioAdminMapper.toNombreCompleto(socio))
                .socioNumero(socioAdminMapper.toNumeroSocio(socio))
                .socioCedula(socioAdminMapper.toCedula(socio))
                .socioCorreo(socioAdminMapper.toCorreo(socio))
                .socioEmpresa(socioAdminMapper.toEmpresa(socio))
                .tipoCreditoId(entity.getTipoCreditoId())
                .tipoCreditoNombre("Tipo Credito " + entity.getTipoCreditoId())
                .montoSolicitado(entity.getMontoSolicitado())
                .plazoMeses(entity.getPlazoMeses())
                .tasaInteresAplicada(entity.getTasaInteresAplicada())
                .cuotaMensualEstimada(entity.getCuotaMensualEstimada())
                .estado(entity.getEstado() != null ? entity.getEstado().name() : "")
                .colateralCuentaId(entity.getColateralCuentaId() != null ? entity.getColateralCuentaId().toString() : null)
                .colateralMontoRetenido(entity.getColateralMontoRetenido())
                .destinoCredito(entity.getDestinoCredito())
                .productoFinanciableId(entity.getProductoFinanciableId())
                .productoNombreSnapshot(entity.getProductoNombreSnapshot())
                .productoPrecioSnapshot(entity.getProductoPrecioSnapshot())
                .productoMonedaSnapshot(entity.getProductoMonedaSnapshot())
                .productoColateralRequeridoSnapshot(entity.getProductoColateralRequeridoSnapshot())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
