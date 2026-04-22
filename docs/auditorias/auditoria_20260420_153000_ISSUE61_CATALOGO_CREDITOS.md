# AUDITORÍA DE SEGURIDAD: Issue #61 - Catálogo de Créditos

**Fecha:** 2026-04-20  
**Auditor:** Lead Software Architect & Cyber-Security Auditor  
**Proyecto:** Fondo de Ahorro - Plataforma FinTech  
**Alcance:** Backend (Java/Spring Boot) + Frontend (Flutter)

---

## RESUMEN EJECUTIVO

| Categoría | CRÍTICA | ALTA | MEDIA |
|----------|---------|------|-------|
| Seguridad | 1 | 2 | 1 |
| Arquitectura | 2 | 3 | 0 |
| Código | 0 | 2 | 2 |
| **TOTAL** | **3** | **7** | **3** |

### Estado General: ⚠️ REQUIERE CORRECCIÓN

La implementación del Issue #61 presenta vulnerabilidades críticas y altas que deben corregirse antes de pasar a producción. Se identificaron 10 hallazgos, de los cuales 3 son CRÍTICOS y 7 son ALTOS.

---

## VIOLACIONES CRÍTICAS (🔴)

### 1. Campo `activo` expuesto en DTO público

- **[CRITICIDAD]**: 🔴 CRÍTICA
- **[CATEGORÍA]**: Seguridad - OWASP A01:2021 Broken Access Control
- **[DESCRIPCIÓN]**: El DTO `TipoCreditoResponse` incluye el campo `activo` (línea 32), exponiendo información sensible sobre el estado de productos crediticios. Un atacante podría inferir qué productos están activos/inactivos y enfocar ataques a productos específicos.

**Archivo:** `backend/src/main/java/com/tufondo/creditos/application/dto/TipoCreditoResponse.java:32`

```java
private Boolean activo;  // ❌ NO DEBERÍA ESTAR EN RESPUESTA PÚBLICA
```

- **[IMPACTO]**: En producción bancaria, esta información podría revelar productos descontinuados o en mantenimiento, facilitando ataques dirigidos.

- **[RECOMENDACIÓN]**: Crear un DTO público separado (`TipoCreditoPublicResponse`) que excluya el campo `activo`:

```java
// NUEVO: TipoCreditoPublicResponse.java
@Data
@Builder
public class TipoCreditoPublicResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;  // Sanitizada
    private BigDecimal tasaInteresAnual;
    private Integer plazoMinimoMeses;
    private Integer plazoMaximoMeses;
    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;
    private BigDecimal porcentajeRequerimientoColateral;
    private BigDecimal comisionApertura;
    private BigDecimal penalidadMoraTasa;
    private Integer diasGracia;
    // ❌ NO incluir activo
}
```

---

### 2. Backend ignora parámetros de paginación

- **[CRITICIDAD]**: 🔴 CRÍTICA
- **[CATEGORÍA]**: Arquitectura - Violación de Contrato API
- **[DESCRIPCIÓN]**: El UseCase `ListarTiposCreditoUseCase.ejecutar()` retorna `List<TipoCreditoResponse>` sin aceptar parámetros de paginación. Sin embargo, el frontend envía `page` y `size` que son completamente ignorados. Esto genera un contrato inconsistente donde la API del frontend espera metadatos de paginación (`tiposCredito`, `page`, `totalPages`, etc.) pero el backend retorna una lista simple.

**Archivo:** `backend/src/main/java/com/tufondo/creditos/application/usecase/ListarTiposCreditoUseCase.java:28-34`

```java
@Transactional(readOnly = true)
public List<TipoCreditoResponse> ejecutar() {  // ❌ Sin parámetros page/size
    List<TipoCredito> tipos = tipoCreditoRepository.listarActivos();
    return tipos.stream()
        .map(mapper::toResponse)
        .collect(Collectors.toList());
}
```

- **[IMPACTO]**: Con 7 tipos de crédito esto no es crítico, pero si el catálogo crece, el frontend no podrá paginar correctamente y los usuarios experimentarán lentitud al cargar grandes volúmenes de datos.

