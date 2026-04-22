# Issue #47 - StorageService para Tokens en Flutter Web

**Proyecto:** Plataforma Fondo de Ahorro  
**Issue:** #47 - StorageService para tokens en Flutter Web  
**Fecha:** 2026-04-20  
**Estado:** ✅ Implementado  
**Módulo:** Auth (Backend Java/Spring Boot + Frontend Flutter)

---

## Resumen

Se implementó autenticación con cookies `httpOnly` para Flutter Web, eliminando el riesgo de XSS en tokens almacenados en `localStorage`. Anteriormente, los tokens JWT se almacenaban directamente en `localStorage`, lo que exponía la aplicación a ataques de Cross-Site Scripting (XSS).

---

## 1. Problema Técnico

### 1.1 Vulnerabilidad Anterior

```
┌─────────────────────────────────────────────────────────────────┐
│                 ARQUITECTURA ANTERIOR (VULNERABLE)               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Flutter Web                                                    │
│  ┌─────────────────┐                                            │
│  │ localStorage    │ ← Tokens almacenados en texto plano        │
│  │ access_token    │   (VULNERABLE A XSS)                       │
│  │ refresh_token   │                                            │
│  └────────┬────────┘                                            │
│           │                                                      │
│           ▼ JavaScript accesible                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Un atacante podría ejecutar JavaScript malicioso para     │   │
│  │ robar los tokens del usuario via XSS                      │   │
│  └─────────────────────────────────────────────────────────┘   │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Solución Implementada

```
┌─────────────────────────────────────────────────────────────────┐
│                 ARQUITECTURA NUEVA (SEGURA)                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  Browser                                                       │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Cookies httpOnly                       │   │
│  │  ┌──────────────────┐  ┌──────────────────────────┐      │   │
│  │  │  access_token    │  │  refresh_token          │      │   │
│  │  │  (15 min)        │  │  (7 días, path=/refresh) │      │   │
│  │  └──────────────────┘  └──────────────────────────┘      │   │
│  │       ▲ NO ACCESO DESDE JavaScript                        │   │
│  └───────┼───────────────────────────────────────────────────┘   │
│          │                                                        │
│          ▼                                                        │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ JwtAuthenticationFilter extrae token desde cookie          │ │
│  │ y valida el JWT                                            │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                 │
│  Backend (Java/Spring Boot)                                     │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ AuthController                                            │  │
│  │ • login-web:  Crea cookies httpOnly                      │  │
│  │ • refresh-web: Renueva cookies + token rotation          │  │
│  │ • logout-web:  Limpia cookies                            │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 2. Arquitectura de Flujo de Autenticación

### 2.1 Diagrama de Flujo Completo

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         FLUJO DE AUTENTICACIÓN WEB                           │
└─────────────────────────────────────────────────────────────────────────────┘

1. LOGIN WEB
═════════════
┌──────────┐         ┌─────────────────┐         ┌────────────────────────┐
│  Flutter  │         │   AuthController │         │      Browser          │
│    Web    │         │   login-web      │         │                        │
└─────┬──────┘         └────────┬────────┘         └──────────┬───────────┘
      │ POST /auth/login-web    │                            │
      │ {identificador, pass}   │                            │
      │────────────────────────▶│                            │
      │                          │                            │
      │                          │ ┌─ ResponseCookie ──────┐│
      │                          │ │ access_token           ││
      │                          │ │ • httpOnly(true)       ││
      │                          │ │ • secure(true)         ││
      │                          │ │ • sameSite("Strict")   ││
      │                          │ │ • path("/")            ││
      │                          │ └───────────────────────┘│
      │                          │                            │
      │                          │ ┌─ ResponseCookie ──────┐│
      │                          │ │ refresh_token          ││
      │                          │ │ • httpOnly(true)       ││
      │                          │ │ • secure(true)         ││
      │                          │ │ • sameSite("Strict")   ││
      │                          │ │ • path(/refresh-web)   ││
      │                          │ └───────────────────────┘│
      │                          │                            │
      │◀── Response + Headers ───┤                            │
      │   X-User-Id: uuid        │                            │
      │   X-User-Rol: SOCIO      │                            │
      │   Set-Cookie: access_... │◀── Cookies almacenadas     │
      │   Set-Cookie: refresh_.. │     (NO accesibles via     │
      │                          │      JavaScript)           │
      │                          │                            │

