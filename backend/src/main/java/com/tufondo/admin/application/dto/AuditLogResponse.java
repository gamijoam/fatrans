package com.tufondo.admin.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private UUID id;
    private String tipoEvento;
    private UUID usuarioId;
    private String nombreUsuario;
    private String ipAddress;
    private Instant timestamp;
    private String detalles;
    private String entityType;
    private String entityId;
    private String action;
}