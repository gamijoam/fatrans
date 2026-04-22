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

    private SolicitudRegistroEntity toEntity(SolicitudRegistro domain) {
        return SolicitudRegistroEntity.builder()
                .id(domain.getId())
                .nombreCompleto(domain.getNombreCompleto())
                .cedula(domain.getCedula())
                .correoElectronico(domain.getCorreoElectronico())
                .telefono(domain.getTelefono())
                .empresa(domain.getEmpresa())
                .estado(domain.getEstado())
                .fechaSolicitud(domain.getFechaSolicitud())
                .fechaRevision(domain.getFechaRevision())
                .revisadoPor(domain.getRevisadoPor())
                .comentario(domain.getComentario())
                .motivoRechazo(domain.getMotivoRechazo())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    private SolicitudRegistro toDomain(SolicitudRegistroEntity entity) {
        return SolicitudRegistro.builder()
                .id(entity.getId())
                .nombreCompleto(entity.getNombreCompleto())
                .cedula(entity.getCedula())
                .correoElectronico(entity.getCorreoElectronico())
                .telefono(entity.getTelefono())
                .empresa(entity.getEmpresa())
                .estado(entity.getEstado())
                .fechaSolicitud(entity.getFechaSolicitud())
                .fechaRevision(entity.getFechaRevision())
                .revisadoPor(entity.getRevisadoPor())
                .comentario(entity.getComentario())
                .motivoRechazo(entity.getMotivoRechazo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}