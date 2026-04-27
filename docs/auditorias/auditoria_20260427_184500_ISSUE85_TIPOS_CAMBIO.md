# AUDITORÍA DE SEGURIDAD - ISSUE #85
## Módulo: Tipos de Cambio - Multi-currency exchange rate management

**Fecha:** 2026-04-27  
**Auditor:** Lead Software Architect & Cyber-Security Auditor  
**Severidad:** CRÍTICA - ALTA  
**Estado:** 🔴 REQUIERE CORRECCIÓN OBLIGATORIA

---

## RESUMEN EJECUTIVO

| Severidad | Cantidad |
|-----------|----------|
| 🔴 CRÍTICA | 2 |
| 🟠 ALTA | 3 |
| 🟡 MEDIA | 1 |
| ⚪ BAJA | 0 |

### Estado General del Código

El módulo de Tipos de Cambio presenta **2 vulnerabilidades críticas** que comprometen la seguridad del sistema financiero:

1. **adminId Falso**: El controlador genera UUIDs aleatorios en lugar de usar el ID real del administrador autenticado, invalidando completamente los logs de auditoría.
2. **Validación JWT Sin Firma**: El frontend decodifica JWTs sin verificar la firma criptográfica, permitiendo tokens伪造.

**VEREDICTO: NO APTO PARA PRODUCCIÓN**

---

## VIOLACIONES CRÍTICAS (🔴)

### 1. [CRÍTICA] - adminId Falso en Controller - Trazabilidad de Auditoría Comprometida

**Archivo:** `backend/src/main/java/com/tufondo/tipocambio/presentation/controller/TipoCambioController.java`

**Líneas affected:**
- Línea 85: `UUID adminId = UUID.randomUUID();`
- Línea 103: `UUID adminId = UUID.randomUUID();`
- Línea 122: `UUID adminId = UUID.randomUUID();`

**Descripción:**
El controlador genera un UUID aleatorio para `adminId` en cada operación CRUD (crear, actualizar, eliminar) en lugar de extraer el ID real del `SecurityContextHolder`. Esto invalida completamente el sistema de auditoría.

**Código actual problemático:**
```java
@PostMapping
@PreAuthorize("hasRole('SUPER_ADMIN')")
public ResponseEntity<TipoCambioResponse> crear(
        @Valid @RequestBody TipoCambioRequest request,
        @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress) {
    try {
        UUID adminId = UUID.randomUUID(); // ❌ INCORRECTO - UUID aleatorio
        TipoCambioResponse response = gestionarUseCase.crear(request, adminId, ipAddress);
        // ...
    }
}
```

**Impacto en producción:**
- **Sin no-repudiation**: Un administrador puede negar operaciones que realizó, o peor, atribuir operaciones a otros administradores.
- **Trazabilidad comprometida**: En caso de auditoría regulatoria (SBSA, Sudeban), los logs son eviden了一场 ineffectivos.
- **Detección de fraude imposible**: No se puede investigar quién creó/modificó una tasa de cambio específica.
- **Cumplimiento regulatorio fallido**: Leyes financieras exigen trazabilidad completa de operaciones.

**Corrección obligatoria:**
```java
@PostMapping
@PreAuthorize("hasRole('SUPER_ADMIN')")
public ResponseEntity<TipoCambioResponse> crear(
        @Valid @RequestBody TipoCambioRequest request,
        @RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress,
        Authentication authentication) {
    try {
        UUID adminId = UUID.fromString(authentication.getName()); // ✅ CORRECTO
        TipoCambioResponse response = gestionarUseCase.crear(request, adminId, ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (GestionarTipoCambioUseCase.TipoCambioYaExisteException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (GestionarTipoCambioUseCase.TasaInvalidaException e) {
        return ResponseEntity.badRequest().build();
    }
}
```

**Nota:** Aplicar el mismo patrón en `actualizar()` (línea 103) y `eliminar()` (línea 122).

---

### 2. [CRÍTICA] - Validación JWT Sin Verificación de Firma Criptográfica

**Archivo:** `frontend-web/src/lib/auth/admin-validation.ts`

**Líneas:** 12-16

**Descripción:**
El código decodifica el JWT manualmente usando `Buffer.from(payload, 'base64')` sin verificar la firma criptográfica. Cualquier atacante puede crear un token JWT falso con rol de ADMIN y obtener acceso.

