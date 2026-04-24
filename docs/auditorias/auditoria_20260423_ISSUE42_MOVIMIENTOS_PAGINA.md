# 🔍 Auditoría de Seguridad: Issue #42 - Página de Movimientos con Filtros

**Fecha:** 2026-04-23  
**Auditor:** Lead Software Architect & Cyber-Security Auditor  
**Rama:** feature/issue-42-movimientos-pagina  
**PR:** #66  
**Estado:** ✅ APPROVED con observaciones

---

## RESUMEN EJECUTIVO

| Criticidad | Cantidad |
|------------|----------|
| 🔴 CRÍTICA | 0 |
| 🟠 ALTA | 2 |
| 🟡 MEDIA | 2 |
| ⚪ BAJA | 0 |

**Estado General:** El código cumple los requisitos funcionales del feature, pero presenta **2 vulnerabilidades de alta severidad** relacionadas con validación de inputs y sanitización. El fix de filtros combinados está implementado correctamente a nivel lógico.

---

## VIOLACIONES CRÍTICAS (🔴)

No se encontraron vulnerabilidades CRÍTICAS en esta auditoría.

---

## VIOLACIONES DE ARQUITECTURA / ALTA SEVERIDAD (🟠)

### 1. [ALTA] - Parámetros de paginación sin validación

- **Archivo:** `backend/src/main/java/com/tufondo/ahorros/api/controller/AhorroController.java:l139-140`
- **Descripción:** Los parámetros `page` y `size` en el endpoint GET `/cuentas/{numeroCuenta}/movimientos` no tienen validación con `@Min`/`@Max`. Un atacante podría enviar valores negativos o excesivamente grandes.
- **Impacto:** DoS por consumo excesivo de memoria si `size` es muy grande. El UseCase limita a 100, pero el controller debe rechazar valores inválidos antes de llegar al repositorio.
- **Corrección:**
```java
@GetMapping("/{numeroCuenta}/movimientos")
public ResponseEntity<MovimientosListResponse> listarMovimientos(
        @PathVariable String numeroCuenta,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
        // ...
```

---

### 2. [ALTA] - Sin validación de rango de fechas (fechaFin >= fechaInicio)

- **Archivo:** `backend/src/main/java/com/tufondo/ahorros/application/usecase/ListarMovimientosUseCase.java:l52-67`
- **Descripción:** El código permite enviar solo `fechaInicio` o solo `fechaFin` sin validar coherencia. Si `fechaFin < fechaInicio`, la query retorna 0 resultados sin error, confundiendo al usuario. No hay validación de formato de fecha.
- **Impacto:** UX confusa, potenciales errores lógicos. Un attacker podría enviar fechas inválidas (ej: `9999-99-99`) si el parsing de `LocalDate` no falla antes.
- **Corrección:**
```java
public MovimientosListResponse ejecutar(...) {
    // ... existentes ...
    
    // Validar rango de fechas
    if (fechaInicio != null && fechaFin != null && fechaFin.isBefore(fechaInicio)) {
        throw new IllegalArgumentException("fechaFin debe ser mayor o igual a fechaInicio");
    }
    
    // Validar fechas no futuras
    if (fechaInicio != null && fechaInicio.isAfter(LocalDate.now())) {
        throw new IllegalArgumentException("fechaInicio no puede ser futura");
    }
    
    // ... resto del código ...
}
```

---

## MEJORAS RECOMENDADAS (🟡)

### 1. [MEDIA] - Falta @Valid en endpoint de movimientos

- **Archivo:** `backend/src/main/java/com/tufondo/ahorros/api/controller/AhorroController.java:l137`
- **Descripción:** El endpoint `listarMovimientos` no usa `@Valid` para validar los parámetros de query. Aunque son primitivos, un custom validator podría asegurar formato correcto de fechas.
- **Recomendación:** Considerar crear un DTO record para los parámetros de query:
```java
public record ListarMovimientosQuery(
    @Min(0) int page,
    @Min(1) @Max(100) int size,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
    TipoMovimiento tipo
) {}
```

---

### 2. [MEDIA] - Frontend: inputs HTML nativos sin validación avanzada

- **Archivo:** `frontend-web/src/app/(dashboard)/dashboard/cuentas/[numero]/movimientos/page.tsx:l207-234`
- **Descripción:** Los inputs `<select>` y `<Input type="date">` usan elementos HTML nativos sin validación custom en frontend. El valor `tipoFiltro` es un string que se compara con `'DEPOSITO'` (hardcoded).
- **Recomendación:**
```typescript
// Definir enum local para validar tipos
const TIPOS_VALIDOS = ['DEPOSITO', 'RETIRO'] as const;
type TipoFiltro = typeof TIPOS_VALIDOS[number];

// Validación antes de enviar
const aplicarFiltros = () => {
  if (fechaInicio && fechaFin && fechaFin < fechaInicio) {
    toast.error('La fecha fin debe ser mayor o igual a la fecha inicio');
    return;
  }
  setPage(0);
  cargarMovimientos();
};
```

---

## ANÁLISIS POSITIVO ✅

### Lo que está bien implementado:

1. **IDOR Prevention (Correcto):** El UseCase verifica ownership en línea 43-45 del `ListarMovimientosUseCase.java`:
```java
if (!isAdmin && !cuenta.getSocioId().equals(socioIdToken)) {
    throw new AccesoCuentaAjenaException();
}
```

2. **JPQL Parameters (Correcto):** Las queries en `MovimientoJpaRepository.java` usan parámetros seguros (líneas 29-36, 41-50), sin concatenación.

3. **Lógica de filtros combinados (Correcta):** El fix en commit `fix(movimientos): allow combining date range and tipo filters` implementa correctamente la combinación de 4 escenarios.

4. **Seguridad de datos sensibles:** El `MovimientoResponse.java` excluye `ipOrigen`, `sessionId`, `requestId` de la exposición (línea 19 - comentario OWASP A02).

5. **Page size limitado:** `Math.min(size, 100)` en línea 48 previene sobrecarga.

6. **No XSS en frontend:** No hay uso de `dangerouslySetInnerHTML`.

7. **CSRF Token:** El `client.ts` implementa double-submit pattern para CSRF (líneas 17-24).

---

## ARCHIVOS AFECTADOS

| Prioridad | Archivo |
|-----------|---------|
| 🟠 ALTA | `backend/src/main/java/com/tufondo/ahorros/api/controller/AhorroController.java` |
| 🟠 ALTA | `backend/src/main/java/com/tufondo/ahorros/application/usecase/ListarMovimientosUseCase.java` |
| 🟡 MEDIA | `frontend-web/src/app/(dashboard)/dashboard/cuentas/[numero]/movimientos/page.tsx` |
| ✅ OK | `backend/src/main/java/com/tufondo/ahorros/infrastructure/persistence/jpa/MovimientoJpaRepository.java` |
| ✅ OK | `backend/src/main/java/com/tufondo/ahorros/infrastructure/persistence/adapter/MovimientoRepositoryImpl.java` |
| ✅ OK | `frontend-web/src/app/api/cuentas/[numeroCuenta]/movimientos/route.ts` |
| ✅ OK | `frontend-web/src/lib/api/client.ts` |

---

## CONCLUSIÓN

El PR #66 resuelve el issue #42 funcionalmente. La lógica de filtros combinados está correctamente implementada. Sin embargo, **se recomienda corregir las 2 vulnerabilidades de alta severidad antes de merge a main**, específicamente la validación de parámetros de paginación y la validación de rango de fechas.

**Recomendación:** APPROVED con condiciones - corregir hallazgos ALTA antes de merge.
