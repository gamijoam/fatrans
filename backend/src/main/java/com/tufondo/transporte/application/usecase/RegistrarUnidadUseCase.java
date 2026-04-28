package com.tufondo.transporte.application.usecase;

import com.tufondo.transporte.application.dto.RegistrarUnidadRequestDTO;
import com.tufondo.transporte.application.dto.UnidadTransporteResponseDTO;
import com.tufondo.transporte.application.mapper.UnidadTransporteMapper;
import com.tufondo.transporte.domain.exception.PlacaDuplicadaException;
import com.tufondo.transporte.infrastructure.persistence.entity.UnidadTransporteEntity;
import com.tufondo.transporte.infrastructure.persistence.jpa.UnidadTransporteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrarUnidadUseCase {

    private final UnidadTransporteJpaRepository repository;
    private final UnidadTransporteMapper mapper;

    @Transactional
    public UnidadTransporteResponseDTO ejecutar(RegistrarUnidadRequestDTO request, UUID socioId) {
        try {
            UnidadTransporteEntity entity = mapper.toEntity(request, socioId);
            UnidadTransporteEntity savedEntity = repository.save(entity);
            return mapper.toDto(savedEntity);
        } catch (DataIntegrityViolationException e) {
            // Asumiendo que la violacion es por la placa unica
            throw new PlacaDuplicadaException(request.getPlaca());
        }
    }
}
