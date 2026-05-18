# REMINDER: Secure Cookies en Producción

**Fecha:** 2026-04-23
**Tipo:** Configuración de Seguridad - REVISAR ANTES DE PRODUCCIÓN
**Etiquetas:** `security`, `production`, `critical`

---

## ⚠️ IMPORTANTE - REMOVER ANTES DE PRODUCCIÓN

El fix actual (`isSecureCookie`) está configurado para desarrollo local. **DEBE SER REMOVIDO O CORREGIDO** antes de desplegar a producción.

### Problema
En producción con HTTPS, las cookies **deben** tener `Secure=true` para protegerse contra ataques MITM.

### Situación Actual (Desarrollo)
```java
private boolean isSecureCookie(HttpServletRequest request) {
    // Detecta localhost y retorna false para permitir cookies en HTTP
    // ESTO ES SOLO PARA DESARROLLO
}
```

### Situación Requerida (Producción)
```java
private boolean isSecureCookie(HttpServletRequest request) {
    return true; // Siempre true en producción
}
```

O mejor, usar una variable de entorno:
```java
private boolean isSecureCookie(HttpServletRequest request) {
    return !isDevelopmentEnvironment();
}
```

---

## Checklist Antes de Producción

- [ ] **REMOVER** la lógica de `isSecureCookie()` que detecta localhost
- [ ] **HABILITAR** `Secure=true` en todas las cookies
- [ ] **VERIFICAR** que el frontend usa HTTPS
- [ ] **TESTEAR** login/logout/refresh en HTTPS
- [ ] **ACTUALIZAR** CORS para solo permitir origins HTTPS

---

## Endpoints Afectados

| Endpoint | Cookie | Flags en Producción |
|----------|--------|-------------------|
| `POST /login-web` | `access_token` | `HttpOnly; Secure; SameSite=Strict` |
| `POST /login-web` | `refresh_token` | `HttpOnly; Secure; SameSite=Strict; Path=/refresh-web` |
| `POST /logout-web` | (clear) | `HttpOnly; Secure; SameSite=Strict; Max-Age=0` |
| `POST /refresh-web` | (renew) | `HttpOnly; Secure; SameSite=Strict` |

---

## Issue Relacionada

- #60: Fix temporal para desarrollo local

---

## Archivo

`backend/src/main/java/.../AuthController.java`
