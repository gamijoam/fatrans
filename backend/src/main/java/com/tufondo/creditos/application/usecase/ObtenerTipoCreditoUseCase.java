// com/tufondo/creditos/application/usecase/ObtenerTipoCreditoUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.TipoCreditoPublicResponse;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObtenerTipoCreditoUseCase {

    private final TipoCreditoRepository tipoCreditoRepository;
    private final CreditosDTOMapper mapper;

    @Transactional(readOnly = true)
    public TipoCreditoPublicResponse ejecutar(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de tipo de crédito inválido");
        }
        TipoCredito tipo = tipoCreditoRepository.buscarPorIdActivo(id)
            .orElseThrow(() -> new com.tufondo.creditos.domain.exception.CreditoNoEncontradoException(id));
        log.info("Tipo de crédito consultado: {}", id);
        return mapper.toPublicResponse(tipo);
    }
}