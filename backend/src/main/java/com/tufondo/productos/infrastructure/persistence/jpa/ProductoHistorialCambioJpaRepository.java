package com.tufondo.productos.infrastructure.persistence.jpa;

import com.tufondo.productos.infrastructure.persistence.entity.ProductoHistorialCambioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoHistorialCambioJpaRepository extends JpaRepository<ProductoHistorialCambioEntity, Long> {

    List<ProductoHistorialCambioEntity> findByProductoIdOrderByCreatedAtDesc(Long productoId);
}
