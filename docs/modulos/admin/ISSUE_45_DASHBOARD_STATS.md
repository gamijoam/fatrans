# Módulo ADMIN - Issue #45: Dashboard Admin - Estadísticas Reales

**Proyecto:** Plataforma Fondo de Ahorro
**Issue:** #45 - Dashboard Admin - Stats Reales
**Fecha:** 2026-04-20
**Estado:** ✅ Implementado
**Módulo:** Admin (Backend Java/Spring Boot + Frontend Flutter)

---

## Resumen

El Issue #45 implementa un dashboard administrativo con estadísticas reales y agregadas del sistema Fondo de Ahorro. El endpoint consume datos de 5 repositorios diferentes (Socio, CuentaAhorro, Movimiento, SolicitudCredito, Amortizacion) para generar métricas consolidadas sobre la salud del fondo.

---

## 1. Arquitectura del Módulo

### 1.1 Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ARQUITECTURA - DASHBOARD ADMIN                           │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           FRONTEND (Flutter)                                │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                    AdminDashboardCubit                                │   │
│  │  • loadStats(forceRefresh)                                            │   │
│  │  • refreshStats()                                                     │   │
│  │  • Cache: 5 min TTL                                                   │   │
│  └────────────────────────────┬─────────────────────────────────────────┘   │
│                               │                                             │
│  ┌────────────────────────────▼─────────────────────────────────────────┐   │
│  │                    AdminRepository                                    │   │
│  │  • getDashboardStats()                                               │   │
│  └────────────────────────────┬─────────────────────────────────────────┘   │
│                               │                                             │
│  ┌────────────────────────────▼─────────────────────────────────────────┐   │
│  │               AdminRemoteDataSourceImpl                               │   │
│  │  • GET /api/v1/admin/dashboard/estadisticas                           │   │
│  └────────────────────────────┬─────────────────────────────────────────┘   │
└────────────────────────────────┼─────────────────────────────────────────────┘
                                 │ HTTP/REST
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           BACKEND (Spring Boot)                              │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │              AdminDashboardController                                │   │
│  │  • GET /api/v1/admin/dashboard/estadisticas                         │   │
│  │  • @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")              │   │
│  │  • SecurityAuditService.logDashboardAcceso()                         │   │
│  └────────────────────────────┬─────────────────────────────────────────┘   │
│                               │                                             │
│  ┌────────────────────────────▼─────────────────────────────────────────┐   │
│  │         ObtenerDashboardEstadisticasUseCase                          │   │
│  │                                                                       │   │
│  │  Agrega datos de 5 repositorios:                                      │   │
│  │  • SocioRepository          → conteos, nuevos socios                   │   │
│  │  • CuentaAhorroRepository  → saldos, cuentas activas                 │   │
│  │  • MovimientoRepository     → depósitos/retiros del mes              │   │
│  │  • SolicitudCreditoRepository → préstamos, capital                   │   │
│  │  • AmortizacionRepository  → cuotas, mora                            │   │
│  │                                                                       │   │
│  │  Calcula tasas:                                                       │   │
│  │  • tasaCumplimiento = sociosActivos / totalSocios                    │   │
│  │  • tasaMora = (cuotasVencidas + cuotasEnMora) / prestamosActivos    │   │
│  └────────────────────────────┬─────────────────────────────────────────┘   │
│                               │                                             │
│                               ▼                                             │
│  ┌──────────────────────────────────────────────────────────────────────┐   │
│  │                    DashboardEstadisticasResponse                       │   │
│  │  • totalSocios, sociosActivos, sociosInactivos, sociosPendientes      │   │
│  │  • totalAportaciones, depositosMes, retirosMes                       │   │
│  │  • prestamosActivos, solicitudesPendientes, capitalDesembolsado      │   │
│  │  • tasas, actividadReciente                                          │   │
│  └──────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Flujo de Datos

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         FLUJO DE DATOS COMPLETO                             │
└─────────────────────────────────────────────────────────────────────────────┘

