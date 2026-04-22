# Módulo AUTH - Especificación Técnica

**Proyecto:** Plataforma Fondo de Ahorro  
**Versión:** 1.0  
**Fecha:** 2026-04-14  
**Estado:** Implementado  
**Complejidad:** Alta

---

## Resumen

El módulo **auth** es responsable de toda la autenticación, autorización y gestión de sesiones del proyecto "Fondo de Ahorro". Implementa un sistema de tokens JWT con refresh token, bloqueo por fuerza bruta, rate limiting y auditoría de seguridad.

---

## 1. Objetivos del Módulo

### 1.1 Objetivo Principal
Proveer un sistema de autenticación seguro y centralizado que gestione el acceso de usuarios al sistema del Fondo de Ahorro mediante tokens JWT.

### 1.2 Objetivos Secundarios
- Implementar autenticación con JWT (access + refresh tokens)
- Gestionar sesiones de usuario con invalidación seguro
- Implementar protección contra ataques de fuerza bruta
- Proveer sistema de recuperación de contraseñas
- Crear usuarios vinculados a socios automáticamente
- Auditar todos los eventos de seguridad

### 1.3 Scope
- ✅ Login/Logout con JWT
- ✅ Refresh token automático
- ✅ Rate limiting por IP
- ✅ Bloqueo por fuerza bruta (5 intentos, 30 min bloqueo)
- ✅ Creación de usuarios vinculados a socios
- ✅ Recuperación de contraseña via email
- ✅ Validación de tokens

### 1.4 Fuera del Scope
- ❌ OAuth social (Google, Facebook, etc.)
- ❌ Autenticación biométrica
- ❌ MFA (Multi-Factor Authentication)

---

## 2. Arquitectura del Sistema

### 2.1 Arquitectura General (Clean Architecture)

```
backend/src/main/java/com/tufondo/auth/
├── domain/                          # Capa de Dominio (puro, sin dependencias externas)
│   ├── model/                      # Entidades de dominio inmutables
│   │   ├── Usuario.java           # Entidad Usuario
│   │   ├── Sesion.java           # Entidad Sesión
│   │   ├── PasswordResetToken.java # Token de recuperación
│   │   └── audit/
│   │       └── SecurityEvent.java # Evento de auditoría
│   ├── repository/                 # Interfaces de repositorios
│   │   ├── UsuarioRepository.java
│   │   ├── SesionRepository.java
│   │   └── PasswordResetTokenRepository.java
│   └── exception/                  # Excepciones de dominio
│       ├── CredencialesInvalidasException.java
│       ├── CuentaBloqueadaException.java
│       ├── CuentaDesactivadaException.java
│       ├── TokenExpiradoException.java
│       ├── TokenInvalidoException.java
│       ├── UsuarioNoEncontradoException.java
│       └── SesionNoEncontradaException.java
│
├── application/                    # Capa de Aplicación (CASOS DE USO)
│   ├── usecase/
│   │   ├── AuthUseCase.java       # Login, Logout, Refresh Token
│   │   ├── ValidarTokenUseCase.java # Validación de tokens
│   │   ├── ObtenerUsuarioActualUseCase.java
│   │   ├── CrearUsuarioManualUseCase.java
│   │   ├── SolicitarRecuperacionPasswordUseCase.java
│   │   └── RestablecerPasswordUseCase.java
│   └── dto/                       # Data Transfer Objects
│       ├── LoginRequestDTO.java
│       ├── LoginResponseDTO.java
│       ├── RefreshTokenRequestDTO.java
│       ├── TokenValidacionDTO.java
│       ├── CrearUsuarioRequestDTO.java
│       ├── CrearUsuarioResponseDTO.java
│       ├── RecuperarPasswordRequestDTO.java
│       └── ResetPasswordRequestDTO.java
│
└── infrastructure/                # Capa de Infraestructura
    ├── configuration/
    │   └── JwtProperties.java     # Configuración JWT (validada al startup)
    ├── presentation/
    │   ├── controller/
    │   │   └── AuthController.java # REST Controller
    │   └── exception/
    │       └── AuthExceptionHandler.java # Manejo centralizado de errores
    ├── security/
    │   ├── SecurityConfig.java    # Configuración Spring Security
    │   ├── JwtAuthenticationFilter.java # Filtro JWT
    │   └── LoginRateLimitFilter.java # Rate limiting (Bucket4j)
    ├── service/
    │   ├── JwtService.java        # Generación y validación JWT
    │   ├── Argon2Hasher.java      # Hashing de passwords (PBKDF2)
    │   ├── EmailService.java      # Envío de emails
    │   └── SecurityAuditService.java # Auditoría de seguridad
    └── persistence/
        ├── entity/                # Entidades JPA
        │   ├── UsuarioEntity.java
        │   ├── SesionEntity.java
        │   └── PasswordResetTokenEntity.java
        ├── jpa/                   # Repositorios JPA
        │   ├── UsuarioJpaRepository.java
        │   ├── SesionJpaRepository.java
        │   └── PasswordResetTokenJpaRepository.java
        └── adapter/               # Implementaciones de repositorios
            ├── UsuarioRepositoryImpl.java
            ├── SesionRepositoryImpl.java
            └── PasswordResetTokenRepositoryImpl.java
```

