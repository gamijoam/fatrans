// 📁 com/tufondo/socios/infrastructure/persistence/jpa/SolicitudRegistroJpaRepository.java
package com.tufondo.socios.infrastructure.persistence.jpa;

import com.tufondo.socios.domain.model.enums.EstadoSolicitud;
import com.tufondo.socios.infrastructure.persistence.entity.SolicitudRegistroEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SolicitudRegistroJpaRepository extends JpaRepository<SolicitudRegistroEntity, UUID> {
    
    boolean existsByCedula(String cedula);
    
    boolean existsByCorreoElectronico(String correo);
    
    @Query("SELECT s FROM SolicitudRegistroEntity s WHERE s.estado = :estado")
    Page<SolicitudRegistroEntity> findByEstado(@Param("estado") EstadoSolicitud estado, Pageable pageable);
}