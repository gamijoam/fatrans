# AUDITORÍA DE SEGURIDAD: Plan Técnico Issue #61 - Catálogo de Créditos

## RESUMEN EJECUTIVO

| Categoría | CRÍTICA | ALTA | MEDIA | Total |
|-----------|---------|------|-------|-------|
| Seguridad | 3 | 2 | 1 | 6 |
| Arquitectura | 1 | 2 | 0 | 3 |
| Rendimiento | 0 | 1 | 1 | 2 |
| **TOTAL** | **4** | **5** | **2** | **11** |

**Estado General:** ⚠️ **El plan técnico tiene gaps significativos que requieren corrección antes de implementación.**

---

## 🔴 VIOLACIONES CRÍTICAS

### 1. [CRÍTICA] Duplicación de Funcionalidad - Endpoints Ya Existentes

**Archivo:** `backend/src/main/java/com/tufondo/creditos/api/controller/CreditoController.java:120-140`

**Descripción:** El plan propone implementar los endpoints `GET /api/v1/creditos/tipos-credito` y `GET /api/v1/creditos/tipos-credito/{id}`, pero estos **YA EXISTEN** en el sistema actual con los UseCases `ListarTiposCreditoUseCase` y `ObtenerTipoCreditoUseCase`.

```java
// Líneas 120-126 - Endpoint YA EXISTE
@GetMapping("/creditos/tipos-credito")
@PreAuthorize("hasAnyRole('SOCIO', 'ADMIN')")
@Operation(summary = "Listar tipos de crédito disponibles")
public ResponseEntity<Map<String, Object>> listarTiposCredito(Authentication authentication) {
    List<TipoCreditoResponse> tipos = listarTiposCreditoUseCase.ejecutar();
    return ResponseEntity.ok(Map.of("tiposCredito", tipos));
}

// Líneas 132-140 - Endpoint YA EXISTE
@GetMapping("/creditos/tipos-credito/{id}")
@PreAuthorize("hasAnyRole('SOCIO', 'ADMIN')")
@Operation(summary = "Consultar tipo de crédito específico")
public ResponseEntity<TipoCreditoResponse> obtenerTipoCredito(
        @PathVariable Long id, Authentication authentication) {
    TipoCreditoResponse tipo = obtenerTipoCreditoUseCase.ejecutar(id);
    return ResponseEntity.ok(tipo);
}
```

**Impacto:** Desperdicio de recursos de desarrollo, potencial inconsistencia de datos si se implementa redundancia, confusión en mantenimiento futuro.

**Corrección:** 
- **Si el objetivo es una nueva feature (ej: cacheo, paginación, filtros avanzados):** Renombrar y extender la funcionalidad existente.
- **Si es solo consulta:** Eliminar esta tarea del issue y documentar que la funcionalidad ya existe.

---

### 2. [CRÍTICA] XSS: Campo `descripcion` Sin Sanitización en Respuesta

**Archivo:** `backend/src/main/java/com/tufondo/creditos/application/dto/TipoCreditoResponse.java:22`

**Descripción:** El campo `descripcion` se mapea directamente sin validación ni sanitización:

```java
public TipoCreditoResponse toResponse(TipoCredito tipoCredito) {
    // ...
    .descripcion(tipoCredito.getDescripcion())  // SIN SANITIZAR
    // ...
}
```

**Impacto:** Si un atacante logra insertar código malicioso en el campo `descripcion` de la base de datos (mediante SQL injection en admin panel, acceso directo DB, etc.), este script se reflejaría en el frontend causando XSS.

**Corrección:**
```java
// Opción 1: Sanitización en Mapper
import org.apache.commons.text.StringEscapeUtils;

.descripcion(tipoCredito.getDescripcion() != null ? 
    StringEscapeUtils.escapeHtml4(tipoCredito.getDescripcion()) : null)

// Opción 2: Validación en Entity (más robusto)
@Column(name = "descripcion", length = 500)
@SafeHtml(message = "La descripción contiene contenido no permitido")
private String descripcion;
```

---

### 3. [CRÍTICA] Sin Rate Limiting en Endpoints de Catálogo

**Archivo:** `backend/src/main/java/com/tufondo/creditos/infrastructure/security/SimulacionRateLimitFilter.java:36`

**Descripción:** El `SimulacionRateLimitFilter` **solo** aplica rate limiting al endpoint `/simulador`. Los endpoints de catálogo `tipos-credito` NO tienen rate limiting:

```java
private static final Pattern PATTERN_SIMULADOR = Pattern.compile("/api/v1/simulador$");
// ^^^ SOLO匹配 simulador, NO tipos-credito
```

**Impacto:** 
- **Enumeración de productos:** Un atacante podría hacer brute-force para descubrir todos los IDs de tipos de crédito.
- **DoS:** Solicitudes masivas sin límite podrían degrar el servicio.
- **Information Disclosure:** La base de datos de productos crediticios es información competitiva sensible.

