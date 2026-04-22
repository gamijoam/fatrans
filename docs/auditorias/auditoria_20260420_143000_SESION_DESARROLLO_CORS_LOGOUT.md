# 🔴 AUDITORÍA DE SEGURIDAD - SESIÓN DE DESARROLLO
## Proyecto: Fondo de Ahorro (FinTech)
## Fecha: 2026-04-20
## Auditor: Lead Software Architect & Cyber-Security Auditor

---

## RESUMEN EJECUTIVO

| Categoría | CRÍTICA | ALTA | MEDIA | Total |
|-----------|---------|------|-------|-------|
| Seguridad | 1 | 0 | 1 | 2 |
| Arquitectura | 1 | 0 | 0 | 1 |
| Configuración | 1 | 1 | 0 | 2 |
| **TOTAL** | **3** | **1** | **1** | **5** |

### Veredicto: ⚠️ **REQUIERE CORRECCIONES ANTES DE PRODUCCIÓN**

---

## 🔴 VIOLACIONES CRÍTICAS (3)

### 1. [CRÍTICA] - Secretos hardcodeados en docker-compose.yml

**Archivo:** `infrastructure/docker-compose.yml:48`
```yaml
JWT_SECRET: ${JWT_SECRET:-Desarrollo2026SecretoBackendMinimo256Bits}
```

**Descripción:** La variable de entorno JWT_SECRET tiene un valor por defecto hardcodeado que se usará si no se configura la variable de entorno. Este valor "Desarrollo2026SecretoBackendMinimo256Bits" aparece como fallback en el código y jamás debería usarse en producción.

**Impacto:** Si alguien despliega este docker-compose sin configurar la variable JWT_SECRET externally, el sistema usará un secreto conocido públicamente. Un atacante podría descifrar todos los tokens JWT y obtener acceso administrativo.

**Corrección requerida:**
```yaml
# Eliminar el valor por defecto - el contenedor NO debería iniciar sin JWT_SECRET configurado
JWT_SECRET: ${JWT_SECRET}  # Error si no está configurado
```

Alternativamente, validar en el backend que el JWT_SECRET no sea el valor de desarrollo por defecto.

---

### 2. [CRÍTICA] - Credenciales MinIO hardcodeadas

**Archivos:**
- `infrastructure/docker-compose.yml:27-28` (servicio minio)
- `infrastructure/docker-compose.yml:50-51` (servicio backend)

```yaml
# MinIO service
MINIO_ROOT_USER: minioadmin
MINIO_ROOT_PASSWORD: minioadmin123

# Backend environment
MINIO_ACCESS_KEY: minioadmin
MINIO_SECRET_KEY: minioadmin123
```

**Descripción:** Las credenciales de MinIO están duplicadas y hardcodeadas. Aunque estén en el repositorio, esto viola el principio de configuración externa para secretos.

**Impacto:** En un escenario de breach, estas credenciales permitirían acceso a todos los documentos almacenados en MinIO (contratos, estados de cuenta, pagarés, etc.).

**Corrección requerida:**
```yaml
# En cada servicio, usar variables de entorno sin valores por defecto
minio:
  environment:
    MINIO_ROOT_USER: ${MINIO_USER}
    MINIO_ROOT_PASSWORD: ${MINIO_PASSWORD}

backend:
  environment:
    MINIO_ACCESS_KEY: ${MINIO_USER}
    MINIO_SECRET_KEY: ${MINIO_PASSWORD}
```

---

### 3. [CRÍTICA] - Contraseñas de base de datos con valores por defecto débiles

**Archivo:** `infrastructure/docker-compose.yml:9,19`

```yaml
POSTGRES_PASSWORD: ${DB_PASS:-secret}
REDIS_PASS: ${REDIS_PASS:-secret}
```

**Descripción:** Las contraseñas de PostgreSQL y Redis tienen fallback a "secret" si no se configuran las variables de entorno. En entorno Docker Compose local esto puede ser aceptable, pero si el template se despliega sin configurar las vars, quedan con secretos débiles.

**Impacto:** En entornos no configurados correctamente, un atacante podría acceder a PostgreSQL y Redis con credenciales "secret".

**Corrección requerida:**
```yaml
# PostgreSQL
POSTGRES_PASSWORD: ${DB_PASS:?La variable DB_PASS debe estar configurada}

# Redis
REDIS_PASS: ${REDIS_PASS:?La variable REDIS_PASS debe estar configurada}
```

