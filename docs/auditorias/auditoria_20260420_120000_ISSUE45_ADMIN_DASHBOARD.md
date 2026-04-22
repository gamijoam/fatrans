# AUDITORÍA DE SEGURIDAD - ISSUE #45 (Admin Dashboard Real Stats)

**Fecha:** 2026-04-20
**Auditor:** Lead Software Architect & Cyber-Security Auditor
**Módulo Auditado:** Admin Dashboard - Estadísticas Aggregadas
**Estado:** 🟠 REQUIERE CORRECCIÓN ANTES DE IMPLEMENTACIÓN

---

## RESUMEN EJECUTIVO

| Severidad | Total |
|-----------|-------|
| 🔴 CRÍTICA | 2 |
| 🟠 ALTA | 3 |
| 🟡 MEDIA | 1 |

### Veredicto General

La especificación técnica para el endpoint `GET /api/v1/admin/dashboard/estadisticas` presenta **vulnerabilidades de seguridad críticas** que deben abordarse antes de la implementación:

1. **Falta de Rate Limiting** para endpoint admin - riesgo DoS
2. **Ausencia de Audit Logging** específico para acceso a estadísticas agregadas - incumplimiento de compliance (PCI-DSS)
3. **Posible fuga de información** mediante timing attacks y enumeration de datos agregados
4. **Falta de validación de parámetros** de consulta (date ranges)

---

## VIOLACIONES CRÍTICAS (🔴)

### 1. Ausencia de Rate Limiting en Endpoint Admin

- **[CRÍTICA]**: **No existe rate limiting para `/api/v1/admin/dashboard/estadisticas`**
- **[CATEGORÍA]**: Seguridad - OWASP A04:2021 (Insecure Design)
- **[DESCRIPCIÓN]**: A diferencia del endpoint `/simulador` que tiene `SimulacionRateLimitFilter` (10 req/min) y `/login` con `LoginRateLimitFilter` (5 req/min), el endpoint de estadísticas admin NO tiene protección contra abuso.

El endpoint consume datos de 5 repositorios diferentes:
- `SocioRepository`
- `CuentaAhorroRepository`
- `MovimientoRepository`
- `SolicitudCreditoRepository`
- `AmortizacionRepository`

**Consulta ejemplo desde `AdminKYCController.java:117-129`:**
```java
long pendientes = verificacionRepository.countByEstado(EstadoVerificacion.PENDIENTE);
long enRevision = verificacionRepository.countByEstado(EstadoVerificacion.EN_REVISION);
long aprobados = verificacionRepository.countByEstado(EstadoVerificacion.APROBADO);
// ... 5 repositorios × múltiples consultas = ~15-25 queries expensive
```

- **[IMPACTO]**: Un atacante (o admin malicioso) podría:
  - Realizar ataques de denegación de servicio inundando el endpoint
  - Causar degradación de rendimiento del sistema completo
  - Realizar enumeración estadística mediante múltiples请求

- **[RECOMENDACIÓN]**: Implementar `AdminDashboardRateLimitFilter`:
```java
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AdminDashboardRateLimitFilter extends OncePerRequestFilter {

    private static final int REQUESTS_PER_MINUTE = 30; // Límite generoso para admins
    private static final Map<String, Bucket> BUCKETS = new ConcurrentHashMap<>();
    private static final Pattern PATTERN = Pattern.compile("/api/v1/admin/dashboard/estadisticas");

    @Override
    protected void doFilterInternal(...) {
        // Extraer userId del JWT, no solo IP
        String userId = extraerUserIdDelToken(request);
        String key = userId != null ? userId : extractClientIp(request);
        Bucket bucket = BUCKETS.computeIfAbsent(key, this::createBucket);
        // ... rate limit logic
    }
}
```

---

### 2. Ausencia de Audit Logging para Acceso a Estadísticas

- **[CRÍTICA]**: **No se registra el acceso al dashboard admin en `SecurityAuditService`**
- **[CATEGORÍA]**: Seguridad - Compliance (PCI-DSS Req.10, ISO27001 A.12.4.1)
- **[DESCRIPCIÓN]**: El `SecurityAuditService.java` solo registra:
```java
// Solo existe:
logLoginExitoso()
logLoginFallido()
logLogout()
logTokenRefresh()
logCuentaBloqueada()
```

