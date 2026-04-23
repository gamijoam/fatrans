package com.tufondo.auth.infrastructure.persistence.adapter;

import com.tufondo.auth.domain.repository.PasswordHistoryRepository;
import com.tufondo.auth.infrastructure.persistence.entity.PasswordHistoryEntity;
import com.tufondo.auth.infrastructure.persistence.jpa.PasswordHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PasswordHistoryRepositoryImpl implements PasswordHistoryRepository {

    private final PasswordHistoryJpaRepository jpaRepository;

    @Override
    @Transactional
    public void guardar(UUID usuarioId, String passwordHash) {
        PasswordHistoryEntity entity = new PasswordHistoryEntity(usuarioId, passwordHash);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> obtenerUltimas5(UUID usuarioId) {
        return jpaRepository.findTop5ByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePasswordReutilizada(UUID usuarioId, String passwordHash) {
        List<String> ultimas = obtenerUltimas5(usuarioId);
        return ultimas.contains(passwordHash);
    }
}