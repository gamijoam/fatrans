// com/tufondo/creditos/infrastructure/persistence/jpa/TipoCreditoJpaRepository.java
package com.tufondo.creditos.infrastructure.persistence.jpa;

import com.tufondo.creditos.infrastructure.persistence.entity.TipoCreditoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository para TipoCreditoEntity.
 */
@Repository
public interface TipoCreditoJpaRepository extends JpaRepository<TipoCreditoEntity, Long> {
    
    Optional<TipoCreditoEntity> findByCodigo(String codigo);
    
    List<TipoCreditoEntity> findByActivoTrue();
    
    Optional<TipoCreditoEntity> findByIdAndActivoTrue(Long id);
    
    Page<TipoCreditoEntity> findByActivoTrue(Pageable pageable);
    
    boolean existsByCodigo(String codigo);
}
