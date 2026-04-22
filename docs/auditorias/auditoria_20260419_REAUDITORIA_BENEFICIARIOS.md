# RE-AUDITORÍA DE SEGURIDAD: MÓDULO DE BENEFICIARIOS
## Correcciones Post-Auditoría - Abril 2026

**Auditor:** Lead Software Architect & Cyber-Security Auditor  
**Fecha:** 2026-04-19  
**Módulo Auditado:** Beneficiarios (com.tufondo.beneficiarios)  
**Estado:** RE-AUDITORÍA

---

## Veredicto: ⚠️ CON OBSERVACIONES

El módulo de Beneficiarios fue corregido exitosamente en sus 5 vulnerabilidades críticas reportadas. Sin embargo, se identificaron **2 nuevas vulnerabilidades ALTAS** introducidas durante las correcciones que deben ser mitigadas antes de pasar a producción.

---

## Correcciones Verificadas:

| # | Problema Original | Estado | Comentario |
|---|-------------------|--------|------------|
| 1 | **IDOR** - Falta validación de acceso | ✅ VERIFICADO | `validarAccesoSocio()` ahora verifica JWT + ROLE_ADMIN. Socio solo ve sus propios beneficiarios. Admin tiene acceso total. |
| 2 | **Auditoría** - Falta auditoría transaccional | ✅ VERIFICADO | `BeneficiarioAuditService` usa `Propagation.REQUIRES_NEW` en los 3 métodos (CREATE, UPDATE, DELETE). |
| 3 | **Rate Limiting** - Usa X-Forwarded-For | ✅ VERIFICADO | Filter ahora solo usa `X-Real-IP` o `remoteAddr`. X-Forwarded-For eliminado. |
| 4 | **Fuga de información** - Excepciones informativas | ✅ VERIFICADO | `UpdateBeneficiarioUseCase` ahora lanza `BeneficiarioNoEncontradoException` genérica. |
| 5 | **Enmascaramiento** - Documento sin mask | ✅ VERIFICADO | `BeneficiarioResponseDTO.enmascararDocumento()` muestra solo últimos 4 dígitos. |

---

## Nuevas Vulnerabilidades Encontradas:

### 🟠 ALTA #1: BeneficiarioAuditService acepta X-Real-IP de cualquier fuente (IP Spoofing)

- **[CATEGORÍA]:** Seguridad / Rate Limiting Bypass
- **[DESCRIPCIÓN]:** El método `getClientIp()` en `BeneficiarioAuditService.java:93-99` obtiene la IP del cliente verificando solo que `X-Real-IP` no esté vacío, sin validar si la solicitud vino realmente a través de un proxy confiable. Un atacante puede enviar un header `X-Real-IP` falsificado para evadir rate limiting o filtros de seguridad basados en IP.

- **[IMPACTO]:** Un atacante malicioso puede:
  - Evadir las restricciones de rate limiting
  - Suplantar la IP de usuarios legítimos
  - Potencial DoS hacia usuarios específicos

- **[ARCHIVO]:** `backend/src/main/java/com/tufondo/beneficiarios/infrastructure/persistence/adapter/BeneficiarioAuditService.java:93-99`
- **[RECOMENDACIÓN]:** Implementar validación contra lista blanca de IPs de proxy conocidas:

```java
private static final Set<String> TRUSTED_PROXY_IPS = Set.of(
    "10.0.0.0/8", "172.16.0.0/12", "192.168.0.0/16"
    // Cargar desde configuración de ambiente
);

private String getClientIp(HttpServletRequest request) {
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty() && isTrustedProxyIp(xRealIp)) {
        return xRealIp.split(",")[0].trim();
    }
    return request.getRemoteAddr();
}

private boolean isTrustedProxyIp(String ip) {
    // Validar contra rangos CIDR de proxies confiables
    return TRUSTED_PROXY_IPS.stream().anyMatch(cidr -> isInCidrRange(ip, cidr));
}
```

---

### 🟠 ALTA #2: BeneficiarioExceptionHandler no maneja AccesoNoAutorizadoException