2. REQUEST AUTENTICADO
════════════════════════
┌──────────┐         ┌─────────────────────┐         ┌────────────────────┐
│  Flutter  │         │ JwtAuthentication    │         │      Browser       │
│    Web    │         │      Filter          │         │                    │
└─────┬──────┘         └──────────┬──────────┘         └─────────┬─────────┘
      │ GET /api/v1/socios        │                              │
      │───────────────────────────▶│                              │
      │                           │ Cookie: access_token ◀──────│
      │                           │ (extraído automáticamente)  │
      │                           │                              │
      │                           │ Valida JWT                   │
      │                           │ Extrae usuarioId, rol        │
      │                           │                              │
      │◀── 200 OK ────────────────│                              │
      │   { datos... }            │                              │

3. TOKEN REFRESH
══════════════════
┌──────────┐         ┌─────────────────────┐         ┌────────────────────┐
│  Flutter  │         │   AuthController    │         │      Browser       │
│    Web    │         │   refresh-web       │         │                    │
└─────┬──────┘         └──────────┬──────────┘         └─────────┬─────────┘
      │ POST /auth/refresh-web     │                              │
      │───────────────────────────▶│                              │
      │                           │ Cookie: refresh_token ◀──────│
      │                           │                              │
      │                           │ Token Rotation:               │
      │                           │ 1. Invalida sesión anterior   │
      │                           │ 2. Genera nuevos tokens      │
      │                           │ 3. Crea nueva sesión          │
      │                           │                              │
      │◀── Response + new Cookies─│                              │
      │   Set-Cookie: access_... │◀── Nuevas cookies             │
      │   Set-Cookie: refresh_..│                              │
      │                          │                              │

4. LOGOUT
═════════
┌──────────┐         ┌─────────────────────┐         ┌────────────────────┐
│  Flutter  │         │   AuthController    │         │      Browser       │
│    Web    │         │   logout-web        │         │                    │
└─────┬──────┘         └──────────┬──────────┘         └─────────┬─────────┘
      │ POST /auth/logout-web     │                              │
      │──────────────────────────▶│                              │
      │                           │                              │
      │                           │ Invalida sesión en BD         │
      │                           │                              │
      │                           │ ┌─ ResponseCookie ──────┐    │
      │                           │ │ access_token (maxAge=0)│───│──▶ ELIMINADA
      │                           │ └───────────────────────┘    │
      │                           │ ┌─ ResponseCookie ──────┐   │
      │                           │ │ refresh_token (maxAge=0)│───│──▶ ELIMINADA
      │                           │ └───────────────────────┘   │
      │◀── 200 OK ───────────────│                              │
```

---

## 3. Endpoints API Nuevos

### 3.1 POST /api/v1/auth/login-web

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

**Headers de respuesta:**
```
HTTP/1.1 200 OK
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
| CUENTA_BLOQUEADA | 403 | Cuenta bloqueada por intentos fallidos |
| CUENTA_DESACTIVADA | 403 | Cuenta desactivada |

---

### 3.2 POST /api/v1/auth/refresh-web

**Descripción:** Refresca los tokens usando el cookie `httpOnly` y realiza token rotation.

**Autenticación:** No requerida (usa cookie)

#### Request

```http
POST /api/v1/auth/refresh-web
Cookie: refresh_token=eyJhbGci...
```

#### Response - 200 OK

**Headers de respuesta:**
```
HTTP/1.1 200 OK
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
| TOKEN_INVALIDO | 401 | Refresh token no válido o sesión no existe |
| TOKEN_EXPIRADO | 401 | Refresh token expirado |

---

### 3.3 POST /api/v1/auth/logout-web

**Descripción:** Invalida la sesión del usuario y elimina las cookies.

**Autenticación:** No requerida (usa cookie)

#### Request

```http
POST /api/v1/auth/logout-web
Cookie: access_token=eyJhbGci...
```

#### Response - 200 OK

**Headers de respuesta:**
```
HTTP/1.1 200 OK
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

## 4. Configuración de Seguridad

### 4.1 Cookies - Flags de Seguridad

| Flag | Valor | Propósito |
|------|-------|-----------|
| `HttpOnly` | `true` | JavaScript no puede acceder al cookie (previene XSS) |
| `Secure` | `true` | Cookie solo se envía por HTTPS |
| `SameSite` | `Strict` | Cookie no se envía en requests cross-site (previene CSRF) |
| `Path` | `/` (access), `/refresh-web` (refresh) | Limita el alcance del cookie |

### 4.2 Variables de Entorno

```bash
# Backend - Orígenes CORS permitidos
CORS_ORIGINS=http://localhost:3000,http://localhost:18081,http://localhost:8081

# Producción (ejemplo):
CORS_ORIGINS=https://app.fondoahorro.com,https://admin.fondoahorro.com
```

