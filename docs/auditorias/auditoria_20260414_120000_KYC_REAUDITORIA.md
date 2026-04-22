# REAUDITORÍA KYC - Módulo KYC
**Fecha:** 14/04/2026
**Auditor:** Lead Software Architect & Cyber-Security Auditor
**Proyecto:** Fondo de Ahorro - Backend KYC Module
**Directorio Auditado:** `/home/gamijoam/Documentos/fondoAhorro/fondo-ahorro-platform/backend/src/main/java/com/tufondo/kyc/`

---

## RESUMEN EJECUTIVO

| Criticidad | Cantidad | Estado |
|------------|----------|--------|
| 🔴 CRÍTICA | 0 | - |
| 🟠 ALTA | 1 | Issue encontrado |
| 🟡 MEDIA | 1 | Implementación incompleta |
| ✅ CORREGIDAS | 8 | Verificadas correctamente |

**Estado General:** El módulo KYC muestra mejora significativa. 8 de 10 correcciones fueron implementadas correctamente. Sin embargo, se identificó 1 issue de alta severidad y 1 de media que requieren atención.

---

## CORRECCIONES VERIFICADAS ✅

### 1. ✅ Auditoría KYC - AuditKYCEntity, AuditKYCJpaRepository, KYCAuditService
**Estado:** CORREGIDO

- `AuditKYCEntity` (línea 27-180): Entidad completa con 18 campos incluyendo indices optimizados para consultas frecuentes.
- `AuditKYCJpaRepository` (línea 16-49): Repository con 6 métodos de consulta por diferentes criterios.
- `KYCAuditService` (línea 22-131): Servicio `@Async` con métodos especializados para cada tipo de evento.

**Verificación:** La arquitectura de auditoría cumple con LOPDP/SUDEBAN. Los eventos incluyen IDOR, rate limit, y cambios de estado.

---

### 2. ✅ Rate Limiting - RateLimitingFilter con Bucket4j
**Estado:** CORREGIDO

```java
// RateLimitingFilter.java líneas 35-39
private static final int DEFAULT_REQUESTS_PER_MINUTE = 60;
private static final int DOCUMENTOS_REQUESTS_PER_MINUTE = 20;
private static final int COLA_REVISION_REQUESTS_PER_MINUTE = 30;
```

**Verificación:**
- Bucket4j 8.10.1 configurado correctamente (pom.xml línea 88-92)
- Tres buckets separados: IP default, usuario+documentos, usuario+cola-revision
- Respuesta JSON adecuada con código `RATE_LIMIT_EXCEDIDO`

---

### 3. ✅ IDOR Fix - Validación estado EN_REVISION en RevisarDocumentosUseCase
**Estado:** CORREGIDO

```java
// RevisarDocumentosUseCase.java líneas 42-46
if (verificacion.getEstado() != EstadoVerificacion.EN_REVISION) {
    throw new AccesoNoAutorizadoException(
        "Verificacion no disponible para revision. Estado actual: " + verificacion.getEstado());
}
```

**Verificación:** Todos los métodos (`obtenerDetalle`, `aprobar`, `rechazar`, `solicitarInfo`) validan que la verificación esté en estado `EN_REVISION` antes de procesarla.

---

### 4. ✅ Optimistic Locking - @Version en entidades JPA
**Estado:** CORREGIDO

```java
// VerificacionKYCEntity.java línea 74
@Version
@Column(name = "version")
private Long version;

// DocumentoIdentidadEntity.java línea 88
@Version
@Column(name = "version")
private Long version;

// ConsentimientoKYCEntity.java línea 51
@Version
@Column(name = "version")
private Long version;
```

**Verificación:** Las tres entidades JPA que pueden ser modificadas tienen `@Version` para prevenir condiciones de carrera.

---

### 5. ✅ Storage URLs - Sanitización en MinIOStorageService
**Estado:** CORREGIDO