La sintaxis `${VAR:?mensaje}强制要求 que la variable esté configurada o fallará el inicio del contenedor.

---

## 🟠 VIOLACIONES DE ARQUITECTURA (1)

### 4. [ALTA] - Estado estático en AdminDashboardRateLimitFilter

**Archivo:** `backend/src/main/java/com/tufondo/auth/infrastructure/security/AdminDashboardRateLimitFilter.java:37`

```java
private static final Map<String, BucketEntry> BUCKETS = new ConcurrentHashMap<>();
```

**Descripción:** El filtro de rate limiting usa un mapa estático para almacenar los buckets. Esto causa problemas severos:

1. **No escala horizontalmente**: Si hay múltiples instancias del backend (Kubernetes), cada una tiene su propio mapa de buckets. Un usuario podría hacer 30 req/min en cada instancia = 90 req/min total.
2. **Memory leak potencial**: Si el cleaning thread falla, los buckets se acumulan.
3. **Thread leak**: El `scheduler` estático (línea 38) es un singleton que podría causar issues en aplicaciones que se recargan.

**Impacto:** El rate limiting es efectivo solo para una sola instancia. En producción con múltiples pods, el endpoint `/api/v1/admin/dashboard/estadisticas` puede ser abusado.

**Corrección requerida:**
- Usar Redis para almacenar los buckets de rate limiting (ej: bucket4j-redis)
- O documentar que esta aplicación solo puede ejecutarse con un solo instance

---

## 🟡 MEJORAS RECOMENDADAS (2)

### 5. [MEDIA] - CORS: demasiados orígenes para desarrollo

**Archivo:** `infrastructure/docker-compose.yml:52`

```yaml
CORS_ORIGINS: "http://localhost:18080,http://localhost:32993,http://localhost:18081,http://localhost:3000,http://127.0.0.1:18080,http://127.0.0.1:32993,http://127.0.0.1:18081,http://127.0.0.1:3000"
```

**Descripción:** Esta configuración incluye 8 orígenes (4 localhost + 4 127.0.0.1) que son todos para desarrollo. Mantener tantos orígenes aumenta la superficie de ataque si la lista se usa incorrectamente.

**Impacto:** Bajo - es solo desarrollo, pero podría causar confusión.

**Recomendación:** Usar variables separadas para development vs production:
```yaml
CORS_ORIGINS: ${CORS_ORIGINS_DEV:-http://localhost:3000,http://localhost:18081}
```

---

### 6. [MEDIA] - SecurityAuditService solo hace logging

**Archivo:** `backend/src/main/java/com/tufondo/auth/infrastructure/service/SecurityAuditService.java`

**Descripción:** El servicio de auditoría solo escribe a logs. Para cumplimiento FinTech/Bancario, los eventos de seguridad deberían persistirse en una tabla de base de datos con immutable audit trail.

**Impacto:** En caso de breach, no habría evidencia forense completa de qué usuarios accedieron a qué datos.

**Recomendación:** Implementar un AuditEventRepository que persista los eventos además del logging.

---

## ✅ HALLAZGOS POSITIVOS

### Autenticación JWT - Implementación sólida
- ✅ Token rotation implementado (línea 122 de AuthUseCase.java)
- ✅ Access y Refresh tokens distinguidos por claim "tipo_token"
- ✅ Lockout de cuenta después de 5 intentos fallidos (línea 30 de AuthUseCase.java)
- ✅ Validación robusta de tokens con manejo de excepciones específicas
- ✅ Claims incluyen usuarioId, correo, rol, nombre_usuario

### Seguridad de Headers
- ✅ `X-Frame-Options: DENY` configurado
- ✅ `Referrer-Policy: STRICT_ORIGIN_WHEN_CROSS_ORIGIN` configurado
- ✅ `Permissions-Policy` desactiva geolocation, microphone, camera, payment

### Frontend - Manejo de errores correcto
- ✅ 401 elimina tokens del secure storage (api_client.dart:111-112)
- ✅ BlocListener en AdminDashboardPage espera estado Unauthenticated antes de navegar (línea 31-36)
- ✅ Retry interceptor con backoff exponencial implementado

---

## 📋 ARCHIVOS AFECTADOS

| Severidad | Archivo | Líneas | Prioridad |
|-----------|---------|--------|-----------|
| CRÍTICA | infrastructure/docker-compose.yml | 9, 19, 27-28, 48, 50-51 | 1 |
| CRÍTICA | infrastructure/docker-compose.yml | 9, 19 | 2 |
| ALTA | AdminDashboardRateLimitFilter.java | 37-53 | 3 |
| MEDIA | docker-compose.yml | 52 | 4 |
| MEDIA | SecurityAuditService.java | TODO | 5 |

---

## 🔧 ACCIONES REQUERIDAS

### Antes de Producción:

1. **[OBLIGATORIO]** Eliminar valores por defecto de JWT_SECRET, DB_PASS, REDIS_PASS
2. **[OBLIGATORIO]** Externalizar credenciales MinIO a variables de entorno
3. **[OBLIGATORIO]** Implementar Redis-based rate limiting o documentar single-instance
4. **[RECOMENDADO]** Implementar persistencia de audit trail en base de datos
5. **[OPCIONAL]** Limpiar configuración CORS para producción

### Issues Pendientes de la Sesión:

- ⚠️ **Dashboard Admin 403**: El backend necesita reinicio de Docker para aplicar cambios de CORS_ORIGINS
- ⚠️ **Verificar** que después del reinicio de Docker, el dashboard carga correctamente

---

## 📊 MÉTRICAS DE CÓDIGO

| Métrica | Valor | Estado |
|---------|-------|--------|
| Líneas Java analizadas | ~2,000+ | ✅ |
| Archivos Dart analizados | 5 | ✅ |
| Archivos YAML analizados | 1 | ✅ |
| Vulnerabilidades críticas | 3 | ⚠️ |
| Vulnerabilidades altas | 1 | ⚠️ |
| Issues pendientes | 1 | ⚠️ |

---

## 🎯 VEREDICTO FINAL

**ESTADO: ⚠️ NO APROBADO PARA PRODUCCIÓN**

El código de la sesión de desarrollo tiene una arquitectura sólida y cumple con muchos estándares de seguridad FinTech (JWT, rate limiting, auditoría, BCrypt). Sin embargo, hay **3 violaciones CRÍTICAS** relacionadas con secretos hardcodeados que DEBEN resolverse antes de cualquier despliegue a producción.

Las correctas implementaciones de:
- Logout con BlocListener para esperar estado Unauthenticated ✅
- withCredentials para CORS en Flutter Web ✅
- Manejo de errores 401 con limpieza de tokens ✅

Son evidencian de buen trabajo. Solo falta corregir la configuración de secretos.

---

*Auditoría generada: 2026-04-20*
*Versión del skill: security-auditor v1.0*