**Corrección:**
```java
// Agregar patrón para catálogo
private static final Pattern PATTERN_CATALOGO = Pattern.compile("/api/v1/creditos/tipos-credito.*");

// En doFilterInternal:
if (!esSimulador(uri) && !esCatalogo(uri)) {
    filterChain.doFilter(request, response);
    return;
}

// Nuevo bucket con límites específicos para catálogo
private static final int CATALOGO_POR_MINUTO = 60; // Más permisivo que simulador
```

---

## 🟠 VIOLACIONES DE ARQUITECTURA

### 4. [ALTA] Gestión de Memoria: Sin Paginación en Listado

**Archivo:** `backend/src/main/java/com/tufondo/creditos/application/usecase/ListarTiposCreditoUseCase.java:27-34`

**Descripción:** El método `ejecutar()` retorna `List<TipoCreditoResponse>` sin paginación:

```java
@Transactional(readOnly = true)
public List<TipoCreditoResponse> ejecutar() {
    List<TipoCredito> tipos = tipoCreditoRepository.listarActivos();
    // ^^^ Carga TODOS los registros en memoria
    log.info("Listados {} tipos de crédito activos", tipos.size());
    return tipos.stream().map(mapper::toResponse).collect(Collectors.toList());
}
```

**Impacto:**
- Si hay 100+ tipos de crédito, se consume memoria innecesariamente.
- En escenarios de alto tráfico concurrente, podría causar `OutOfMemoryError`.
- Violación del principio de mínima exponencia (exponer solo lo necesario).

**Corrección:**
```java
// UseCase con paginación
public Page<TipoCreditoResponse> ejecutar(Pageable pageable) {
    Page<TipoCredito> tipos = tipoCreditoRepository.listarActivos(pageable);
    return tipos.map(mapper::toResponse);
}

// Repository
Page<TipoCredito> listarActivos(Pageable pageable);

// Controller
@GetMapping("/creditos/tipos-credito")
public ResponseEntity<Page<TipoCreditoResponse>> listarTiposCredito(
        @PageableDefault(size = 10) Pageable pageable) {
    return ResponseEntity.ok(listarTiposCreditoUseCase.ejecutar(pageable));
}
```

---

### 5. [ALTA] Fuga de Información: Campos Sensibles Expuestos a Rol SOCIO

**Archivo:** `backend/src/main/java/com/tufondo/creditos/application/dto/TipoCreditoResponse.java:28-32`

**Descripción:** El DTO expone campos que podrían considerarse sensibles desde el punto de vista competitivo/bancario:

```java
private BigDecimal porcentajeRequerimientoColateral;  // Ventaja competitiva
private BigDecimal comisionApertura;                  // Información sensitiva
private BigDecimal penalidadMoraTasa;                  // Variables de pricing
```

**Impacto:** Un SOCIO puede ver estructuras de pricing interno que podrían ser utilizadas por competidores o para arbitrage.

**Corrección:** Evaluar si estos campos son necesarios en la respuesta para el rol SOCIO:
```java
// Crear DTO específico para consulta pública
public class TipoCreditoPublicResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal tasaInteresAnual;
    private Integer plazoMinimoMeses;
    private Integer plazoMaximoMeses;
    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;
    // NO incluir: comisionApertura, penalidadMoraTasa, porcentajeRequerimientoColateral
}

// Mapper condicional según rol
public TipoCreditoResponse toResponsePublic(TipoCredito tipoCredito) { ... }
```

---

### 6. [ALTA] Seguridad: Falta Validación de ID Negativo o Cero

**Archivo:** `backend/src/main/java/com/tufondo/creditos/application/usecase/ObtenerTipoCreditoUseCase.java:26-30`

**Descripción:** El método recibe `Long id` sin validación de rango:

```java
@Transactional(readOnly = true)
public TipoCreditoResponse ejecutar(Long id) {
    TipoCredito tipo = tipoCreditoRepository.buscarPorId(id)
        .orElseThrow(() -> new CreditoNoEncontradoException(id));
    // ^^^ Si id <= 0, podría generar error de BD o comportamiento inesperado
    return mapper.toResponse(tipo);
}
```

**Impacto:** Un ID negativo o cero podría causar:
- Excepciones no controladas si la BD no maneja este caso.
- Información de debug en responses de error.
- Potencial para timing attacks (medir tiempo de respuesta para enumeração).

**Corrección:**
```java
@Transactional(readOnly = true)
public TipoCreditoResponse ejecutar(Long id) {
    if (id == null || id <= 0) {
        throw new IllegalArgumentException("ID de tipo de crédito inválido: " + id);
    }
    TipoCredito tipo = tipoCreditoRepository.buscarPorId(id)
        .orElseThrow(() -> new CreditoNoEncontradoException(id));
    return mapper.toResponse(tipo);
}
```

---

### 7. [ALTA] Consultas Sin Filtro `activo` en `buscarPorId`

