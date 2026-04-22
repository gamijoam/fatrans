// com/tufondo/beneficiarios/infrastructure/persistence/adapter/BeneficiarioAuditService.java
package com.tufondo.beneficiarios.infrastructure.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tufondo.beneficiarios.domain.model.Beneficiario;
import com.tufondo.beneficiarios.infrastructure.persistence.entity.BeneficiarioAuditEntity;
import com.tufondo.beneficiarios.infrastructure.persistence.jpa.BeneficiarioAuditJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio de auditoría para registrar cambios en beneficiarios.
 * 🔒 SECURITY: Implementa auditoría transaccional para compliance bancario.
 * Propagation.REQUIRES_NEW asegura que la auditoría se persiste incluso si la
 * transacción principal hace rollback.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeneficiarioAuditService {

    private static final String ENTIDAD_TIPO = "BENEFICIARIO";
    private static final String ACCION_CREATE = "CREATE";
    private static final String ACCION_UPDATE = "UPDATE";
    private static final String ACCION_DELETE = "DELETE";

    private final BeneficiarioAuditJpaRepository auditJpaRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarCreate(Beneficiario beneficiario, HttpServletRequest request) {
        registrarAuditoria(ACCION_CREATE, beneficiario.getId(), beneficiario, null, request);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarUpdate(Beneficiario beneficiarioAnterior, Beneficiario beneficiarioNuevo, HttpServletRequest request) {
        registrarAuditoria(ACCION_UPDATE, beneficiarioNuevo.getId(), beneficiarioNuevo, beneficiarioAnterior, request);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarDelete(Beneficiario beneficiario, HttpServletRequest request) {
        registrarAuditoria(ACCION_DELETE, beneficiario.getId(), null, beneficiario, request);
    }

    private void registrarAuditoria(String accion, UUID entidadId, Beneficiario datosNuevos,
            Beneficiario datosAnteriores, HttpServletRequest request) {
        try {
            BeneficiarioAuditEntity audit = BeneficiarioAuditEntity.builder()
                    .entidadTipo(ENTIDAD_TIPO)
                    .entidadId(entidadId)
                    .accion(accion)
                    .usuarioId(getUsuarioId())
                    .rolUsuario(getUsuarioRol())
                    .ipCliente(getClientIp(request))
                    .datosAnteriores(serializar(datosAnteriores))
                    .datosNuevos(serializar(datosNuevos))
                    .fechaEvento(java.time.Instant.now())
                    .build();

            auditJpaRepository.save(audit);
            log.info("AUDIT: {} - {} - {} - {}", accion, ENTIDAD_TIPO, entidadId, getUsuarioId());
        } catch (Exception e) {
            log.error("AUDIT_FAILURE: No se pudo registrar auditoría para {} {}: {}", accion, entidadId, e.getMessage());
            throw new RuntimeException("Error crítico de auditoría, la operación no puede continuar", e);
        }
    }

    private String getUsuarioId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    private String getUsuarioRol() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream().findFirst().map(Object::toString).orElse("UNKNOWN");
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && isTrustedProxyIp(xRealIp)) {
            return xRealIp.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isTrustedProxyIp(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        if (ip.contains(",")) return false;
        if ("127.0.0.1".equals(ip) || "0.0.0.0".equals(ip)) return false;
        return ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
    }

    private String serializar(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Error al serializar objeto para auditoría: {}", e.getMessage());
            return "{}";
        }
    }
}