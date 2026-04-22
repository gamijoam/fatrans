// com/tufondo/creditos/domain/repository/TipoCreditoRepository.java
package com.tufondo.creditos.domain.repository;

import com.tufondo.creditos.domain.model.TipoCredito;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface para TipoCredito.
 */
public interface TipoCreditoRepository {
    
    /**
     * Guarda un tipo de crédito.
     */
    TipoCredito guardar(TipoCredito tipoCredito);
    
    /**
     * Busca tipo de crédito por ID.
     */
    Optional<TipoCredito> buscarPorId(Long id);
    
    /**
     * Busca tipo de crédito por ID solo si está activo.
     */
    Optional<TipoCredito> buscarPorIdActivo(Long id);
    
    /**
     * Busca tipo de crédito por código.
     */
    Optional<TipoCredito> buscarPorCodigo(String codigo);
    
    /**
     * Lista todos los tipos de crédito activos.
     */
    List<TipoCredito> listarActivos();
    
    /**
     * Lista todos los tipos de crédito activos con paginación.
     */
    Page<TipoCredito> listarActivosPaginado(org.springframework.data.domain.Pageable pageable);
    
    /**
     * Lista todos los tipos de crédito.
     */
    List<TipoCredito> listarTodos();
    
    /**
     * Verifica si existe un código de tipo de crédito.
     */
    boolean existePorCodigo(String codigo);
}
