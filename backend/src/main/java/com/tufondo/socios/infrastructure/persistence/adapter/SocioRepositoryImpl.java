// com/tufondo/socios/infrastructure/persistence/adapter/SocioRepositoryImpl.java
// 🔧 SECURITY FIX: Añadido @Transactional(readOnly=true) a métodos de lectura
// 🔧 SECURITY FIX: Soft delete - listar() ahora excluye socios ELIMINADOS
package com.tufondo.socios.infrastructure.persistence.adapter;

import com.tufondo.socios.domain.model.Socio;
import com.tufondo.socios.domain.model.valueobjects.Direccion;
import com.tufondo.socios.domain.model.valueobjects.ContactoEmergencia;
import com.tufondo.socios.domain.model.enums.EstadoSocio;
import com.tufondo.socios.domain.repository.SocioRepository;
import com.tufondo.socios.infrastructure.persistence.entity.SocioEntity;
import com.tufondo.socios.infrastructure.persistence.jpa.SocioJpaRepository;
import com.tufondo.socios.domain.exception.SocioNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SocioRepositoryImpl implements SocioRepository {

    private final SocioJpaRepository jpaRepository;

    @Override
    @Transactional
    public Socio guardar(Socio socio) {
        SocioEntity entity = toEntity(socio);
        SocioEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Socio> buscarPorId(UUID id) {
        return jpaRepository.findById(id)
                .filter(e -> e.getEstado() != EstadoSocio.ELIMINADO)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Socio> listar(Pageable pageable) {
        // SECURITY FIX: Soft delete - Excluir socios con estado ELIMINADO
        Page<SocioEntity> allEntities = jpaRepository.findAll(pageable);
        List<SocioEntity> filteredList = allEntities.getContent().stream()
                .filter(e -> e.getEstado() != EstadoSocio.ELIMINADO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(filteredList, pageable, allEntities.getTotalElements())
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Socio> buscarPorCriterios(String nombre, String apellido,
            String numeroDocumento, String numeroSocio, String correo, Pageable pageable) {
        return jpaRepository.buscarPorCriterios(
                nombre, apellido, numeroDocumento, numeroSocio, correo, pageable)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Socio> buscarPorCorreo(String correo) {
        return jpaRepository.findByCorreoElectronico(correo)
                .filter(e -> e.getEstado() != EstadoSocio.ELIMINADO)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Socio> buscarPorIdIn(List<UUID> ids) {
        return jpaRepository.findByIdIn(ids).stream()
                .filter(e -> e.getEstado() != EstadoSocio.ELIMINADO)
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNumeroSocio(String numeroSocio) {
        return jpaRepository.existsByNumeroSocio(numeroSocio);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorNumeroDocumento(String numeroDocumento) {
        return jpaRepository.existsByNumeroDocumento(numeroDocumento);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCorreo(String correo) {
        return jpaRepository.existsByCorreo(correo);
    }

    @Override
    @Transactional
    public void eliminar(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return jpaRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByEstado(EstadoSocio estado) {
        return jpaRepository.countByEstado(estado);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin) {
        return jpaRepository.countByFechaRegistroBetween(inicio, fin);
    }

    private SocioEntity toEntity(Socio socio) {
        return SocioEntity.builder()
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
                .direccionResidencia(toEmbed(socio.getDireccionResidencia()))
                .direccionLaboral(toEmbed(socio.getDireccionLaboral()))
                .empresa(socio.getEmpresa())
                .departamento(socio.getDepartamento())
                .cargo(socio.getCargo())
                .tipoContrato(socio.getTipoContrato())
                .salario(socio.getSalario())
                .montoAhorro(socio.getMontoAhorro())
                .numeroCuentaNomina(socio.getNumeroCuentaNomina())
                .bancoNomina(socio.getBancoNomina())
                .contactoEmergencia(toEmbed(socio.getContactoEmergencia()))
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

    private Socio toDomain(SocioEntity entity) {
        return Socio.builder()
                .id(entity.getId())
                .numeroSocio(entity.getNumeroSocio())
                .tipoDocumento(entity.getTipoDocumento())
                .numeroDocumento(entity.getNumeroDocumento())
                .primerNombre(entity.getPrimerNombre())
                .segundoNombre(entity.getSegundoNombre())
                .primerApellido(entity.getPrimerApellido())
                .segundoApellido(entity.getSegundoApellido())
                .fechaNacimiento(entity.getFechaNacimiento())
                .genero(entity.getGenero())
                .estadoCivil(entity.getEstadoCivil())
                .correoElectronico(entity.getCorreoElectronico())
                .telefonoPrincipal(entity.getTelefonoPrincipal())
                .telefonoSecundario(entity.getTelefonoSecundario())
                .direccionResidencia(fromEmbed(entity.getDireccionResidencia()))
                .direccionLaboral(fromEmbed(entity.getDireccionLaboral()))
                .empresa(entity.getEmpresa())
                .departamento(entity.getDepartamento())
                .cargo(entity.getCargo())
                .tipoContrato(entity.getTipoContrato())
                .salario(entity.getSalario())
                .montoAhorro(entity.getMontoAhorro())
                .numeroCuentaNomina(entity.getNumeroCuentaNomina())
                .bancoNomina(entity.getBancoNomina())
                .contactoEmergencia(fromEmbed(entity.getContactoEmergencia()))
                .estado(entity.getEstado())
                .fechaIngreso(entity.getFechaIngreso())
                .fechaRegistro(entity.getFechaRegistro())
                .fechaActualizacion(entity.getFechaActualizacion())
                .fechaActivacion(entity.getFechaActivacion())
                .fechaDesactivacion(entity.getFechaDesactivacion())
                .motivoDesactivacion(entity.getMotivoDesactivacion())
                .roles(entity.getRoles())
                .build();
    }

    private SocioEntity.DireccionEmbed toEmbed(Direccion d) {
        if (d == null) return null;
        return SocioEntity.DireccionEmbed.builder()
                .calle(d.getCalle())
                .numero(d.getNumero())
                .ciudad(d.getCiudad())
                .estado(d.getDepartamento()) // Note: mapping domain department to entity state
                .codigoPostal(d.getCodigoPostal())
                .pais(d.getPais())
                .build();
    }

    private Direccion fromEmbed(SocioEntity.DireccionEmbed e) {
        if (e == null) return null;
        return Direccion.builder()
                .calle(e.getCalle())
                .numero(e.getNumero())
                .ciudad(e.getCiudad())
                .departamento(e.getEstado())
                .codigoPostal(e.getCodigoPostal())
                .pais(e.getPais())
                .build();
    }

    private SocioEntity.ContactoEmergenciaEmbed toEmbed(ContactoEmergencia c) {
        if (c == null) return null;
        return SocioEntity.ContactoEmergenciaEmbed.builder()
                .nombre(c.getNombre())
                .telefono(c.getTelefono())
                .parentesco(c.getParentesco())
                .build();
    }

    private ContactoEmergencia fromEmbed(SocioEntity.ContactoEmergenciaEmbed e) {
        if (e == null) return null;
        return ContactoEmergencia.builder()
                .nombre(e.getNombre())
                .telefono(e.getTelefono())
                .parentesco(e.getParentesco())
                .build();
    }
}