1. USUARIO ADMIN ACCEDE DASHBOARD
   ══════════════════════════════
   ┌─────────┐         ┌──────────────────┐         ┌────────────────────────┐
   │ Flutter  │         │ AdminDashboard   │         │  SecurityConfig        │
   │ App      │         │ RateLimitFilter  │         │  (JwtAuthentication)   │
   └────┬─────┘         └────────┬─────────┘         └───────────┬────────────┘
        │                        │                               │
        │ GET /admin/dashboard   │                               │
        │────────────────────────▶│                               │
        │                        │  Rate limit check             │
        │                        │──────────────────────────────▶│
        │                        │                               │
        │                        │  ¿Límite excedido?             │
        │                        │◀───────────────────────────────│
        │                        │                               │
        │                        │  429 → Response               │
        │◀───────────────────────│                               │
        │  (Rate Limited)         │                               │
        │                        │                               │
        │  200 OK                │◀───────────────────────────────│
        │◀───────────────────────│  (Continúa al controller)     │
        │  JSON Stats            │                               │

2. CONTROLLER PROCESO
   ════════════════════
   ┌──────────────────┐         ┌──────────────────────────┐
   │ AdminDashboard   │         │ SecurityAuditService     │
   │ Controller       │         │                          │
   └────────┬─────────┘         └───────────┬──────────────┘
            │                               │
            │  Extraer userId del auth      │
            │  Extraer IP del request       │
            │                               │
            │  logDashboardAcceso()         │
            │──────────────────────────────▶│
            │                               │
            │                               │ SecurityEvent
            │                               │ DASHBOARD_ADMIN_ACCESS
            │                               │ logged to SLF4J
            │                               │
            │                               │
            ▼                               ▼
   ┌─────────────────────────────────────────────────────┐
   │         ObtenerDashboardEstadisticasUseCase        │
   │                                                     │
   │  LocalDateTime ahora = LocalDateTime.now()         │
   │  LocalDateTime inicioMes = ahora.withDayOfMonth(1)│
   │  LocalDateTime hace30Dias = ahora.minusDays(30)   │
   │                                                     │
   └─────────────────────────────────────────────────────┘
            │
            ▼
   ┌─────────────────────────────────────────────────────┐
   │              5 REPOSITORIES AGREGADOS               │
   │                                                     │
   │  1. SocioRepository                                 │
   │     • count() → totalSocios                        │
   │     • countByEstado(ACTIVO) → sociosActivos        │
   │     • countByEstado(INACTIVO) → sociosInactivos    │
   │     • countByEstado(PENDIENTE) → sociosPendientes  │
   │     • countByFechaRegistroBetween → nuevosSocios   │
   │                                                     │
   │  2. CuentaAhorroRepository                         │
   │     • count() → totalCuentasAhorro                │
   │     • countByEstado(ACTIVA) → cuentasActivas      │
   │     • countByEstado(SUSPENDIDA) → suspendidas     │
   │     • sumSaldoActualCuentasActivas → totalAport.  │
   │                                                     │
   │  3. MovimientoRepository                            │
   │     • sumDepositosMes(inicioMes) → depositosMes   │
   │     • sumRetirosMes(inicioMes) → retirosMes       │
   │     • countByTipoAfter(DEPOSITO, hace30Dias)      │
   │     • countByTipoAfter(RETIRO, hace30Dias)        │
   │                                                     │
   │  4. SolicitudCreditoRepository                     │
   │     • countByEstado(DESEMBOLSADO) → prestamosAct │
   │     • countByEstado(PENDIENTE) → solicitudesPen  │
   │     • countByEstado(APROBADA) → solicitudesApro  │
   │     • countByEstado(RECHAZADA) → solicitudesRech │
   │     • sumMontoSolicitadoByEstado(DESEMBOLSADO)   │
   │     • countByEstadoAndCreatedAtAfter → actividad  │
   │                                                     │
   │  5. AmortizacionRepository                         │
   │     • countByEstado(VENCIDA) → cuotasVencidas   │
   │     • countByEstado(CURSO_MORA) → cuotasEnMora   │
   │     • countByEstado(PAGADA) → cuotasPagadas      │
   │     • sumInteresesMoraPendientes() → mora        │
   └─────────────────────────────────────────────────────┘
            │
            ▼
   ┌─────────────────────────────────────────────────────┐
   │           CÁLCULOS DE TASAS                         │
   │                                                     │
   │  tasaCumplimiento =                               │
   │    totalSocios > 0                                 │
   │      ? (double)sociosActivos / totalSocios        │
   │      : 0.0                                         │
   │                                                     │
   │  tasaMora =                                        │
   │    prestamosActivos > 0                           │
   │      ? (double)(cuotasVencidas + cuotasEnMora)   │
   │        / prestamosActivos                         │
   │      : 0.0                                         │
   └─────────────────────────────────────────────────────┘
            │
            ▼
   ┌─────────────────────────────────────────────────────┐
   │        DashboardEstadisticasResponse               │
   │                                                     │
   │  {                                                   │
   │    "totalSocios": 150,                             │
   │    "sociosActivos": 140,                          │
   │    "totalAportaciones": 2500000.00,               │
   │    "tasaCumplimiento": 0.93,                      │
   │    ...                                             │
   │    "actividadReciente": {                         │
   │      "nuevosSociosMes": 5,                       │
   │      "depositosMes": 25,                          │
   │      ...                                           │
   │    }                                               │
   │  }                                                 │
   └─────────────────────────────────────────────────────┘
