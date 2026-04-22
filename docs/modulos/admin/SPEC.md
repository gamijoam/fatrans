# Módulo ADMIN - Especificación Técnica

**Proyecto:** Plataforma Fondo de Ahorro
**Versión:** 1.0
**Fecha:** 2026-04-20
**Estado:** Implementado
**Complejidad:** Media-Alta

---

## Resumen

El módulo **admin** es responsable de la administración y monitoreo del sistema Fondo de Ahorro. Provee un dashboard con estadísticas agregadas en tiempo real sobre socios, cuentas de ahorro, préstamos y amortizaciones.

---

## 1. Objetivos del Módulo

### 1.1 Objetivo Principal
Proveer un panel administrativo que permita a los administradores y super-administradores visualizar métricas consolidadas del sistema para la toma de decisiones.

### 1.2 Objetivos Secundarios
- Aggregar datos de múltiples módulos (Socios, Ahorros, Créditos, KYC)
- Calcular métricas de salud del fondo (tasas de cumplimiento, mora)
- Proveer acceso rápido a acciones administrativas comunes
- Mantener auditoría de accesos a información sensible

### 1.3 Scope
- ✅ Dashboard con estadísticas agregadas
- ✅ Métricas de socios (total, activos, inactivos, pendientes)
- ✅ Métricas de cuentas de ahorro (saldos, cuentas activas)
- ✅ Métricas de préstamos (solicitudes, desembolso, capital)
- ✅ Métricas de amortizaciones (cuotas vencidas, mora)
- ✅ Actividad reciente (últimos 30 días)
- ✅ Rate limiting para protección DoS
- ✅ Auditoría de accesos

### 1.4 Fuera del Scope
- ❌ Edición directa de datos desde el dashboard
- ❌ Reportes exportables (PDF/Excel)
- ❌ Dashboards personalizables
- ❌ Notificaciones push

---

## 2. Arquitectura del Sistema

### 2.1 Arquitectura General (Clean Architecture)

```
backend/src/main/java/com/tufondo/admin/
├── application/                          # Capa de Aplicación
│   ├── dto/
│   │   └── DashboardEstadisticasResponse.java  # DTO de respuesta
│   └── usecase/
│       └── ObtenerDashboardEstadisticasUseCase.java  # Caso de uso
│
└── presentation/
    └── controller/
        └── AdminDashboardController.java  # REST Controller

frontend-mobile/lib/features/admin/
├── data/
│   └── datasources/
│       └── admin_remote_datasource.dart   # Consumo de API
├── domain/
│   ├── entities/
│   │   └── dashboard_stats.dart          # Entidad Flutter
│   └── repositories/
│       └── admin_repository.dart         # Interfaz + Impl
└── presentation/
    ├── bloc/
    │   ├── admin_dashboard_cubit.dart    # Lógica de estado
    │   └── admin_dashboard_state.dart    # Estados
    └── pages/
        └── admin_dashboard_page.dart     # UI
```

### 2.2 Dependencias entre Módulos

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DEPENDENCIAS DEL MÓDULO ADMIN                        │
└─────────────────────────────────────────────────────────────────────────────┘

Admin Module
     │
     ├──► Socios Module (read-only)
     │         └── SocioRepository
     │              • count()
     │              • countByEstado()
     │              • countByFechaRegistroBetween()
     │
     ├──► Ahorros Module (read-only)
     │         ├── CuentaAhorroRepository
     │         │    • count()
     │         │    • countByEstado()
     │         │    • sumSaldoActualCuentasActivas()
     │         │
     │         └── MovimientoRepository
     │              • sumDepositosMes()
     │              • sumRetirosMes()
     │              • countByTipoAndFechaAfter()
     │
     ├──► Créditos Module (read-only)
     │         ├── SolicitudCreditoRepository
     │         │    • countByEstado()
     │         │    • countByEstadoAndCreatedAtAfter()
     │         │    • sumMontoSolicitadoByEstado()
     │         │
     │         └── AmortizacionRepository
     │              • countByEstado()
     │              • sumInteresesMoraPendientes()
     │
     └──► Auth Module (security)
              ├── SecurityAuditService
              │    • logDashboardAcceso()
              │
              └── AdminDashboardRateLimitFilter
                   • Rate limiting: 30 req/min
```

---

## 3. Modelo de Dominio

### 3.1 DashboardStats (Frontend)

```dart
class DashboardStats extends Equatable {
  // Socios
  final int totalSocios;
  final int sociosActivos;
  final int sociosInactivos;
  final int sociosPendientes;

  // Cuentas y Ahorros
  final double totalAportaciones;
  final int totalCuentasAhorro;
  final int cuentasActivas;
  final int cuentasSuspendidas;
  final double depositosMes;
  final double retirosMes;

  // Créditos
  final int prestamosActivos;
  final int solicitudesPendientes;
  final int solicitudesAprobadas;
  final int solicitudesRechazadas;
  final double capitalDesembolsado;
  final double carteraVencida;

  // Amortizaciones
  final int cuotasVencidas;
  final int cuotasEnMora;
  final int cuotasPagadas;
  final double interesesMoraGenerados;

