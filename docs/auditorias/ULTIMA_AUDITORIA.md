# 🔒 AUDITORÍA DE SEGURIDAD - Issue #101
## Verificación de Contraseña para Acciones Sensibles

**Fecha:** 2026-04-27  
**Auditor:** Lead Software Architect & Cyber-Security Auditor  
**Alcance:** Frontend + Backend (Verificación de Password)  
**Clasificación:** PÚBLICA

---

## RESUMEN EJECUTIVO

| Criticidad | Cantidad |
|------------|----------|
| 🔴 CRÍTICA | 3 |
| 🟠 ALTA | 4 |
| 🟡 MEDIA | 2 |
| ⚪ BAJA | 0 |

### Estado General

La implementación de Issue #101 presenta **fallas críticas de seguridad** que exponen el sistema a ataques de **fuerza bruta** y **secuestro de sesiones**. Si bien la arquitectura base es sólida (uso de PasswordEncoder, tokens UUID, auditoría), el endpoint `/api/v1/perfil/verificar-password` **carece de protección por rate limiting** y tiene **control de intentos insuficiente**, violando principios fundamentales de seguridad bancaria.

---

## VIOLACIONES CRÍTICAS (🔴)

### 1. Endpoint sin Rate Limiting - Ataque de Fuerza Bruta

**Archivo:** `backend/src/main/java/com/tufondo/core/infrastructure/security/ratelimit/GlobalRateLimitFilter.java:32-41`

**Descripción:**
El endpoint `/api/v1/perfil/verificar-password` **NO está incluido** en la lista de endpoints protegidos por rate limiting. El filtro protege login (5/min), registro (3/min), pero ignora completamente el endpoint de verificación de password.

```java
private static final Map<String, RateLimitConfig> ENDPOINT_LIMITS = Map.of(
    "/api/v1/auth/login", new RateLimitConfig(5, Duration.ofMinutes(1)),
    "/api/v1/auth/registro", new RateLimitConfig(3, Duration.ofMinutes(1)),
    "/api/v1/admin/**", new RateLimitConfig(30, Duration.ofMinutes(1)),
    "/api/v1/socios/**", new RateLimitConfig(60, Duration.ofMinutes(1)),
    // ⚠️ FALTA: "/api/v1/perfil/verificar-password"
    "/api/v1/cuentas/**", new RateLimitConfig(30, Duration.ofMinutes(1)),
    ...
);
```

**Impacto:**
Un atacante con acceso a credenciales válidas puede realizar **ataques de fuerza bruta** contra el endpoint de verificación de password sin ser bloqueado. Esto compromete cualquier acción sensible posterior (cambio de contraseña, modificación de datos, etc.).

**Corrección:**
```java
"/api/v1/perfil/verificar-password", new RateLimitConfig(5, Duration.ofMinutes(1)),
```

---

### 2. Sin Control de Intentos en Verificación de Password

**Archivo:** `backend/src/main/java/com/tufondo/auth/infrastructure/service/VerificacionService.java:32-34`

**Descripción:**
El método `verificarPasswordUsuario` NO implementa control de intentos, a diferencia de `confirmarCodigo` que sí tiene `MAX_INTENTOS = 3`.

```java
public boolean verificarPasswordUsuario(UUID usuarioId, String password, String hashedPassword) {
    return passwordEncoder.matches(password, hashedPassword);  // ⚠️ Sin control de intentos
}
```

vs.

```java
if (entity.getIntentos() >= MAX_INTENTOS) {  // ✓ confirmarCodigo SÍ tiene esto
    throw new ExcesoIntentosException("Superaste el número máximo de intentos");
}
```

**Impacto:**
Un atacante puede realizar **ataques de diccionario infinitos** hasta encontrar la contraseña correcta. El sistema solo registra el intento fallido en auditoría pero **no bloquea ni limita**.

**Corrección:**
```java
@Transactional
public boolean verificarPasswordUsuario(UUID usuarioId, String password, String hashedPassword) {
    // Verificar si hay intentos fallidos previos bloqueados
    Optional<VerificacionTokenEntity> bloqueo = tokenRepository
        .findByUsuarioIdAndTipoAndUsedFalseAndExpiresAtAfter(
            usuarioId, TipoVerificacion.PASSWORD_FALLO, Instant.now());
    
    if (bloqueo.isPresent() && bloqueo.get().getIntentos() >= MAX_INTENTOS_PASSWORD) {
        throw new ExcesoIntentosException("Demasiados intentos fallidos. Intente en 5 minutos.");
    }
    
    boolean valido = passwordEncoder.matches(password, hashedPassword);
    
    if (!valido) {
        // Registrar intento fallido
        registrarIntentoFalloPassword(usuarioId);
    }
    
    return valido;
}
```

---

### 3. Token de Verificación Predecible (UUID)

**Archivo:** `backend/src/main/java/com/tufondo/auth/infrastructure/service/VerificacionService.java:37-38`