```

---

## 2. Endpoints API

### 2.1 GET /api/v1/admin/dashboard/estadisticas

**Descripción:** Obtiene estadísticas completas del dashboard administrativo.

**Autenticación:** JWT Bearer Token (Requerido)

**Autorización:** Roles `ADMIN` o `SUPER_ADMIN`

**Rate Limit:** 30 solicitudes por minuto por usuario (o por IP si no autenticado)

#### Request

```http
GET /api/v1/admin/dashboard/estadisticas
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Host: api.fondoahorro.com
X-Forwarded-For: 192.168.1.100
```

#### Response - 200 OK

```json
{
  "totalSocios": 150,
  "sociosActivos": 140,
  "sociosInactivos": 8,
  "sociosPendientes": 2,
  "totalAportaciones": 2500000.50,
  "totalCuentasAhorro": 150,
  "cuentasActivas": 145,
  "cuentasSuspendidas": 5,
  "depositosMes": 500000.00,
  "retirosMes": 100000.00,
  "prestamosActivos": 12,
  "solicitudesPendientes": 3,
  "solicitudesAprobadas": 8,
  "solicitudesRechazadas": 2,
  "capitalDesembolsado": 500000.00,
  "carteraVencida": 15000.00,
  "cuotasVencidas": 2,
  "cuotasEnMora": 1,
  "cuotasPagadas": 150,
  "interesesMoraGenerados": 5000.00,
  "tasaCumplimiento": 0.93,
  "tasaMora": 0.08,
  "actividadReciente": {
    "nuevosSociosMes": 5,
    "depositosMes": 25,
    "retirosMes": 10,
    "prestamosAprobadosMes": 3,
    "prestamosDesembolsadosMes": 2,
    "montoDepositadoMes": 500000.00,
    "montoRetiradoMes": 100000.00
  }
}
```

#### Response - 401 Unauthorized

```json
{
  "codigo": "TOKEN_INVALIDO",
  "mensaje": "Token JWT inválido o expirado"
}
```

#### Response - 403 Forbidden

```json
{
  "codigo": "ACCESO_DENEGADO",
  "mensaje": "No tiene permisos para acceder a este recurso"
}
```

#### Response - 429 Too Many Requests

```json
{
  "codigo": "RATE_LIMIT_EXCEDIDO",
  "mensaje": "Demasiadas solicitudes. Intente nuevamente en 60 segundos."
}
```

---

## 3. DTOs

### 3.1 DashboardEstadisticasResponse (Backend)

**Ubicación:** `backend/src/main/java/com/tufondo/admin/application/dto/DashboardEstadisticasResponse.java`

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardEstadisticasResponse {

    // Socios
    private long totalSocios;
    private long sociosActivos;
    private long sociosInactivos;
    private long sociosPendientes;

    // Cuentas y Ahorros
    private BigDecimal totalAportaciones;
    private long totalCuentasAhorro;
    private long cuentasActivas;
    private long cuentasSuspendidas;
    private BigDecimal depositosMes;
    private BigDecimal retirosMes;

    // Créditos
    private long prestamosActivos;
    private long solicitudesPendientes;
    private long solicitudesAprobadas;
    private long solicitudesRechazadas;
    private BigDecimal capitalDesembolsado;
    private BigDecimal carteraVencida;

    // Amortizaciones
    private long cuotasVencidas;
    private long cuotasEnMora;
    private long cuotasPagadas;
    private BigDecimal interesesMoraGenerados;

    // Métricas Calculadas
    private double tasaCumplimiento;    // sociosActivos / totalSocios
    private double tasaMora;             // (cuotasVencidas + cuotasEnMora) / prestamosActivos
    private BigDecimal rendimientoPromedio;

    // Actividad Reciente
    private ActividadRecienteResponse actividadReciente;

    @Data
    public static class ActividadRecienteResponse {
        private long nuevosSociosMes;
        private long depositosMes;
        private long retirosMes;
        private long prestamosAprobadosMes;
        private long prestamosDesembolsadosMes;
        private BigDecimal montoDepositadoMes;
        private BigDecimal montoRetiradoMes;
    }
}
```

