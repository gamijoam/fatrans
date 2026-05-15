// 📁 com/tufondo/socios/application/usecase/SolicitudRegistroDTOMapper.java
package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.SolicitudRegistroResponseDTO;
import com.tufondo.socios.domain.model.SolicitudRegistro;
import org.springframework.stereotype.Component;

/**
 * Mapper de SolicitudRegistro (domain) a DTO de respuesta.
 *
 * Mapea TODOS los datos relevantes que un admin necesita ver para decidir si
 * aprueba o rechaza una solicitud. Antes solo se mapeaban 12 campos básicos
 * y el admin quedaba a ciegas sobre datos laborales, dirección, contacto de
 * emergencia y consentimientos legales — bug reportado al revisar la cola
 * en `/admin/solicitudes`.
 *
 * NO mapea metadata LOPDP (ip, user-agent, consent timestamp). Ese acceso
 * queda restringido a auditoría legal específica, no al flujo operativo.
 */
@Component
public class SolicitudRegistroDTOMapper {

    public SolicitudRegistroResponseDTO toResponseDTO(SolicitudRegistro solicitud) {
        return SolicitudRegistroResponseDTO.builder()
                .id(solicitud.getId())
                // Datos personales
                .nombreCompleto(solicitud.getNombreCompleto())
                .tipoDocumento(solicitud.getTipoDocumento())
                .cedula(solicitud.getCedula())
                .fechaNacimiento(solicitud.getFechaNacimiento())
                .genero(solicitud.getGenero())
                .estadoCivil(solicitud.getEstadoCivil())
                // Contacto
                .correoElectronico(solicitud.getCorreoElectronico())
                .telefono(solicitud.getTelefono())
                // Laboral
                .empresa(solicitud.getEmpresa())
                .rifEmpresa(solicitud.getRifEmpresa())
                .departamento(solicitud.getDepartamento())
                .cargo(solicitud.getCargo())
                .salario(solicitud.getSalario())
                // Dirección
                .direccionEstado(solicitud.getDireccionEstado())
                .direccionCiudad(solicitud.getDireccionCiudad())
                .direccionMunicipio(solicitud.getDireccionMunicipio())
                .direccionCalle(solicitud.getDireccionCalle())
                // Emergencia
                .emergenciaNombre(solicitud.getEmergenciaNombre())
                .emergenciaTelefono(solicitud.getEmergenciaTelefono())
                .emergenciaParentesco(solicitud.getEmergenciaParentesco())
                // Consentimientos
                .aceptaTerminos(solicitud.getAceptaTerminos())
                .aceptaLopdp(solicitud.getAceptaLopdp())
                // Trazabilidad
                .estado(solicitud.getEstado())
                .fechaSolicitud(solicitud.getFechaSolicitud())
                .fechaRevision(solicitud.getFechaRevision())
                .revisadoPor(solicitud.getRevisadoPor())
                .comentario(solicitud.getComentario())
                .motivoRechazo(solicitud.getMotivoRechazo())
                .build();
    }
}