---

## 3. Modelo de Dominio

### 3.1 Usuario - Entidad Principal

```java
public final class Usuario {
    private final UUID id;                      // Identificador único
    private final String nombreUsuario;         // Username (único)
    private final String correoElectronico;     // Email (único)
    private final String passwordHash;          // Hash de contraseña (BCrypt)
    private final String nombreCompleto;        // Nombre completo
    private final Rol rol;                      // Rol (SOCIO, ADMIN, SUPER_ADMIN)
    private final UUID socioId;                 // Referencia al módulo socios
    private final boolean cuentaActiva;         // Estado de la cuenta
    private final Instant fechaCreacion;        // Fecha de creación
    private final Instant ultimaModificacion;   // Última modificación
    private final int intentosFallidos;        // Contador de intentos fallidos
    private final Instant fechaBloqueo;         // Fecha de bloqueo (si existe)
}
```

**Métodos de fábrica:**
- `Usuario.crear(...)` - Crea un nuevo usuario activo
- `Usuario.desdeParametros(...)` - Crea desde parámetros existentes
- `Usuario.conIntentosFallidos(...)` - Incrementa intentos y establece bloqueo
- `Usuario.conIntentosReseteados()` - Resetea intentos a 0
- `Usuario.conCuentaDesactivada()` - Desactiva la cuenta

**Relaciones:**
- Relación débil con `socioId` (UUID referencia al módulo Socios)
- Relación uno-a-muchos con `Sesion` (una sesión pertenece a un usuario)

---

### 3.2 Sesion - Entidad de Sesión

```java
public final class Sesion {
    private final UUID id;                       // Identificador único
    private final UUID usuarioId;                // FK a Usuario
    private final String refreshToken;           // Hash del refresh token
    private final Instant accessTokenExpiracion;  // Expiración del access token
    private final Instant refreshTokenExpiracion; // Expiración del refresh token
    private final boolean activo;                // Estado de la sesión
    private final TipoToken tipoToken;           // Tipo de token
    private final Instant fechaCreacion;          // Fecha de creación
    private final Instant ultimaActividad;       // Última actividad
}
```

**Métodos de dominio:**
- `estaExpirado()` - Verifica si el refresh token expiró
- `accessTokenExpirado()` - Verifica si el access token expiró

---

### 3.3 PasswordResetToken - Entidad de Recuperación

```java
public final class PasswordResetToken {
    private final UUID id;                       // Identificador único
    private final UUID usuarioId;                 // FK a Usuario
    private final String tokenHash;               // Hash del token (almacenado)
    private final Instant fechaCreacion;          // Cuándo se creó
    private final Instant fechaExpiracion;        // Cuándo expira (1 hora)
    private final boolean usado;                  // Si ya fue utilizado
    private final Instant fechaUso;               // Cuándo fue usado
}
```

**Métodos de dominio:**
- `estaExpirado()` - Verifica si el token expiró
- `estaUsado()` - Verifica si ya fue utilizado
- `marcarComoUsado()` - Marca el token como utilizado

**Flujo de recuperación:**
1. Usuario solicita `POST /auth/recuperar-password` con email
2. Sistema genera token y envía email con link
3. Usuario hace click en link y presenta `POST /auth/reset-password`
4. Sistema valida token, actualiza password y marca token como usado

---

### 3.4 Enumeraciones

#### Rol
```java
public enum Rol {
    SOCIO,      // Usuario regular (miembro del fondo)
    ADMIN,      // Administrador del sistema
    SUPER_ADMIN // Administrador supreme
}
```

#### TipoToken
```java
public enum TipoToken {
    ACCESS_TOKEN,   // Token de acceso (15 min por defecto)
    REFRESH_TOKEN  // Token de refresco (7 días por defecto)
}
```

---

## 4. Casos de Uso (Application Layer)

### 4.1 AuthUseCase