### 3.2 DashboardStats (Frontend)

**Ubicación:** `frontend-mobile/lib/features/admin/domain/entities/dashboard_stats.dart`

```dart
class DashboardStats extends Equatable {
  final int totalSocios;
  final int sociosActivos;
  final int sociosInactivos;
  final int sociosPendientes;
  final double totalAportaciones;
  final int totalCuentasAhorro;
  final int cuentasActivas;
  final int cuentasSuspendidas;
  final double depositosMes;
  final double retirosMes;
  final int prestamosActivos;
  final int solicitudesPendientes;
  final int solicitudesAprobadas;
  final int solicitudesRechazadas;
  final double capitalDesembolsado;
  final double carteraVencida;
  final int cuotasVencidas;
  final int cuotasEnMora;
  final int cuotasPagadas;
  final double interesesMoraGenerados;
  final double tasaCumplimiento;
  final double tasaMora;
  final ActividadReciente actividadReciente;
  // ...
}

class ActividadReciente extends Equatable {
  final int nuevosSociosMes;
  final int depositosMes;
  final int retirosMes;
  final int prestamosAprobadosMes;
  final int prestamosDesembolsadosMes;
  final double montoDepositadoMes;
  final double montoRetiradoMes;
  // ...
}
```

---

## 4. Casos de Uso

### 4.1 ObtenerDashboardEstadisticasUseCase

**Ubicación:** `backend/src/main/java/com/tufondo/admin/application/usecase/ObtenerDashboardEstadisticasUseCase.java`

**Responsabilidad:** Agrega datos de 5 repositorios y calcula métricas.

**Dependencias:**
- `SocioRepository`
- `CuentaAhorroRepository`
- `MovimientoRepository`
- `SolicitudCreditoRepository`
- `AmortizacionRepository`

