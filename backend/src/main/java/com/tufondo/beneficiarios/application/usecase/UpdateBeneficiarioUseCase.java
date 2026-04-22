// com/tufondo/beneficiarios/application/usecase/UpdateBeneficiarioUseCase.java
package com.tufondo.beneficiarios.application.usecase;

import com.tufondo.beneficiarios.application.dto.BeneficiarioResponseDTO;
import com.tufondo.beneficiarios.application.dto.UpdateBeneficiarioRequestDTO;
import com.tufondo.beneficiarios.application.port.SocioQueryPort;
import com.tufondo.beneficiarios.domain.exception.*;
import com.tufondo.beneficiarios.domain.model.Beneficiario;
import com.tufondo.beneficiarios.domain.repository.BeneficiarioRepository;
import com.tufondo.beneficiarios.infrastructure.persistence.adapter.BeneficiarioAuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateBeneficiarioUseCase {

    private static final BigDecimal CIEN = new BigDecimal("100.00");

    private final BeneficiarioRepository repository;
    private final SocioQueryPort socioQueryPort;
    private final BeneficiarioAuditService auditService;

    @Transactional
    public BeneficiarioResponseDTO ejecutar(UUID socioId, UUID beneficiarioId,
            UpdateBeneficiarioRequestDTO request, HttpServletRequest httpRequest) {
        Beneficiario beneficiario = repository.buscarPorId(beneficiarioId)
                .orElseThrow(() -> new BeneficiarioNoEncontradoException(beneficiarioId));

        if (!beneficiario.getSocioId().equals(socioId)) {
            throw new BeneficiarioNoEncontradoException(beneficiarioId);
        }

        Beneficiario snapshotAnterior = Beneficiario.builder()
                .id(beneficiario.getId())
                .socioId(beneficiario.getSocioId())
                .nombreCompleto(beneficiario.getNombreCompleto())
                .numeroDocumento(beneficiario.getNumeroDocumento())
                .tipoDocumento(beneficiario.getTipoDocumento())
                .parentesco(beneficiario.getParentesco())
                .porcentaje(beneficiario.getPorcentaje())
                .telefono(beneficiario.getTelefono())
                .activo(beneficiario.isActivo())
                .fechaRegistro(beneficiario.getFechaRegistro())
                .fechaActualizacion(beneficiario.getFechaActualizacion())
                .build();

        boolean documentoCambio = !beneficiario.getNumeroDocumento().equals(request.numeroDocumento())
                || !beneficiario.getTipoDocumento().equals(request.tipoDocumento());

        if (documentoCambio) {
            String numeroDocumentoSocio = socioQueryPort.getNumeroDocumentoById(socioId);
            if (numeroDocumentoSocio != null && numeroDocumentoSocio.equals(request.numeroDocumento())) {
                throw new DocumentoIgualAlTitularException();
            }

            if (repository.existePorDocumento(socioId, request.tipoDocumento(),
                    request.numeroDocumento(), beneficiarioId)) {
                throw new BeneficiarioDuplicadoException(request.numeroDocumento());
            }
        }

        BigDecimal sumaActual = repository.sumarPorcentajesPorSocioId(socioId);
        BigDecimal sumaSinEste = sumaActual.subtract(beneficiario.getPorcentaje());
        BigDecimal nuevaSuma = sumaSinEste.add(request.porcentaje());

        if (nuevaSuma.compareTo(CIEN) > 0) {
            throw new PorcentajeSumExcedidoException(sumaSinEste, request.porcentaje());
        }

        beneficiario.setNombreCompleto(request.nombreCompleto());
        beneficiario.setNumeroDocumento(request.numeroDocumento());
        beneficiario.setTipoDocumento(request.tipoDocumento());
        beneficiario.setParentesco(request.parentesco());
        beneficiario.setPorcentaje(request.porcentaje());
        beneficiario.setTelefono(request.telefono());
        beneficiario.conActualizacion();

        Beneficiario updated = repository.guardar(beneficiario);
        auditService.registrarUpdate(snapshotAnterior, updated, httpRequest);

        return BeneficiarioResponseDTO.fromDomain(updated);
    }
}