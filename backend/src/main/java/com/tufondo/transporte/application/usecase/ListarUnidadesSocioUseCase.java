package com.tufondo.transporte.application.usecase;

import com.tufondo.transporte.application.dto.UnidadTransporteResponseDTO;
import com.tufondo.transporte.application.mapper.UnidadTransporteMapper;
import com.tufondo.transporte.infrastructure.persistence.jpa.UnidadTransporteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListarUnidadesSocioUseCase {

    private final UnidadTransporteJpaRepository repository;
    private final UnidadTransporteMapper mapper;

    public List<UnidadTransporteResponseDTO> ejecutar(UUID socioId) {
        return repository.findBySocioId(socioId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