- **[RECOMENDACIÓN]**: Modificar el UseCase para aceptar parámetros de paginación:

```java
@Transactional(readOnly = true)
public Page<TipoCreditoResponse> ejecutar(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
    Page<TipoCredito> tipos = tipoCreditoRepository.listarActivos(pageable);
    return tipos.map(mapper::toResponse);
}
```

---

### 3. Ausencia de sanitización XSS en descripciones

- **[CRITICIDAD]**: 🔴 CRÍTICA
- **[CATEGORÍA]**: Seguridad - OWASP XSS
- **[DESCRIPCIÓN]**: La descripción del issue menciona `XssSanitizer.java`, pero **no existe tal archivo** en el codebase. Las descripciones de productos crediticios se almacenan y retornan sin sanitización, permitiendo XSS存储型攻击.

**Archivos afectados:**
- `backend/src/main/java/com/tufondo/creditos/domain/model/TipoCredito.java:20` - Campo `descripcion` sin sanitizar
- `frontend-mobile/lib/features/credito_catalogo/presentation/widgets/credito_producto_card.dart:103` - Renderiza `producto.descripcion` directamente
- `frontend-mobile/lib/features/credito_catalogo/presentation/screens/credito_detalle_screen.dart:251` - Renderiza `producto.descripcion` directamente

```dart
// frontend-mobile/lib/features/credito_catalogo/presentation/widgets/credito_producto_card.dart:103
Text(
  producto.descripcion,  // ❌ Peligro: podría contener scripts
  maxLines: 2,
  overflow: TextOverflow.ellipsis,
```

- **[IMPACTO]**: Si un atacante logra insertar `<script>alert('XSS')</script>` en el campo `descripcion` de la base de datos, todos los usuarios que visualicen el catálogo执行arán código malicioso.

- **[RECOMENDACIÓN]**: Implementar sanitización en ambos extremos:

**Backend - Crear XssSanitizer.java:**
```java
@Component
public class XssSanitizer {
    private static final Pattern SCRIPT_PATTERN = 
        Pattern.compile("<script[^>*]*>.*?</script>", Pattern.CASE_INSENSITIVE);
    
    public String sanitize(String input) {
        if (input == null) return null;
        return SCRIPT_PATTERN.matcher(input).replaceAll("");
    }
}
```

**Frontend - Usar widget de texto seguro:**
```dart
// Reemplazar Text() directo con HtmlEscape
HtmlEscape(
  child: Text(
    producto.descripcion,
    style: TextStyle(color: Colors.grey[600]),
  ),
)
```

---

## VIOLACIONES ALTAS (🟠)

### 4. Validación de ID ausente en ObtenerTipoCreditoUseCase

- **[CRITICIDAD]**: 🟠 ALTA
- **[CATEGORÍA]**: Seguridad - OWASP Numeric Validation
- **[DESCRIPCIÓN]**: El UseCase no valida que el `id` sea mayor que 0 antes de consultar la base de datos. Un atacante podría enviar `id=-1` o `id=0` causando comportamiento inesperado.

**Archivo:** `backend/src/main/java/com/tufondo/creditos/application/usecase/ObtenerTipoCreditoUseCase.java:26-30`

```java
public TipoCreditoResponse ejecutar(Long id) {
    // ❌ Sin validación de id <= 0
    TipoCredito tipo = tipoCreditoRepository.buscarPorId(id)
        .orElseThrow(() -> new CreditoNoEncontradoException(id));
```

- **[IMPACTO]**: Aunque Spring Boot convierte `id=0` en null para tipos primitivos, un atacante podría causar consultas innecesarias o revelar información mediante IDs secuenciales (enumeración).

- **[RECOMENDACIÓN]**: Agregar validación:

```java
public TipoCreditoResponse ejecutar(Long id) {
    if (id == null || id <= 0) {
        throw new IllegalArgumentException("ID de tipo de crédito inválido");
    }
    TipoCredito tipo = tipoCreditoRepository.buscarPorId(id)
        .orElseThrow(() -> new CreditoNoEncontradoException(id));
```

---