Caso de uso principal que maneja login, logout y refresh de tokens.

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthUseCase {
    private static final int MAX_INTENTOS_FALLIDOS = 5;
    private static final int MINUTOS_BLOQUEO = 30;
    
    // Dependencies
    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Argon2Hasher argon2Hasher;
    private final SecurityAuditService auditService;
}
```

**Operaciones:**

| Método | Descripción | Transaccional |
|--------|-------------|---------------|
| `login(LoginRequestDTO, String clientIp)` | Autentica usuario y crea sesión | `@Transactional` |
| `refreshToken(RefreshTokenRequestDTO, String clientIp)` | Genera nuevos tokens | `@Transactional` |
| `logout(String authHeader, String clientIp)` | Invalida todas las sesiones del usuario | `@Transactional` |

**Flujo de Login:**
1. Busca usuario por `nombreUsuario` o `correoElectronico`
2. Verifica si la cuenta está activa
3. Verifica si la cuenta no está bloqueada
4. Valida la contraseña con BCrypt
5. Resetea intentos fallidos
6. Genera access token y refresh token
7. Crea sesión en base de datos
8. Registra auditoría
9. Retorna `LoginResponseDTO`

**Protección contra fuerza bruta:**
- Máximo 5 intentos fallidos
- Bloqueo de 30 minutos al exceder
- Cada intento fallido se registra en auditoría

---

### 4.2 ValidarTokenUseCase

Valida la estructura y expiración de un JWT.

```java
@Component
@RequiredArgsConstructor
public class ValidarTokenUseCase {
    public TokenValidacionDTO ejecutar(String token);
    public boolean esValido(String token);
}
```

---

### 4.3 ObtenerUsuarioActualUseCase

Obtiene el usuario autenticado desde el SecurityContext.

```java
@Component
@RequiredArgsConstructor
public class ObtenerUsuarioActualUseCase {
    public LoginResponseDTO.UsuarioDTO ejecutar();
}
```

---

### 4.4 CrearUsuarioManualUseCase

Crea un usuario vinculado a un socio (solo ADMIN).

```java
@Component
@RequiredArgsConstructor
public class CrearUsuarioManualUseCase {
    public CrearUsuarioResponseDTO ejecutar(CrearUsuarioRequestDTO request);
}
```

**Flujo:**
1. Valida que el socio existe
2. Valida que el socio no tiene usuario ya vinculado
3. Valida nombre de usuario y correo únicos
4. Genera password temporal
5. Crea usuario con password hasheado
6. Envía email con credenciales
7. Retorna información del usuario creado

---

### 4.5 SolicitarRecuperacionPasswordUseCase

Solicita recuperación de contraseña.

```java
@Component
@RequiredArgsConstructor
public class SolicitarRecuperacionPasswordUseCase {
    public MensajeResponseDTO ejecutar(RecuperarPasswordRequestDTO request);
}
```

**Flujo:**
1. Busca usuario por correo electrónico
2. Si existe, genera token de recuperación (1 hora de validez)
3. Envía email con link de recuperación
4. Siempre retorna "OK" para no revelar si el email existe

---

### 4.6 RestablecerPasswordUseCase

Restablece la contraseña usando token.

```java
@Component
@RequiredArgsConstructor
public class RestablecerPasswordUseCase {
    public LoginResponseDTO ejecutar(ResetPasswordRequestDTO request);
}
```

**Flujo:**
1. Busca token de recuperación
2. Valida que no esté usado ni expirado
3. Valida nueva contraseña (regex)
4. Actualiza password del usuario
5. Marca token como usado
6. Invalida todas las sesiones del usuario
7. Retorna tokens de login

---

## 5. Estados y Transiciones

### 5.1 Cuenta de Usuario

```
┌─────────────────┐
│    ACTIVO       │◄─────────────────┐
└────────┬────────┘                  │
         │                           │
         │ (5 intentos fallidos)      │
         ▼                           │
┌─────────────────┐                  │
│   BLOQUEADO     │──────────────────┘
└─────────────────┘   (30 min después)
         │
         │ (admin reactiva)
         ▼
┌─────────────────┐
│  DESACTIVADO    │
└─────────────────┘
```

### 5.2 Sesión

```
[SESIÓN CREADA] → ACTIVO
                        │
                        ├── Refresh token usado ──► INVALIDADO
                        │
                        ├── Logout ──► INVALIDADO
                        │
                        └── Expiración ──► INVALIDADO (cleanup automático)
