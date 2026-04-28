# 🔒 INFORME DE AUDITORÍA DE SEGURIDAD - Issue #18 (Módulo de Créditos Frontend)

**PR:** #67
**Rama:** feature/issue-18-modulo-creditos
**Fecha:** 2026-04-24
**Auditor:** Lead Software Architect & Cyber-Security Auditor
**Alcance:** Módulo de Créditos Frontend + Backend (verificación de uso)

---

## RESUMEN EJECUTIVO

| Categoría | Cantidad |
|----------|----------|
| 🔴 CRÍTICAS | 2 |
| 🟠 ALTAS | 3 |
| 🟡 MEDIAS | 0 |
| **TOTAL** | **5** |

**Estado General:** ⚠️ **REQUIERE CORRECCIÓN** - Se encontraron vulnerabilidades de IDOR que deben corregirse antes de producción.

---

## VIOLACIONES CRÍTICAS (🔴)

### 1. [CRÍTICA] - IDOR (Insecure Direct Object Reference) en consulta de solicitudes

- **Archivo:** `frontend-web/src/app/(dashboard)/dashboard/creditos/[numero]/page.tsx:62`
- **Descripción:** El endpoint `/api/creditos/solicitudes/[numero]` permite acceso directo a cualquier solicitud de crédito solo con conocer su número. Aunque el backend valida en `ObtenerSolicitudUseCase`, el frontend no envía el `socioIdToken` para que esta validación sea efectiva.
- **Impacto:** Cualquier socio autenticado puede ver los detalles de solicitudes de crédito de OTROS socios manipulando el número de solicitud en la URL. Esto viola completamente el principio de autorización.
- **Corrección:**

```typescript
// En creditos/[numero]/page.tsx - Modificar llamada API
const res = await creditosApi.getSolicitud(numero, user.socioId);  // Pasar socioId

// En client.ts - Modificar getSolicitud
getSolicitud: (numero: string, socioId?: string) => {
  const params = socioId ? `?socioId=${socioId}` : '';
  return apiClient.get(`/api/creditos/solicitudes/${numero}${params}`);
},
```

---

### 2. [CRÍTICA] - IDOR en listado de solicitudes por socio

- **Archivo:** `frontend-web/src/app/api/creditos/solicitudes/socio/[socioId]/route.ts:16-26`
- **Descripción:** El endpoint permite a un socio consultar las solicitudes de CUALQUIER socio solo cambiando el `socioId` en la URL. No hay validación de que el `socioId` del token coincida con el solicitado.
- **Impacto:** Un atacante puede enumerar todas las solicitudes de crédito de todos los socios del sistema.
- **Corrección:**

```typescript
// En route.ts - Agregar validación de proprietà
export async function GET(
  request: NextRequest,
  { params }: { params: { socioId: string } }
) {
  const accessToken = request.cookies.get('access_token');
  if (!accessToken) {
    return NextResponse.json({ message: 'No autenticado' }, { status: 401 });
  }

  // Obtener socioId del token JWT para validar
  const tokenPayload = JSON.parse(atob(accessToken.value.split('.')[1]));
  const socioIdToken = tokenPayload.socioId;
  
  // VALIDACIÓN IDOR CRÍTICA
  if (socioIdToken !== params.socioId) {
    return NextResponse.json({ message: 'No autorizado' }, { status: 403 });
  }

  // Continuar con la lógica...
}
```

---

## VIOLACIONES ALTAS (🟠)

### 3. [ALTA] - Falta Rate Limiting en endpoint de simulación del Frontend

- **Archivo:** `frontend-web/src/app/api/creditos/simulador/route.ts`
- **Descripción:** El endpoint `/api/creditos/simulador` del frontend no implementa rate limiting. Aunque el backend tiene `SimulacionRateLimitFilter`, un atacante puede evadirlo haciendo llamadas directas al API del frontend.
- **Impacto:** Ataques de fuerza bruta o denegación de servicio contra el simulador.
- **Corrección:**

```typescript
// En route.ts - Agregar rate limiting en memoria
const rateLimitMap = new Map<string, { count: number; resetTime: number }>();
const RATE_LIMIT = 10;
const WINDOW_MS = 60_000;

export async function POST(request: NextRequest) {
  const clientIp = request.headers.get('x-forwarded-for')?.split(',')[0] || 'unknown';
  const now = Date.now();
  const record = rateLimitMap.get(clientIp);
  
  if (record && now < record.resetTime) {
    if (record.count >= RATE_LIMIT) {
      return NextResponse.json(
        { message: 'Demasiadas solicitudes' },
        { status: 429, headers: { 'Retry-After': '60' } }
      );
    }
    record.count++;
  } else {
    rateLimitMap.set(clientIp, { count: 1, resetTime: now + WINDOW_MS });
  }
  
  // Continuar con el resto del código...
}
```

---

