# Base del Proyecto - Documentación Frontend

**Proyecto:** Fondo de Ahorro (FATRANS)
**Versión:** 1.0
**Fecha:** 2026-04-21
**Estado:** Planificado

---

## Índice

| Documento | Descripción |
|-----------|-------------|
| [FRONTEND_ROADMAP.md](./FRONTEND_ROADMAP.md) | Roadmap completo de implementación (20 semanas) |
| [ESTRUCTURA_PAGINAS.md](./ESTRUCTURA_PAGINAS.md) | Todas las páginas a crear con rutas y componentes |
| [ROLES_PERMISOS.md](./ROLES_PERMISOS.md) | Matriz de roles y permisos por módulo |
| [DISENO_SISTEMA.md](./DISENO_SISTEMA.md) | Design system, colores, tipografía, componentes |
| [API_CONTRACTS.md](./API_CONTRACTS.md) | Contratos FE-BE firmados por endpoint |
| [STACK_TECNOLOGICO.md](./STACK_TECNOLOGICO.md) | Stack tecnológico y justificación |
| [FLUJO_AUTENTICACION.md](./FLUJO_AUTENTICACION.md) | Flujo de auth con BFF y cookies httpOnly |

---

## Resumen Ejecutivo

### Stack Elegido
- **Frontend:** Next.js 14+ (App Router)
- **Estilo:** Tailwind CSS + shadcn/ui
- **Estado:** Zustand
- **Datos:** TanStack Query
- **Validación:** Zod + React Hook Form
- **API:** Axios con interceptors
- **Moneda:** Decimal.js
- **Notificaciones:** Sonner (toasts)

### Arquitectura BFF
```
Browser → Next.js (BFF) → Spring Boot API
              ↓
         httpOnly Cookies (JWT)
```

### Decisiones Clave
| Decisión | Valor |
|----------|-------|
| Idioma | Español |
| Colores | Blanco (#FFFFFF), Verde claro (#90EE90), Azul claro (#ADD8E6) |
| Registro | Requiere aprobación admin |
| Beneficiarios | Suma = 100% desde inicio |
| Simulador | Requiere autenticación |
| KYC | Obligatorio antes de transacciones |

### Roles del Sistema
- `SOCIO` - Usuario miembro del fondo
- `ADMIN` - Administrador
- `SUPER_ADMIN` - Admin supreme (futuro)

### Fases de Desarrollo
| Fase | Duración | Focus |
|------|----------|-------|
| 0 | Semanas 1-2 | Plataforma base, UI components, layouts |
| 1 | Semanas 3-4 | Landing page + Auth completo |
| 2 | Semanas 5-7 | Core Socio/Admin (registro, dashboard) |
| 3 | Semanas 8-10 | Ahorros y movimientos |
| 4 | Semanas 11-14 | Créditos |
| 5 | Semanas 15-18 | KYC + Beneficiarios + Documentos |
| 6 | Semanas 19-20 | Hardening + Go-Live |

---

## Referencias

- Documentación módulos: `/docs/modulos/*/API.md`
- Visión frontend: `/docs/frontend/VISION.md`
- Especificación backend: `/backend/src/main/java/com/tufondo/`
- Repo: https://github.com/gamijoam/fatrans