### 5. Método `buscarPorIdActivo` prometido pero no implementado

- **[CRITICIDAD]**: 🟠 ALTA
- **[CATEGORÍA]**: Arquitectura - Inconsistencia de Contrato
- **[DESCRIPCIÓN]**: El issue #61 promete un nuevo método `buscarPorIdActivo` en `TipoCreditoRepository`, pero el repositorio solo tiene `buscarPorId(Long id)` genérico que retorna un tipo de crédito sin verificar si está activo.

**Archivo:** `backend/src/main/java/com/tufondo/creditos/domain/repository/TipoCreditoRepository.java:22`

```java
Optional<TipoCredito> buscarPorId(Long id);  // ❌ No filtra por activo
```

- **[IMPACTO]**: Un producto desactivado podría ser consultado directamente si se conoce su ID, exponiendo información innecesaria.

- **[RECOMENDACIÓN]**: Agregar el método prometido:

```java
/**
 * Busca tipo de crédito por ID solo si está activo.
 */
Optional<TipoCredito> buscarPorIdActivo(Long id);
```

Y en el JPA repository:
```java
Optional<TipoCreditoEntity> findByIdAndActivoTrue(Long id);
```

---

### 6. Rate Limiting inconsistente entre endpoints

- **[CRITICIDAD]**: 🟠 ALTA
- **[CATEGORÍA]**: Seguridad - OWASP Rate Limiting
- **[DESCRIPCIÓN]**: El issue menciona `CatalogoRateLimitFilter` con 60 req/min para el catálogo, pero solo existe `SimulacionRateLimitFilter` para `/simulador`. Los endpoints de catálogo NO tienen rate limiting implementado.

**Archivo:** `backend/src/main/java/com/tufondo/creditos/infrastructure/security/SimulacionRateLimitFilter.java`

- El filtro solo aplica a `/api/v1/simulador`
- Los endpoints `/creditos/tipos-credito` y `/creditos/tipos-credito/{id}` no tienen límite

- **[IMPACTO]**: Un atacante podría hacer scraping completo del catálogo con Request rápidos.

