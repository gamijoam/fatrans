# Análisis del Plan de Deploy - Fatrans

**Fecha:** 29 Abril 2026
**Proyecto:** Fondo de Ahorro Platform
**Versión Backend:** Spring Boot 3.2.4 / Java 21
**Versión Frontend:** Next.js 14.2.0

---

## 1. Resumen Ejecutivo

El plan de deploy propuesto es **sólido en su arquitectura general**, pero requiere ajustes importantes en seguridad y configuración antes de producción. El stack tecnológico (Next.js + Spring Boot + PostgreSQL + Redis + MinIO) es apropiado para una aplicación FinTech.

---

## 2. Análisis por Componente

### 2.1 Arquitectura de Subdominios

| Subdominio Propuesto | Ruta Nginx | Módulo Next.js | Instancia | Aislamiento |
|---------------------|------------|----------------|-----------|-------------|
| www.fatrans.com | `:80/:443` | `(public)/` | Public (:3000) | Aisla landing |
| auth.fatrans.com | `/auth/*` | `(auth)/` | Public (:3000) | Aisla login |
| app.fatrans.com | `/app/*` | `(dashboard)/` | Protected (:3001) | Aisla app socios |
| admin.fatrans.com | `/admin/*` | `(admin)/` | Protected (:3001) | Aisla admin |
| api.fatrans.com | `:8080` interno | Backend | N/A | Aislamiento total |

**Arquitectura de Aislamiento Implementada:**
- **Frontend Public (:3000):** www + auth - expuesto a internet
- **Frontend Protected (:3001):** app + admin - mayor protección
- **Backend API (:8080):** Solo accesible via nginx - nunca expuesto

**Beneficio:** Si comprometen el landing (www), login (auth) y app de socios (app) siguen funcionando en instancias separadas.

### 2.2 Seguridad del Servidor (VPS)

| Medida | Plan Original | Estado Actual | Recomendación |
|--------|---------------|---------------|---------------|
| SSH con claves | ✅ Recomendado | No configurado | Implementar |
| Puerto SSH no estándar | ✅ Puerto 2222 | Puerto 22 default | Cambiar a 2222 |
| UFW Firewall | ✅ Configurar | No configurado | Instalar y configurar |
| Fail2Ban | ✅ 5 intentos | No instalado | Instalar y configurar |
| Updates automáticos | ✅ unattended-upgrades | No configurado | Habilitar |

### 2.3 Nginx + SSL

| Configuración | Plan Original | Estado Actual | Prioridad |
|--------------|---------------|---------------|-----------|
| Let's Encrypt | ✅ Wildcard o por dominio | No configurado | Alta |
| TLS 1.2+ | ✅ Obligatorio | No enforced | Alta |
| HSTS | ✅ Incluir subdominios | No configurado | Alta |
| CSP | ✅ Estricto | No configurado | Alta |
| X-Frame-Options | ✅ DENY | ✅ Configurado | - |
| X-Content-Type-Options | ✅ nosniff | ✅ Configurado | - |
| Rate limiting | ✅ 5 req/s login | ✅ Bucket4j | - |

### 2.4 Stack de Deploy

**Plan Original:**
```
NGINX :80/:443
├── :3000 (WWW)
├── :3001 (APP)
├── :3002 (ADMIN)
└── :8080 (API)
```

**Arquitectura Implementada (Aislamiento Bancario):**
```
NGINX :80/:443
├── :3000 (Frontend Public - www + auth)
├── :3001 (Frontend Protected - app + admin)
└── :8080 (Backend Spring Boot - api)
```

| Componente | Puerto | Aislamiento |
|------------|--------|-------------|
| Frontend Public | :3000 | www.fatrans.com, auth.fatrans.com |
| Frontend Protected | :3001 | app.fatrans.com, admin.fatrans.com |
| Backend API | :8080 | Solo via nginx |

Docker para PostgreSQL, Redis, MinIO y frontends.
No se requiere PM2 - Docker compose maneja los contenedores.

---

## 3. Hallazgos Críticos de Seguridad

### 3.1 CORS Configuration - CRÍTICO

**Archivo:** `backend/src/main/java/com/tufondo/auth/infrastructure/security/SecurityConfig.java:85`

```java
// ACTUAL - LÍNEAS 85-91
String envOrigins = System.getenv().getOrDefault("CORS_ORIGINS",
    "http://localhost:3000,http://localhost:18081,http://localhost:8081");
```

**Problema:** Los valores por defecto incluyen `localhost`. En producción, si `CORS_ORIGINS` no está configurado correctamente, aceptará orígenes inseguros.

**Acción Requerida:**
```bash
CORS_ORIGINS="https://auth.fatrans.com,https://app.fatrans.com,https://admin.fatrans.com,https://www.fatrans.com"
```

### 3.2 Cookies sin Flags de Seguridad

**Problema:** No se observan en SecurityConfig los flags `Secure` y `HttpOnly` para cookies JWT.

**Acción Requerida:** Agregar configuración de cookies en SecurityConfig:
```java
http.cookie(cookie -> cookie
    .httpOnly(true)
    .secure(true)
    .sameSite("Strict")
);
```

