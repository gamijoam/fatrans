# Módulo AUTH - Referencia de API

**Proyecto:** Plataforma Fondo de Ahorro  
**Versión:** 1.0  
**Fecha:** 2026-04-14

---

## Resumen

Documentación completa de los **9 endpoints** del módulo AUTH con ejemplos de request/response, códigos de error y notas de seguridad. Incluye autenticación JWT, gestión de sesiones y recuperación de contraseñas.

---

## Base URL

```
Production: https://api.fondoahorro.com/v1
Development: http://localhost:8080/api/v1
```

---

## Autenticación

Los endpoints `/login`, `/refresh`, `/recuperar-password` y `/reset-password` son públicos (no requieren autenticación).

Todos los demás endpoints requieren:

```
Authorization: Bearer <jwt_token>
```

---

## Endpoints del Módulo AUTH

### 1. POST /auth/login - Iniciar Sesión

**Descripción:** Autentica un usuario y devuelve tokens JWT.

**Autenticación:** No requerida

#### Request

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "identificador": "usuario@ejemplo.com",
  "password": "ContraseñaSegura123!"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| identificador | String | Sí | 3-100 caracteres | Nombre de usuario o correo electrónico |
| password | String | Sí | Mín 8 chars, 1 mayúscula, 1 minúscula, 1 número, 1 carácter especial | Contraseña |