**Lógica principal:**
```java
public DashboardEstadisticasResponse ejecutar() {
    LocalDateTime ahora = LocalDateTime.now();
    LocalDateTime inicioMes = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
    LocalDateTime hace30Dias = ahora.minus(30, ChronoUnit.DAYS);

    // Conteos de Socios
    long totalSocios = socioRepository.count();
    long sociosActivos = socioRepository.countByEstado(EstadoSocio.ACTIVO);
    long sociosInactivos = socioRepository.countByEstado(EstadoSocio.INACTIVO);
    long sociosPendientes = socioRepository.countByEstado(EstadoSocio.PENDIENTE_APROBACION);

    // Saldos de Cuentas
    BigDecimal totalAportaciones = cuentaAhorroRepository.sumSaldoActualCuentasActivas();

    // Movimientos del Mes
    BigDecimal depositosMes = movimientoRepository.sumDepositosMes(inicioMes);
    BigDecimal retirosMes = movimientoRepository.sumRetirosMes(inicioMes);

    // Créditos
    long prestamosActivos = solicitudCreditoRepository.countByEstado(EstadoSolicitud.DESEMBOLSADO);
    BigDecimal capitalDesembolsado = solicitudCreditoRepository.sumMontoSolicitadoByEstado(...);

    // Amortizaciones
    long cuotasVencidas = amortizacionRepository.countByEstado(EstadoAmortizacion.VENCIDA);
    long cuotasEnMora = amortizacionRepository.countByEstado(EstadoAmortizacion.CURSO_MORA);
    BigDecimal interesesMora = amortizacionRepository.sumInteresesMoraPendientes();

    // Tasas Calculadas
    double tasaCumplimiento = totalSocios > 0
        ? (double) sociosActivos / totalSocios : 0.0;

    double tasaMora = prestamosActivos > 0
        ? (double) (cuotasVencidas + cuotasEnMora) / prestamosActivos : 0.0;

    // ...
}
```

---

## 5. Seguridad

### 5.1 Rate Limiting

**Filtro:** `AdminDashboardRateLimitFilter`

**Ubicación:** `backend/src/main/java/com/tufondo/auth/infrastructure/security/AdminDashboardRateLimitFilter.java`

**Configuración:**
- **Límite:** 30 solicitudes por minuto
- **Clave de rate limit:**
  - Para usuarios autenticados: `user:{usuarioId}`
  - Para usuarios anónimos: `ip:{direcciónIP}`
- **Bucket expiration:** 10 minutos
- **Cleanup interval:** 1 minuto

**Implementación:**
```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AdminDashboardRateLimitFilter extends OncePerRequestFilter {

    private static final int REQUESTS_PER_MINUTE = 30;
    private static final long BUCKET_EXPIRATION_MINUTES = 10;
    private static final Map<String, BucketEntry> BUCKETS = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(...) {
        if (!request.getRequestURI().equals("/api/v1/admin/dashboard/estadisticas")) {
            filterChain.doFilter(request, response);
            return;
        }

        String rateLimitKey = getRateLimitKey(request);
        Bucket bucket = BUCKETS.computeIfAbsent(rateLimitKey, this::createBucket);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            // 429 Too Many Requests
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "60");
        }
    }

    private String getRateLimitKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();  // userId del SecurityContext
        }
        return "ip:" + extractClientIp(request);  // Fallback por IP
    }
}
```

### 5.2 Audit Logging

**Servicio:** `SecurityAuditService`

**Método:** `logDashboardAcceso(String usuarioId, String ip, String rol)`

**Tipo de evento:** `DASHBOARD_ADMIN_ACCESS`

**Log format:**
```
AUDIT [2026-04-20T12:00:00Z] usuario=550e8400-e29b-41d4-a716-446655440000 ip=192.168.1.100 tipo=DASHBOARD_ADMIN_ACCESS detalles=rol=ADMIN
```

### 5.3 Autorización

**Anotación:** `@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")`

**Ubicación:** `AdminDashboardController.obtenerEstadisticas()`

---

## 6. Frontend - Capa de Estado (BLoC)

### 6.1 AdminDashboardState

```dart
sealed class AdminDashboardState extends Equatable {
  const AdminDashboardState();
}

final class AdminDashboardInitial extends AdminDashboardState {}

final class AdminDashboardLoading extends AdminDashboardState {}

final class AdminDashboardLoaded extends AdminDashboardState {
  final DashboardStats stats;
  final DateTime cachedAt;

  bool get isCacheExpired {
    return DateTime.now().difference(cachedAt).inMinutes > 5;
  }
}

final class AdminDashboardError extends AdminDashboardState {
  final String message;
}

final class AdminDashboardRateLimited extends AdminDashboardState {
  final int retryAfterSeconds;
  final String message;
}
```