### 3.3 DDL Auto: Update - CRÍTICO

**Archivo:** `backend/src/main/resources/application.yml:19`

```yaml
jpa:
  hibernate:
    ddl-auto: update  # ❌ NUNCA en producción
```

**Riesgo:** Hibernate puede modificar el esquema de BD automáticamente. En producción debe ser `validate` o `migrate` (con Flyway).

**Acción Requerida:** Cambiar a `ddl-auto: validate` y usar Flyway para migraciones.

### 3.4 MinIO Credentials Default

**Archivo:** `backend/src/main/resources/application.yml:59-60`

```yaml
minio:
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin123}
```

**Problema:** Credenciales por defecto visibles en código.

**Acción Requerida:** Forzar que estas variables de entorno sean obligatorias, sin defaults.

### 3.5 Rate Limiting Login

**Estado:** ✅ Implementado con Bucket4j en Redis

```
/api/v1/auth/login → 5 requests/minuto
/api/v1/auth/registro → 3 requests/minuto
```

Esto es correcto, pero el plan menciona "5 req/s" (por segundo), cuando en realidad son 5 por minuto.

---

## 4. Checklist de Pre-Deploy

### 4.1 Seguridad Obligatoria

| # | Item | Estado | Acción |
|---|------|--------|--------|
| 1 | Rotar JWT_SECRET | ⚠️ configurable | Generar nuevo, 256+ bits |
| 2 | Rotar DB_PASS | ⚠️ configurable | Generar password fuerte |
| 3 | Rotar REDIS_PASS | ⚠️ configurable | Generar password fuerte |
| 4 | CORS_ORIGINS producción | ❌ localhost | Configurar solo dominios fatrans.com |
| 5 | HttpOnly cookies | ❌ No visible | Agregar en SecurityConfig |
| 6 | Secure cookies | ❌ No visible | Agregar en SecurityConfig |
| 7 | ddl-auto: validate | ❌ update | Cambiar en application.yml |
| 8 | MinIO credenciales | ⚠️ con defaults | Remover defaults, forzar env |
| 9 | Firmar digital keystore | ⚠️ configurable | Crear en producción |

### 4.2 Headers de Seguridad

| Header | Estado | Notas |
|--------|--------|-------|
| Strict-Transport-Security | ❌ | Agregar en Nginx |
| Content-Security-Policy | ❌ | Agregar en Nginx |
| X-Frame-Options | ✅ | DENY |
| X-Content-Type-Options | ✅ | nosniff |
| X-Robots-Tag | ❌ | noindex en /app y /admin |

### 4.3 Configuración Next.js

| Item | Estado | Acción |
|------|--------|--------|
| next.config.js | ❌ No existe | Crear con removeConsole |
| Environment vars producción | ⚠️ Parcial | Completar NEXT_PUBLIC_* |
| Build production | ⚠️ No probado | Ejecutar `npm run build` |

---

## 5. Backup y Recuperación

### 5.1 PostgreSQL

**Estado:** ❌ No hay script de backup

**Requerido:**
- Backup diario automático (pg_dump)
- Retención de 30 días
- Backup en ubicación geográfica diferente
- Test de restauración quarterly

### 5.2 MinIO (Object Storage)

**Estado:** ⚠️ Datos en volumen Docker

**Requerido:**
- Replication cross-region (opcional pero recomendado)
- Lifecycle policies para expirear documentos temporales

---

## 6. Scripts Requeridos para Producción

```bash
# /opt/fatrans/scripts/
├── backup-postgres.sh      # pg_dump diario
├── backup-minio.sh         # mc mirror
├── ssl-renew.sh            # Certbot renew
├── health-check.sh         # Monitoring
└── restore-postgres.sh     # Restauración
```

---

## 7. Resumen de Cambios Necesarios

### Alta Prioridad (Bloqueantes)

1. **SecurityConfig.java** - Agregar Secure + HttpOnly cookies
2. **application.yml** - Cambiar `ddl-auto: update` → `validate`
3. **CORS_ORIGINS** - Configurar solo dominios producción
4. **MinIO credentials** - Remover defaults en application.yml
5. **SSH hardening** - Claves + puerto no estándar

### Prioridad Media

6. **next.config.js** - Crear con removeConsole y producción
7. **Nginx config** - Headers HSTS, CSP, reverse proxy
8. **SSL certificates** - Let's Encrypt wildcard
9. **Backup scripts** - PostgreSQL y MinIO

### Buena Práctica

10. **Unattended upgrades** - Habilitar en VPS
11. **Monitoring** - Logs centralizados, alertas
12. **X-Robots-Tag** - noindex en app/admin

---

## 8. Compatibilidad del Plan Original

| Aspecto | Veredicto |
|---------|-----------|
| Arquitectura subdominios | ✅ Válido con ajuste de ports |
| Seguridad servidor | ✅ Correcto |
| Nginx + SSL | ✅ Correcto |
| Docker stack | ✅ Válido |
| Orden implementación | ✅ Correcto |

**Calificación General:** 8/10 - El plan es bueno, requiere ajustes en configuración de seguridad específicos del código.

---

*Documento generado para revisión del plan de deploy de Fatrans.*