```java
// MinIOStorageService.java líneas 132-137
private String normalizePath(String path) {
    return path.replaceAll("[/]+", "/")
        .replaceAll("^/", "")
        .replaceAll("/$", "");
}

// Validación en upload (línea 37), delete (línea 74), generatePresignedUrl (línea 94), exists (línea 116)
if (normalizedPath.contains("..")) {
    throw new SecurityException("Path traversal attempt detected");
}
```

**Verificación:** Todas las operaciones de storage sanitizan el path y validan contra `..` (path traversal).

---

### 8. ✅ Validación IP - Regex en KYCController
**Estado:** CORREGIDO

```java
// KYCController.java líneas 148-155
private static final Pattern IPV4_PATTERN = Pattern.compile(
    "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

private static final Pattern IPV6_PATTERN = Pattern.compile(
    "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::$|^([0-9a-fA-F]{1,4}:){1,7}:$|^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|^([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}$|^([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}$|^([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}$|^([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}$|^[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})$|^:((:[0-9a-fA-F]{1,4}){1,7}|:)$|^fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}$|^::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])$|^([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1?[0-9])?[0-9])\\.){3}(25[0-5]|(2[0-4]|1?[0-9])?[0-9])$");

private static final Pattern IPV4_MAPPED_IPV6_PATTERN = Pattern.compile(
    "^::ffff:(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
```

**Verificación:** Se validan IPv4, IPv6 y IPv4-mapped-IPv6. X-Forwarded-For se parsea correctamente tomando solo la primera IP.

---

### 9. ✅ Validación nombreOriginal - @Pattern contra path traversal
**Estado:** CORREGIDO

```java
// SubirDocumentoRequest.java línea 35
@Pattern(regexp = "^[^./\\\\]+$", message = "Nombre de archivo invalido (no se permiten rutas ni ../)")
private String nombreOriginal;
```

**Verificación:** El patrón impide `.`, `/`, `\` y `..` en el nombre de archivo.

---

### 10. ✅ Estadísticas - Queries reales al repository
**Estado:** CORREGIDO

```java
// AdminKYCController.java líneas 117-132
long pendientes = verificacionRepository.countByEstado(EstadoVerificacion.PENDIENTE);
long enRevision = verificacionRepository.countByEstado(EstadoVerificacion.EN_REVISION);
// ...
Double tiempoPromedioHoras = verificacionRepository.calculateTiempoPromedioRevisionHoras();
Long kycPorExpirar = verificacionRepository.countPorExpirarEntreFechas(...);
```

**Verificación:** Las estadísticas usan queries reales al repository en lugar de cálculos en memoria.

---

## VIOLACIONES ENCONTRADAS

### 🟠 ALTA - Exception PoliticaPrivacidadInvalidaException definida pero NO UTILIZADA

**Archivo:** `IniciarKYCUseCase.java:50`

**Descripción:** La exception `PoliticaPrivacidadInvalidaException` fue creada para validar la versión de política de privacidad, pero **nunca se lanza en ningún lugar del código**.

```java
// IniciarKYCUseCase.java líneas 42-53
// 2. Guardar consentimiento (LOPDP)
ConsentimientoKYC consentimiento = ConsentimientoKYC.builder()
    .socioId(socioId)
    .tipoConsentimiento("KYC_" + request.getNivel().name())
    .aceptado(request.getConsentimientoAceptado())
    .fechaConsentimiento(LocalDateTime.now())
    .ipCliente(request.getIpCliente())
    .userAgent(request.getUserAgent())
    .versionPolitica(request.getVersionPolitica())  // ❌ Sin validación
    .build();