**Código actual vulnerable:**
```typescript
export function validateAdminAccess(context: AdminAuthContext): { valid: boolean; status: number; message: string } {
  if (!context.accessToken) {
    return { valid: false, status: 401, message: 'No autenticado' };
  }

  try {
    const payload = context.accessToken.split('.')[1];
    const decoded = Buffer.from(payload, 'base64').toString('utf-8');
    const data = JSON.parse(decoded);
    if (!ADMIN_ROLES.includes(data.rol)) { // ❌ Solo verifica rol, no firma
      return { valid: false, status: 403, message: 'No autorizado' };
    }
    return { valid: true, status: 200, message: 'OK' };
  } catch {
    return { valid: false, status: 401, message: 'Token inválido' };
  }
}
```

**Impacto en producción:**
- **Autenticación completamente comprometida**: Un atacante puede impersonar cualquier rol (incluyendo SUPER_ADMIN).
- **Acceso no autorizado a operaciones financieras**: Puede crear, modificar o eliminar tipos de cambio.
- **Fraude financiero**: Modificación de tasas de cambio para beneficio personal.
- **Violación OWASP A07:2021 - Identification and Authentication Failures**.

**Corrección obligatoria:**
```typescript
import jwt from 'jsonwebtoken';

const JWT_SECRET = process.env.JWT_SECRET || 'fallback-secret-no-usar-en-produccion';

export function validateAdminAccess(context: AdminAuthContext): { valid: boolean; status: number; message: string } {
  if (!context.accessToken) {
    return { valid: false, status: 401, message: 'No autenticado' };
  }

  try {
    // ✅ Verificar firma criptográfica
    const decoded = jwt.verify(context.accessToken, JWT_SECRET) as { rol: string; sub: string };
    
    if (!ADMIN_ROLES.includes(decoded.rol)) {
      return { valid: false, status: 403, message: 'No autorizado' };
    }
    return { valid: true, status: 200, message: 'OK' };
  } catch (err) {
    return { valid: false, status: 401, message: 'Token inválido o expirado' };
  }
}
```

---

## VIOLACIONES DE ARQUITECTURA / ALTA (🟠)

### 3. [ALTA] - Header X-Forwarded-For Sin Validación (IP Spoofing)

**Archivo:** `backend/src/main/java/com/tufondo/tipocambio/presentation/controller/TipoCambioController.java`

**Líneas:** 83, 101, 120

**Descripción:**
El header `X-Forwarded-For` se usa directamente para logging sin validación. Este header puede ser fácilmente falsificado por un atacante para ocultar su IP real.

**Código actual:**
```java
@RequestHeader(value = "X-Forwarded-For", required = false) String ipAddress
```

**Impacto:**
- **Evasión de detecciones**: Un atacante puede evadir rate limits y bans de IP.
- **Logs no confiables**: La IP registrada no es confiable para investigaciones.
- **Auditoría comprometida**: En caso de incidente, la evidencia de IP es cuestionable.

**Recomendación:**
```java
// Sanitizar y validar X-Forwarded-For
private String sanitizarIpAddress(String ipAddress) {
    if (ipAddress == null || ipAddress.isBlank()) {
        return null;
    }
    // Tomar solo la primera IP (la original del cliente)
    String ip = ipAddress.split(",")[0].trim();
    // Validar formato IPv4 o IPv6
    if (!ip.matches("^(\\d{1,3}\\.){3}\\d{1,3}$") && !ip.matches("^[0-9a-fA-F:]+$")) {
        return null; // Invalid format, don't log
    }
    return ip;
}
```

---

### 4. [ALTA] - Sin Rate Limiting en Endpoints de Tipos de Cambio

**Archivo:** `backend/src/main/java/com/tufondo/tipocambio/presentation/controller/TipoCambioController.java`

**Descripción:**
Los endpoints POST, PUT, DELETE no tienen rate limiting visible. Un atacante podría:
- Flood de creación de tipos de cambio
- Fuerza bruta en operaciones de actualización/eliminación
- Denial of Service

**Recomendación:**
Agregar anotación `@RateLimiter` o configurar en `SecurityConfig`:
```java
@RateLimiter(name = "tipocambio", fallbackMethod = "rateLimitFallback")
@PostMapping
public ResponseEntity<TipoCambioResponse> crear(...) { }
```

---

### 5. [ALTA] - Falta Validación de Rangos en Frontend

**Archivo:** `frontend-web/src/app/(admin)/admin/tipos-cambio/page.tsx`

