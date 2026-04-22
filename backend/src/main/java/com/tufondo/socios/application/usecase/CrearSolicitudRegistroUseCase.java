// 📁 com/tufondo/socios/application/usecase/CrearSolicitudRegistroUseCase.java
package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.SolicitudRegistroRequestDTO;
import com.tufondo.socios.application.dto.SolicitudRegistroResponseDTO;
import com.tufondo.socios.domain.exception.CedulaDuplicadaException;
import com.tufondo.socios.domain.exception.CorreoDuplicadoException;
import com.tufondo.socios.domain.model.SolicitudRegistro;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.repository.SolicitudRegistroRepository;
import com.tufondo.socios.domain.repository.SocioRepository;
import com.tufondo.socios.infrastructure.notification.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CrearSolicitudRegistroUseCase {
    
    private final SolicitudRegistroRepository solicitudRepository;
    private final SocioRepository socioRepository;
    private final EmailNotificationService emailNotificationService;
    private final SolicitudRegistroDTOMapper dtoMapper;
    
    @Transactional
    public SolicitudRegistroResponseDTO ejecutar(SolicitudRegistroRequestDTO request) {
        // Validar que la cédula no exista en solicitudes ni en socios
        if (solicitudRepository.existePorCedula(request.getCedula())) {
            throw new CedulaDuplicadaException(request.getCedula());
        }
        if (socioRepository.existePorNumeroDocumento(request.getCedula())) {
            throw new CedulaDuplicadaException(request.getCedula());
        }
        
        // Validar que el email no exista en solicitudes ni en socios
        if (solicitudRepository.existePorCorreo(request.getCorreoElectronico())) {
            throw new CorreoDuplicadoException(request.getCorreoElectronico());
        }
        if (socioRepository.existePorCorreo(request.getCorreoElectronico())) {
            throw new CorreoDuplicadoException(request.getCorreoElectronico());
        }
        
        // Crear la solicitud con estado PENDIENTE
        SolicitudRegistro solicitud = SolicitudRegistro.builder()
                .nombreCompleto(request.getNombreCompleto())
                .cedula(request.getCedula())
                .correoElectronico(request.getCorreoElectronico())
                .telefono(request.getTelefono())
                .empresa(request.getEmpresa())
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now())
                .build();
        
        // Guardar la solicitud
        SolicitudRegistro saved = solicitudRepository.guardar(solicitud);
        
        // Enviar email de confirmación
        emailNotificationService.enviarNotificacionSolicitudRecibida(request.getCorreoElectronico());
        
        return dtoMapper.toResponseDTO(saved);
    }
}