### 6.2 AdminDashboardCubit

```dart
class AdminDashboardCubit extends Cubit<AdminDashboardState> {
  final AdminRepository repository;

  Future<void> loadStats({bool forceRefresh = false}) async {
    final currentState = state;

    // Si no es refresh forzado y hay cache válido, no recargar
    if (!forceRefresh && currentState is AdminDashboardLoaded) {
      if (!currentState.isCacheExpired) {
        return;
      }
    }

    emit(const AdminDashboardLoading());

    try {
      final stats = await repository.getDashboardStats();
      emit(AdminDashboardLoaded(
        stats: stats,
        cachedAt: DateTime.now(),
      ));
    } catch (e) {
      emit(AdminDashboardError(message: e.toString()));
    }
  }

  Future<void> refreshStats() async {
    await loadStats(forceRefresh: true);
  }
}
```

---

## 7. Tests

### 7.1 DashboardStats Test Suite

**Ubicación:** `frontend-mobile/test/features/admin/domain/dashboard_stats_test.dart`

**Tests implementados (7 tests):**

| Test | Descripción |
|------|-------------|
| `parses valid JSON correctly` | Verifica parsing de JSON válido con todos los campos |
| `handles null values with defaults` | Verifica que valores null usen defaults (0, 0.0) |
| `parses string numbers correctly` | Verifica parsing de números como strings |
| `handles int values correctly` | Verifica que ints se conviertan a double correctamente |
| `handles empty JSON gracefully` | Verifica que JSON vacío no cause errores |
| `ActividadReciente fromJson parses correctly` | Verifica parsing de actividad reciente |
| `ActividadReciente fromJson handles null values` | Verifica defaults nulos en actividad reciente |

---

## 8. Archivos Creados/Modificados

### Backend (Java)

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `admin/application/dto/DashboardEstadisticasResponse.java` | **NUEVO** | DTO de respuesta con estadísticas |
| `admin/application/usecase/ObtenerDashboardEstadisticasUseCase.java` | **NUEVO** | Caso de uso para obtener estadísticas |
| `admin/presentation/controller/AdminDashboardController.java` | **NUEVO** | REST Controller del dashboard |
| `auth/infrastructure/service/SecurityAuditService.java` | Modificado | Nuevo método `logDashboardAcceso()` |
| `auth/domain/model/audit/SecurityEvent.java` | Modificado | Nuevo factory `dashboardAcceso()` |
| `auth/infrastructure/security/AdminDashboardRateLimitFilter.java` | **NUEVO** | Rate limiting para endpoint admin |
| `auth/infrastructure/security/SecurityConfig.java` | Modificado | Registra `AdminDashboardRateLimitFilter` |
| `socios/domain/repository/SocioRepository.java` | Modificado | Nuevos métodos: `count()`, `countByEstado()`, `countByFechaRegistroBetween()` |
| `ahorros/domain/repository/CuentaAhorroRepository.java` | Modificado | Nuevos métodos: `count()`, `countByEstado()`, `sumSaldoActualCuentasActivas()` |
| `ahorros/domain/repository/MovimientoRepository.java` | Modificado | Nuevos métodos: `sumDepositosMes()`, `sumRetirosMes()`, `countByTipoAndFechaAfter()` |
| `creditos/domain/repository/SolicitudCreditoRepository.java` | Modificado | Nuevos métodos: `countByEstado()`, `countByEstadoAndCreatedAtAfter()`, `sumMontoSolicitadoByEstado()` |
| `creditos/domain/repository/AmortizacionRepository.java` | Modificado | Nuevos métodos: `countByEstado()`, `sumInteresesMoraPendientes()` |

