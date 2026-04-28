# Frontend Roadmap - Fondo de Ahorro

**Proyecto:** FATRANS
**Versión:** 1.0
**Fecha:** 2026-04-21

---

## 1. Resumen del Estado Actual

### Estado del Frontend
| Componente | Estado | Detalle |
|------------|--------|---------|
| Stack tecnológico | ✅ Listo | Next.js 14, React 18, Tailwind, TanStack Query, Zustand |
| API Client base | ✅ Listo | Axios con interceptors, CSRF, refresh token |
| Auth Store base | ✅ Listo | Zustand con persistencia |
| Middleware | ✅ Listo | Protección de rutas pública/admin |
| Landing Page | ❌ Vacía | Solo texto placeholder |
| Auth Flow | ❌ No implementado | Solo estructura middleware |
| Dashboard Socio | ❌ No implementado | - |
| Admin Portal | ❌ No implementado | - |
| UI Components | ⚠️ Parcial | Solo CSS base + QueryProvider |

### Backend API - Endpoints Disponibles
| Módulo | Endpoints | Descripción |
|--------|-----------|-------------|
| Auth | 9 | Login, logout, refresh, recovery, me, crear-usuario |
| Socios | 13 | CRUD, solicitudes, búsqueda, activar/desactivar |
| Cuentas/Ahorros | 12 | Crear, consultar, saldo, depósito, retiro, movimientos |
| Créditos | 14 | Solicitudes, tipos, evaluación, aprobación, desembolso, cuotas |
| KYC | 12+1 | Iniciar, estado, documentos, enviar, revocar |
| Beneficiarios | 4 | CRUD por socioId |
| Documentos PDF | 9 | Estados cuenta, constancias, tabla amortización |
| Admin Dashboard | 1 | Estadísticas consolidadas |

**Total: ~73 endpoints REST**

---

## 2. Arquitectura Objetivo

### Diagrama de Arquitectura BFF

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              ARQUITECTURA BFF                                │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────┐         ┌─────────────────┐         ┌────────────────────────┐
│  Browser │────────▶│   Next.js BFF   │────────▶│   Spring Boot API      │
│          │         │                 │         │                        │
│  React   │◀────────│  (Server Side)  │◀────────│  Java 21 + Spring     │
│  App     │  Cookies │                 │  JSON   │  Clean Architecture   │
└──────────┘  httpOnly└─────────────────┘         └────────────────────────┘
                                                                │
                              ┌─────────────────────────────────┤
                              │                                 │
                    ┌─────────▼─────────┐         ┌─────────────▼────────┐
                    │     PostgreSQL    │         │       Redis          │
                    │   (Persistence)   │         │    (Sessions)        │
                    └───────────────────┘         └──────────────────────┘
```

### Flujo de Autenticación

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         FLUJO DE AUTENTICACIÓN                               │
└─────────────────────────────────────────────────────────────────────────────┘

1. LOGIN
═══════════
┌──────────┐         ┌─────────────────┐         ┌────────────────────────┐
│  Login   │ POST    │   Next.js BFF    │         │    Spring Boot API     │
│  Page    │────────▶│  /api/auth/login │────────▶│   /api/v1/auth/login   │
│          │         │                 │         │                        │
└──────────┘         └────────┬────────┘         └────────────┬───────────┘
                               │                                  │
                               │◀─ Response + Set-Cookie ─────────┤
                               │    httpOnly, Secure, SameSite    │
                               │                                  │
                               ▼                                  ▼
                        ┌─────────────────┐              ┌───────────────────┐
                        │  Browser Cookie │              │  JWT in Body      │
                        │  (access_token) │              │  (no guardar)     │
                        └─────────────────┘              └───────────────────┘

2. REQUEST AUTENTICADO
═════════════════════════
┌──────────┐         ┌─────────────────┐         ┌────────────────────────┐
│  Any     │ GET     │   Next.js BFF    │ Forward │    Spring Boot API     │
│  Page    │────────▶│  /api/socios/me  │────────▶│   /api/v1/socios/me   │
│          │  Cookie │                 │ Bearer  │                        │
└──────────┘         └────────┬────────┘         └────────────┬───────────┘
                               │                                  │
                               │◀─ 200 OK ─────────────────────────┤
                               │    { socio data }                 │
                               ▼                                  ▼
                        ┌─────────────────┐              ┌───────────────────┐
                        │  React State    │              │  Validated        │
                        │  (Zustand)      │              │  + Logged          │
                        └─────────────────┘              └───────────────────┘

3. TOKEN REFRESH (401)
══════════════════════════
┌──────────┐         ┌─────────────────┐         ┌────────────────────────┐
│  Axios   │ 401     │   Next.js BFF    │ POST    │    Spring Boot API     │
│ Interceptor│◀──────│  /api/auth/refresh│────────▶│  /api/v1/auth/refresh  │
│          │         │                 │ Cookie  │                        │
└──────────┘         └────────┬────────┘         └────────────┬───────────┘
                               │                                  │
                               │◀─ New Cookies ───────────────────┤
                               │    (Token Rotation)              │
                               ▼                                  ▼
                        ┌─────────────────┐              ┌───────────────────┐
                        │  Retry Request  │              │  Session Updated  │
                        │  with new token │              │  + New JWT        │
                        └─────────────────┘              └───────────────────┘

4. LOGOUT
═══════════
┌──────────┐         ┌─────────────────┐         ┌────────────────────────┐
│  Logout  │ POST    │   Next.js BFF    │ POST    │    Spring Boot API     │
│  Button  │────────▶│  /api/auth/logout │────────▶│  /api/v1/auth/logout   │
│          │         │                 │ Bearer  │                        │
└──────────┘         └────────┬────────┘         └────────────┬───────────┘
                               │                                  │
                               │◀─ Clear Cookies ───────────────┤
                               │    (Max-Age=0)                  │
                               ▼                                  ▼
                        ┌─────────────────┐              ┌───────────────────┐
                        │  Clear State    │              │  Session Invalid  │
                        │  + Redirect /    │              │  + Audit Log     │
                        └─────────────────┘              └───────────────────┘
```

