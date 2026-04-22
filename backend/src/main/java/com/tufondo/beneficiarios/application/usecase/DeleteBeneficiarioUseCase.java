// com/tufondo/beneficiarios/application/usecase/DeleteBeneficiarioUseCase.java
package com.tufondo.beneficiarios.application.usecase;

import com.tufondo.beneficiarios.application.dto.DeleteBeneficiarioResponseDTO;
import com.tufondo.beneficiarios.domain.exception.BeneficiarioNoEncontradoException;
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
public class DeleteBeneficiarioUseCase {

    private final BeneficiarioRepository repository;
    private final BeneficiarioAuditService auditService;

    @Transactional
    public DeleteBeneficiarioResponseDTO ejecutar(UUID socioId, UUID beneficiarioId, HttpServletRequest httpRequest) {
        Beneficiario beneficiario = repository.buscarPorId(beneficiarioId)
                .orElseThrow(() -> new BeneficiarioNoEncontradoException(beneficiarioId));

        if (!beneficiario.getSocioId().equals(socioId)) {
            throw new BeneficiarioNoEncontradoException(beneficiarioId);
        }

        Beneficiario snapshot = Beneficiario.builder()
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

        beneficiario.marcarInactivo();
        repository.guardar(beneficiario);
        auditService.registrarDelete(snapshot, httpRequest);

        BigDecimal sumaRestante = repository.sumarPorcentajesPorSocioId(socioId);

        if (sumaRestante.compareTo(new BigDecimal("100.00")) != 0) {
            return DeleteBeneficiarioResponseDTO.withWarning(beneficiarioId, socioId, sumaRestante);
        } else {
            return DeleteBeneficiarioResponseDTO.success(beneficiarioId, socioId, sumaRestante);
        }
    }
}