**Líneas:** 110-115

**Descripción:**
El frontend hace `parseFloat` sin validar rangos razonables:
```javascript
const payload = {
  fecha: form.fecha,
  tasaCompra: parseFloat(form.tasaCompra),
  tasaVenta: parseFloat(form.tasaVenta),
  fuente: form.fuente.trim() || null,
};
```

Un usuario malicioso o bug podría enviar valores extremos que causen overflow en cálculos financieros.

**Recomendación:**
```javascript
const tasaCompra = parseFloat(form.tasaCompra);
const tasaVenta = parseFloat(form.tasaVenta);

// Validación de rango
if (tasaCompra < 0.01 || tasaCompra > 1000000 || tasaVenta < 0.01 || tasaVenta > 1000000) {
  toast.error('Las tasas deben estar en un rango razonable');
  return;
}
```

---

## MEJORAS RECOMENDADAS (🟡)

### 6. [MEDIA] - Consistencia en el Manejo de Excepciones

**Archivo:** `backend/src/main/java/com/tufondo/tipocambio/application/usecase/GestionarTipoCambioUseCase.java`

**Descripción:**
Las excepciones personalizadas (`TipoCambioNoEncontradoException`, `TipoCambioYaExisteException`, `TasaInvalidaException`) extienden `RuntimeException` sin código de error estandarizado.

**Recomendación:**
Considerar usar un código de error estándar para APIs:
```java
public class TipoCambioNoEncontradoException extends RuntimeException {
    private final String errorCode = "TC_404";
    public TipoCambioNoEncontradoException(UUID id) {
        super("Tipo de cambio no encontrado con ID: " + id);
    }
    public String getErrorCode() { return errorCode; }
}
```

---

## ARCHIVOS AFECTADOS

### Requiere corrección inmediata (CRÍTICA):
| Archivo | Prioridad |
|---------|-----------|
| `backend/src/main/java/com/tufondo/tipocambio/presentation/controller/TipoCambioController.java` | 🔴 CRÍTICA |
| `frontend-web/src/lib/auth/admin-validation.ts` | 🔴 CRÍTICA |

### Requiere corrección (ALTA):
| Archivo | Prioridad |
|---------|--------|
| `backend/src/main/java/com/tufondo/tipocambio/presentation/controller/TipoCambioController.java` | 🟠 ALTA |
| `frontend-web/src/app/(admin)/admin/tipos-cambio/page.tsx` | 🟠 ALTA |

### Mejora recomendada (MEDIA):
| Archivo | Prioridad |
|---------|--------|
| `backend/src/main/java/com/tufondo/tipocambio/application/usecase/GestionarTipoCambioUseCase.java` | 🟡 MEDIA |

---

## CHECKLIST OWASP Top 10 - Hallazgos

| Categoría OWASP | Estado | Hallazgo |
|-----------------|--------|----------|
| A01 - Broken Access Control | ❌ FALLA | adminId falso permite suplantación |
| A02 - Cryptographic Failures | ❌ FALLA | JWT sin verificación de firma |
| A03 - Injection | ✅ PASÓ | Uso de JPA/Spring Data, no SQL dinámico |
| A04 - Insecure Design | 🟡 MEJORAR | Sin rate limiting |
| A05 - Security Misconfiguration | 🟡 MEJORAR | X-Forwarded-For sin sanitización |
| A06 - Vulnerable Components | ✅ PASÓ | Dependencias actualizadas |
| A07 - Auth Failures | ❌ FALLA | Validación JWT sin firma |
| A08 - Data Integrity | ❌ FALLA | Logs de auditoría no confiables |
| A09 - Logging Failures | ❌ FALLA | adminId falso |
| A10 - SSRF | 🟡 MEJORAR | Posible via X-Forwarded-For spoofing |

---

## CONCLUSIÓN

El módulo **Issue #85** presenta **2 vulnerabilidades críticas** que deben ser corregidas antes de cualquier despliegue a producción:

1. **adminId Falso** - Compromete la trazabilidad y auditoría regulatory
2. **JWT Sin Firma** - Compromete toda la autenticación del sistema

**RECOMENDACIÓN FINAL:** NO PROCEDER A PRODUCCIÓN hasta corregir las 2 vulnerabilidades críticas. Reprobar el módulo.

---

*Reporte generado: 2026-04-27 18:45:00*
*Auditor: Lead Software Architect & Cyber-Security Auditor*