consentimientoRepository.save(consentimiento);
```

**Impacto:** Un atacante podría enviar cualquier string arbitrario como `versionPolitica` (ej: `"invalid"`, `"hack"`) y sería almacenado sin validación. Esto viola el principio de defensa en profundidad y el cumplimiento LOPDP que requiere consentimientos con versiones válidas de políticas.

**Recomendación:** Implementar validación en `IniciarKYCUseCase`:

```java
// Agregar validación antes de guardar
List<String> versionesValidas = List.of("1.0", "2.0", "2.1"); // Obtener de config o DB
if (!versionesValidas.contains(request.getVersionPolitica())) {
    throw new PoliticaPrivacidadInvalidaException(request.getVersionPolitica());
}
```

---

### 🟡 MEDIA - RevocarConsentimientoUseCase modifica registro en lugar de crear nuevo

**Archivo:** `RevocarConsentimientoUseCase.java:54`

**Descripción:** Según las mejores prácticas de auditoría LOPDP, cuando un consentimiento es revocado, se debería crear un **nuevo registro** de revocación (manteniendo historial), en lugar de modificar el registro existente.

```java
// RevocarConsentimientoUseCase.java líneas 53-58
// 3. Revocar consentimiento (marcar como no aceptado)
consentimiento.setAceptado(false);
// Nota: En una implementación real, podrías querer crear un nuevo registro
// o mantener historial. Por ahora invalidamos el actual.

consentimientoRepository.save(consentimiento);
```

**Impacto:** La modificación del registro original pierde trazabilidad. En una auditoría regulatoria, no habría forma de distinguir cuándo se dio el consentimiento original vs. cuándo se revocó.

**Recomendación:** Crear un nuevo registro de revocación con timestamp y motivos:

```java
ConsentimientoKYC revocacion = ConsentimientoKYC.builder()
    .socioId(socioId)
    .tipoConsentimiento("REVOCACION_" + consentimiento.getTipoConsentimiento())
    .aceptado(false)
    .fechaConsentimiento(LocalDateTime.now())
    .ipCliente(ipCliente)
    .userAgent(userAgent)
    .versionPolitica(consentimiento.getVersionPolitica())
    .build();
consentimientoRepository.save(revocacion);
```

---

## CORRECCIÓN #6 - versionPolitica - PARCIALMENTE CORREGIDA ⚠️

**Estado:** La exception existe y el campo se almacena, pero **no hay validación activa**.

La exception `PoliticaPrivacidadInvalidaException` fue creada correctamente en:
- `domain/exception/PoliticaPrivacidadInvalidaException.java:7-15`

Sin embargo, **nunca se lanza**. El `IniciarKYCUseCase` simplemente almacena `request.getVersionPolitica()` sin validar contra una lista de versiones activas.

---

## CORRECCIÓN #7 - Revocación consentimiento - IMPLEMENTADA ✅

**Estado:** CORREGIDO

- `POST /kyc/revocar-consentimiento` implementado en `KYCController.java:127-142`
- `RevocarConsentimientoUseCase.java:35-68` maneja la lógica
- Request/Response DTOs creados correctamente

**Nota:** La implementación funciona pero tiene el issue de media severidad mencionado arriba (debería crear nuevo registro en lugar de modificar).

---

## ARCHIVOS AFECTADOS

| Prioridad | Archivo | Issue |
|-----------|---------|-------|
| 🟠 ALTA | `application/usecase/IniciarKYCUseCase.java` | No valida versionPolitica contra lista activa |
| 🟡 MEDIA | `application/usecase/RevocarConsentimientoUseCase.java` | Debería crear nuevo registro en lugar de modificar |

---

## CONCLUSIÓN

El módulo KYC ha mejorado significativamente su postura de seguridad. Las correcciones de auditoría, rate limiting, IDOR, optimistic locking, sanitización de storage, validación de IP y nombre de archivo están correctamente implementadas.

**Acciones requeridas:**
1. **ALTA:** Implementar validación de `versionPolitica` en `IniciarKYCUseCase` usando `PoliticaPrivacidadInvalidaException`
2. **MEDIA:** Refactorizar `RevocarConsentimientoUseCase` para crear nuevo registro de revocación en lugar de modificar el existente

**Compilación:** El código compila correctamente (Java 21, Spring Boot 3.2.4, Bucket4j 8.10.1).