**NO existe método para registrar:**
- Acceso a estadísticas agregadas
- Consulta de datos sensibles de múltiples módulos
- Acciones administrativas

- **[IMPACTO]**: 
  - **Incumplimiento regulatorio**: PCI-DSS requiere logging de TODO acceso a datos de titulares de tarjetas
  - **No hay trazabilidad**: No se puede auditar quién consultó estadísticas del fondo
  - **Detección de fraude**: No hay forma de detectar abuso administratif

- **[RECOMENDACIÓN]**: Agregar al `SecurityAuditService`:
```java
public void logAccesoDashboardEstadisticas(String usuarioId, String ip, 
        DashboardEstadisticasRequest request) {
    SecurityEvent event = SecurityEvent.dashboardEstadisticas(
            UUID.fromString(usuarioId),
            ip,
            request.fechaInicio(),
            request.fechaFin()
    );
    log.info("AUDIT_DASHBOARD [{}] usuario={} ip={} modulo={} tipo={}",
            event.timestamp(),
            event.usuarioId(),
            event.ipAddress(),
            "DASHBOARD_ESTADISTICAS",
            event.tipoEvento()
    );
}
```

---

## VIOLACIONES DE ARQUITECTURA (🟠)

### 3. Falta Validación de Parámetros de Fecha

- **[ALTA]**: **Parámetros `fechaInicio`/`fechaFin` no validados en especificación**
- **[CATEGORÍA]**: Seguridad - OWASP A03:2021 (Injection)
- **[DESCRIPCIÓN]**: La especificación menciona `actividadReciente (nuevosSociosMes, depositosMes, etc.)` pero NO especifica:
- ¿Qué formato de fecha? (ISO-8601, epoch, etc.)
- ¿Valores permitidos? (rango máximo: 1 año, 30 días, etc.)
- ¿Timezone? (UTC, local, etc.)

Los repositorios actuales SÍ usan consultas parametrizadas (`@Param` en JPQL), pero **no hay validación a nivel de controller/DTO**:

```java
// MovimientoJpaRepository.java - USA @Param (correcto)
@Query("SELECT m FROM MovimientoEntity m WHERE m.cuentaAhorroId = :cuentaId " +
       "AND m.fechaMovimiento BETWEEN :fechaInicio AND :fechaFechaFin")
Page<MovimientoEntity> findByCuentaYRangoFechas(
        @Param("cuentaId") UUID cuentaAhorroId,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin,
        Pageable pageable);
```

- **[IMPACTO]**: 
  - Un atacante podría enviar fechas inválidas causando excepciones no manejadas
  - Fechas muy amplias podrían causar queries lentas (full table scans)
  - Timezone handling incorrecto podría filtrar datos accidentalmente

- **[RECOMENDACIÓN]**: Crear DTO de request con validación:
```java
public record DashboardEstadisticasRequest(
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime fechaInicio,
    
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime fechaFin,
    
    @Min(value = 1, message = "El rango no puede ser menor a 1 día")
    @Max(value = 365, message = "El rango no puede exceder 365 días")
    Integer maximoDias
) {
    public LocalDateTime getFechaFinCalculada() {
        return fechaInicio.plusDays(maximoDias != null ? maximoDias : 30);
    }
}
```

---

### 4. Missing `@PreAuthorize` Consistency para SUPER_ADMIN

- **[ALTA]**: **Inconsistencia en uso de roles entre endpoints admin**
- **[CATEGORÍA]**: Arquitectura - Autorización
- **[DESCRIPCIÓN]**: La especificación dice `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")` pero hay inconsistencias en el codebase:

```java
// AdminKYCController.java:113 - USA solo ADMIN
@PreAuthorize("hasRole('ADMIN')")

// CreditoController.java:165 - USA solo ADMIN
@PreAuthorize("hasRole('ADMIN')")

// CreditoController.java:149 - USA ADMIN y SISTEMA
@PreAuthorize("hasAnyRole('ADMIN', 'SISTEMA')")

// DocumentoController.java:101 - USA ADMIN y SISTEMA
@PreAuthorize("hasAnyRole('ADMIN', 'SISTEMA')")
```

