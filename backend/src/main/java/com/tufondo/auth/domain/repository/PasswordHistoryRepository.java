package com.tufondo.auth.domain.repository;

import java.util.List;
import java.util.UUID;

public interface PasswordHistoryRepository {
    void guardar(UUID usuarioId, String passwordHash);
    List<String> obtenerUltimas5(UUID usuarioId);
    boolean existePasswordReutilizada(UUID usuarioId, String passwordHash);
}