package com.tufondo.auth.infrastructure.persistence.jpa;

import com.tufondo.auth.infrastructure.persistence.entity.PasswordHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordHistoryJpaRepository extends JpaRepository<PasswordHistoryEntity, UUID> {
    
    @Query(value = """
        SELECT password_hash FROM password_history 
        WHERE usuario_id = :usuarioId 
        ORDER BY fecha_creacion DESC 
        LIMIT 5
        """, nativeQuery = true)
    List<String> findTop5ByUsuarioIdOrderByFechaCreacionDesc(@Param("usuarioId") UUID usuarioId);
    
    List<PasswordHistoryEntity> findByUsuarioIdOrderByFechaCreacionDesc(UUID usuarioId);
}