---

## 3. Fases de Desarrollo

### FASE 0: Plataforma Base (Semanas 1-2) 🔴 CRÍTICA

**Objetivo:** Establecer cimientos técnicos

| Entregable | Descripción | Prioridad |
|------------|-------------|-----------|
| Estructura App Router | Layouts: `(public)`, `(auth)`, `(app)`, `(admin)` | MUST |
| API Client BFF | Axios + interceptors, manejo 401/403/429 | MUST |
| Auth con httpOnly cookies | Login → cookies seguras, middleware | MUST |
| Design System base | Tailwind + shadcn/ui con colores verde/azul claro | MUST |
| UI Components | Button, Input, Card, Dialog, Table, Badge, Alert, etc. | MUST |
| Pipeline CI | Lint, typecheck, tests unitarios | MUST |

**Criterios de aceptación:**
- [ ] `npm run build` pasa sin errores
- [ ] Rutas protegidas redirigen a login
- [ ] Auth store sincroniza con cookies
- [ ] 401/403/429 manejados uniformemente

---

### FASE 1: Landing + Auth Enterprise (Semanas 3-4) 🔴 CRÍTICA

**Objetivo:** Página pública y flujo completo de autenticación

| Página | Endpoint API | Prioridad |
|--------|-------------|-----------|
| Landing page `/` | - | MUST |
| Login `/auth/login` | `POST /auth/login` | MUST |
| Logout `/auth/logout` | `POST /auth/logout` | MUST |
| Registro solicitud `/auth/registro` | `POST /socios/solicitud` | MUST |
| Recuperar password `/auth/recuperar-password` | `POST /auth/recuperar-password` | MUST |
| Reset password `/auth/reset-password` | `POST /auth/reset-password` | MUST |
| Landing SEO | Meta tags, Open Graph, JSON-LD | MUST |

**Criterios de aceptación:**
- [ ] Landing con SSR para SEO
- [ ] Login/logout/refresh funcionales
- [ ] Rate limiting visual (spinner + disable)
- [ ] Errores de auth mostrados correctamente
- [ ] Registro → Admin aprueba → Email credenciales

---

### FASE 2: Core Socio/Admin Inicial (Semanas 5-7) 🔴 CRÍTICA

**Objetivo:** Primer circuito punta-a-punta

#### Socio
| Página | Endpoint API | Prioridad |
|--------|-------------|-----------|
| Dashboard `/dashboard` | `GET /socios/{id}`, `GET /cuentas/socio/{id}` | MUST |
| Ver estado solicitud | Query params + mensaje | MUST |

#### Admin
| Página | Endpoint API | Prioridad |
|--------|-------------|-----------|
| Dashboard `/admin` | `GET /admin/dashboard/estadisticas` | MUST |
| Solicitudes registro `/admin/socios/solicitudes` | `GET /socios/solicitudes` | MUST |
| Aprobar solicitud `/admin/socios/solicitudes/[id]/aprobar` | `POST /socios/solicitudes/{id}/aprobar` | MUST |
| Rechazar solicitud `/admin/socios/solicitudes/[id]/rechazar` | `POST /socios/solicitudes/{id}/rechazar` | MUST |

