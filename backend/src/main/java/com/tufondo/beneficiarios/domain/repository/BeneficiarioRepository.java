// com/tufondo/beneficiarios/domain/repository/BeneficiarioRepository.java
package com.tufondo.beneficiarios.domain.repository;

import com.tufondo.beneficiarios.domain.model.Beneficiario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interfaz del repositorio de Beneficiarios.
 * Define las operaciones de persistencia para la entidad Beneficiario.
 */
public interface BeneficiarioRepository {

    /**
     * Guarda un beneficiario (crear o actualizar).
     */
    Beneficiario guardar(Beneficiario beneficiario);

    /**
     * Busca un beneficiario por su ID.
     * @return Optional con el beneficiario si existe y está activo
     */
    Optional<Beneficiario> buscarPorId(UUID id);

    /**
     * Lista todos los beneficiarios activos de un socio.
     */
    List<Beneficiario> listarPorSocioId(UUID socioId);

    /**
     * Cuenta los beneficiarios activos de un socio.
     */
    int countActivosPorSocioId(UUID socioId);

    /**
     * Verifica si existe un beneficiario activo con el mismo documento.
     * @param socioId ID del socio
     * @param tipoDocumento Tipo de documento
     * @param numeroDocumento Número de documento
     * @param excludeId ID a excluir (para updates)
     * @return true si existe un duplicado
     */
    boolean existePorDocumento(UUID socioId, com.tufondo.beneficiarios.domain.model.enums.TipoDocumento tipoDocumento, String numeroDocumento, UUID excludeId);

    /**
     * Obtiene la suma de porcentajes de beneficiarios activos de un socio.
     */
    java.math.BigDecimal sumarPorcentajesPorSocioId(UUID socioId);

    /**
     * Busca un beneficiario por ID sin filtrar por estado activo.
     * Para uso interno de auditoría.
     */
    Optional<Beneficiario> buscarPorIdIncluyendoInactivos(UUID id);
}