**PROBLEMA**: `SUPER_ADMIN` existe en `Rol.java`:
```java
public enum Rol {
    SOCIO,
    ADMIN,
    SUPER_ADMIN  // <-- Existe pero casi no se usa
}
```

- **[IMPACTO]**: 
  - Si `SUPER_ADMIN` tiene privilegios adicionales, los endpoints que solo verifican `ADMIN` podrían no darle acceso
  - Confusión sobre qué rol puede acceder a qué

- **[RECOMENDACIÓN]**: 
  1. Documentar explícitamente la jerarquía de roles
  2. Usar consistentemente `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")` para todos los endpoints admin
  3. O definir una jerarquía con `@RoleHierarchy`

---

### 5. Arquitectura Clean - Controller Hace Demasiado

- **[ALTA]**: **El controller viola principio de responsabilidad única (SRP)**
- **[CATEGORÍA]**: Arquitectura - Clean Architecture
- **[DESCRIPCIÓN]**: Basado en el patrón de `AdminKYCController.java`, la implementación propuesta tendría:

```java
@GetMapping("/admin/dashboard/estadisticas")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public ResponseEntity<DashboardEstadisticasResponse> obtenerEstadisticas() {
    // 15-25 queries a diferentes repositorios
    long sociosActivos = socioRepository.countByEstado(EstadoSocio.ACTIVO);
    long sociosInactivos = socioRepository.countByEstado(EstadoSocio.INACTIVO);
    long cuentasActivas = cuentaAhorroRepository.countByEstado(EstadoCuenta.ACTIVA);
    BigDecimal depositosMes = movimientoRepository.sumDepositosMes();
    // ... más queries
    return ResponseEntity.ok(response);
}
```

- **[IMPACTO]**: 
  - Violación de Clean Architecture: La lógica de negocio está en el controller
  - No hay forma de cachar este cálculoexpensive
  - Violación de DRY si otro endpoint necesita estadísticas similares

- **[RECOMENDACIÓN]**: Crear `ObtenerDashboardEstadisticasUseCase`:
```java
@Application
@RequiredArgsConstructor
public class ObtenerDashboardEstadisticasUseCase {
    
    private final SocioRepository socioRepository;
    private final CuentaAhorroRepository cuentaAhorroRepository;
    private final MovimientoRepository movimientoRepository;
    private final SolicitudCreditoRepository solicitudCreditoRepository;
    private final AmortizacionRepository amortizacionRepository;
    
    public DashboardEstadisticasResponse ejecutar(DashboardEstadisticasRequest request) {
        // Usar @Async para queries parallel si es necesario
        // Implementar caching con @Cacheable
        // Validaciones de dominio
    }
}
```

---

## MEJORAS RECOMENDADAS (🟡)

### 6. Potential N+1 Query Problem

- **[MEDIA]**: **Múltiples queries secuenciales en lugar de bulk**
- **[CATEGORÍA]**: Rendimiento
- **[DESCRIPCIÓN]**: La implementación de `AdminKYCController.obtenerEstadisticas()` hace:
```java
long pendientes = verificacionRepository.countByEstado(EstadoVerificacion.PENDIENTE);
long enRevision = verificacionRepository.countByEstado(EstadoVerificacion.EN_REVISION);
long aprobados = verificacionRepository.countByEstado(EstadoVerificacion.APROBADO);
long rechazados = verificacionRepository.countByEstado(EstadoVerificacion.RECHAZADO);
long expirados = verificacionRepository.countByEstado(EstadoVerificacion.EXPIRADO);
```

5 queries secuenciales cuando podría ser 1.

- **[IMPACTO]**: Latencia excesiva si los repositorios están en diferentes bases de datos o servicios.

- **[RECOMENDACIÓN]**: Crear método agregado en repository:
```java
@Query("SELECT e.estado, COUNT(e) FROM VerificacionKYCEntity e GROUP BY e.estado")
List<Object[]> countByEstadoGrouped();
```