**Criterios de aceptación:**
- [ ] Registro público → Admin ve solicitud → Aprueba → Socio recibe email
- [ ] Dashboard admin muestra métricas reales
- [ ] Primera historia E2E completa

---

### FASE 3: Ahorros y Movimientos (Semanas 8-10) 🔴 CRÍTICA

**Objetivo:** Módulo de cuentas de ahorro completo

| Operación | Endpoint API | Prioridad |
|-----------|-------------|-----------|
| Mis cuentas `/dashboard/cuentas` | `GET /cuentas/socio/{socioId}` | MUST |
| Detalle cuenta `/dashboard/cuentas/[numero]` | `GET /cuentas/{numeroCuenta}` | MUST |
| Saldo detallado `/dashboard/cuentas/[numero]/saldo` | `GET /cuentas/{numeroCuenta}/saldo` | MUST |
| Depósito `/dashboard/cuentas/[numero]/depositar` | `POST /cuentas/{numeroCuenta}/depositos` | MUST |
| Retiro `/dashboard/cuentas/[numero]/retirar` | `POST /cuentas/{numeroCuenta}/retiros` | MUST |
| Movimientos `/dashboard/cuentas/[numero]/movimientos` | `GET /cuentas/{numeroCuenta}/movimientos` | MUST |
| Rendimientos `/dashboard/cuentas/[numero]/rendimientos` | `GET /cuentas/{numeroCuenta}/rendimientos` | SHOULD |

**Validaciones frontend:**
- Monto depósito: min 0.01, max 500,000
- Monto retiro: no puede exceder saldo disponible
- Límites diarios de retiro

**Criterios de aceptación:**
- [ ] Depósitos y retiros con validación en tiempo real
- [ ] Historial de movimientos paginado
- [ ] Estado vacío cuando no hay operaciones

---

### FASE 4: Créditos (Semanas 11-14) 🔴 CRÍTICA

**Objetivo:** Módulo de créditos en ciclo completo

#### Socio
| Página | Endpoint API | Prioridad |
|--------|-------------|-----------|
| Catálogo tipos `/dashboard/creditos` | `GET /creditos/tipos-credito` | MUST |
| Simulador `/dashboard/creditos/simulador` | `POST /simulador` (requiere auth) | MUST |
| Solicitar crédito `/dashboard/creditos/solicitar` | `POST /creditos/solicitudes` | MUST |
| Mis solicitudes `/dashboard/creditos` | `GET /creditos/solicitudes/socio/{socioId}` | MUST |
| Detalle crédito `/dashboard/creditos/[numero]` | `GET /creditos/{numero}` | MUST |
| Plan amortización | `GET /creditos/solicitudes/{num}/plan` | MUST |
| Pagar cuota `/dashboard/creditos/[numero]/pagar` | `POST /creditos/cuotas/{id}/pago` | MUST |

#### Admin
| Página | Endpoint API | Prioridad |
|--------|-------------|-----------|
| Cola solicitudes `/admin/creditos/solicitudes` | `GET /creditos/solicitudes` | MUST |
| Evaluar `/admin/creditos/[numero]/evaluar` | `POST /creditos/solicitudes/{num}/evaluar` | MUST |
| Aprobar `/admin/creditos/[numero]/aprobar` | `POST /creditos/solicitudes/{num}/aprobar` | MUST |
| Rechazar `/admin/creditos/[numero]/rechazar` | `POST /creditos/solicitudes/{num}/rechazar` | MUST |
| Desembolsar `/admin/creditos/[numero]/desembolson` | `POST /creditos/solicitudes/{num}/desembolson` | MUST |

**Criterios de aceptación:**
- [ ] Simulador con inputs interactivos
- [ ] Tabla de amortización con scroll
- [ ] Indicador de cuotas vencidas
- [ ] Double-payment prevention (idempotency key)

---

### FASE 5: KYC + Beneficiarios + Documentos (Semanas 15-18) 🟡 MEDIA

**Objetivo:** Cumplimiento regulatorio y documentación

