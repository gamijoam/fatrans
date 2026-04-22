// 📁 com/tufondo/socios/application/usecase/SolicitudRegistroDTOMapper.java
package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.SolicitudRegistroResponseDTO;
import com.tufondo.socios.domain.model.SolicitudRegistro;
import org.springframework.stereotype.Component;

@Component
public class SolicitudRegistroDTOMapper {
    
    public SolicitudRegistroResponseDTO toResponseDTO(SolicitudRegistro solicitud) {
        return SolicitudRegistroResponseDTO.builder()
                .id(solicitud.getId())
                .nombreCompleto(solicitud.getNombreCompleto())
                .cedula(solicitud.getCedula())
                .correoElectronico(solicitud.getCorreoElectronico())
                .telefono(solicitud.getTelefono())
                .empresa(solicitud.getEmpresa())
                .estado(solicitud.getEstado())
                .fechaSolicitud(solicitud.getFechaSolicitud())
                .fechaRevision(solicitud.getFechaRevision())
                .revisadoPor(solicitud.getRevisadoPor())
                .comentario(solicitud.getComentario())
                .motivoRechazo(solicitud.getMotivoRechazo())
                .build();
    }
}