---

## ANÁLISIS POSITIVO ✓

### Lo que está bien en la especificación:

1. **Uso correcto de `@PreAuthorize`** ✅:
   - La especificación usa `hasAnyRole('ADMIN', 'SUPER_ADMIN')` que es correcto
   - Coincide con el Rol enum existente

2. **JWToken validation** ✅:
   - El `JwtService` valida correctamente tokens con expiración
   - Usa HMAC-SHA (Keys.hmacShaKeyFor)
   - Implementa `esAccessTokenValido()` y `esRefreshTokenValido()`

3. **Patrón de Repository con JPQL parametrizado** ✅:
   - Usa `@Param` para evitar SQL injection
   - No hay concatenación de strings en queries

4. **Separación de módulos** ✅:
   - Las estadísticas se agregan de múltiples módulos (Socios, Ahorros, Créditos)
   - Cada módulo tiene su propio repositorio

---

## DATOS EXPUESTOS EN EL RESPONSE DTO

Según la especificación, el DTO de respuesta incluye:

```java
// Datos de Socios
totalSocios, sociosActivos, sociosInactivos, sociosPendientes

// Datos de Cuentas y Movimientos
totalAportaciones, cuentasActivas, depositosMes, retirosMes

// Datos de Créditos
prestamosActivos, solicitudesPendientes, capitalDesembolsado

// Datos de Amortizaciones
cuotasVencidas, cuotasEnMora, interesesMoraGenerados

// Métricas calculadas
tasaCumplimiento, tasaMora

// Actividad Reciente
actividadReciente (nuevosSociosMes, depositosMes, etc.)
```

### ⚠️ **PELIGRO POTENCIAL - Información Sensible Expuesta**:

| Dato | Sensibilidad | Riesgo |
|------|--------------|--------|
| `capitalDesembolsado` | 🔴 ALTA | Revela totales del fondo |
| `interesesMoraGenerados` | 🔴 ALTA | Indica morosidad del portafolio |
| `tasaCumplimiento` | 🟠 MEDIA | Métrica financiera clave |
| `cuotasVencidas` | 🟠 MEDIA | Indica salud de cartera |

**RECOMENDACIÓN**: Considerar si estos datos deben estar detrás de un flag de configuración o separados por permisos más granulares.

---

## ARCHIVOS AFECTADOS (para implementación)

| Archivo | Criticidad | Prioridad |
|---------|------------|-----------|
| `backend/src/main/java/com/tufondo/auth/infrastructure/service/SecurityAuditService.java` | 🔴 CRÍTICA | ALTA |
| (NEW) `AdminDashboardRateLimitFilter.java` | 🔴 CRÍTICA | ALTA |
| (NEW) `ObtenerDashboardEstadisticasUseCase.java` | 🟠 ALTA | ALTA |
| (NEW) `DashboardEstadisticasRequest.java` | 🟠 ALTA | ALTA |
| `backend/src/main/java/com/tufondo/kyc/api/controller/AdminKYCController.java` | 🟠 ALTA | MEDIA |
| `backend/src/main/java/com/tufondo/creditos/api/controller/CreditoController.java` | 🟠 ALTA | BAJA |
| `backend/src/main/java/com/tufondo/auth/domain/model/enums/Rol.java` | 🟡 MEDIA | BAJA |

---

## CONCLUSIÓN

La especificación técnica de Issue #45 presenta un diseño funcional pero con **debilidades de seguridad críticas** que deben resolverse:

1. **Falta de Rate Limiting** - Puede causar DoS
2. **Sin Audit Logging** - Incumplimiento regulatorio  
3. **Validación de输入 débil** - Potencial injection
4. **Arquitectura incorrecta** - Controller haciendo demasiado

**Recomendación:** NO proceder con la implementación hasta que se hayan addressado los hallazgos 🔴 CRÍTICA. Los hallazgos 🟠 ALTA deben resolverse antes de pasar a producción.

---

*Auditor: Lead Software Architect & Cyber-Security Auditor*
*Fecha: 2026-04-20*
*Versión: 1.0*