#### KYC
| Página | Endpoint API | Prioridad |
|--------|-------------|-----------|
| Dashboard KYC `/dashboard/kyc` | `GET /kyc/estado` | MUST |
| Subir documentos `/dashboard/kyc/documentos` | `POST /kyc/documentos` | MUST |
| Enviar a revisión `/dashboard/kyc/enviar` | `POST /kyc/enviar` | MUST |
| Admin: Cola revisión `/admin/kyc` | `GET /kyc/cola-revision` | MUST |
| Admin: Aprobar/Rechazar | `POST /kyc/revision/{id}/aprobar|rechazar` | MUST |

#### Beneficiarios
| Página | Endpoint API | Prioridad |
|--------|-------------|-----------|
| Lista `/dashboard/beneficiarios` | `GET /socios/{id}/beneficiarios` | MUST |
| Crear `/dashboard/beneficiarios/nuevo` | `POST /socios/{id}/beneficiarios` | MUST |
| Editar `/dashboard/beneficiarios/[id]/editar` | `PUT /socios/{id}/beneficiarios/{id}` | MUST |
| Eliminar `/dashboard/beneficiarios/[id]/eliminar` | `DELETE /socios/{id}/beneficiarios/{id}` | MUST |

**Regla de negocio:** Suma de porcentajes = 100% desde el inicio

#### Documentos PDF
| Página | Endpoint API | Prioridad |
|--------|-------------|-----------|
| Lista `/dashboard/documentos` | `GET /documentos/socio/{socioId}` | MUST |
| Generar estado cuenta | `GET /documentos/estado-cuenta/{cuentaId}` | MUST |
| Generar constancia | `GET /documentos/constancia-afiliacion/{socioId}` | MUST |
| Descargar | `GET /documentos/{docId}/descargar` | MUST |

---

### FASE 6: Hardening y Go-Live (Semanas 19-20) 🟢 MEDIA

**Objetivo:** Calidad enterprise para producción

| Entregable | Descripción | Prioridad |
|------------|-------------|-----------|
| E2E Tests | Playwright/Cypress para flujos críticos | MUST |
| Responsive Design | Mobile, tablet, desktop | MUST |
| Accesibilidad | WCAG AA | MUST |
| Performance | Core Web Vitals | MUST |
| SEO | Sitemap, robots.txt, meta tags | MUST |
| Deployment | Docker, Vercel, monitoreo | MUST |

---

## 4. Checklist de Implementación

### MUST HAVE (Fase 0-2)
- [ ] Sistema UI Components (Button, Input, Card, Dialog, Table, etc.)
- [ ] Layouts (public, auth, dashboard, admin)
- [ ] Login page + logout
- [ ] Middleware con redirect y roles
- [ ] Auth store con sync de cookies
- [ ] API client con interceptors
- [ ] Dashboard layout con sidebar
- [ ] Dashboard principal socio (stats)
- [ ] Module Cuentas (lista, detalle, saldo)
- [ ] Module Créditos (lista, solicitud)
- [ ] Landing page con SEO

### SHOULD HAVE (Fase 3-4)
- [ ] Depósitos y retiros
- [ ] Movimientos
- [ ] Detalle crédito + cuotas
- [ ] Module Beneficiarios
- [ ] Admin dashboard stats
- [ ] Registro flow completo

### WOULD HAVE (Fase 5+)
- [ ] KYC completo
- [ ] Documentos PDF
- [ ] Admin gestión socios
- [ ] Admin gestión créditos
- [ ] Perfil usuario
- [ ] Notificaciones push

---

## 5. No Funcionales

### Responsive Breakpoints
| Breakpoint | Layout |
|------------|--------|
| < 640px | Mobile - sidebar overlay |
| 640-1024px | Tablet - sidebar collapsed |
| > 1024px | Desktop - sidebar visible |

### Validación de Formularios
- React Hook Form + Zod
- Validación en tiempo real (onChange)
- Mensajes de error específicos por campo
- Prevención de submit doble (loading state)

### Manejo de Errores
- ErrorBoundary en cada page
- Retry logic en queries (retry: 1)
- Toast notifications para errores
- Fallback UI cuando backend no responde

### Estados de Carga
- Skeleton components para content
- Spinner para actions
- Progress bar para uploads
- Optimistic updates para mutations

---

## 6. Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-21 | @product-manager | Creación inicial del roadmap |

---

## 7. Referencias

- Índice del proyecto: [README.md](./README.md)
- Estructura de páginas: [ESTRUCTURA_PAGINAS.md](./ESTRUCTURA_PAGINAS.md)
- Roles y permisos: [ROLES_PERMISOS.md](./ROLES_PERMISOS.md)
- Design system: [DISENO_SISTEMA.md](./DISENO_SISTEMA.md)
- API contracts: [API_CONTRACTS.md](./API_CONTRACTS.md)