- **[RECOMENDACIÓN]**: Crear `CatalogoRateLimitFilter` para los endpoints de catálogo:

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class CatalogoRateLimitFilter extends OncePerRequestFilter {
    private static final int PETICIONES_POR_MINUTO = 60;
    private static final Map<String, Bucket> BUCKETS = new ConcurrentHashMap<>();
    private static final Pattern PATTERN_CATALOGO = Pattern.compile(
        "/api/v1/creditos/tipos-credito.*");
    
    // Implementar lógica similar a SimulacionRateLimitFilter
}
```

---

### 7. Falta de validación de parámetros de paginación en Controller

- **[CRITICIDAD]**: 🟠 ALTA
- **[CATEGORÍA]**: Seguridad - Input Validation
- **[DESCRIPCIÓN]**: El controller de cuotas valida page/size (línea 240-241), pero los endpoints de tipos de crédito no tienen validación para `page` y `size`.

**Archivo:** `backend/src/main/java/com/tufondo/creditos/api/controller/CreditoController.java:120-126`

```java
@GetMapping("/creditos/tipos-credito")
@PreAuthorize("hasAnyRole('SOCIO', 'ADMIN')")
public ResponseEntity<Map<String, Object>> listarTiposCredito(Authentication authentication) {
    List<TipoCreditoResponse> tipos = listarTiposCreditoUseCase.ejecutar();
    return ResponseEntity.ok(Map.of("tiposCredito", tipos));  // ❌ Sin page/size
}
```

- **[IMPACTO]**: Valores negativos o extremadamente grandes para `page` o `size` podrían causar comportamiento inesperado.

- **[RECOMENDACIÓN]**: Agregar validación explícita:

```java
@GetMapping("/creditos/tipos-credito")
public ResponseEntity<?> listarTiposCredito(
    @RequestParam(defaultValue = "0") @Min(0) int page,
    @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
    Authentication authentication) {
```

---

### 8. Inconsistencia de respuesta API entre listado y detalle

- **[CRITICIDAD]**: 🟠 ALTA
- **[CATEGORÍA]**: Arquitectura - API Inconsistency
- **[DESCRIPCIÓN]**: El listado retorna estructura envuelta:
```json
{"tiposCredito": [...], "page": 0, "size": 20, ...}
```
Pero el detalle retorna el objeto directo:
```json
{"id": 1, "codigo": "...", ...}
```

**Archivos:**
- `backend/src/main/java/com/tufondo/creditos/api/controller/CreditoController.java:124-125`
- `backend/src/main/java/com/tufondo/creditos/api/controller/CreditoController.java:138`

- **[IMPACTO]**: El frontend debe manejar estructuras diferentes, aumentando la complejidad y posibilidad de errores.

- **[RECOMENDACIÓN]**: Estandarizar la respuesta. Crear un wrapper `CatalogoResponse` para consistencia:

```java
public record CatalogoResponse<T>(
    List<T> items,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {}
```

---

### 9. Scroll controller no verifica estado antes de dispose

- **[CRITICIDAD]**: 🟠 ALTA
- **[CATEGORÍA]**: Código - Null Safety / Memory
- **[DESCRIPCIÓN]**: El `_scrollController.dispose()` se llama en `dispose()` pero no se verifica si el widget está montado antes de llamar a `loadMore()`.

**Archivo:** `frontend-mobile/lib/features/credito_catalogo/presentation/screens/credito_catalogo_screen.dart:46-56`

```dart
@override
void dispose() {
  _scrollController.dispose();  // ⚠️ Necesario pero correcto
  super.dispose();
}

void _onScroll() {
  if (_isBottom) {
    context.read<CreditoCatalogoCubit>().loadMore();  // ⚠️ Podría fallar si widget desmntado
  }
}
```

- **[IMPACTO]**: Si el widget se desmonta durante un scroll, podría lanzarse `LateError` al intentar usar el context.

- **[RECOMENDACIÓN]**: Verificar `mounted` antes de invocar métodos del cubit:

```dart
void _onScroll() {
  if (!mounted) return;  // ❌ Agregar verificación
  if (_isBottom) {
    context.read<CreditoCatalogoCubit>().loadMore();
  }
}
```

---

### 10. Optimistic locking expuesto en respuesta pública

- **[CRITICIDAD]**: 🟠 ALTA
- **[CATEGORÍA]**: Seguridad - Information Disclosure
- **[DESCRIPCIÓN]**: El campo `version` del optimistic locking se mapea en el `toDomain()` pero no se excluye del DTO público. Aunque no está en `TipoCreditoResponse`, el mapper lo incluye.

**Archivo:** `backend/src/main/java/com/tufondo/creditos/infrastructure/persistence/adapter/TipoCreditoRepositoryImpl.java:83`

```java
.version(entity.getVersion())  // ⚠️ Mapeado en domain, podría filtrarse
```

- **[IMPACTO]**: El `version` de JPA podría filtrarse en respuestas JSON si alguien agrega el campo al DTO posteriormente.

- **[RECOMENDACIÓN]**: Excluir explícitamente `version` en el mapper del dominio:

```java
private TipoCredito toDomain(TipoCreditoEntity entity) {
    return TipoCredito.builder()
        // ... otros campos ...
        // NO incluir .version() - es interno de infraestructura
        .build();
}
```

---

## MEJORAS RECOMENDADAS (🟡)

### 11. Cacheo de datos del catálogo

- **[CATEGORÍA]**: Rendimiento
- **[DESCRIPCIÓN]**: Los tipos de crédito rara vez cambian, pero se consultan sin cache. Un atacante podría hacer múltiples запросов al endpoint.

- **[RECOMENDACIÓN]**: Implementar cacheo con Spring Cache:

```java
@Cacheable(value = "tiposCredito", unless = "#result.isEmpty()")
public List<TipoCreditoResponse> ejecutar() {
    return tipoCreditoRepository.listarActivos()...
}

@CacheEvict(value = "tiposCredito", allEntries = true)
public void guardar(TipoCredito tipoCredito) {...}
```

---

### 12. El widget CreditoProductoCard usa colores hardcodeados

- **[CATEGORÍA]**: Mantenibilidad - Code Smell
- **[DESCRIPCIÓN]**: El card usa colores como `Colors.green.shade100` en lugar de theme.

**Archivo:** `frontend-mobile/lib/features/credito_catalogo/presentation/widgets/credito_producto_card.dart:72`

```dart
color: Colors.green.shade100,  // ⚠️ Hardcoded
```

- **[RECOMENDACIÓN]**: Usar `Theme.of(context).colorScheme` para consistencia.

---

### 13. Falta de código de error estructurado en frontend

- **[CATEGORÍA]**: Código - Error Handling
- **[DESCRIPCIÓN]**: El `CreditoCatalogoError` solo tiene `message` string, no código de error para maneja diferenciada.

**Archivo:** `frontend-mobile/lib/features/credito_catalogo/presentation/cubit/credito_catalogo_state.dart:61-68`

```dart
class CreditoCatalogoError extends CreditoCatalogoState {
  final String message;  // ⚠️ Solo string, sin código
  final List<CreditoProducto>? productos;
```

- **[RECOMENDACIÓN]**: Agregar campo `errorCode` para manejo diferenciado:

```dart
enum CreditoCatalogoErrorCode {
  networkError,
  serverError,
  notFound,
  unauthorized,
}

class CreditoCatalogoError extends CreditoCatalogoState {
  final CreditoCatalogoErrorCode code;
  final String message;
```

---

## ARCHIVOS AFECTADOS

| Prioridad | Archivo |
|-----------|---------|
| 🔴 CRÍTICA | `backend/src/main/java/com/tufondo/creditos/application/dto/TipoCreditoResponse.java` |
| 🔴 CRÍTICA | `backend/src/main/java/com/tufondo/creditos/application/usecase/ListarTiposCreditoUseCase.java` |
| 🔴 CRÍTICA | `frontend-mobile/lib/features/credito_catalogo/presentation/widgets/credito_producto_card.dart` |
| 🔴 CRÍTICA | `frontend-mobile/lib/features/credito_catalogo/presentation/screens/credito_detalle_screen.dart` |
| 🟠 ALTA | `backend/src/main/java/com/tufondo/creditos/application/usecase/ObtenerTipoCreditoUseCase.java` |
| 🟠 ALTA | `backend/src/main/java/com/tufondo/creditos/domain/repository/TipoCreditoRepository.java` |
| 🟠 ALTA | `backend/src/main/java/com/tufondo/creditos/infrastructure/persistence/jpa/TipoCreditoJpaRepository.java` |
| 🟠 ALTA | `backend/src/main/java/com/tufondo/creditos/api/controller/CreditoController.java` |
| 🟠 ALTA | `backend/src/main/java/com/tufondo/creditos/infrastructure/persistence/adapter/TipoCreditoRepositoryImpl.java` |
| 🟠 ALTA | `frontend-mobile/lib/features/credito_catalogo/presentation/screens/credito_catalogo_screen.dart` |
| 🟡 MEDIA | `frontend-mobile/lib/features/credito_catalogo/domain/entities/credito_producto_entity.dart` |
| 🟡 MEDIA | `frontend-mobile/lib/features/credito_catalogo/presentation/cubit/credito_catalogo_state.dart` |
| 🟡 MEDIA | `frontend-mobile/lib/features/credito_catalogo/presentation/widgets/credito_producto_card.dart` |

---

## CONCLUSIÓN

La implementación del Issue #61 requiere correcciones urgentes antes de producción:

1. **CRÍTICO**: Crear DTO público sin campo `activo`
2. **CRÍTICO**: Implementar paginación real en backend
3. **CRÍTICO**: Crear `XssSanitizer.java` y sanitizar en frontend
4. **ALTA**: Validar ID en ObtenerTipoCreditoUseCase
5. **ALTA**: Implementar método `buscarPorIdActivo`
6. **ALTA**: Crear `CatalogoRateLimitFilter` para 60 req/min

**Recomendación**: NO MERGEAR hasta que las vulnerabilidades CRÍTICAS y ALTAS sean resueltas.