- **[CATEGORÍA]:** Seguridad / Information Leakage
- **[DESCRIPCIÓN]:** El `BeneficiarioExceptionHandler` no tiene un handler para `AccesoNoAutorizadoException`. Cuando se lanza esta excepción desde `validarAccesoSocio()` (línea 115 de BeneficiarioController), cae al handler genérico del `GlobalExceptionHandler` que retorna HTTP 500. Un atacante puede distinguir entre "recurso no encontrado" (404) y "error interno" (500), confirmando la existencia de recursos.

- **[IMPACTO]:** Un atacante puede:
  - Determinar si un beneficiario existe mediante timing attacks o análisis de respuestas
  - Mapear la base de datos de beneficiarios
  - Preparar ataques dirigidos

- **[ARCHIVO]:** `backend/src/main/java/com/tufondo/beneficiarios/presentation/exception/BeneficiarioExceptionHandler.java`
- **[RECOMENDACIÓN]:** Agregar handler explícito que retorne 403 genérico:

```java
@ExceptionHandler(AccesoNoAutorizadoException.class)
public ResponseEntity<Map<String, Object>> handleAccesoNoAutorizado(AccesoNoAutorizadoException ex) {
    return buildResponse(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "Acceso denegado");
}
```

---

## Observaciones Arquitectónicas (No Bloqueantes):

### ⚪ Observación #1: Auditoría guarda documento completo sin enmascarar

- **ARCHIVO:** `BeneficiarioAuditService.java:63-64`
- **DESCRIPCIÓN:** Los métodos `registrarAuditoria()` guardan `datosAnteriores` y `datosNuevos` serializados como JSONB. El documento del beneficiario se guarda **sin enmascarar** en la tabla de auditoría.
- **ANÁLISIS:** Esto es **correcto para compliance bancario** - la auditoría debe guardar el estado real para forensic analysis. Sin embargo, representa un riesgo si la tabla de auditoría es comprometida.
- **RECOMENDACIÓN:** Documentar que la tabla `beneficiaries_audit` debe tener los mismos controles de acceso que datos financieros sensibles.

---

## Archivos Afectados:

| Criticidad | Archivo | Cambio Requerido |
|------------|---------|------------------|
| 🟠 ALTA | `infrastructure/persistence/adapter/BeneficiarioAuditService.java:93-99` | Validar X-Real-IP contra lista blanca de proxies |
| 🟠 ALTA | `presentation/exception/BeneficiarioExceptionHandler.java` | Agregar handler para AccesoNoAutorizadoException |

---

## Checklist de Seguridad:

| Control | Estado | Notas |
|---------|--------|-------|
| JWT validación | ✅ CUMPLE | SecurityConfig con filtro stateless |
| IDOR protection | ✅ CUMPLE | validarAccesoSocio() implementado |
| Rate limiting | ⚠️ PARCIAL | X-Real-IP vulnerable a spoofing |
| Data masking | ✅ CUMPLE | Documento enmascarado en responses |
| Error handling | ⚠️ PARCIAL | Falta handler para AccesoNoAutorizadoException |
| Audit logging | ✅ CUMPLE | Propagation.REQUIRES_NEW implementado |
| SQL Injection | ✅ CUMPLE | Uso de JPA repository |
| XSS | ✅ CUMPLE | Spring Boot default encoding |

---

## Recomendaciones Finales:

1. **BLOQUEANTE:** Implementar validación de IP de proxy en `BeneficiarioAuditService.getClientIp()`
2. **BLOQUEANTE:** Agregar handler para `AccesoNoAutorizadoException` en `BeneficiarioExceptionHandler`
3. **DOCUMENTACIÓN:** Documentar que `beneficiaries_audit` es tabla sensible con restricciones de acceso RBAC
4. **POST-PRODUCCIÓN:** Monitorear logs de auditoría por intentos de IDOR (ya existe log.warn en línea 109-110)

---

**Firma del Auditor:** Lead Software Architect & Cyber-Security Auditor  
**Próxima Auditoría Programada:** Antes del release a producción