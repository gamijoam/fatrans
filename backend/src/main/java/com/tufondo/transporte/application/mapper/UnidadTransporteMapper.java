package com.tufondo.transporte.application.mapper;

import com.tufondo.transporte.application.dto.RegistrarUnidadRequestDTO;
import com.tufondo.transporte.application.dto.UnidadTransporteResponseDTO;
import com.tufondo.transporte.infrastructure.persistence.entity.UnidadTransporteEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UnidadTransporteMapper {

    public UnidadTransporteEntity toEntity(RegistrarUnidadRequestDTO request, UUID socioId) {
        UnidadTransporteEntity entity = new UnidadTransporteEntity();
        entity.setSocioId(socioId);
        entity.setPlaca(request.getPlaca());
        entity.setMarca(request.getMarca());
        entity.setModelo(request.getModelo());
        entity.setAno(request.getAno());
        entity.setTipoUnidad(request.getTipoUnidad());
        entity.setCapacidadPasajeros(request.getCapacidadPasajeros());
        entity.setSoatVencimiento(request.getSoatVencimiento());
        entity.setSeguroVencimiento(request.getSeguroVencimiento());
        entity.setRevisionTecnicaVencimiento(request.getRevisionTecnicaVencimiento());
        return entity;
    }

    public UnidadTransporteResponseDTO toDto(UnidadTransporteEntity entity) {
        return UnidadTransporteResponseDTO.builder()
                .id(entity.getId())
                .socioId(entity.getSocioId())
                .placa(entity.getPlaca())
                .marca(entity.getMarca())
                .modelo(entity.getModelo())
                .ano(entity.getAno())
                .tipoUnidad(entity.getTipoUnidad())
                .capacidadPasajeros(entity.getCapacidadPasajeros())
                .soatVencimiento(entity.getSoatVencimiento())
                .seguroVencimiento(entity.getSeguroVencimiento())
                .revisionTecnicaVencimiento(entity.getRevisionTecnicaVencimiento())
                .estado(entity.getEstado())
                .fechaRegistro(entity.getFechaRegistro())
                .build();
    }
}
