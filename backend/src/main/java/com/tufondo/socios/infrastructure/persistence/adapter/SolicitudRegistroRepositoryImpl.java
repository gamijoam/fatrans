// 📁 com/tufondo/socios/infrastructure/persistence/adapter/SolicitudRegistroRepositoryImpl.java
package com.tufondo.socios.infrastructure.persistence.adapter;

import com.tufondo.socios.domain.model.SolicitudRegistro;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.domain.repository.SolicitudRegistroRepository;
import com.tufondo.socios.infrastructure.persistence.entity.SolicitudRegistroEntity;
import com.tufondo.socios.infrastructure.persistence.jpa.SolicitudRegistroJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SolicitudRegistroRepositoryImpl implements SolicitudRegistroRepository {

    private final SolicitudRegistroJpaRepository jpaRepository;

    @Override
    @Transactional
    public SolicitudRegistro guardar(SolicitudRegistro solicitud) {
        SolicitudRegistroEntity entity = toEntity(solicitud);
        if (entity.getFechaSolicitud() == null) {
            entity.setFechaSolicitud(java.time.LocalDateTime.now());
        }
        SolicitudRegistroEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SolicitudRegistro> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SolicitudRegistro> listarPorEstado(EstadoSolicitud estado, Pageable pageable) {
        return jpaRepository.findByEstado(estado, pageable).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SolicitudRegistro> listar(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCedula(String cedula) {
        return jpaRepository.existsByCedula(cedula);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCorreo(String correo) {
        return jpaRepository.existsByCorreoElectronico(correo);
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPorEstado(EstadoSolicitud estado) {
        return jpaRepository.countByEstado(estado);
    }

    private SolicitudRegistroEntity toEntity(SolicitudRegistro domain) {
        return SolicitudRegistroEntity.builder()
                .id(domain.getId())
                .nombreCompleto(domain.getNombreCompleto())
                .tipoDocumento(domain.getTipoDocumento())
                .cedula(domain.getCedula())
                .fechaNacimiento(domain.getFechaNacimiento())
                .genero(domain.getGenero())
                .estadoCivil(domain.getEstadoCivil())
                .correoElectronico(domain.getCorreoElectronico())
                .telefono(domain.getTelefono())
                .empresa(domain.getEmpresa())
                .rifEmpresa(domain.getRifEmpresa())
                .departamento(domain.getDepartamento())
                .cargo(domain.getCargo())
                .salario(domain.getSalario())
                .direccionEstado(domain.getDireccionEstado())
                .direccionCiudad(domain.getDireccionCiudad())
                .direccionMunicipio(domain.getDireccionMunicipio())
                .direccionCalle(domain.getDireccionCalle())
                .emergenciaNombre(domain.getEmergenciaNombre())
                .emergenciaTelefono(domain.getEmergenciaTelefono())
                .emergenciaParentesco(domain.getEmergenciaParentesco())
                .estado(domain.getEstado())
                .fechaSolicitud(domain.getFechaSolicitud())
                .fechaRevision(domain.getFechaRevision())
                .revisadoPor(domain.getRevisadoPor())
                .comentario(domain.getComentario())
                .motivoRechazo(domain.getMotivoRechazo())
                .aceptaTerminos(domain.getAceptaTerminos())
                .aceptaLopdp(domain.getAceptaLopdp())
                .aceptaLocdoft(domain.getAceptaLocdoft())
                .ipRegistro(domain.getIpRegistro())
                .userAgentRegistro(domain.getUserAgentRegistro())
                .consentLopdpTimestamp(domain.getConsentLopdpTimestamp())
                .consentLocdoftTimestamp(domain.getConsentLocdoftTimestamp())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private SolicitudRegistro toDomain(SolicitudRegistroEntity entity) {
        return SolicitudRegistro.builder()
                .id(entity.getId())
                .nombreCompleto(entity.getNombreCompleto())
                .tipoDocumento(entity.getTipoDocumento())
                .cedula(entity.getCedula())
                .fechaNacimiento(entity.getFechaNacimiento())
                .genero(entity.getGenero())
                .estadoCivil(entity.getEstadoCivil())
                .correoElectronico(entity.getCorreoElectronico())
                .telefono(entity.getTelefono())
                .empresa(entity.getEmpresa())
                .rifEmpresa(entity.getRifEmpresa())
                .departamento(entity.getDepartamento())
                .cargo(entity.getCargo())
                .salario(entity.getSalario())
                .direccionEstado(entity.getDireccionEstado())
                .direccionCiudad(entity.getDireccionCiudad())
                .direccionMunicipio(entity.getDireccionMunicipio())
                .direccionCalle(entity.getDireccionCalle())
                .emergenciaNombre(entity.getEmergenciaNombre())
                .emergenciaTelefono(entity.getEmergenciaTelefono())
                .emergenciaParentesco(entity.getEmergenciaParentesco())
                .estado(entity.getEstado())
                .fechaSolicitud(entity.getFechaSolicitud())
                .fechaRevision(entity.getFechaRevision())
                .revisadoPor(entity.getRevisadoPor())
                .comentario(entity.getComentario())
                .motivoRechazo(entity.getMotivoRechazo())
                .aceptaTerminos(entity.getAceptaTerminos())
                .aceptaLopdp(entity.getAceptaLopdp())
                .aceptaLocdoft(entity.getAceptaLocdoft())
                .ipRegistro(entity.getIpRegistro())
                .userAgentRegistro(entity.getUserAgentRegistro())
                .consentLopdpTimestamp(entity.getConsentLopdpTimestamp())
                .consentLocdoftTimestamp(entity.getConsentLocdoftTimestamp())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}