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

import java.time.Instant;
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
        if (solicitudRepository.existePorCedula(request.getCedula())) {
            throw new CedulaDuplicadaException(request.getCedula());
        }
        if (socioRepository.existePorNumeroDocumento(request.getCedula())) {
            throw new CedulaDuplicadaException(request.getCedula());
        }
        
        if (solicitudRepository.existePorCorreo(request.getCorreoElectronico())) {
            throw new CorreoDuplicadoException(request.getCorreoElectronico());
        }
        if (socioRepository.existePorCorreo(request.getCorreoElectronico())) {
            throw new CorreoDuplicadoException(request.getCorreoElectronico());
        }
        
        SolicitudRegistro solicitud = SolicitudRegistro.builder()
                .nombreCompleto(request.getNombreCompleto())
                .tipoDocumento(request.getTipoDocumento())
                .cedula(request.getCedula())
                .fechaNacimiento(request.getFechaNacimiento())
                .genero(request.getGenero())
                .estadoCivil(request.getEstadoCivil())
                .correoElectronico(request.getCorreoElectronico())
                .telefono(request.getTelefono())
                .empresa(request.getEmpresa())
                .rifEmpresa(request.getRifEmpresa())
                .departamento(request.getDepartamento())
                .cargo(request.getCargo())
                .salario(request.getSalario())
                .direccionEstado(request.getDireccionEstado())
                .direccionCiudad(request.getDireccionCiudad())
                .direccionMunicipio(request.getDireccionMunicipio())
                .direccionCalle(request.getDireccionCalle())
                .emergenciaNombre(request.getEmergenciaNombre())
                .emergenciaTelefono(request.getEmergenciaTelefono())
                .emergenciaParentesco(request.getEmergenciaParentesco())
                .aceptaTerminos(request.getAceptaTerminos())
                .aceptaLopdp(request.getAceptaLopdp())
                .ipRegistro(request.getIpRegistro())
                .userAgentRegistro(request.getUserAgentRegistro())
                // Sello de tiempo del consentimiento LOPDP: SOLO si el usuario aceptó.
                // No confiamos en el cliente para esto — siempre Instant.now() server-side.
                .consentLopdpTimestamp(Boolean.TRUE.equals(request.getAceptaLopdp()) ? Instant.now() : null)
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now())
                .build();
        
        SolicitudRegistro saved = solicitudRepository.guardar(solicitud);
        
        emailNotificationService.enviarNotificacionSolicitudRecibida(request.getCorreoElectronico());
        
        return dtoMapper.toResponseDTO(saved);
    }
}