### Frontend (Flutter)

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `features/admin/domain/entities/dashboard_stats.dart` | **NUEVO** | Entidad DashboardStats con fromJson defensivo |
| `features/admin/data/datasources/admin_remote_datasource.dart` | **NUEVO** | DataSource para consumo de API |
| `features/admin/domain/repositories/admin_repository.dart` | **NUEVO** | Repositorio abstracto e implementación |
| `features/admin/presentation/bloc/admin_dashboard_cubit.dart` | **NUEVO** | Cubit con cache de 5 min TTL |
| `features/admin/presentation/bloc/admin_dashboard_state.dart` | **NUEVO** | Estados del cubit |
| `features/admin/presentation/pages/admin_dashboard_page.dart` | **NUEVO** | UI del dashboard con GridView responsive |
| `core/constants/api_endpoints.dart` | Modificado | Nuevo endpoint `adminDashboardStats` |
| `test/features/admin/domain/dashboard_stats_test.dart` | **NUEVO** | Tests unitarios para DashboardStats |

---

## 9. Decisiones de Diseño

### 9.1 Por qué Use Case separado del Controller

**Decisión:** Se creó `ObtenerDashboardEstadisticasUseCase` en lugar de colocar la lógica en el controller.

**Razón:** Clean Architecture - el controller solo debe manejar HTTP, no lógica de negocio. Además, permite:
- Testing unitario de la lógica de agregación
- Reutilización del caso de uso desde otros endpoints
- Posibilidad de agregar caching futuro con `@Cacheable`

### 9.2 Por qué Cache en Frontend con TTL de 5 minutos

**Decisión:** Se implementó cache local en el cubit con TTL de 5 minutos.

**Razón:**
- El endpoint hace ~15-25 queries a la base de datos
- Un admin probablemente refresheará la página múltiples veces
- 5 minutos es un balance entre frescura de datos y rendimiento
- `forceRefresh` permite forzar actualización cuando sea necesario

### 9.3 Por qué Rate Limit por userId y no solo por IP

**Decisión:** El filtro extrae el userId del `SecurityContextHolder` (no hace parsing de JWT manual).

**Razón:**
- Un atacante podría spoofear IPs si hay proxy involucrado
- Los usuarios legítimos detrás de un proxy compartirían el rate limit
- El userId ya está disponible después del `JwtAuthenticationFilter`
- No se duplica lógica de validación de JWT

### 9.4 Por qué parsing defensivo en Frontend

**Decisión:** `_parseIntSafe()` y `_parseDoubleSafe()` manejan null, int, num, y String.

**Razón:**
- La API puede devolver integers o decimals dependiendo del caso
- Valores null son posibles si no hay datos
- String numbers pueden venir del parsing JSON de某些 API gateways
- Evita crashes en producción por tipos inesperados

---

## 10. Métricas y Rangos de Tiempo

| Métrica | Rango de Tiempo | Repository |
|---------|-----------------|------------|
| `depositosMes`, `retirosMes` | Inicio del mes actual → ahora | MovimientoRepository |
| `nuevosSociosMes` | Inicio del mes actual → ahora | SocioRepository |
| `depositosMes` (actividad) | Últimos 30 días | MovimientoRepository |
| `prestamosAprobadosMes` | Últimos 30 días | SolicitudCreditoRepository |
| `prestamosDesembolsadosMes` | Últimos 30 días | SolicitudCreditoRepository |
| Totales (socios, cuentas, prestamos) | Snapshot actual | Respective repositories |

---

## 11. Historial de Cambios

| Fecha | Agente | Descripción |
|-------|--------|-------------|
| 2026-04-20 | @auditoria | Auditoría de seguridad pre-implementación (hallazgos críticos) |
| 2026-04-20 | @implementador | Implementación de todas las correcciones de seguridad |

---

## 12. Referencias

- Auditoría pre-implementación: `/docs/auditorias/auditoria_20260420_120000_ISSUE45_ADMIN_DASHBOARD.md`
- SPEC.md del módulo: `/docs/modulos/admin/SPEC.md`
- Post-implementación: `/docs/auditorias/POST_IMPLEMENTACION_ISSUE45.md`