```

---

## 6. Excepciones

| Excepción | HTTP Status | Código | Descripción |
|-----------|-------------|--------|-------------|
| `CredencialesInvalidasException` | 401 | CREDENCIALES_INVALIDAS | Usuario o contraseña incorrectos |
| `UsuarioNoEncontradoException` | 404 | USUARIO_NO_ENCONTRADO | Usuario no existe |
| `CuentaBloqueadaException` | 403 | CUENTA_BLOQUEADA | Cuenta bloqueada por intentos fallidos |
| `CuentaDesactivadaException` | 403 | CUENTA_DESACTIVADA | Cuenta desactivada |
| `TokenExpiradoException` | 401 | TOKEN_EXPIRADO | Token JWT expirado |
| `TokenInvalidoException` | 401 | TOKEN_INVALIDO | Token JWT malformado o inválido |
| `SesionNoEncontradaException` | 404 | SESION_NO_ENCONTRADA | Sesión no existe |
| `PasswordNoCumpleRequisitosException` | 400 | PASSWORD_INVALIDO | Password no cumple requisitos |
| `TokenRecuperacionInvalidoException` | 400 | TOKEN_RECUPERACION_INVALIDO | Token de recuperación inválido |
| `NombreUsuarioYaExisteException` | 409 | NOMBRE_USUARIO_EXISTE | Nombre de usuario ya existe |
| `SocioYaTieneUsuarioException` | 409 | SOCIO_YA_TIENE_USUARIO | El socio ya tiene usuario vinculado |

---

## 7. Seguridad

### 7.1 Configuración Spring Security

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .referrerPolicy(refer -> refer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .permissionsPolicy(perm -> perm
                    .policy("geolocation=(), microphone=(), camera=(), payment=()")
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**Endpoints públicos:** `/api/v1/auth/**`, `/actuator/health`, Swagger

**Configuración CORS:**
- Orígenes configurables via variable de entorno `CORS_ORIGINS` (default: `http://localhost:3000`)
- Métodos: GET, POST, PUT, DELETE, OPTIONS
- Headers permitidos: Authorization, Content-Type, X-Requested-With
- Credentials: true
- Max age: 3600 segundos

---

### 7.2 JwtAuthenticationFilter

Filtro que procesa el header `Authorization: Bearer <token>` en cada request.

**Responsabilidades:**
1. Extrae el token del header Authorization
2. Valida que sea un access token válido (no refresh)
3. Extrae `usuarioId` y `rol` del token
4. Crea un `UsernamePasswordAuthenticationToken` con authorities basadas en el rol
5. Establece el authentication en el SecurityContext

**No filtra:** Requests a `/api/v1/auth/**`

---

### 7.3 LoginRateLimitFilter

Filtro de Rate Limiting usando **Bucket4j**.

**Configuración:**
- **Límite:** 5 solicitudes por minuto por IP
- **Ubicación:** `/api/v1/auth/login`
- **IP Extraction:** X-Forwarded-For → X-Real-IP → RemoteAddr

**Response cuando se excede (429 Too Many Requests):**
```json
{
  "codigo": "RATE_LIMIT_EXCEDIDO",
  "mensaje": "Demasiadas solicitudes. Intente nuevamente en 60 segundos."
}
```

---

### 7.4 Bloqueo por Fuerza Bruta

```
MAX_INTENTOS_FALLIDOS = 5
MINUTOS_BLOQUEO = 30
```

**Flujo:**
1. Usuario falla login → `intentosFallidos`++
2. Si `intentosFallidos >= 5` → `fechaBloqueo = now() + 30 minutos`
3. Login subsiguiente con cuenta bloqueada → `CuentaBloqueadaException`
4. Login exitoso → `intentosFallidos = 0`

---

### 7.5 Validación de Password

Regex de validación:
```
^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$
```

Requisitos:
- Mínimo 8 caracteres
- Al menos una mayúscula
- Al menos una minúscula
- Al menos un número
- Al menos un carácter especial: `@$!%*?&`

---

## 8. Dependencias Externas

| Dependencia | Propósito |
|-------------|-----------|
| `jjwt-api` | Manipulación de tokens JWT |
| `spring-boot-starter-security` | Seguridad de Spring |
| `spring-boot-starter-data-jpa` | Persistencia JPA |
| `bucket4j` | Rate limiting |
| `springdoc-openapi` | Documentación Swagger |

---

## 9. Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-14 | @documentador | Creación inicial - Reorganización de documentación |
| 1.1 | 2026-04-20 | @documentador | Issue #47: Cookies httpOnly para Flutter Web - Token rotation |

---

## 10. Referencias

- API: `/docs/modulos/auth/API.md`
- Modelo de datos: `/docs/modulos/auth/MODELO_DATOS.md`
- Módulo SOCIOS: `/docs/modulos/socios/SPEC.md`
- Módulo KYC: `/docs/modulos/kyc/SPEC.md`
- Issue #47 - Cookies httpOnly: `/docs/modulos/auth/ISSUE_47_COOKIES_HTTPONLY.md`
