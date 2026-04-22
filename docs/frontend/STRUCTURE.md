# Estructura del Proyecto

## Vista General

```
frontend-web/
├── src/
│   ├── app/                          # Next.js App Router
│   │   ├── (public)/                 # Rutas públicas
│   │   │   ├── layout.tsx
│   │   │   ├── page.tsx             # Landing page
│   │   │   ├── login/
│   │   │   └── registro/
│   │   ├── (auth)/                   # Grupo: autenticación
│   │   ├── (dashboard)/              # Portal socio
│   │   ├── (admin)/                  # Portal admin
│   │   ├── api/                      # API Routes (proxy)
│   │   ├── layout.tsx               # Root layout
│   │   └── globals.css
│   ├── components/
│   │   ├── ui/                       # Componentes base (shadcn-like)
│   │   ├── forms/                    # Formularios reutilizables
│   │   ├── dashboard/               # Componentes de dashboard
│   │   ├── landing/                 # Componentes de landing
│   │   └── shared/                   # Componentes compartidos
│   ├── features/                     # Feature modules
│   ├── lib/
│   │   ├── api/                     # API client
│   │   └── utils/                   # Utilidades
│   ├── stores/                       # Zustand stores
│   ├── types/                       # Tipos
│   └── hooks/                       # Custom hooks globales
├── public/
├── .env.local
├── next.config.ts
├── tailwind.config.ts
├── tsconfig.json
└── package.json
```

## Directorios Principales

### `src/app/` - App Router

Contiene las rutas de Next.js organizadas por grupos:

| Ruta | Propósito |
|------|-----------|
| `(public)/` | Landing page, login, registro |
| `(auth)/` | Páginas post-login (reset password) |
| `(dashboard)/` | Portal del socio autenticado |
| `(admin)/` | Portal administrativo |
| `api/proxy/[...path]/` | Proxy para evitar CORS |

### `src/features/` - Feature Modules

Cada módulo del backend tiene su correspondiente feature module:

```
features/
├── auth/           → Login, logout, refresh, reset
├── cuentas/        → Cuentas ahorro, depósitos, retiros
├── creditos/       → Tipos crédito, solicitudes, cuotas
├── kyc/            → Verificación identidad, documentos
├── beneficiarios/  → CRUD beneficiarios
└── documentos/    → Generación y descarga de PDFs
```

### `src/components/ui/` - Sistema de Diseño

Componentes base headless estilados con Tailwind:

- Button, Input, Card, Badge
- Dialog, DropdownMenu, Table
- Tabs, Select, Separator, Skeleton

### `src/lib/` - Utilidades

| Archivo | Propósito |
|---------|-----------|
| `api/client.ts` | Axios instance con interceptors |
| `utils/cn.ts` | classnames helper (clsx + twMerge) |
| `utils/money.ts` | Clase Money con Decimal.js |
| `utils/validators.ts` | Zod schemas compartidos |

### `src/stores/` - Estado Global

| Store | Propósito |
|-------|-----------|
| `auth-store.ts` | Usuario autenticado, sesión |
| `ui-store.ts` | Estado UI (sidebar, theme) |
| `notifications-store.ts` | Notificaciones toast |

## Mapa de Rutas

```
/                          → Landing page pública
/login                     → Login
/registro                  → Registro
/dashboard                → Dashboard socio
/dashboard/cuentas        → Cuentas de ahorro
/dashboard/cuentas/[id]    → Detalle cuenta
/dashboard/creditos       → Créditos
/dashboard/kyc            → Verificación KYC
/dashboard/beneficiarios  → Beneficiarios
/dashboard/documentos     → Documentos PDF
/admin                    → Dashboard admin
/admin/socios             → Gestión socios
/admin/creditos           → Gestión créditos
/admin/aportaciones       → Gestión aportaciones
/admin/reportes           → Reportes
```