**Archivo:** `backend/src/main/java/com/tufondo/creditos/infrastructure/persistence/adapter/TipoCreditoRepositoryImpl.java:37-39`

**Descripción:** El método `buscarPorId` retorna cualquier registro, incluyendo inactivos:

```java
@Override
public Optional<TipoCredito> buscarPorId(Long id) {
    return jpaRepository.findById(id).map(this::toDomain);
    // ^^^ No filtra por activo - retorna incluso tipos inactivos
}
```

**Impacto:** Un ADMIN podría consultar el detalle de un tipo de crédito que ya no está activo, causando:
- Inconsistencia en lógico de negocio (solicitar crédito de producto inactivo).
- Exposición de información histórica sensible.

**Corrección:**
```java
@Override
public Optional<TipoCredito> buscarPorId(Long id) {
    return jpaRepository.findByIdAndActivoTrue(id).map(this::toDomain);
}

// JpaRepository
Optional<TipoCreditoEntity> findByIdAndActivoTrue(Long id);
```

---

## 🟡 MEJORAS RECOMENDADAS (≤3 hallazgos)

### 8. [MEDIA] Missing Audit Log en Acceso a Catálogo

**Archivo:** `backend/src/main/java/com/tufondo/creditos/application/usecase/ListarTiposCreditoUseCase.java:30`

**Descripción:** Solo hay log a nivel INFO, no hay audit trail de seguridad:

```java
log.info("Listados {} tipos de crédito activos", tipos.size());
// ^^^ Solo info, no auditoría de seguridad (quién, cuándo, IP, resultado)
```

**Recomendación:**
```java
// Agregar auditoría de seguridad
auditService.logAccesoCatalogo(
    AuditAction.CATALOG_ACCESS,
    usuario.getSocioId(),
    ipOrigen,
    tipos.size(),
    Boolean.SUCCESS
);
```

---

### 9. [MEDIA] Sin Validación de Tasa Interés Razonable

**Archivo:** `backend/src/main/java/com/tufondo/creditos/domain/model/TipoCredito.java:38-46`

**Descripción:** Los métodos `validaMonto` y `validaPlazo` existen pero no hay validación de rango razonable para `tasaInteresAnual`:

```java
// Solo valida límites de rango del tipo, no si la tasa es razonable
public boolean validaMonto(BigDecimal monto) {
    if (montoMinimo != null && monto.compareTo(montoMinimo) < 0) return false;
    if (montoMaximo != null && monto.compareTo(montoMaximo) > 0) return false;
    return true;
}
```

**Recomendación:**
```java
public boolean validaTasa(BigDecimal tasa) {
    if (tasa == null) return false;
    // Tasas negativas o > 100% son invalidas
    if (tasa.compareTo(BigDecimal.ZERO) < 0 || tasa.compareTo(BigDecimal.ONE) > 0) {
        return false;
    }
    return true;
}
```

---

## ARCHIVOS AFECTADOS

| Prioridad | Archivo | Cambio Requerido |
|-----------|---------|-----------------|
| CRÍTICA | `backend/src/main/java/com/tufondo/creditos/api/controller/CreditoController.java` | Verificar si nueva implementación es necesaria o duplicada |
| CRÍTICA | `backend/src/main/java/com/tufondo/creditos/application/mapper/CreditosDTOMapper.java:25` | Agregar sanitización HTML para `descripcion` |
| CRÍTICA | `backend/src/main/java/com/tufondo/creditos/infrastructure/security/SimulacionRateLimitFilter.java` | Agregar rate limiting para endpoints de catálogo |
| ALTA | `backend/src/main/java/com/tufondo/creditos/application/usecase/ListarTiposCreditoUseCase.java` | Implementar paginación |
| ALTA | `backend/src/main/java/com/tufondo/creditos/application/dto/TipoCreditoResponse.java` | Crear DTO público sin campos sensibles para rol SOCIO |
| ALTA | `backend/src/main/java/com/tufondo/creditos/application/usecase/ObtenerTipoCreditoUseCase.java:26` | Validar que id > 0 |
| ALTA | `backend/src/main/java/com/tufondo/creditos/infrastructure/persistence/adapter/TipoCreditoRepositoryImpl.java` | Filtrar por `activo=true` en buscarPorId |

---

## CONCLUSIÓN

El plan técnico para el issue #61 presenta **4 hallazgos CRÍTICOS** que deben ser corregidos antes de cualquier implementación:

1. **Duplicación de funcionalidad** - Verificar si la feature ya existe
2. **XSS en campo descripcion** - Sanitización obligatoria
3. **Sin rate limiting** - Vulnerable a DoS y enumeración
4. **Arquitectura sin paginación** - Problemas de memoria y rendimiento

**Recomendación:** Antes de proceder con la implementación, se debe:
1. Confirmar si el objetivo es extender la funcionalidad existente o crear algo nuevo
2. Corregir la vulnerabilidad XSS en el mapper
3. Implementar rate limiting para los endpoints de catálogo
4. Diseñar una estrategia de paginación escalable