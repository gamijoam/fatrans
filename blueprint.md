# BLUEPRINT: Frontend Next.js 14+ para Fondo de Ahorro

**Proyecto:** Plataforma Fondo de Ahorro
**Versión:** 1.0
**Fecha:** 2026-04-21
**Estado:** Blueprint - Listo para Implementación

---

## TABLA DE CONTENIDOS

1. [Visión General y Objetivos](#1-visión-general-y-objetivos)
2. [Stack Tecnológico](#2-stack-tecnológico)
3. [Estructura de Carpetas](#3-estructura-de-carpetas)
4. [Variables de Entorno](#4-variables-de-entorno)
5. [Arquitectura de Patrones](#5-arquitectura-de-patrones)
6. [Flujo de Autenticación](#6-flujo-de-autenticación)
7. [Gestión de Tipos desde OpenAPI](#7-gestión-de-tipos-desde-openapi)
8. [Seguridad](#8-seguridad)
9. [Módulos del Backend - Cobertura Frontend](#9-módulos-del-backend---cobertura-frontend)
10. [Consideraciones FinTech](#10-consideraciones-fintech)
11. [Plan de Implementación](#11-plan-de-implementación)

---

## 1. VISIÓN GENERAL Y OBJETIVOS

### 1.1 Propósito

Crear un frontend web moderno y mantenible para la plataforma de Fondo de Ahorro, reemplazando la implementación actual de Flutter Web y Astro, con énfasis en seguridad, rendimiento y experiencia del desarrollador.

### 1.2 Objetivos

| Objetivo | Descripción | Prioridad |
|----------|-------------|-----------|
| **Seguridad** | Implementar manejo seguro de tokens con HttpOnly cookies | 🔴 Crítica |
| **UX FinTech** | Dashboard financiero con métricas en tiempo real | 🔴 Alta |
| **Mantenibilidad** | Arquitectura extensible y testeable | 🔴 Alta |
| **Rendimiento** | Carga rápida con SSR y caching optimizado | 🟡 Media |
| **SEO** | Landing pages indexables | 🟢 Baja |

### 1.3 Alcance

```
FRONTEND WEB (Next.js 14+)
├── Landing Page pública (SEO)
├── Portal Socio (autenticado)
│   ├── Dashboard (resumen ahorro/créditos)
│   ├── Cuentas de Ahorro
│   ├── Créditos
│   ├── KYC
│   ├── Beneficiarios
│   └── Documentos PDF
├── Portal Admin (autenticado)
│   ├── Dashboard stats
│   ├── Gestión Socios
│   ├── Gestión Créditos
│   └── Reportes
└── Auth (login, registro, recuperación)
```

---

## 2. STACK TECNOLÓGICO

### 2.1 Dependencias de Producción

| Paquete | Versión | Propósito | Justificación |
|---------|---------|-----------|---------------|
| `next` | `14.2.0` | Framework | App Router, Server Components, middleware |
| `react` | `^18.3.0` | UI Library | Integración Next.js |
| `react-dom` | `^18.3.0` | React DOM | Rendering |
| `@tanstack/react-query` | `^5.28.0` | Data Fetching | Cache, refetch, loading states |
| `axios` | `^1.6.8` | HTTP Client | Interceptors, cancelation |
| `zustand` | `^4.5.2` | State Management | Simple, TypeScript-first |
| `zod` | `^3.22.4` | Validation | Schema validation, type inference |
| `react-hook-form` | `^7.51.2` | Forms | Performance, validation integration |
| `decimal.js` | `^10.4.3` | decimals | Precisión decimal para monedas |
| `jose` | `^5.2.4` | JWT handling | Browser-side JWT decode/verify |
| `js-cookie` | `^3.0.5` | Cookie handling | Lectura/escritura de cookies |
| `sonner` | `^1.4.3` | Toasts | Notificaciones |
| `recharts` | `^2.12.3` | Gráficos | Dashboard stats |
| `lucide-react` | `^0.359.0` | Iconos | Iconos SVG consistentes |
| `tailwindcss` | `^3.4.3` | CSS | Utility-first, performant |
| `class-variance-authority` | `^0.7.0` | Variants | Component variants |
| `clsx` | `^2.1.0` | Classes | Conditional classes |
| `tailwind-merge` | `^2.2.1` | Tailwind merge | Merge tailwind classes |
| `@react-pdf/renderer` | `^3.4.4` | PDF rendering | Preview PDFs en cliente |
| `date-fns` | `^3.6.0` | Fechas | Format/manipulate dates |

### 2.2 Dependencias de Desarrollo

| Paquete | Versión | Propósito |
|---------|---------|-----------|
| `typescript` | `^5.4.4` | Type safety |
| `@types/react` | `^18.3.0` | React types |
| `@types/node` | `^20.12.0` | Node types |
| `@types/js-cookie` | `^3.0.6` | Cookie types |
| `eslint` | `^8.57.0` | Linting |
| `eslint-config-next` | `14.2.0` | Next.js ESLint |
| `prettier` | `^3.2.5` | Formatting |
| `prettier-plugin-tailwindcss` | `^0.5.13` | Tailwind formatting |
| `tailwindcss-animate` | `^1.0.7` | Animations |
| `vitest` | `^1.4.0` | Testing |
| `@testing-library/react` | `^14.2.2` | React testing |
| `@testing-library/jest-dom` | `^6.4.2` | Jest DOM |
| `@vitejs/plugin-react` | `^4.2.1` | Vite React |

### 2.3 Justificación del Stack

| Decisión | Razón |
|----------|-------|
| **Next.js 14 App Router** | SSR/SSG, middleware integrado, API routes, mejor DX que Pages Router |
| **TanStack Query** | Cache inteligente, deduplicación, refetch automático, loading states |
| **Zustand** | Boilerplate mínimo vs Redux, TypeScript-first, middleware simple |
| **React Hook Form + Zod** | Validación type-safe, performance superior a Formik |
| **Decimal.js** | Evita floating point errors en cálculos financieros |
| **jose** | JWT verification en cliente (vs jwt-decode que no verifica) |
| **Tailwind CSS** | Desarrollo rápido, tree-shaking, consistencia |

---

## 3. ESTRUCTURA DE CARPETAS

### 3.1 Vista General

```
frontend-web/
├── src/
│   ├── app/                          # Next.js App Router
│   │   ├── (public)/                 # Grupo: rutas públicas
│   │   │   ├── layout.tsx           # Layout público
│   │   │   ├── page.tsx             # Landing page
│   │   │   ├── login/
│   │   │   │   └── page.tsx
│   │   │   └── registro/
│   │   │       └── page.tsx
│   │   ├── (auth)/                   # Grupo: autenticación
│   │   │   ├── layout.tsx           # Layout auth (middleware protected)
│   │   │   ├── login/
│   │   │   ├── logout/
│   │   │   └── reset-password/
│   │   ├── (dashboard)/              # Grupo: dashboard socio
│   │   │   ├── layout.tsx           # Layout con sidebar
│   │   │   ├── dashboard/
│   │   │   ├── cuentas/
│   │   │   ├── creditos/
│   │   │   ├── kyc/
│   │   │   ├── beneficiarios/
│   │   │   └── documentos/
│   │   ├── (admin)/                  # Grupo: portal admin
│   │   │   ├── layout.tsx
│   │   │   ├── dashboard/
│   │   │   ├── socios/
│   │   │   ├── creditos/
│   │   │   ├── aportaciones/
│   │   │   └── reportes/
│   │   ├── api/                      # API Routes (proxy al backend)
│   │   │   ├── auth/
│   │   │   │   ├── login/route.ts
│   │   │   │   ├── refresh/route.ts
│   │   │   │   └── logout/route.ts
│   │   │   └── proxy/[...path]/route.ts
│   │   ├── layout.tsx               # Root layout
│   │   └── globals.css
│   ├── components/
│   │   ├── ui/                       # Componentes base (shadcn-like)
│   │   │   ├── button.tsx
│   │   │   ├── input.tsx
│   │   │   ├── card.tsx
│   │   │   ├── badge.tsx
│   │   │   ├── dialog.tsx
│   │   │   ├── dropdown-menu.tsx
│   │   │   ├── table.tsx
│   │   │   ├── tabs.tsx
│   │   │   ├── select.tsx
│   │   │   ├── separator.tsx
│   │   │   └── skeleton.tsx
│   │   ├── forms/                    # Formularios reutilizables
│   │   │   ├── login-form.tsx
│   │   │   ├── deposito-form.tsx
│   │   │   ├── retiro-form.tsx
│   │   │   └── beneficiario-form.tsx
│   │   ├── dashboard/               # Componentes de dashboard
│   │   │   ├── sidebar.tsx
│   │   │   ├── stats-card.tsx
│   │   │   ├── stats-grid.tsx
│   │   │   ├── recent-movements.tsx
│   │   │   └── quick-actions.tsx
│   │   └── shared/                   # Componentes compartidos
│   │       ├── protected-route.tsx
│   │       ├── loading-spinner.tsx
│   │       ├── error-boundary.tsx
│   │       ├── toaster.tsx
│   │       └── page-header.tsx
│   ├── features/                     # Feature modules
│   │   ├── auth/
│   │   │   ├── api/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   ├── schemas/
│   │   │   └── types/
│   │   ├── cuentas/
│   │   ├── creditos/
│   │   ├── kyc/
│   │   ├── beneficiarios/
│   │   └── documentos/
│   ├── lib/
│   │   ├── api/
│   │   │   ├── client.ts
│   │   │   ├── endpoints.ts
│   │   │   └── interceptors/
│   │   ├── utils/
│   │   │   ├── money.ts
│   │   │   ├── date.ts
│   │   │   ├── cn.ts
│   │   │   └── format.ts
│   │   └── constants.ts
│   ├── stores/
│   │   ├── auth-store.ts
│   │   ├── ui-store.ts
│   │   └── notifications-store.ts
│   ├── types/
│   │   ├── api.ts
│   │   ├── user.ts
│   │   └── index.ts
│   ├── hooks/
│   │   ├── use-auth.ts
│   │   ├── use-permission.ts
│   │   └── use-debounce.ts
│   └── middleware.ts
├── public/
│   ├── favicon.svg
│   └── images/
├── .env.local
├── .env.example
├── next.config.ts
├── tailwind.config.ts
├── tsconfig.json
├── eslint.config.mjs
├── prettier.config.mjs
└── package.json
```

### 3.2 Explicación de Estructura

#### `src/app/` - App Router

| Ruta | Propósito |
|------|-----------|
| `(public)/` | Landing page pública, login, registro (no requiere auth) |
| `(auth)/` | Páginas de autenticación (post-login: reset password) |
| `(dashboard)/` | Portal del socio autenticado |
| `(admin)/` | Portal administrativo (roles ADMIN/GESTOR) |
| `api/proxy/[...path]/` | Proxy para evitar CORS y manejar cookies |

#### `src/features/` - Feature Modules

Cada módulo del backend tiene su correspondiente feature module:

```
Backend Module    →  Frontend Feature
─────────────────────────────────
auth             →  features/auth
ahorros          →  features/cuentas
creditos         →  features/creditos
kyc              →  features/kyc
beneficiarios    →  features/beneficiarios
documentospdf    →  features/documentos
socios           →  features/socios (admin)
```

### 3.3 Estructura Detallada de un Feature Module

```
features/cuentas/
├── api/
│   └── cuentas.api.ts
├── components/
│   ├── cuenta-list.tsx
│   ├── cuenta-detail.tsx
│   ├── deposito-form.tsx
│   ├── retiro-form.tsx
│   ├── movimiento-list.tsx
│   └── movimiento-detail.tsx
├── hooks/
│   └── use-cuentas.ts
├── schemas/
│   └── cuentas.schemas.ts
├── types/
│   └── cuentas.types.ts
└── index.ts
```

---

## 4. VARIABLES DE ENTORNO

### 4.1 Variables Requeridas

```env
# .env.local

# ============================================
# API Configuration
# ============================================
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_API_VERSION=v1

# ============================================
# Application
# ============================================
NEXT_PUBLIC_APP_NAME=FATRANS
NEXT_PUBLIC_APP_URL=http://localhost:3000

# ============================================
# Auth
# ============================================
AUTH_SECRET=your-secret-key-min-32-chars-long!!!
AUTH_ACCESS_TOKEN_TTL=900
AUTH_REFRESH_TOKEN_TTL=604800

# ============================================
# Features Flags
# ============================================
NEXT_PUBLIC_ENABLE_ANALYTICS=false
NEXT_PUBLIC_ENABLE_DEBUG=false
```

### 4.2 Justificación de Variables

| Variable | Propósito |
|----------|-----------|
| `NEXT_PUBLIC_API_URL` | URL base del backend |
| `AUTH_SECRET` | Secret para JWT en cliente |
| `AUTH_*_TOKEN_TTL` | Sincronización con backend |

---

## 5. ARQUITECTURA DE PATRONES

### 5.1 Feature-Based Architecture

Cada módulo del backend tiene su correspondiente feature module en el frontend.

**Beneficios:**
- Cohesión: código relacionado está junto
- Escalabilidad: nuevos features no rompen existentes
- Testabilidad: cada feature puede probarse aisladamente

### 5.2 Patrón de Componentes

```
UI Components (src/components/ui/*)
         ↓
Feature Components (src/features/{module}/components/*)
         ↓
Pages / Layouts (src/app/(group)/{page,layout}.tsx)
```

### 5.3 Patrón de Estado

```
Zustand Stores (src/stores/*)
         ↓
React Query (TanStack Query) - estado del servidor
```

### 5.4 Patrón de Validación

```
React Hook Form + Zod schemas
         ↓
API layer con validación reutilizable
```

---

## 6. FLUJO DE AUTENTICACIÓN

### 6.1 Diagrama de Flujo

```
USUARIO → Login Page → POST /login → BACKEND API
                                         ↓
                            Response (HttpOnly Cookies)
                            - accessToken
                            - refreshToken
                            - usuario
                                         ↓
                            Browser Cookie Store
                                         ↓
                            Auth Store (Zustand)
                                         ↓
                            API Client (Axios)
                            - Adjunta cookies
                            - Maneja CSRF
                            - Refresh automático
```

### 6.2 Implementación del API Client

```typescript
// src/lib/api/client.ts
import axios from 'axios';
import Cookies from 'js-cookie';

export const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  timeout: 10_000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  if (['post', 'put', 'delete', 'patch'].includes(config.method?.toLowerCase() || '')) {
    const csrfToken = Cookies.get('csrf_token');
    if (csrfToken) {
      config.headers['X-CSRF-Token'] = csrfToken;
    }
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const newToken = await refreshAccessToken();
        if (newToken) {
          originalRequest.headers['Authorization'] = `Bearer ${newToken}`;
          return apiClient(originalRequest);
        }
      } catch {
        await logout();
        window.location.href = '/login';
      }
    }
    throw error;
  }
);

export async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = Cookies.get('refresh_token');
  if (!refreshToken) return null;
  try {
    const response = await apiClient.post('/v1/auth/refresh', { refreshToken });
    return response.data.accessToken;
  } catch {
    return null;
  }
}

export async function logout() {
  try {
    await apiClient.post('/v1/auth/logout');
  } finally {
    Cookies.remove('access_token');
    Cookies.remove('refresh_token');
    Cookies.remove('csrf_token');
    Cookies.remove('usuario');
  }
}
```

### 6.3 Auth Store (Zustand)

```typescript
// src/stores/auth-store.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { devtools } from 'zustand/middleware';

export type UserRol = 'ADMIN' | 'ADMINISTRADOR' | 'GESTOR' | 'SOCIO';

export interface User {
  id: string;
  nombreUsuario: string;
  correoElectronico: string;
  nombreCompleto: string;
  rol: UserRol;
  socioId?: string;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setUser: (user: User | null) => void;
  setLoading: (loading: boolean) => void;
  logout: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  devtools(
    persist(
      (set) => ({
        user: null,
        isAuthenticated: false,
        isLoading: true,
        setUser: (user) => set({ user, isAuthenticated: !!user }),
        setLoading: (isLoading) => set({ isLoading }),
        logout: async () => {
          try {
            await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
          } finally {
            set({ user: null, isAuthenticated: false });
          }
        },
      }),
      { name: 'auth-storage', partialize: (state) => ({ user: state.user }) }
    ),
    { name: 'AuthStore' }
  )
);
```

### 6.4 Middleware de Autenticación

```typescript
// src/middleware.ts
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const pathname = request.nextUrl.pathname;
  const publicRoutes = ['/', '/login', '/registro', '/recuperar-password'];
  const isPublicRoute = publicRoutes.some(route => pathname === route);

  if (isPublicRoute) return NextResponse.next();

  const accessToken = request.cookies.get('access_token');
  if (!accessToken) {
    const loginUrl = new URL('/login', request.url);
    loginUrl.searchParams.set('redirect', pathname);
    return NextResponse.redirect(loginUrl);
  }

  if (pathname.startsWith('/admin')) {
    const userCookie = request.cookies.get('usuario');
    if (userCookie) {
      try {
        const user = JSON.parse(userCookie.value);
        if (!['ADMIN', 'ADMINISTRADOR', 'GESTOR'].includes(user.rol)) {
          return NextResponse.redirect(new URL('/dashboard', request.url));
        }
      } catch {
        return NextResponse.redirect(new URL('/login', request.url));
      }
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/((?!_next/static|_next/image|favicon.ico).*)'],
};
```

---

## 7. GESTIÓN DE TIPOS DESDE OPENAPI

### 7.1 Generación Automática de Tipos

```bash
npm install -D @openapitools/openapi-generator-cli

npx openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g typescript-fetch \
  -o src/types/generated \
  --additional-properties=typescriptSingleModule=true
```

### 7.2 Tipos Custom para Money

```typescript
// src/lib/utils/money.ts
import Decimal from 'decimal.js';

Decimal.set({ precision: 19, scale: 4, rounding: Decimal.ROUND_HALF_UP });

export class Money {
  private constructor(private readonly value: Decimal) {}

  static fromNumber(amount: number): Money {
    return new Money(new Decimal(amount));
  }

  static fromString(amount: string): Money {
    return new Money(new Decimal(amount));
  }

  static zero(): Money {
    return new Money(new Decimal(0));
  }

  get raw(): number { return this.value.toNumber(); }
  get cents(): number { return this.value.times(100).round().toNumber(); }

  add(other: Money): Money { return new Money(this.value.plus(other.value)); }
  subtract(other: Money): Money { return new Money(this.value.minus(other.value)); }
  multiply(factor: number): Money { return new Money(this.value.times(factor)); }
  divide(divisor: number): Money { return new Money(this.value.dividedBy(divisor)); }

  isGreaterThan(other: Money): boolean { return this.value.greaterThan(other.value); }
  isLessThan(other: Money): boolean { return this.value.lessThan(other.value); }
  isZero(): boolean { return this.value.isZero(); }

  format(locale = 'es-VE', currency = 'VES'): string {
    return new Intl.NumberFormat(locale, { style: 'currency', currency }).format(this.value.toNumber());
  }

  toString(): string { return this.value.toFixed(4); }
}

export function parseMoney(value: string | number): Money {
  if (typeof value === 'number') return Money.fromNumber(value);
  return Money.fromString(value.replace(/[^\d.,]/g, '').replace(',', '.'));
}
```

---

## 8. SEGURIDAD

### 8.1 HttpOnly Cookies + CSRF

| Aspecto | HttpOnly Cookies | localStorage |
|---------|-----------------|--------------|
| **XSS Attack** | ✅ Seguro | ❌ Vulnerable |
| **CSRF Attack** | ⚠️ Requiere protección | ✅ No afectado |

### 8.2 Headers de Seguridad

```typescript
// next.config.ts
const nextConfig = {
  async headers() {
    return [{
      source: '/:path*',
      headers: [
        { key: 'Strict-Transport-Security', value: 'max-age=63072000; includeSubDomains; preload' },
        { key: 'X-Content-Type-Options', value: 'nosniff' },
        { key: 'X-Frame-Options', value: 'DENY' },
        { key: 'X-XSS-Protection', value: '1; mode=block' },
        { key: 'Referrer-Policy', value: 'strict-origin-when-cross-origin' },
      ],
    }];
  },
};
```

---

## 9. MÓDULOS DEL BACKEND - COBERTURA FRONTEND

| Módulo Backend | Endpoints | Feature Frontend | Prioridad |
|----------------|-----------|------------------|----------|
| **Auth** | 9 | features/auth | 🔴 Crítica |
| **Socios** | 13 | features/socios | 🔴 Crítica |
| **Ahorros** | 12 | features/cuentas | 🔴 Crítica |
| **Créditos** | 14 | features/creditos | 🔴 Alta |
| **KYC** | 12 | features/kyc | 🟡 Media |
| **Beneficiarios** | 4 | features/beneficiarios | 🟡 Media |
| **Documentos PDF** | 9 | features/documentos | 🟡 Media |

---

## 10. CONSIDERACIONES FINTECH

### 10.1 Validación de Datos Financieros

```typescript
// src/lib/utils/validators.ts
import { z } from 'zod';

export const moneySchema = z.object({
  monto: z.string().refine((val) => {
    const num = parseFloat(val.replace(/[^\d.,]/g, '').replace(',', '.'));
    return !isNaN(num) && num >= 0.01 && num <= 500000;
  }, { message: 'Monto inválido (0.01 - 500,000)' }),
});

export const documentoSchema = z.object({
  tipoDocumento: z.enum(['CEDULA_IDENTIDAD', 'PASAPORTE', 'CEDULA_EXTRANJERA']),
  numeroDocumento: z.string().regex(/^[VEJPGvejpg]\d{6,12}$/, 'Formato inválido'),
});

export const beneficiarioSchema = z.object({
  nombreCompleto: z.string().min(3).max(200),
  tipoDocumento: z.enum(['CEDULA_IDENTIDAD', 'RIF', 'PASAPORTE', 'CEDULA_EXTRANJERO']),
  numeroDocumento: z.string().min(5),
  parentesco: z.enum(['CONYUGE', 'HIJO', 'PADRE', 'MADRE', 'HERMANO', 'ABUELO', 'NIETO', 'SOBRINO', 'TIO', 'OTRO']),
  porcentaje: z.string().refine((val) => {
    const num = parseFloat(val);
    return !isNaN(num) && num >= 0.01 && num <= 100;
  }, { message: 'Porcentaje inválido (0.01 - 100)' }),
  telefono: z.string().optional(),
});
```

### 10.2 Rate Limiter Cliente

```typescript
class ClientRateLimiter {
  private buckets: Map<string, { tokens: number; lastRefill: number }> = new Map();

  constructor(
    private maxTokens: number = 10,
    private refillRate: number = 1,
    private windowMs: number = 1000
  ) {}

  async acquire(key: string): Promise<boolean> {
    let bucket = this.buckets.get(key);
    if (!bucket) {
      bucket = { tokens: this.maxTokens, lastRefill: Date.now() };
      this.buckets.set(key, bucket);
    }

    const now = Date.now();
    const elapsed = now - bucket.lastRefill;
    const tokensToAdd = (elapsed / this.windowMs) * this.refillRate;
    bucket.tokens = Math.min(this.maxTokens, bucket.tokens + tokensToAdd);
    bucket.lastRefill = now;

    if (bucket.tokens >= 1) {
      bucket.tokens--;
      return true;
    }
    return false;
  }
}

export const rateLimiter = new ClientRateLimiter(10, 1, 1000);
```

---

## 11. PLAN DE IMPLEMENTACIÓN

### FASE 1 - Foundation (Semanas 1-2)
- [ ] Setup proyecto Next.js 14 con TypeScript y Tailwind
- [ ] Configurar ESLint, Prettier, husky
- [ ] Crear estructura de carpetas
- [ ] Implementar API client con interceptors
- [ ] Configurar auth flow con HttpOnly cookies
- [ ] Setup Zustand stores
- [ ] Crear componentes UI base (shadcn-like)

### FASE 2 - Auth (Semana 2-3)
- [ ] Middleware de autenticación
- [ ] Login page + layout
- [ ] Protected routes
- [ ] Logout flow
- [ ] Refresh token automático

### FASE 3 - Dashboard Socio (Semanas 3-4)
- [ ] Dashboard layout con sidebar
- [ ] Stats cards y gráficos
- [ ] Página principal
- [ ] Quick actions

### FASE 4 - Módulos Core (Semanas 4-6)
- [ ] Cuentas de Ahorro (lista, detalle, saldo)
- [ ] Depósitos y Retiros
- [ ] Movimientos
- [ ] Créditos (tipos, solicitudes, cuotas)
- [ ] KYC (verificación, documentos)
- [ ] Beneficiarios (CRUD)

### FASE 5 - Admin Portal (Semanas 6-7)
- [ ] Admin layout
- [ ] Dashboard admin
- [ ] Gestión Socios
- [ ] Gestión Créditos
- [ ] Reportes básicos

### FASE 6 - Documentos (Semana 7-8)
- [ ] Lista de documentos
- [ ] Preview PDF
- [ ] Descarga

### FASE 7 - Polish (Semana 8-9)
- [ ] Testing
- [ ] Responsive design
- [ ] Optimización performance
- [ ] SEO para landing
- [ ] Deployment

---

## COMANDOS DE SETUP

```bash
# Crear proyecto
npx create-next-app@14 frontend-web --typescript --tailwind --eslint --app --src-dir --import-alias "@/*" --use-npm

# Instalar dependencias principales
cd frontend-web
npm install next@14.2.0 react@^18.3.0 react-dom@^18.3.0

# Data fetching
npm install @tanstack/react-query@^5.28.0 axios@^1.6.8

# State management
npm install zustand@^4.5.2

# Validación
npm install zod@^3.22.4 react-hook-form@^7.51.2 @hookform/resolvers

# Utilities
npm install decimal.js@^10.4.3 jose@^5.2.4 js-cookie@^3.0.5 sonner@^1.4.3 date-fns@^3.6.0

# UI & Icons
npm install lucide-react@^0.359.0 recharts@^2.12.3 class-variance-authority@^0.7.0 clsx@^2.1.0 tailwind-merge@^2.2.1

# Dev dependencies
npm install -D typescript@^5.4.4 @types/react@^18.3.0 @types/node@^20.12.0 @types/js-cookie@^3.0.6 eslint@^8.57.0 eslint-config-next@14.2.0 prettier@^3.2.5 prettier-plugin-tailwindcss@^0.5.13 tailwindcss-animate@^1.0.7

# PDF
npm install @react-pdf/renderer@^3.4.4

# Generar tipos OpenAPI
npm install -D @openapitools/openapi-generator-cli
npx openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g typescript-fetch \
  -o src/types/generated \
  --additional-properties=typescriptSingleModule=true
```

---

**Última actualización:** 2026-04-21
**Estado:** Listo para implementación