**Descripción:**
El token de verificación se genera usando `UUID.randomUUID()` que, aunque parece aleatorio, es **predecible y no diseñado para seguridad criptográfica**.

```java
public String generarTokenVerificacion(UUID usuarioId, String ipAddress, String userAgent) {
    String token = UUID.randomUUID().toString();  // ⚠️ No es criptográficamente seguro
    // ...
}
```

**Impacto:**
Un atacante que obtenga acceso a la base de datos (SQL Injection, insider threat) puede:
1. Listar todos los tokens pendientes
2. Adivinar tokens UUID v4 (known weakness)
3. Usar tokens robados para acceder a funciones sensibles

**Corrección:**
```java
import java.security.SecureRandom;
import java.util.Base64;

public String generarTokenVerificacion(UUID usuarioId, String ipAddress, String userAgent) {
    SecureRandom secureRandom = new SecureRandom();
    byte[] tokenBytes = new byte[32];  // 256 bits
    secureRandom.nextBytes(tokenBytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    // ... resto igual
}
```

---

## VIOLACIONES DE ARQUITECTURA / ALTA (🟠)

### 4. IP Spoofing en getClientIp

**Archivo:** `backend/src/main/java/com/tufondo/socios/application/usecase/VerificacionUseCase.java:106-112`

**Descripción:**
El método `getClientIp` usa `X-Forwarded-For` sin validación, permitiendo a un atacante falsificar la IP.

```java
private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
        return xForwardedFor.split(",")[0].trim();  // ⚠️ Sin validar formato
    }
    return request.getRemoteAddr();
}
```

**Impacto:**
Un atacante puede falsificar la IP en los logs de auditoría para ocultar su origen real, dificultando la investigación forense y el rastreo.

**Corrección:**
```java
private String getClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
        String ip = xForwardedFor.split(",")[0].trim();
        // Validar formato IPv4 o IPv6
        if (isValidIpAddress(ip)) {
            return ip;
        }
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty() && isValidIpAddress(xRealIp)) {
        return xRealIp;
    }
    return request.getRemoteAddr();
}

private boolean isValidIpAddress(String ip) {
    if (ip == null || ip.isEmpty()) return false;
    // IPv4 basic regex
    return ip.matches("^(\\d{1,3}\\.){3}\\d{1,3}$") || 
           ip.matches("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");
}
```

---

### 5. Exposición de Información en Mensajes de Error

**Archivo:** `frontend-web/src/app/api/perfil/verificar-password/route.ts:37-38`

**Descripción:**
El endpoint propaga mensajes de error específicos del backend al cliente.

```typescript
return NextResponse.json(
    { message: errorData.message || 'Contraseña incorrecta', valido: false },
    { status: backendResponse.status }
);
```

**Impacto:**
Un atacante puede distinguir entre "usuario no existe" vs "contraseña incorrecta" si el backend tiene mensajes diferentes. Además, mensajes de error detallados pueden ayudar en ataques de enumeración.

**Corrección:**
```typescript
// En el route.ts del frontend
if (!backendResponse.ok) {
    return NextResponse.json(
        { message: 'Credenciales inválidas', valido: false },  // Mensaje genérico
        { status: 401 }
    );
}
```

---

### 6. Token No Vinculado al Contexto de Seguridad

**Archivo:** `backend/src/main/java/com/tufondo/auth/infrastructure/service/VerificacionService.java:37-57`

**Descripción:**
El token de verificación generado NO incluye un hash del contexto original (IP, User-Agent) para validar que no fue robado.

```java
public String generarTokenVerificacion(UUID usuarioId, String ipAddress, String userAgent) {
    String token = UUID.randomUUID().toString();
    // ⚠️ El token se guarda pero NO se almacena hash del contexto
    // Un atacante puede robar el token y usarlo desde otra IP
}
```

**Impacto:**
Si un atacante intercepta el `tokenVerificacion` (man-in-the-middle, acceso a logs, base de datos), puede usarlo desde cualquier ubicación para realizar acciones sensibles.

**Corrección:**
```java
public String generarTokenVerificacion(UUID usuarioId, String ipAddress, String userAgent) {
    SecureRandom secureRandom = new SecureRandom();
    byte[] tokenBytes = new byte[32];
    secureRandom.nextBytes(tokenBytes);
    String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    
    // Hash del contexto para verificación posterior
    String contextoHash = hashContexto(ipAddress, userAgent);
    
    VerificacionTokenEntity entity = VerificacionTokenEntity.builder()
            .token(token)
            .usuarioId(usuarioId)
            .tipo(TipoVerificacion.EMAIL)
            .contextoHash(contextoHash)  // Nuevo campo
            .createdAt(Instant.now())
            .expiresAt(Instant.now().plus(TTL_MINUTES, ChronoUnit.MINUTES))
            .used(false)
            .intentos(0)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
    // ...
}

public boolean validarTokenVerificacion(UUID usuarioId, String token, String ipActual, String userAgentActual) {
    // Validar que el contexto coincide
    if (!entity.getContextoHash().equals(hashContexto(ipActual, userAgentActual))) {
        auditService.registrarIntentoVerificacion(usuarioId, "TOKEN_CTX_MISMATCH", false, ipActual);
        return false;
    }
}
```