### 4.3 Duración de Tokens

| Token | Duración | Cookie Max-Age |
|-------|----------|----------------|
| Access Token | 15 minutos | 900 segundos |
| Refresh Token | 7 días | 604800 segundos |

---

## 5. Cambios en Backend (Java/Spring Boot)

### 5.1 Archivos Modificados

#### JwtAuthenticationFilter.java
**Ubicación:** `backend/src/main/java/com/tufondo/auth/infrastructure/security/JwtAuthenticationFilter.java`

**Cambios:**
- Nuevo método `extractTokenFromCookie()` para extraer token desde cookies
- Ahora intenta primero extraer del header `Authorization`, si no existe, busca en cookies
- El filtro ignora requests a `/api/v1/auth/**` (endpoints públicos)

```java
// Extracción dual: header primero, luego cookie
String token = extractTokenFromHeader(request);
if (token == null) {
    token = extractTokenFromCookie(request);
}

private String extractTokenFromCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) return null;
    return Arrays.stream(cookies)
            .filter(c -> ACCESS_TOKEN_COOKIE.equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
}
```

#### SecurityConfig.java
**Ubicación:** `backend/src/main/java/com/tufondo/auth/infrastructure/security/SecurityConfig.java`

**Cambios:**
- CORS corregido: `setAllowedOrigins(allowedOrigins)` en lugar de `setAllowedOriginPatterns("*")`
- `setExposedHeaders` solo expone `X-User-Id`, `X-User-Rol`, `X-Correlation-Id` (ya no Authorization)

```java
configuration.setExposedHeaders(List.of("X-User-Id", "X-User-Rol", "X-Correlation-Id"));
// ✅ Authorization header NO se expone (previene fuga de tokens)
```

#### AuthUseCase.java
**Ubicación:** `backend/src/main/java/com/tufondo/auth/application/usecase/AuthUseCase.java`

**Cambios:**
- Token rotation en refresh: invalida la sesión anterior usando `refreshTokenHash`
- Antes: `sesionRepository.invalidarPorTokenId(sesion.id().toString())`
- Ahora: `sesionRepository.invalidarPorRefreshToken(refreshTokenHash)` ✅

```java
// Token rotation: invalidar sesión anterior usando el refresh token
sesionRepository.invalidarPorRefreshToken(refreshTokenHash);
```

#### AuthController.java
**Ubicación:** `backend/src/main/java/com/tufondo/auth/infrastructure/presentation/controller/AuthController.java`

**Endpoints nuevos:**
| Método | Path | Descripción |
|--------|------|-------------|
| POST | `/login-web` | Login con cookies httpOnly |
| POST | `/logout-web` | Logout que limpia cookies |
| POST | `/refresh-web` | Refresh con cookie y token rotation |

**Creación de cookies:**
```java
ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, login.accessToken())
        .httpOnly(true)
        .secure(true)
        .path("/")
        .maxAge(ACCESS_TOKEN_MAX_AGE)
        .sameSite("Strict")
        .build();
```

#### LoginWebResponseDTO.java (NUEVO)
**Ubicación:** `backend/src/main/java/com/tufondo/auth/application/dto/LoginWebResponseDTO.java`

**Propósito:** DTO para respuestas web sin tokens (info usuario via headers)

```java
public record LoginWebResponseDTO(
        String id,
        String nombreUsuario,
        String correoElectronico,
        String nombreCompleto,
        String rol
) {}
```

---

## 6. Cambios en Frontend (Flutter)

### 6.1 Archivos Modificados

#### token_storage_service.dart (NUEVO)
**Ubicación:** `frontend-mobile/lib/core/auth/services/token_storage_service.dart`

**Propósito:** Interfaz unificada para almacenamiento de tokens con fallback según plataforma.

```dart
abstract class TokenStorageService {
  Future<void> saveTokens(AuthToken tokens);
  Future<AuthToken?> getTokens();
  Future<void> clearTokens();
  Future<void> saveUser(User user);
  Future<User?> getUser();
  Future<void> clearUser();
  Future<void> clearAll();
}

class SecureTokenStorageService implements TokenStorageService {
  // Web: usa html.window.localStorage
  // Mobile: usa FlutterSecureStorage
  
  bool get isWeb => kIsWeb;
  
  @override
  Future<void> saveTokens(AuthToken tokens) async {
    if (kIsWeb) {
      _saveWeb(_accessTokenKey, tokens.accessToken);
      _saveWeb(_refreshTokenKey, tokens.refreshToken);
    } else {
      await _secureStorage.write(key: _accessTokenKey, value: tokens.accessToken);
      // ...
    }
  }
}
```

