# AUDITORÍA DE SEGURIDAD: Issue #26 - Módulo de Reportes Admin

**Fecha:** 24-abr-2026  
**Auditor:** Lead Software Architect & Cyber-Security Auditor  
**Proyecto:** Fondo de Ahorro - Platform  
**Categoría:** Frontend Web - Next.js 14 App Router  

---

## RESUMEN EJECUTIVO

| Criticidad | Cantidad |
|------------|----------|
| 🔴 CRÍTICA | 1 |
| 🟠 ALTA | 2 |
| 🟡 MEDIA | 0 |
| **TOTAL** | **3** |

### Resultado por Categoría

| Categoría | Estado | Hallazgos |
|-----------|--------|-----------|
| Validación de roles admin | ✅ PASS | - |
| IDOR / Validación de parámetros | 🔴 FAIL | 1 crítico |
| XSS / Renderizado seguro | ✅ PASS | - |
| Validación de inputs | 🟠 FAIL | 2 altos |
| Gestión de errores | ✅ PASS | - |
| Link vs `<a>` | ✅ PASS | - |
| Dependencias @radix-ui | ✅ PASS | - |

---

## VIOLACIONES CRÍTICAS (🔴)

### 1. [CRÍTICA] - IDOR: socioId sin validación en estado-cuenta

- **Archivo:** `frontend-web/src/app/api/admin/reportes/estado-cuenta/route.ts:líneas 14-22`
- **Descripción:** El parámetro `socioId` se extrae de los query params y se envía directamente al backend sin ninguna validación de formato UUID ni verificación deOwnership.
- **Impacto:** Un administrador malicioso podría enumerar estados de cuenta de otros socios modificando el UUID en la URL. Esto constituye una vulnerabilidad IDOR (Insecure Direct Object Reference) clásica.
- **Código actual:**
  ```typescript
  const socioId = searchParams.get('socioId');
  const anio = searchParams.get('anio');
  const mes = searchParams.get('mes');
  
  const params = new URLSearchParams();
  if (socioId) params.set('socioId', socioId); // ❌ Sin validación
  if (anio) params.set('anio', anio);
  if (mes) params.set('mes', mes);
  ```
- **Corrección:**
  ```typescript
  // Validar formato UUID
  const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
  
  if (!socioId || !UUID_REGEX.test(socioId)) {
    return NextResponse.json({ message: 'ID de socio inválido' }, { status: 400 });
  }
  
  // Validar año y mes
  const anioNum = parseInt(anio, 10);
  const mesNum = parseInt(mes, 10);
  
  if (isNaN(anioNum) || anioNum < 2020 || anioNum > 2030) {
    return NextResponse.json({ message: 'Año inválido' }, { status: 400 });
  }
  if (isNaN(mesNum) || mesNum < 1 || mesNum > 12) {
    return NextResponse.json({ message: 'Mes inválido' }, { status: 400 });
  }
  ```

---

## VIOLACIONES DE ARQUITECTURA / ALTA (🟠)

### 2. [ALTA] - Parámetros anio y mes sin validación de rango

- **Archivo:** `frontend-web/src/app/api/admin/reportes/estado-cuenta/route.ts:líneas 16-17`
- **Descripción:** Los parámetros `anio` y `mes` se pasan directamente al backend sin validación de tipo ni rangos válidos.
- **Impacto:** Un atacante podría enviar valores fuera de rango (ej: `mes=99`, `anio=-1`) causando comportamiento inesperado en el backend.
- **Corrección:** Implementar validación de rango después del parseo a entero.

### 3. [ALTA] - Función sanitizarTexto existe pero no se utiliza

- **Archivo:** `frontend-web/src/lib/auth/admin-validation.ts:líneas 29-32`
- **Descripción:** Existe una función `sanitizarTexto` que elimina caracteres peligrosos (`<>` `"` `'`), pero nunca se llama en las proxy routes.
- **Impacto:** Si el backend devuelve datos con contenido malicioso en descripciones o nombres, estos se renderizarían sin sanitización. Aunque actualmente React escapa por defecto, si en el futuro se usa `dangerouslySetInnerHTML` esto sería un problema.
- **Recomendación:** Aplicar sanitización a todos los campos de texto que vengan del backend antes de renderizarlos, especialmente en la tabla de movimientos de estado-cuenta donde se muestra `mov.descripcion`.

---

## HALLAZGOS POSITIVOS ✅

| Categoría | Observación |
|-----------|-------------|
| **Validación admin** | Las tres proxy routes usan correctamente `validateAdminAccess` verificando token y rol ADMIN |
| **XSS** | No se detecta uso de `dangerouslySetInnerHTML`. Todos los datos se renderizan via JSX seguro |
| **Gestión de errores** | Los catch blocks retornan mensajes genéricos `Error interno del servidor` sin exponer stack traces |
| **Link component** | Todas las páginas usan correctamente `<Link>` de Next.js para navegación interna |
| **@radix-ui** | Versión `^1.1.15` de `@radix-ui/react-alert-dialog` es reciente y estable |

---

## ARCHIVOS AFECTADOS

| Prioridad | Archivo | Hallazgo |
|-----------|---------|----------|
| 🔴 CRÍTICA | `frontend-web/src/app/api/admin/reportes/estado-cuenta/route.ts` | IDOR - socioId sin validación |
| 🟠 ALTA | `frontend-web/src/app/api/admin/reportes/estado-cuenta/route.ts` | anio/mes sin validación de rango |
| 🟠 ALTA | `frontend-web/src/lib/auth/admin-validation.ts` | sanitizarTexto sin usar |

---

## RECOMENDACIONES DE MEJORA

1. **Crear middleware de validación centralizado** para query params en proxy routes
2. **Añadir Zod schema validation** en las API routes para validar DTOs de entrada
3. **Implementar logging de auditoría** para registro de acceso a reportes administrativos
4. **Tests de seguridad** con fuzzing de parámetros para validar límites

---

## CONCLUSIÓN

**Resultado: FAIL** - Se encontró **1 violación CRÍTICA** (IDOR) y **2 altas** que deben corregirse antes de merge a producción.

El módulo de reportes admin tiene una implementación base sólida en cuanto a autenticación y gestión de errores, pero presenta una vulnerabilidad IDOR crítica en el endpoint de estado-cuenta que permite enumeración de datos financieros de socios.

---
*Auditoría generada: 24-abr-2026 12:00:00*
