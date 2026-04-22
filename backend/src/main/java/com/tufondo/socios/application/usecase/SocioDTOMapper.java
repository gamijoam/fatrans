package com.tufondo.socios.application.usecase;

import com.tufondo.socios.application.dto.ActualizarSocioDTO;
import com.tufondo.socios.application.dto.SocioResponseDTO;
import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.model.valueobjects.Direccion;
import com.tufondo.socios.domain.model.valueobjects.ContactoEmergencia;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class SocioDTOMapper {

    public SocioResponseDTO toResponseDTO(Socio socio) {
        return SocioResponseDTO.builder()
                .id(socio.getId())
                .numeroSocio(socio.getNumeroSocio())
                .tipoDocumento(socio.getTipoDocumento())
                .numeroDocumento(socio.getNumeroDocumento())
                .primerNombre(socio.getPrimerNombre())
                .segundoNombre(socio.getSegundoNombre())
                .primerApellido(socio.getPrimerApellido())
                .segundoApellido(socio.getSegundoApellido())
                .fechaNacimiento(socio.getFechaNacimiento())
                .genero(socio.getGenero())
                .estadoCivil(socio.getEstadoCivil())
                .correoElectronico(socio.getCorreoElectronico())
                .telefonoPrincipal(socio.getTelefonoPrincipal())
                .telefonoSecundario(socio.getTelefonoSecundario())
                .direccionResidencia(socio.getDireccionResidencia())
                .direccionLaboral(socio.getDireccionLaboral())
                .empresa(socio.getEmpresa())
                .departamento(socio.getDepartamento())
                .cargo(socio.getCargo())
                .tipoContrato(socio.getTipoContrato())
                .salario(socio.getSalario())
                .montoAhorro(socio.getMontoAhorro())
                .numeroCuentaNomina(socio.getNumeroCuentaNomina())
                .bancoNomina(socio.getBancoNomina())
                .contactoEmergencia(socio.getContactoEmergencia())
                .estado(socio.getEstado())
                .fechaIngreso(socio.getFechaIngreso())
                .fechaRegistro(socio.getFechaRegistro())
                .fechaActualizacion(socio.getFechaActualizacion())
                .fechaActivacion(socio.getFechaActivacion())
                .fechaDesactivacion(socio.getFechaDesactivacion())
                .motivoDesactivacion(socio.getMotivoDesactivacion())
                .roles(socio.getRoles())
                .build();
    }

    public Socio toDomain(ActualizarSocioDTO dto, Socio existente) {
        if (dto.getPrimerNombre() != null) existente.setPrimerNombre(dto.getPrimerNombre().trim());
        if (dto.getSegundoNombre() != null) existente.setSegundoNombre(dto.getSegundoNombre().trim());
        if (dto.getPrimerApellido() != null) existente.setPrimerApellido(dto.getPrimerApellido().trim());
        if (dto.getSegundoApellido() != null) existente.setSegundoApellido(dto.getSegundoApellido().trim());

        if (dto.getTipoDocumento() != null) existente.setTipoDocumento(dto.getTipoDocumento());
        if (dto.getNumeroDocumento() != null) existente.setNumeroDocumento(dto.getNumeroDocumento().trim());

        if (dto.getGenero() != null) existente.setGenero(dto.getGenero());
        if (dto.getFechaNacimiento() != null) existente.setFechaNacimiento(dto.getFechaNacimiento());
        if (dto.getEstadoCivil() != null) existente.setEstadoCivil(dto.getEstadoCivil());

        if (dto.getCorreoElectronico() != null) existente.setCorreoElectronico(dto.getCorreoElectronico());
        if (dto.getTelefonoPrincipal() != null) existente.setTelefonoPrincipal(dto.getTelefonoPrincipal());
        if (dto.getTelefonoSecundario() != null) existente.setTelefonoSecundario(dto.getTelefonoSecundario());

        if (dto.getDireccionResidencia() != null) existente.setDireccionResidencia(dto.getDireccionResidencia());
        if (dto.getDireccionLaboral() != null) existente.setDireccionLaboral(dto.getDireccionLaboral());

        if (dto.getEmpresa() != null) existente.setEmpresa(dto.getEmpresa().trim());
        if (dto.getDepartamento() != null) existente.setDepartamento(dto.getDepartamento().trim());
        if (dto.getCargo() != null) existente.setCargo(dto.getCargo().trim());
        if (dto.getTipoContrato() != null) existente.setTipoContrato(dto.getTipoContrato());
        
        if (dto.getNumeroCuentaNomina() != null) existente.setNumeroCuentaNomina(dto.getNumeroCuentaNomina());
        if (dto.getBancoNomina() != null) existente.setBancoNomina(dto.getBancoNomina());
        
        if (dto.getContactoEmergencia() != null) existente.setContactoEmergencia(dto.getContactoEmergencia());
        // Nota: estado y roles NO se actualizan via DTO - son operaciones reservadas al sistema
        existente.setFechaActualizacion(java.time.LocalDateTime.now());
        
        return existente;
    }
}