#### Response - 200 OK

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "usuario": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "nombreUsuario": "usuario",
    "correoElectronico": "usuario@ejemplo.com",
    "nombreCompleto": "Juan Pérez",
    "rol": "SOCIO"
  }
}
```

**Nota:** `expiresIn` está en segundos (900 = 15 minutos)

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| CREDENCIALES_INVALIDAS | 401 | Usuario o contraseña incorrectos |
| CUENTA_BLOQUEADA | 403 | Cuenta bloqueada por intentos fallidos |
| CUENTA_DESACTIVADA | 403 | Cuenta desactivada |
| RATE_LIMIT_EXCEDIDO | 429 | Demasiadas solicitudes |

---

### 2. POST /auth/refresh - Refrescar Tokens

**Descripción:** Genera nuevos tokens JWT usando un refresh token válido.

**Autenticación:** No requerida

#### Request

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### Request Schema

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| refreshToken | String | Sí | Refresh token JWT válido |

#### Response - 200 OK

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "usuario": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "nombreUsuario": "usuario",
    "correoElectronico": "usuario@ejemplo.com",
    "nombreCompleto": "Juan Pérez",
    "rol": "SOCIO"
  }
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| TOKEN_EXPIRADO | 401 | Refresh token expirado |
| TOKEN_INVALIDO | 401 | Refresh token inválido |
| SESION_NO_ENCONTRADA | 404 | Sesión no existe o ya fue invalidada |

---

### 3. POST /auth/logout - Cerrar Sesión

**Descripción:** Invalida todas las sesiones del usuario actual.

**Autenticación:** Sí (Bearer token)

#### Request

```http
POST /api/v1/auth/logout
Authorization: Bearer <access_token>
```

#### Response - 200 OK

```json
{
  "mensaje": "Sesión cerrada correctamente"
}
```

**Nota:** Esta operación invalida TODAS las sesiones activas del usuario, no solo la actual.

---

### 4. GET /auth/me - Obtener Usuario Actual

**Descripción:** Obtiene la información del usuario autenticado.

**Autenticación:** Sí (Bearer token)

#### Request

```http
GET /api/v1/auth/me
Authorization: Bearer <access_token>
```

#### Response - 200 OK

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombreUsuario": "usuario",
  "correoElectronico": "usuario@ejemplo.com",
  "nombreCompleto": "Juan Pérez",
  "rol": "SOCIO"
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| TOKEN_INVALIDO | 401 | Token JWT inválido |
| TOKEN_EXPIRADO | 401 | Token JWT expirado |

---

### 5. POST /auth/validar - Validar Token

**Descripción:** Valida un token JWT y devuelve su información.

**Autenticación:** Sí (Bearer token)

#### Request

```http
POST /api/v1/auth/validar
Authorization: Bearer <access_token>
```

#### Response - 200 OK

```json
{
  "usuarioId": "550e8400-e29b-41d4-a716-446655440000",
  "nombreUsuario": "usuario",
  "correoElectronico": "usuario@ejemplo.com",
  "rol": "SOCIO",
  "expiracion": "2026-04-14T15:15:00Z",
  "valido": true
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| TOKEN_INVALIDO | 401 | Token JWT inválido |
| TOKEN_EXPIRADO | 401 | Token JWT expirado |

---

### 6. POST /auth/crear-usuario - Crear Usuario (Admin)

**Descripción:** Crea un usuario vinculado a un socio existente.

**Autenticación:** Sí (Bearer token, rol ADMIN)

#### Request

```http
POST /api/v1/auth/crear-usuario
Content-Type: application/json
Authorization: Bearer <admin_token>

{
  "socioId": "330e8400-e29b-41d4-a716-446655440004",
  "correoElectronico": "juan@empresa.com",
  "nombreCompleto": "Juan Pérez García",
  "rol": "SOCIO"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| socioId | UUID | Sí | UUID válido | ID del socio |
| correoElectronico | String | Sí | Formato email | Correo electrónico |
| nombreCompleto | String | Sí | Max 100 chars | Nombre completo |
| rol | Enum | Sí | SOCIO, ADMIN, SUPER_ADMIN | Rol del usuario |

#### Response - 201 Created

```json
{
  "id": "220e8400-e29b-41d4-a716-446655440008",
  "socioId": "330e8400-e29b-41d4-a716-446655440004",
  "nombreUsuario": "juan.perez",
  "correoElectronico": "juan@empresa.com",
  "nombreCompleto": "Juan Pérez García",
  "rol": "SOCIO",
  "mensaje": "Usuario creado exitosamente. Las credenciales fueron enviadas al correo."
}
```

**Nota:** El sistema genera automáticamente:
- Nombre de usuario (ej: `juan.perez`)
- Contraseña temporal (12 caracteres, aleatoria)
- Envía email con las credenciales

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| SOCIO_NO_ENCONTRADO | 404 | Socio no existe |
| SOCIO_YA_TIENE_USUARIO | 409 | El socio ya tiene usuario vinculado |
| NOMBRE_USUARIO_EXISTE | 409 | El nombre de usuario ya existe |
| CORREO_YA_EXISTE | 409 | El correo electrónico ya está registrado |

---

### 7. POST /auth/recuperar-password - Solicitar Recuperación

**Descripción:** Solicita un enlace de recuperación de contraseña.

**Autenticación:** No requerida

#### Request

```http
POST /api/v1/auth/recuperar-password
Content-Type: application/json

{
  "correoElectronico": "usuario@ejemplo.com"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| correoElectronico | String | Sí | Formato email | Correo electrónico registrado |

#### Response - 200 OK

```json
{
  "mensaje": "Si el correo existe en nuestro sistema, recibirás un enlace de recuperación."
}
```

**Nota de seguridad:** Siempre retorna el mismo mensaje, tanto si el correo existe como si no, para evitar enumeration attacks.

#### Flujo interno

1. Busca usuario por correo electrónico
2. Si existe:
   - Genera token de recuperación (hash almacenado)
   - Envía email con link de recuperación: `https://app.fondoahorro.com/reset-password?token=xxx`
3. Si no existe: Retorna el mismo mensaje (sin revelar si el email existe)

---

### 8. POST /auth/reset-password - Restablecer Contraseña

**Descripción:** Restablece la contraseña usando un token de recuperación.

**Autenticación:** No requerida

#### Request

```http
POST /api/v1/auth/reset-password
Content-Type: application/json

{
  "token": "abc123def456...",
  "nuevaPassword": "NuevaContraseña123!",
  "confirmarPassword": "NuevaContraseña123!"
}
```

#### Request Schema

| Campo | Tipo | Requerido | Validaciones | Descripción |
|-------|------|-----------|--------------|-------------|
| token | String | Sí | Token válido de recuperación | Token de la URL |
| nuevaPassword | String | Sí | Mín 8 chars, 1 mayúscula, 1 minúscula, 1 número, 1 carácter especial | Nueva contraseña |
| confirmarPassword | String | Sí | Debe ser igual a nuevaPassword | Confirmación |

#### Response - 200 OK

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "mensaje": "Contraseña restablecida exitosamente"
}
```

**Nota:** El endpoint devuelve tokens nuevos para que el usuario pueda hacer login inmediatamente.

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| TOKEN_RECUPERACION_INVALIDO | 400 | Token inválido, expirado o ya usado |
| PASSWORD_INVALIDO | 400 | La contraseña no cumple requisitos |
| PASSWORD_NO_COINCIDE | 400 | Las contraseñas no coinciden |

---

### 9. DELETE /auth/sesiones - Invalidar Todas las Sesiones

**Descripción:** Invalida todas las sesiones activas del usuario (excepto la actual si se especifica).

**Autenticación:** Sí (Bearer token)

#### Request

```http
DELETE /api/v1/auth/sesiones
Authorization: Bearer <access_token>
```

#### Query Parameters

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| exceptuarActual | Boolean | No | false | Si true, mantiene la sesión actual activa |

#### Response - 200 OK

```json
{
  "sesionesInvalidadas": 3,
  "mensaje": "Todas las sesiones han sido invalidadas"
}
```

---

### 10. POST /auth/login-web - Login para Flutter Web

**Descripción:** Autentica un usuario y crea cookies `httpOnly` para Flutter Web.

**Autenticación:** No requerida

#### Request

```http
POST /api/v1/auth/login-web
Content-Type: application/json

{
  "identificador": "usuario@ejemplo.com",
  "password": "ContraseñaSegura123!"
}
```

#### Response - 200 OK

**Headers:**
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
X-User-Rol: SOCIO
Set-Cookie: access_token=eyJhbGci...; HttpOnly; Secure; Path=/; Max-Age=900; SameSite=Strict
Set-Cookie: refresh_token=eyJhbGci...; HttpOnly; Secure; Path=/api/v1/auth/refresh-web; Max-Age=604800; SameSite=Strict
```

**Body:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombreUsuario": "usuario",
  "correoElectronico": "usuario@ejemplo.com",
  "nombreCompleto": "Juan Pérez",
  "rol": "SOCIO"
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| CREDENCIALES_INVALIDAS | 401 | Usuario o contraseña incorrectos |
| CUENTA_BLOQUEADA | 403 | Cuenta bloqueada |
| CUENTA_DESACTIVADA | 403 | Cuenta desactivada |

---

### 11. POST /auth/refresh-web - Refrescar Tokens (Flutter Web)

**Descripción:** Refresca los tokens usando el cookie `httpOnly` y realiza token rotation.

**Autenticación:** No requerida (usa cookie)

#### Request

```http
POST /api/v1/auth/refresh-web
Cookie: refresh_token=eyJhbGci...
```

#### Response - 200 OK

**Headers:**
```
X-User-Id: 550e8400-e29b-41d4-a716-446655440000
X-User-Rol: SOCIO
Set-Cookie: access_token=eyJhbGci...; HttpOnly; Secure; Path=/; Max-Age=900; SameSite=Strict
Set-Cookie: refresh_token=eyJhbGci...; HttpOnly; Secure; Path=/api/v1/auth/refresh-web; Max-Age=604800; SameSite=Strict
```

**Body:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "nombreUsuario": "usuario",
  "correoElectronico": "usuario@ejemplo.com",
  "nombreCompleto": "Juan Pérez",
  "rol": "SOCIO"
}
```

#### Códigos de Error

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| TOKEN_INVALIDO | 401 | Refresh token no válido |
| TOKEN_EXPIRADO | 401 | Refresh token expirado |

---

### 12. POST /auth/logout-web - Logout (Flutter Web)

**Descripción:** Invalida la sesión y elimina las cookies `httpOnly`.

**Autenticación:** No requerida (usa cookie)

#### Request

```http
POST /api/v1/auth/logout-web
Cookie: access_token=eyJhbGci...
```

#### Response - 200 OK

**Headers:**
```
Set-Cookie: access_token=; HttpOnly; Secure; Path=/; Max-Age=0; SameSite=Strict
Set-Cookie: refresh_token=; HttpOnly; Secure; Path=/api/v1/auth/refresh-web; Max-Age=0; SameSite=Strict
```

**Body:**
```json
{
  "mensaje": "Sesión cerrada correctamente"
}
```

---

## Códigos de Error Comunes

| Código | HTTP Status | Descripción |
|--------|-------------|-------------|
| CREDENCIALES_INVALIDAS | 401 | Usuario o contraseña incorrectos |
| USUARIO_NO_ENCONTRADO | 404 | Usuario no existe |
| CUENTA_BLOQUEADA | 403 | Cuenta bloqueada por intentos fallidos |
| CUENTA_DESACTIVADA | 403 | Cuenta desactivada |
| TOKEN_EXPIRADO | 401 | Token JWT expirado |
| TOKEN_INVALIDO | 401 | Token JWT malformado o inválido |
| SESION_NO_ENCONTRADA | 404 | Sesión no existe |
| ERROR_INTERNO | 500 | Error interno del servidor |

---

## Rate Limiting

| Endpoint | Límite | Ventana |
|----------|--------|---------|
| POST /auth/login | 5 req/min | Por IP |
| POST /auth/recuperar-password | 3 req/min | Por IP |
| POST /auth/reset-password | 3 req/min | Por IP |

---

## Headers de Response

Todos los responses incluyen:

| Header | Descripción | Ejemplo |
|--------|-------------|---------|
| X-Request-Id | ID único de la request | `req_xyz789` |
| X-Response-Time | Tiempo de procesamiento (ms) | `45` |
| X-Correlation-Id | ID de correlación para logs | `corr_abc123` |

---

## Estructura de Respuestas de Error

```json
{
  "codigo": "CREDENCIALES_INVALIDAS",
  "mensaje": "Credenciales inválidas",
  "timestamp": "2026-04-14T15:00:00Z"
}
```

---

## Notas de Seguridad

### 9.1 Protección contra Fuerza Bruta

- Máximo 5 intentos fallidos de login por IP
- Bloqueo de 30 minutos al exceder límite
- Cada intento fallido se registra en auditoría

### 9.2 Seguridad en Refresh Tokens

- Los refresh tokens se almacenan hasheados en BD (Argon2)
- Nunca se almacenan en texto plano
- Un único refresh token puede invalidar todas las sesiones del usuario
- Token rotation: cada refresh invalida la sesión anterior

### 9.3 Fail-Fast en Configuración

Si `jwt.secret`, `jwt.accessToken` o `jwt.refreshToken` no están configurados, la aplicación **no arrancará**.

### 9.4 Cookies httpOnly para Flutter Web

Para Flutter Web, los tokens se transmiten via cookies httpOnly (no en headers):

| Cookie | Flags | Descripción |
|--------|-------|-------------|
| `access_token` | HttpOnly, Secure, SameSite=Strict | Access token (15 min) |
| `refresh_token` | HttpOnly, Secure, SameSite=Strict, Path=/refresh-web | Refresh token (7 días) |

Esta configuración mitiga ataques XSS al no permitir acceso JavaScript a los tokens.

---

## Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-14 | @documentador | Creación inicial - Reorganización de documentación |
| 1.1 | 2026-04-20 | @documentador | Issue #47: Endpoints login-web, refresh-web, logout-web con cookies httpOnly |

---

## Referencias

- Especificación técnica: `/docs/modulos/auth/SPEC.md`
- Modelo de datos: `/docs/modulos/auth/MODELO_DATOS.md`
- Issue #47 - Cookies httpOnly: `/docs/modulos/auth/ISSUE_47_COOKIES_HTTPONLY.md`
