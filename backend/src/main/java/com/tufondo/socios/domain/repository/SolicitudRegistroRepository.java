// 📁 com/tufondo/socios/domain/repository/SolicitudRegistroRepository.java
package com.tufondo.socios.domain.repository;

import com.tufondo.socios.domain.model.SolicitudRegistro;
import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto del dominio para Solicitud de Registro.
 * Define las operaciones de persistencia disponibles.
 */
public interface SolicitudRegistroRepository {
    
    /**
     * Guarda una solicitud de registro (crear o actualizar).
     */
    SolicitudRegistro guardar(SolicitudRegistro solicitud);
    
    /**
     * Busca una solicitud por su ID.
     */
    Optional<SolicitudRegistro> buscarPorId(UUID id);
    
    /**
     * Lista solicitudes filtradas por estado.
     */
    Page<SolicitudRegistro> listarPorEstado(EstadoSolicitud estado, Pageable pageable);
    
    /**
     * Lista todas las solicitudes con paginación.
     */
    Page<SolicitudRegistro> listar(Pageable pageable);
    
    /**
     * Verifica si existe una solicitud con la cédula proporcionada.
     */
    boolean existePorCedula(String cedula);
    
    /**
     * Verifica si existe una solicitud con el correo electrónico proporcionado.
     */
    boolean existePorCorreo(String correo);
    
    /**
     * Elimina una solicitud por su ID.
     */
    void eliminar(UUID id);
}