#### auth_remote_datasource.dart
**Ubicación:** `frontend-mobile/lib/core/auth/datasources/auth_remote_datasource.dart`

**Métodos agregados:**
| Método | Descripción |
|--------|-------------|
| `loginWeb()` | POST a `/auth/login-web` |
| `refreshTokenWeb()` | POST a `/auth/refresh-web` |
| `logoutWeb()` | POST a `/auth/logout-web` |

**Ejemplo loginWeb:**
```dart
@override
Future<User> loginWeb({
  required String identificador,
  required String password,
}) async {
  final response = await apiClient.post(
    ApiEndpoints.authLoginWeb,
    data: {'identificador': identificador, 'password': password},
  );

  final headers = response.headers;
  final userId = headers.value('X-User-Id') ?? '';
  final userRol = headers.value('X-User-Rol') ?? '';
  final data = response.data as Map<String, dynamic>;

  return User(
    id: userId.isNotEmpty ? userId : data['id']?.toString() ?? '',
    // ...
  );
}
```

#### auth_repository.dart
**Ubicación:** `frontend-mobile/lib/core/auth/repositories/auth_repository.dart`

**Métodos agregados en interfaz:**
```dart
Future<Either<Failure, User>> loginWeb({
  required String identificador,
  required String password,
});

Future<Either<Failure, User>> refreshTokenWeb();

Future<Either<Failure, void>> logoutWeb();
```

#### auth_repository_impl.dart
**Ubicación:** `frontend-mobile/lib/core/auth/repositories/auth_repository_impl.dart`

**Implementación destacada:**
- `kIsWeb` detecta si es web para usar endpoints específicos
- `isAuthenticated()` en web solo verifica que exista usuario (no tokens)

```dart
@override
Future<bool> isAuthenticated() async {
  if (kIsWeb) {
    final user = await localDataSource.getUser();
    return user != null;  // ✅ En web, solo verifica usuario
  }
  
  final tokens = await localDataSource.getTokens();
  return tokens != null && !tokens.isExpired;
}
```

#### api_endpoints.dart
**Ubicación:** `frontend-mobile/lib/core/constants/api_endpoints.dart`

**Endpoints agregados:**
```dart
static const String authLoginWeb = '/auth/login-web';
static const String authRefreshWeb = '/auth/refresh-web';
static const String authLogoutWeb = '/auth/logout-web';
```

#### user.dart
**Ubicación:** `frontend-mobile/lib/core/auth/entities/user.dart`

**Cambio en `isAdmin`:**
```dart
bool get isAdmin => rol == 'ADMIN' || rol == 'administrador' || rol == 'GESTOR' || rol == 'GESTOR';
// ✅ Se agregó 'GESTOR' al check de admin
```

---

## 7. Token Rotation - Detalle de Implementación

### 7.1 Flujo de Refresh con Rotation

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    TOKEN ROTATION EN REFRESH WEB                            │
└─────────────────────────────────────────────────────────────────────────────┘

1. Solicitud received
   └─ refreshToken desde cookie httpOnly

2. Validación
   ├─ Hash del refresh token → search in DB
   └─ Verificar expiración

3. INVALIDACIÓN de sesión anterior ⚠️ CLAVE
   └─ sesionRepository.invalidarPorRefreshToken(refreshTokenHash)
      └─ UPDATE SesionEntity SET activo = false WHERE refreshTokenHash = :hash

4. Generación de NUEVOS tokens
   ├─ Generar access token (15 min)
   └─ Generar refresh token (7 días)

5. Crear NUEVA sesión
   └─ Sesion con nuevo refreshTokenHash

6. Respuesta con nuevas cookies
   ├─ access_token cookie (nuevo)
   └─ refresh_token cookie (nuevo)

RESULTADO: Un refresh token robado es INUTILIZABLE después de su uso
          (ya que la sesión anterior fue invalidada)
