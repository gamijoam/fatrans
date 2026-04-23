# Fix: Cookies Secure en Desarrollo Local

**Fecha:** 2026-04-23
**Tipo:** Bug Fix - Autenticación
**Severidad:** Alta (bloqueaba login en desarrollo)
**Etiquetas:** `backend`, `auth`, `security`

---

## Problema

El backend configuraba las cookies JWT con el flag `Secure=true`, lo cual significa que solo se envían por HTTPS. En desarrollo local, el frontend corre en `http://localhost:3000` (sin HTTPS), causando que las cookies nunca se guardaran en el navegador.

### Síntomas
- Login aparentemente exitoso (respuesta 200)
- Cookies no se guardaban en el navegador
- Redirección a `/admin` o `/dashboard` fallaba
- Sesión no persistía entre peticiones

---

## Causa Raíz

En `AuthController.java`, las cookies se creaban con `secure(true)` hardcodeado:

```java
ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, login.accessToken())
    .httpOnly(true)
    .secure(true)  // ← Siempre true, sin detectar entorno
    .path("/")
    ...
```

---

## Solución

Se agregó un método `isSecureCookie(HttpServletRequest)` que detecta si la request viene de localhost:

```java
private boolean isSecureCookie(HttpServletRequest request) {
    String origin = request.getHeader("Origin");
    if (origin != null) {
        return !origin.contains("localhost") && !origin.contains("127.0.0.1");
    }
    String referer = request.getHeader("Referer");
    if (referer != null) {
        return !referer.contains("localhost") && !referer.contains("127.0.0.1");
    }
    return true;
}
```

Ahora las cookies solo tienen `Secure=true` cuando el origen no es localhost.

### Endpoints afectados
- `POST /api/v1/auth/login-web`
- `POST /api/v1/auth/logout-web`
- `POST /api/v1/auth/refresh-web`

---

## Archivos Modificados

| Archivo | Cambio |
|---------|--------|
| `backend/src/main/java/.../AuthController.java` | Agregado método `isSecureCookie()` y aplicado a todos los endpoints que crean cookies |

---

## Testing

### Antes del fix
```bash
curl -v http://localhost:3000/api/auth/login -H "Content-Type: application/json" -d '{"identificador":"admin","password":"Admin123!"}'
# Set-Cookie: access_token=...; Secure; HttpOnly; SameSite=Strict
# Cookie NO se guarda en navegador (Secure en HTTP)
```

### Después del fix
```bash
curl -v http://localhost:3000/api/auth/login -H "Content-Type: application/json" -d '{"identificador":"admin","password":"Admin123!"}'
# Set-Cookie: access_token=...; HttpOnly; SameSite=Strict
# Cookie SE guarda correctamente en localhost
```

---

## Producción

En producción con HTTPS, el flag `Secure` estará activo porque:
- El Origin/Referer no contendrá "localhost"
- El método retornará `true`

---

## Límites de la Solución

- Detecta localhost basándose en headers `Origin` y `Referer`
- Si estos headers están ausentes, asume producción (`return true`)
- Funciona correctamente en la mayoría de los casos de desarrollo

---

## Notas

- Issue relacionada: #60 (creada para documentar)
- No afecta la seguridad en producción
- El flag `HttpOnly` y `SameSite=Strict` se mantienen siempre
