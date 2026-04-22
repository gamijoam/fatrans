// com/tufondo/beneficiarios/application/usecase/CreateBeneficiarioUseCase.java
package com.tufondo.beneficiarios.application.usecase;

import com.tufondo.beneficiarios.application.dto.BeneficiarioResponseDTO;
import com.tufondo.beneficiarios.application.dto.CreateBeneficiarioRequestDTO;
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

/**
 * Caso de uso para crear un nuevo beneficiario.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateBeneficiarioUseCase {

    private static final int MAX_BENEFICIARIOS = 5;
    private static final BigDecimal CIEN = new BigDecimal("100.00");

    private final BeneficiarioRepository repository;
    private final SocioQueryPort socioQueryPort;
    private final BeneficiarioAuditService auditService;

    @Transactional
    public BeneficiarioResponseDTO ejecutar(UUID socioId, CreateBeneficiarioRequestDTO request, HttpServletRequest httpRequest) {
        if (!socioQueryPort.existsByIdAndActivoTrue(socioId)) {
            throw new SocioNoEncontradoException(socioId);
        }

        String numeroDocumentoSocio = socioQueryPort.getNumeroDocumentoById(socioId);
        if (numeroDocumentoSocio != null && numeroDocumentoSocio.equals(request.numeroDocumento())) {
            throw new DocumentoIgualAlTitularException();
        }

        if (repository.existePorDocumento(socioId, request.tipoDocumento(),
                request.numeroDocumento(), null)) {
            throw new BeneficiarioDuplicadoException(request.numeroDocumento());
        }

        int countActivos = repository.countActivosPorSocioId(socioId);
        if (countActivos >= MAX_BENEFICIARIOS) {
            throw new MaximoBeneficiariosExcedidoException();
        }

        BigDecimal sumaActual = repository.sumarPorcentajesPorSocioId(socioId);
        BigDecimal nuevaSuma = sumaActual.add(request.porcentaje());
        if (nuevaSuma.compareTo(CIEN) > 0) {
            throw new PorcentajeSumExcedidoException(sumaActual, request.porcentaje());
        }

        Beneficiario beneficiario = Beneficiario.crear(
                socioId,
                request.nombreCompleto(),
                request.numeroDocumento(),
                request.tipoDocumento(),
                request.parentesco(),
                request.porcentaje(),
                request.telefono()
        );

        Beneficiario saved = repository.guardar(beneficiario);
        auditService.registrarCreate(saved, httpRequest);

        return BeneficiarioResponseDTO.fromDomain(saved);
    }
}