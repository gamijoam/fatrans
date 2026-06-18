package com.tufondo.productos.infrastructure.persistence.jpa;

import com.tufondo.productos.infrastructure.persistence.entity.ProductoImagenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoImagenJpaRepository extends JpaRepository<ProductoImagenEntity, Long> {
    long countByProductoIdAndActivaTrue(Long productoId);
    List<ProductoImagenEntity> findByProductoIdAndActivaTrueOrderByOrdenAscIdAsc(Long productoId);
    List<ProductoImagenEntity> findByProductoIdAndActivaTrue(Long productoId);
    Optional<ProductoImagenEntity> findByIdAndProductoIdAndActivaTrue(Long id, Long productoId);
}
