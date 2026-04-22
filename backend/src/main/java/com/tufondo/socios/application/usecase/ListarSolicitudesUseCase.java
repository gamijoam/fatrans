// 📁 com/tufondo/socios/application/usecase/ListarSolicitudesUseCase.java
package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.SolicitudRegistroResponseDTO;
import com.tufondo.socios.domain.model.SolicitudRegistro;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.repository.SolicitudRegistroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListarSolicitudesUseCase {
    
    private final SolicitudRegistroRepository solicitudRepository;
    private final SolicitudRegistroDTOMapper dtoMapper;
    
    @Transactional(readOnly = true)
    public Page<SolicitudRegistroResponseDTO> ejecutar(EstadoSolicitud estado, Pageable pageable) {
        Page<SolicitudRegistro> solicitudes;
        
        if (estado != null) {
            solicitudes = solicitudRepository.listarPorEstado(estado, pageable);
        } else {
            solicitudes = solicitudRepository.listar(pageable);
        }
        
        return solicitudes.map(dtoMapper::toResponseDTO);
    }
}