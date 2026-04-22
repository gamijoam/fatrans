// com/tufondo/creditos/application/usecase/ListarTiposCreditoUseCase.java
package com.tufondo.creditos.application.usecase;

import com.tufondo.creditos.application.dto.TipoCreditoPublicResponse;
import com.tufondo.creditos.application.mapper.CreditosDTOMapper;
import com.tufondo.creditos.domain.model.TipoCredito;
import com.tufondo.creditos.domain.repository.TipoCreditoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListarTiposCreditoUseCase {

    private final TipoCreditoRepository tipoCreditoRepository;
    private final CreditosDTOMapper mapper;

    @Transactional(readOnly = true)
    public Map<String, Object> ejecutar(int page, int size) {
        Page<TipoCredito> tipoPage = tipoCreditoRepository.listarActivosPaginado(
            PageRequest.of(page, size, Sort.by("nombre").ascending())
        );

        var tiposPublicos = tipoPage.getContent().stream()
            .map(mapper::toPublicResponse)
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("tiposCredito", tiposPublicos);
        result.put("page", tipoPage.getNumber());
        result.put("size", tipoPage.getSize());
        result.put("totalElements", tipoPage.getTotalElements());
        result.put("totalPages", tipoPage.getTotalPages());
        result.put("first", tipoPage.isFirst());
        result.put("last", tipoPage.isLast());

        log.info("Listados {} tipos de crédito activos", tiposPublicos.size());
        return result;
    }
}