---

### 7. Falta Rate Limit Específico en el Controller

**Archivo:** `backend/src/main/java/com/tufondo/socios/presentation/controller/PerfilVerificacionController.java:28-43`

**Descripción:**
El controller de verificación no tiene protección específica contra abuso, rely únicamente en el filtro global que no cubre este endpoint.

```java
@PostMapping("/verificar-password")
public ResponseEntity<VerificarPasswordResponseDTO> verificarPassword(
        @Valid @RequestBody VerificarPasswordRequestDTO request,
        Authentication authentication,
        HttpServletRequest httpRequest) {
    // ⚠️ Sin @RateLimited o similar
}
```

**Impacto:**
Además del filtro global, este endpoint requiere su propia configuración de rate limiting específica debido a la naturaleza sensible de la operación.

**Corrección:**
```java
@PostMapping("/verificar-password")
@RateLimiting(value = 5, unit = RateLimiting.Unit.MINUTE)  // Nueva anotación
public ResponseEntity<VerificarPasswordResponseDTO> verificarPassword(...)
```

---

## MEJORAS RECOMENDADAS (🟡)

### 8. Validación de Longitud de Contraseña

**Archivo:** `backend/src/main/java/com/tufondo/socios/application/dto/VerificarPasswordRequestDTO.java:15-16`

**Descripción:**
El DTO solo tiene `@NotBlank`, no valida longitud mínima.

```java
@NotBlank(message = "La contraseña es obligatoria")
private String password;  // ⚠️ Sin @Size(min=8)
```

**Recomendación:**
```java
@NotBlank(message = "La contraseña es obligatoria")
@Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
private String password;
```

---

### 9. Logging de Códigos SMS en Desarrollo

**Archivo:** `backend/src/main/java/com/tufondo/auth/infrastructure/service/VerificacionService.java:90-93`

**Descripción:**
En entorno de desarrollo se loguean códigos de verificación SMS.

```java
log.info("========= SMS MOCK: CÓDIGO DE VERIFICACIÓN =========");
log.info("Para: {}", valor);
log.info("Código: {}", codigo);  // ⚠️ En producción esto es CRÍTICO
log.info("===================================================");
```

**Recomendación:**
Verificar que en producción estos logs no se ejecuten:
```java
if (log.isDebugEnabled()) {
    log.debug("SMS MOCK: Para: {}, Código: {}", valor, codigo);
}
```

---

## ARCHIVOS AFECTADOS

| Prioridad | Archivo |
|-----------|---------|
| 🔴 CRÍTICA | `backend/src/main/java/com/tufondo/core/infrastructure/security/ratelimit/GlobalRateLimitFilter.java` |
| 🔴 CRÍTICA | `backend/src/main/java/com/tufondo/auth/infrastructure/service/VerificacionService.java` |
| 🔴 CRÍTICA | `backend/src/main/java/com/tufondo/auth/infrastructure/service/VerificacionService.java` |
| 🟠 ALTA | `backend/src/main/java/com/tufondo/socios/application/usecase/VerificacionUseCase.java` |
| 🟠 ALTA | `frontend-web/src/app/api/perfil/verificar-password/route.ts` |
| 🟠 ALTA | `backend/src/main/java/com/tufondo/auth/infrastructure/service/VerificacionService.java` |
| 🟠 ALTA | `backend/src/main/java/com/tufondo/socios/presentation/controller/PerfilVerificacionController.java` |
| 🟡 MEDIA | `backend/src/main/java/com/tufondo/socios/application/dto/VerificarPasswordRequestDTO.java` |
| 🟡 MEDIA | `backend/src/main/java/com/tufondo/auth/infrastructure/service/VerificacionService.java` |

---

## CONCLUSIONES

La implementación de Issue #101 requiere **correcciones inmediatas** antes de producción:

1. **AGREGAR rate limiting** al endpoint `/api/v1/perfil/verificar-password` (5 intentos/minuto máximo)
2. **IMPLEMENTAR control de intentos** en `verificarPasswordUsuario` similar a `confirmarCodigo`
3. **SUSTITUIR UUID.randomUUID()** por `SecureRandom + Base64` para token criptográficamente seguro
4. **AÑADIR hash de contexto** al token de verificación para prevenir token hijacking

La arquitectura general es correcta (PasswordEncoder, auditoría, DTOs con validación), pero carece de protecciones críticas anti-fuerza-bruta que son **obligatorias en sistemas FinTech**.

---

**Firma del Auditor:** Lead Software Architect & Cyber-Security Auditor  
**Fecha del Reporte:** 2026-04-27  
**Próxima Auditoría:** Antes de liberación a producción