  // Métricas Calculadas
  final double tasaCumplimiento;  // sociosActivos / totalSocios
  final double tasaMora;           // (cuotasVencidas + cuotasEnMora) / prestamosActivos

  // Actividad Reciente
  final ActividadReciente actividadReciente;
}
```

### 3.2 ActividadReciente (Frontend)

```dart
class ActividadReciente extends Equatable {
  final int nuevosSociosMes;           // Socios registrados este mes
  final int depositosMes;              // Conteo de depósitos (últimos 30 días)
  final int retirosMes;               // Conteo de retiros (últimos 30 días)
  final int prestamosAprobadosMes;    // Préstamos aprobados (últimos 30 días)
  final int prestamosDesembolsadosMes; // Préstamos desembolsados (últimos 30 días)
  final double montoDepositadoMes;   // Suma de depósitos del mes
  final double montoRetiradoMes;      // Suma de retiros del mes
}
```

---

## 4. Casos de Uso

### 4.1 ObtenerDashboardEstadisticasUseCase

**Responsabilidad:** Aggrega datos de 5 repositorios y calcula métricas de salud del fondo.

**Entrada:** Ninguna (usa fechas actuales del sistema)

**Salida:** `DashboardEstadisticasResponse`

**Lógica:**
1. Obtener fecha actual, inicio de mes, y hace 30 días
2. Consultar conteos de socios por estado
3. Sumar saldos de cuentas activas
4. Calcular depósitos y retiros del mes
5. Contar solicitudes de crédito por estado
6. Sumar capital desembolsado
7. Contar amortizaciones por estado
8. Calcular tasa de cumplimiento y mora
9. Retornar respuesta agregada

---

## 5. Endpoints API

### 5.1 GET /api/v1/admin/dashboard/estadisticas

| Atributo | Valor |
|----------|-------|
| Método | GET |
| Path | `/api/v1/admin/dashboard/estadisticas` |
| Autenticación | JWT Bearer Token |
| Autorización | `ADMIN` o `SUPER_ADMIN` |
| Rate Limit | 30 req/min por userId |

**Response 200:**
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
  "actividadReciente": { ... }
}
```

---

## 6. Estados y Transiciones

### 6.1 Estado del Dashboard (Frontend)

```
┌─────────────────────────┐
│   AdminDashboardInitial  │◄── Estado inicial
└───────────┬─────────────┘
            │ loadStats()
            ▼
┌─────────────────────────┐
│   AdminDashboardLoading  │◄── Cargando datos
└───────────┬─────────────┘
            │
            ├──► Éxito ──► AdminDashboardLoaded ──┐
            │                                     │
            │                                     │ (5 min TTL)
            │                                     ▼
            │                           ¿Cache expirado?
            │                                     │
            │                                     ├──► Sí ──► Auto-refresh
            │                                     │
            │                                     ├──► No ──► Mantener
            │                                     │
            └──► Error ──► AdminDashboardError ◄──┘
                          │
                          └──► Rate Limited ──► AdminDashboardRateLimited
```

---

## 7. Seguridad

### 7.1 Rate Limiting

| Configuración | Valor |
|---------------|-------|
| Límite | 30 solicitudes / minuto |
| Bucket expiration | 10 minutos |
| Cleanup | Cada 1 minuto |
| Key (autenticado) | `user:{usuarioId}` |
| Key (anónimo) | `ip:{direcciónIP}` |

### 7.2 Auditoría

| Campo | Descripción |
|-------|-------------|
| Tipo evento | `DASHBOARD_ADMIN_ACCESS` |
| Usuario | userId del JWT |
| IP | Extraída de X-Forwarded-For / X-Real-IP / RemoteAddr |
| Detalles | Rol del usuario |

### 7.3 Autorización

```java
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
```

---

## 8. Dependencias Externas

| Dependencia | Propósito |
|-------------|-----------|
| Bucket4j | Rate limiting con Token Bucket |
| Spring Data JPA | Acceso a repositorios |
| JWT (jjwt) | Autenticación (transitiva via auth module) |

---

## 9. Frontend - Pantallas

### 9.1 AdminDashboardPage

**Ruta:** `/admin/dashboard`

**Componentes:**
- Header con nombre de usuario y botón de logout
- Grid de estadísticas con 4 breakpoints responsive
- Pull-to-refresh para actualizar datos
- Acciones rápidas a módulosadmin

**Breakpoints:**
| Ancho | Columnas |
|-------|----------|
| >= 1200px | 4 |
| >= 800px | 3 |
| >= 600px | 2 |
| < 600px | 1 |

---

## 10. Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-20 | @implementador | Creación inicial - Issue #45 Dashboard Admin |

---

## 11. Referencias

- Issue #45: Dashboard Admin - Stats Reales: `/docs/modulos/admin/ISSUE_45_DASHBOARD_STATS.md`
- Auditoría de seguridad: `/docs/auditorias/auditoria_20260420_120000_ISSUE45_ADMIN_DASHBOARD.md`
- Post-implementación: `/docs/auditorias/POST_IMPLEMENTACION_ISSUE45.md`
