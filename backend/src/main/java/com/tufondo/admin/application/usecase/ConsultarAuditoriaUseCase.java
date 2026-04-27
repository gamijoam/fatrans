package com.tufondo.admin.application.usecase;

import com.tufondo.admin.application.dto.AuditLogResponse;
import com.tufondo.auth.domain.model.Usuario;
import com.tufondo.auth.domain.repository.UsuarioRepository;
import com.tufondo.auth.infrastructure.persistence.entity.SecurityAuditEntity;
import com.tufondo.auth.infrastructure.persistence.jpa.SecurityAuditJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsultarAuditoriaUseCase {

    private final SecurityAuditJpaRepository auditRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> listarAuditoria(int page, int size, UUID usuarioId, String tipoEvento,
                                                 Instant fechaInicio, Instant fechaFin) {
        Pageable pageable = PageRequest.of(page, size);

        Page<SecurityAuditEntity> auditPage = auditRepository.buscarConFiltros(
                usuarioId, tipoEvento, fechaInicio, fechaFin, pageable);

        Map<UUID, String> usuarioCache = new HashMap<>();

        List<AuditLogResponse> logs = auditPage.getContent().stream()
                .map(entity -> {
                    String nombreUsuario = null;
                    if (entity.getUsuarioId() != null) {
                        nombreUsuario = usuarioCache.computeIfAbsent(entity.getUsuarioId(), id -> {
                            return usuarioRepository.buscarPorId(id)
                                    .map(Usuario::nombreCompleto)
                                    .orElse("Usuario no encontrado");
                        });
                    }
                    return toResponse(entity, nombreUsuario);
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("auditoria", logs);
        result.put("page", auditPage.getNumber());
        result.put("size", auditPage.getSize());
        result.put("totalElements", auditPage.getTotalElements());
        result.put("totalPages", auditPage.getTotalPages());
        result.put("first", auditPage.isFirst());
        result.put("last", auditPage.isLast());

        return result;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> listarRecientes(int limit) {
        List<SecurityAuditEntity> recent = auditRepository.findTop100ByOrderByTimestampDesc();
        if (limit > 0 && limit < recent.size()) {
            recent = recent.subList(0, limit);
        }

        Map<UUID, String> usuarioCache = new HashMap<>();

        return recent.stream()
                .map(entity -> {
                    String nombreUsuario = null;
                    if (entity.getUsuarioId() != null) {
                        nombreUsuario = usuarioCache.computeIfAbsent(entity.getUsuarioId(), id -> {
                            return usuarioRepository.buscarPorId(id)
                                    .map(Usuario::nombreCompleto)
                                    .orElse("Usuario no encontrado");
                        });
                    }
                    return toResponse(entity, nombreUsuario);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> listarTiposEventos() {
        return List.of(
                "LOGIN_SUCCESS",
                "LOGIN_FAILED",
                "LOGOUT",
                "TOKEN_REFRESH",
                "ACCOUNT_LOCKED",
                "DASHBOARD_ADMIN_ACCESS",
                "SESSIONS_INVALIDATED",
                "SESSION_INVALIDATED",
                "TIPO_CREDITO_CREADO",
                "TIPO_CREDITO_ACTUALIZADO",
                "TIPO_CREDITO_ACTIVADO",
                "TIPO_CREDITO_DESACTIVADO",
                "ADMIN_CREADO",
                "ADMIN_ACTUALIZADO",
                "ADMIN_ACTIVADO",
                "ADMIN_DESACTIVADO"
        );
    }

    private AuditLogResponse toResponse(SecurityAuditEntity entity, String nombreUsuario) {
        return AuditLogResponse.builder()
                .id(entity.getId())
                .tipoEvento(entity.getTipoEvento())
                .usuarioId(entity.getUsuarioId())
                .nombreUsuario(nombreUsuario)
                .ipAddress(entity.getIpAddress())
                .timestamp(entity.getTimestamp())
                .detalles(entity.getDetalles())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .action(entity.getAction())
                .build();
    }
}