### 4. [ALTA] - Falta validación de tipoCreditoId en simulador

- **Archivo:** `frontend-web/src/app/(dashboard)/dashboard/creditos/simulador/page.tsx:116-120`
- **Descripción:** El frontend envía `tipoCreditoId` al endpoint sin verificar que corresponda a un tipo de crédito válido existente. Podría enviarse un ID arbitrario.
- **Impacto:** Inconsistencia de datos o posible explotación si el backend no valida correctamente.
- **Corrección:**

```typescript
const handleSimular = async () => {
  // ... validaciones existentes ...

  // Validar que tipoCreditoId exista en tiposCredito
  const tipoValido = tiposCredito.find(t => t.id === tipoCreditoId);
  if (!tipoValido) {
    toast.error('Tipo de crédito inválido');
    return;
  }

  setSimulando(true);
  try {
    const res = await creditosApi.simular({
      tipoCreditoId,
      monto: montoNum,
      plazoMeses: plazoNum,
    });
    // ...
  }
};
```

---

### 5. [ALTA] - Exposición de datos sensibles en logs de error

- **Archivos:** 
  - `frontend-web/src/app/(dashboard)/dashboard/creditos/[numero]/page.tsx:65`
  - `frontend-web/src/app/(dashboard)/dashboard/creditos/simulador/page.tsx:69,124`
  - `frontend-web/src/app/api/creditos/tipos-credito/route.ts:37`
- **Descripción:** Se usa `console.error()` para registrar errores completos, incluyendo stack traces y datos de configuración.
- **Impacto:** Información sensible puede filtrarse en logs del cliente o herramientas de monitoreo.
- **Corrección:**

```typescript
// ❌ ANTES (expuesto)
console.error('Error al cargar tipos:', err);

// ✅ DESPUÉS (sanitizado)
console.error('Error al cargar tipos:', err instanceof Error ? err.message : 'Error desconocido');
```

---

## HALLAZGOS POSITIVOS (Lo que está bien implementado)

| Control | Estado | Descripción |
|---------|--------|-------------|
| Validación IDOR Backend | ✅ | `ObtenerSolicitudUseCase` valida que `socioIdToken` coincida |
| Rate Limiting Backend | ✅ | `SimulacionRateLimitFilter` limita a 10 req/min por IP |
| Validación DTOs | ✅ | `SimulacionRequest` tiene `@DecimalMin`, `@DecimalMax`, `@Min`, `@Max` |
| XSS Sanitization | ✅ | `XssSanitizer` elimina scripts y eventos HTML |
| SQL Injection | ✅ | JPA Repository usa métodos derivados, no concatenación |
| CSRF Protection | ✅ | Client.ts incluye token CSRF en headers |
| Autenticación | ✅ | Todos los endpoints validan `access_token` |
| Roles/Authorization | ✅ | `@PreAuthorize` en controller restringe por rol |

---

## ARCHIVOS AFECTADOS (Requieren corrección)

| Prioridad | Archivo |
|-----------|---------|
| 🔴 CRÍTICA | `frontend-web/src/app/(dashboard)/dashboard/creditos/[numero]/page.tsx` |
| 🔴 CRÍTICA | `frontend-web/src/app/api/creditos/solicitudes/socio/[socioId]/route.ts` |
| 🔴 CRÍTICA | `frontend-web/src/lib/api/client.ts` (agregar socioId a getSolicitud) |
| 🟠 ALTA | `frontend-web/src/app/api/creditos/simulador/route.ts` |
| 🟠 ALTA | `frontend-web/src/app/(dashboard)/dashboard/creditos/simulador/page.tsx` |
| 🟠 ALTA | Múltiples archivos con `console.error` expuesto |

---

## RECOMENDACIONES ADICIONALES

1. **En producción:** Implementar un API Gateway con rate limiting centralizado
2. **Agregar headers de seguridad:** `X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy`
3. **Auditar regularmente:** Revisar logs de acceso para detectar patrones sospechosos
4. **Validar tokens JWT en el frontend:** Considerar usar un middleware de Next.js para verificar el token antes de renderizar páginas protegidas

---

## CRITERIOS OWASP CUMPLIDOS/PENDIENTES

| Criterio OWASP | Estado | Notas |
|----------------|--------|-------|
| 1. Injection (SQL/JPQL) | ✅ CUMPLE | JPA Repository usa métodos derivados |
| 2. Broken Access Control (IDOR) | ⚠️ PARCIAL | Backend valida, pero frontend no pasa datos necesarios |
| 3. Data Exposure | ⚠️ PARCIAL | Logs expuestos en console.error |
| 4. Rate Limiting | ⚠️ PARCIAL | Solo en backend, frontend vulnerable |
| 5. Input Validation | ⚠️ PARCIAL | Validaciones existen pero tipoCreditoId no se valida |

---

**Generado:** 2026-04-24
**Próx. auditoría:** Antes de merge a main
