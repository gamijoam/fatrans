package com.tufondo.kyc.infrastructure.persistence.jpa;

import com.tufondo.kyc.infrastructure.persistence.entity.ConsentimientoBiometricoEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentimientoBiometricoJpaRepository extends JpaRepository<ConsentimientoBiometricoEntity, UUID> {

    /**
     * Devuelve el consentimiento vigente más reciente. Antes esto retornaba
     * {@code Optional<...>} directo y Spring Data tiraba
     * {@code IncorrectResultSizeDataAccessException} si por cualquier motivo
     * había más de una fila vigente (ej. un usuario que hace doble-click en
     * "Aceptar consentimiento" antes de que llegue la respuesta). Ahora
     * pedimos una List + Pageable LIMIT 1 y nos quedamos con el primero.
     */
    @Query("SELECT c FROM ConsentimientoBiometricoEntity c " +
           "WHERE c.socioId = :socioId AND c.aceptado = true AND c.fechaRevocacion IS NULL " +
           "ORDER BY c.fechaConsentimiento DESC")
    List<ConsentimientoBiometricoEntity> findVigentesBySocioId(@Param("socioId") UUID socioId, Pageable pageable);

    default Optional<ConsentimientoBiometricoEntity> findVigenteBySocioId(UUID socioId) {
        List<ConsentimientoBiometricoEntity> vigentes = findVigentesBySocioId(socioId, PageRequest.of(0, 1));
        return vigentes.isEmpty() ? Optional.empty() : Optional.of(vigentes.get(0));
    }
}
