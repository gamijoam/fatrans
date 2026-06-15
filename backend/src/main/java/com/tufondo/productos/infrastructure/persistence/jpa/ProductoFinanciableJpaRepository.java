package com.tufondo.productos.infrastructure.persistence.jpa;

import com.tufondo.productos.infrastructure.persistence.entity.ProductoFinanciableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoFinanciableJpaRepository extends JpaRepository<ProductoFinanciableEntity, Long> {
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsBySlugIgnoreCase(String slug);
    Optional<ProductoFinanciableEntity> findBySlugAndEstado(String slug, String estado);
    List<ProductoFinanciableEntity> findByEstadoOrderByUpdatedAtDesc(String estado);
    List<ProductoFinanciableEntity> findAllByOrderByUpdatedAtDesc();
}
