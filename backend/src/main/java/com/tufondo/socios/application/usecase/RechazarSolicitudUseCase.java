// 📁 com/tufondo/socios/application/usecase/RechazarSolicitudUseCase.java
package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.RechazarSolicitudRequestDTO;
import com.tufondo.socios.application.dto.SolicitudRegistroResponseDTO;
import com.tufondo.socios.domain.exception.SolicitudNoEditableException;
import com.tufondo.socios.domain.exception.SolicitudNoEncontradaException;
import com.tufondo.socios.domain.model.SolicitudRegistro;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.repository.SolicitudRegistroRepository;
import com.tufondo.socios.infrastructure.notification.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RechazarSolicitudUseCase {
    
    private final SolicitudRegistroRepository solicitudRepository;
    private final EmailNotificationService emailNotificationService;
    private final SolicitudRegistroDTOMapper dtoMapper;
    
    @Transactional
    public SolicitudRegistroResponseDTO ejecutar(UUID solicitudId, RechazarSolicitudRequestDTO request, String adminId) {
        // 1. Obtener y validar solicitud
        SolicitudRegistro solicitud = solicitudRepository.buscarPorId(solicitudId)
                .orElseThrow(() -> new SolicitudNoEncontradaException(solicitudId));
        
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new SolicitudNoEditableException("La solicitud no está pendiente");
        }
        
        // 2. Rechazar la solicitud usando el método del dominio
        solicitud.rechazar(adminId, request.getMotivo());
        SolicitudRegistro solicitudRechazada = solicitudRepository.guardar(solicitud);
        
        // 3. Enviar notificación de rechazo
        emailNotificationService.enviarNotificacionRechazo(
                solicitud.getCorreoElectronico(),
                request.getMotivo()
        );
        
        log.info("Solicitud {} rechazada por {}: {}", solicitudId, adminId, request.getMotivo());
        
        return dtoMapper.toResponseDTO(solicitudRechazada);
    }
}