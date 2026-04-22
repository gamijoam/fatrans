// com/tufondo/socios/domain/repository/SocioRepository.java
package com.tufondo.socios.domain.repository;

import com.tufondo.socios.domain.model.Socio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SocioRepository {
    Socio guardar(Socio socio);
    Optional<Socio> buscarPorId(UUID id);
    Page<Socio> listar(Pageable pageable);
    Page<Socio> buscarPorCriterios(String nombre, String apellido,
            String numeroDocumento, String numeroSocio, String correo, Pageable pageable);

    Optional<Socio> buscarPorCorreo(String correo);

    List<Socio> buscarPorIdIn(List<UUID> ids);

    boolean existePorNumeroSocio(String numeroSocio);
    boolean existePorNumeroDocumento(String numeroDocumento);
    boolean existePorCorreo(String correo);

    long count();
    long countByEstado(com.tufondo.socios.domain.model.enums.EstadoSocio estado);
    long countByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);

    void eliminar(UUID id);
}
