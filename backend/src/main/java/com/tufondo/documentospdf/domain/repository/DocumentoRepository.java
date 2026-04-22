// com.tufondo.documentospdf.domain.repository.DocumentoRepository
package com.tufondo.documentospdf.domain.repository;

import com.tufondo.documentospdf.domain.model.Documento;
import com.tufondo.documentospdf.domain.model.enums.EstadoDocumento;
import com.tufondo.documentospdf.domain.model.enums.TipoDocumento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto (port) del repositorio de documentos.
 * Define las operaciones de persistencia que debe implementar
 * la capa de infraestructura.
 */
public interface DocumentoRepository {

    /**
     * Guarda un documento en el repositorio.
     */
    Documento guardar(Documento documento);

    /**
     * Busca un documento por su ID.
     */
    Optional<Documento> buscarPorId(UUID id);

    /**
     * Lista todos los documentos de un socio con paginación.
     */
    List<Documento> listarPorSocio(UUID socioId, int page, int size);

    /**
     * Lista documentos por socio y tipo.
     */
    List<Documento> listarPorSocioYTipo(UUID socioId, TipoDocumento tipo, int page, int size);

    /**
     * Lista documentos por socio y estado.
     */
    List<Documento> listarPorSocioYEstado(UUID socioId, EstadoDocumento estado, int page, int size);

    /**
     * Cuenta el total de documentos de un socio.
     */
    long contarPorSocio(UUID socioId);

    /**
     * Cuenta documentos por socio y tipo.
     */
    long contarPorSocioYTipo(UUID socioId, TipoDocumento tipo);

    /**
     * Cuenta documentos por socio y estado.
     */
    long contarPorSocioYEstado(UUID socioId, EstadoDocumento estado);

    /**
     * Actualiza el estado de un documento.
     */
    void actualizarEstado(UUID id, EstadoDocumento estado);
}