```

### 7.2 Seguridad del Refresh Token

| Aspecto | Protección |
|---------|-----------|
| Almacenamiento | Hash Argon2 en BD (nunca texto plano) |
| Transmisión | Solo en cookies httpOnly (no JavaScript) |
| Rotation | Invalida sesión anterior en cada refresh |
| Exposición | Si es robado, se invalida al usarse |

---

## 8. Uso en Flutter Web

### 8.1 Detección Automática

El `AuthRepositoryImpl` detecta automáticamente si está en web:

```dart
@override
Future<Either<Failure, User>> login({
  required String identificador,
  required String password,
}) async {
  try {
    if (kIsWeb) {
      return loginWeb(identificador: identificador, password: password);
    }
    // ... mobile login
  }
}
```

### 8.2 Uso Manual (si se requiere control específico)

```dart
// En una pantalla de login
final authRepository = ref.read(authRepositoryProvider);

final result = await authRepository.loginWeb(
  identificador: emailController.text,
  password: passwordController.text,
);

result.fold(
  (failure) => showError(failure.mensaje),
  (user) => navigateToHome(user),
);
```

### 8.3 Logout en Web

```dart
// En cualquier parte de la app
final result = await authRepository.logoutWeb();

result.fold(
  (failure) => debugPrint('Logout falló: ${failure.mensaje}'),
  (_) => navigateToLogin(),
);
```

---

## 9. Comparación: Anterior vs Nuevo

### 9.1 Almacenamiento de Tokens

| Aspecto | Anterior (localStorage) | Nuevo (Cookies httpOnly) |
|---------|-------------------------|---------------------------|
| **Acceso JS** | ✅ Lectura directa | ❌ No accesible |
| **XSS Risk** | 🔴 Alto | ✅ Mitigado |
| **CSRF Protection** | ❌ No tiene | ✅ sameSite=Strict |
| **Transmisión** | Header Authorization | Automática en cookies |
| **Mobile** | ✅ Funciona | ✅ Funciona |

### 9.2 Flujo de Login

| Paso | Anterior | Nuevo |
|------|----------|-------|
| 1 | POST /auth/login | POST /auth/login-web |
| 2 | Recibe tokens en body | Recibe cookies + headers |
| 3 | Guarda en localStorage | Cookies automatizadas |
| 4 | Incluye token en cada request | Cookie enviado automáticamente |

---

## 10. Archivos Afectados - Resumen

### Backend (Java)

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `JwtAuthenticationFilter.java` | Modificado | Extracción desde cookie |
| `SecurityConfig.java` | Modificado | CORS corregido, exposed headers |
| `AuthUseCase.java` | Modificado | Token rotation con refreshTokenHash |
| `AuthController.java` | Modificado | 3 nuevos endpoints web |
| `LoginWebResponseDTO.java` | Nuevo | DTO sin tokens para web |

### Frontend (Flutter)

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `token_storage_service.dart` | Nuevo | Abstacción de storage |
| `auth_remote_datasource.dart` | Modificado | Métodos web |
| `auth_repository.dart` | Modificado | Interfaz con métodos web |
| `auth_repository_impl.dart` | Modificado | Implementación con kIsWeb |
| `api_endpoints.dart` | Modificado | 3 endpoints web |
| `user.dart` | Modificado | isAdmin incluye GESTOR |

---

## 11. Notas de Implementación

### 11.1 Desarrollo Local (HTTP)

⚠️ **Problema conocido:** Las cookies con `secure(true)` NO funcionarán en `http://localhost`.

**Solución temporal:** En desarrollo local, el navegador simplemente no almacenará las cookies. La aplicación seguirá funcionando pero sin persistencia de sesión entre requests.

**Alternativa:** Usar un navegador que permita cookies seguras en localhost o deshabilitar temporalmente `secure` en desarrollo.

### 11.2 Path del Refresh Cookie

El cookie de refresh token usa path `/api/v1/auth/refresh-web` para limitar su envío solo al endpoint que lo consume. Esto cumple con el principio de mínimo privilegio.

### 11.3 Headers X-User-Id y X-User-Rol

Estos headers se usan para que Flutter Web pueda obtener la información del usuario inmediatamente después del login, sin necesidad de hacer una llamada adicional a `/auth/me`.

---

## 12. Historial de Cambios

| Fecha | Agente | Descripción |
|-------|--------|-------------|
| 2026-04-20 | @documentador | Documentación inicial de Issue #47 |
| 2026-04-20 | @auditoria | Corrección semántica en token rotation (refreshTokenHash) |

---

## 13. Referencias

- SPEC.md del módulo Auth: `/docs/modulos/auth/SPEC.md`
- API.md del módulo Auth: `/docs/modulos/auth/API.md`
- Auditoría de seguridad: `/docs/auditorias/auditoria_20260420_000000.md`
- Guía arquitectura Flutter: `/docs/frontend/FLUTTER_ARCHITECTURE_GUIDE.md`
