# Stack Tecnológico - Fondo de Ahorro

**Proyecto:** FATRANS
**Versión:** 1.0
**Fecha:** 2026-04-21

---

## 1. Stack Elegido

### 1.1 Frontend (Next.js 14+)

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Next.js | 14+ | Framework React con App Router |
| React | 18+ | Librería UI |
| TypeScript | 5+ | Tipado estático |
| Tailwind CSS | 3.4+ | Framework CSS utility-first |
| shadcn/ui | latest | Componentes UI accesibles |
| TanStack Query | 5+ | Gestión de estado servidor |
| Zustand | 4+ | Gestión de estado cliente |
| React Hook Form | 7+ | Manejo de formularios |
| Zod | 3+ | Validación de esquemas |
| Axios | 1+ | Cliente HTTP |
| Decimal.js | 10+ | Cálculos monetarios precisos |
| Sonner | 1+ | Notificaciones toast |
| Lucide React | latest | Iconos |
| Recharts | 2+ | Gráficos |
| Framer Motion | 11+ | Animaciones |
| next/navigation | - | Navegación |

### 1.2 Backend (Spring Boot)

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 21 | Lenguaje |
| Spring Boot | 3.2+ | Framework |
| Spring Security | 6+ | Seguridad |
| Spring Data JPA | 3+ | Persistencia |
| PostgreSQL | 15+ | Base de datos |
| Redis | 7+ | Cache y sesiones |
| MinIO | latest | Object storage |
| Bucket4j | 8+ | Rate limiting |
| JJWT | 0.12+ | Tokens JWT |
| SpringDoc | 2+ | Documentación OpenAPI |

### 1.3 Infraestructura

| Tecnología | Propósito |
|------------|-----------|
| Docker | Containerización |
| Docker Compose | Orquestación local |
| Vercel | Hosting frontend |
| Railway/Render | Hosting backend |

---

## 2. Justificación de Decisiones

### 2.1 Next.js sobre Flutter Web

| Factor | Next.js | Flutter Web |
|--------|---------|-------------|
| SEO | ✅ SSR nativo | ⚠️ Requiere trabajo extra |
| Server Components | ✅ Nativo | ❌ No soportado |
| BFF Pattern | ✅ Fácil | ⚠️ Posible pero no idiomático |
| Ecosistema | ✅ React 18 | ⚠️ Limitado |
| Comunidad | ✅ Muy grande | ⚠️ Emergente |
| Hiring | ✅ Más developers | ⚠️ Menos specialists |

**Decisión:** Next.js 14+ con App Router para mejor SEO y patrón BFF.

### 2.2 TanStack Query sobre Redux/RTK

| Factor | TanStack Query | Redux Toolkit |
|--------|-----------------|---------------|
| Boilerplate | ✅ Mínimo | ⚠️ Más código |
| Cache automático | ✅ Integrado | ❌ Manual |
| Optimistic updates | ✅ Integrado | ❌ Manual |
| Suspense | ✅ Integrado | ⚠️ Wrapper necesario |
| SSR support | ✅ Integrado | ⚠️ Manual |

**Decisión:** TanStack Query por su simplicidad y features专为 datos del servidor.

### 2.3 Zustand sobre Context/Jotai

| Factor | Zustand | Context | Jotai |
|--------|---------|---------|-------|
| Boilerplate | ✅ Mínimo | ⚠️ Provider hell | ✅ Mínimo |
| Performance | ✅ Selector-based | ⚠️ Re-renders | ✅ Atomic |
| DevTools | ✅ Integrado | ❌ Manual | ✅ |
| Persistencia | ✅ Plugin | ❌ Manual | ✅ Plugin |

**Decisión:** Zustand por su simplicidad y plugin de persistencia.

### 2.4 BFF Pattern

```
┌─────────────────────────────────────────────────────────────────────┐
│                    PATRÓN BFF (Backend for Frontend)                │
└─────────────────────────────────────────────────────────────────────┘

                    ┌──────────────────────────────────┐
                    │           BROWSER                 │
                    │   (Next.js React Application)     │
                    └──────────────┬───────────────────┘
                                   │
                                   │ HTTPS
                                   ▼
                    ┌──────────────────────────────────┐
                    │        NEXT.JS BFF              │
                    │  (Server Side + API Routes)     │
                    │                                   │
                    │  • SSG/SSR para SEO              │
                    │  • Manejo de cookies httpOnly    │
                    │  • Token refresh automático       │
                    │  • Protección CSRF              │
                    │  • CORS simplification          │
                    └──────────────┬───────────────────┘
                                   │
                                   │ HTTPS
                                   ▼
                    ┌──────────────────────────────────┐
                    │       SPRING BOOT API             │
                    │  (Java 21 + Spring Security)     │
                    │                                   │
                    │  • Validación JWT                │
                    │  • Autorización por roles        │
                    │  • Rate limiting                 │
                    │  • Auditoría                     │
                    └──────────────────────────────────┘
```

**Beneficios:**
1. **Seguridad:** Tokens nunca expuestos al browser
2. **SEO:** Pages pre-renderizadas
3. **Performance:** Menos requests desde el browser
4. **Simplicidad CORS:** Un origen (next.js)

### 2.5 Cookies httpOnly sobre localStorage

| Factor | Cookies httpOnly | localStorage |
|--------|------------------|--------------|
| XSS Risk | ✅ Mitigado (JS no puede acceder) | ❌ Vulnerable |
| CSRF Protection | ✅ SameSite attribute | ❌ No tiene |
| Transmisión | ✅ Automática en cada request | ❌ Manual |
| Tamaño | ⚠️ 4KB por cookie | ✅ ~5MB |
| HTTPOnly | ✅ Flag disponible | ❌ No disponible |

**Decisión:** Cookies httpOnly para tokens JWT.

---

## 3. Arquitectura de Archivos

```
frontend-web/
├── src/
│   ├── app/                      # Next.js App Router
│   │   ├── (public)/            # Grupo de rutas públicas
│   │   │   ├── layout.tsx       # Public layout
│   │   │   ├── page.tsx         # Landing page
│   │   │   ├── productos/
│   │   │   ├── quienes-somos/
│   │   │   ├── terminos/
│   │   │   └── privacidad/
│   │   │
│   │   ├── (auth)/             # Grupo de rutas auth
│   │   │   ├── layout.tsx       # Auth layout
│   │   │   ├── login/
│   │   │   ├── registro/
│   │   │   ├── recuperar-password/
│   │   │   └── reset-password/
│   │   │
│   │   ├── (app)/              # Grupo de rutas socio
│   │   │   ├── layout.tsx       # Dashboard layout
│   │   │   ├── dashboard/
│   │   │   ├── cuentas/
│   │   │   ├── creditos/
│   │   │   ├── kyc/
│   │   │   ├── beneficiarios/
│   │   │   ├── documentos/
│   │   │   ├── perfil/
│   │   │   └── configuracion/
│   │   │
│   │   ├── (admin)/            # Grupo de rutas admin
│   │   │   ├── layout.tsx       # Admin layout
│   │   │   ├── admin/
│   │   │   ├── socios/
│   │   │   ├── creditos/
│   │   │   ├── kyc/
│   │   │   ├── documentos/
│   │   │   └── estadisticas/
│   │   │
│   │   ├── api/                 # API Routes (BFF)
│   │   │   └── [...nextauth]/   # Auth handlers
│   │   │
│   │   ├── layout.tsx           # Root layout
│   │   ├── globals.css          # Tailwind + custom
│   │   └── not-found.tsx        # 404 page
│   │
│   ├── components/
│   │   ├── ui/                  # shadcn/ui components
│   │   │   ├── button.tsx
│   │   │   ├── input.tsx
│   │   │   ├── card.tsx
│   │   │   ├── dialog.tsx
│   │   │   ├── table.tsx
│   │   │   ├── badge.tsx
│   │   │   ├── select.tsx
│   │   │   ├── tabs.tsx
│   │   │   └── ...
│   │   │
│   │   ├── shared/              # Componentes compartidos
│   │   │   ├── query-provider.tsx
│   │   │   ├── skeleton.tsx
│   │   │   ├── empty-state.tsx
│   │   │   └── error-boundary.tsx
│   │   │
│   │   ├── layout/              # Layout components
│   │   │   ├── public-navbar.tsx
│   │   │   ├── auth-navbar.tsx
│   │   │   ├── dashboard-sidebar.tsx
│   │   │   ├── admin-sidebar.tsx
│   │   │   ├── header.tsx
│   │   │   └── footer.tsx
│   │   │
│   │   └── forms/              # Form components
│   │       ├── login-form.tsx
│   │       ├── registro-form.tsx
│   │       └── ...
│   │
│   ├── features/               # Feature-based modules
│   │   ├── auth/
│   │   │   ├── api/
│   │   │   │   └── auth.api.ts
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   │   ├── use-login.ts
│   │   │   │   ├── use-logout.ts
│   │   │   │   └── use-usuario-actual.ts
│   │   │   ├── schemas/
│   │   │   │   └── auth.schemas.ts
│   │   │   └── types/
│   │   │       └── auth.types.ts
│   │   │
│   │   ├── cuentas/
│   │   │   ├── api/
│   │   │   ├── components/
│   │   │   ├── hooks/
│   │   │   └── ...
│   │   │
│   │   ├── creditos/
│   │   ├── kyc/
│   │   ├── beneficiarios/
│   │   ├── documentos/
│   │   └── admin/
│   │
│   ├── lib/
│   │   ├── api/
│   │   │   ├── client.ts        # Axios instance
│   │   │   ├── interceptors.ts  # 401/403/429 handlers
│   │   │   └── endpoints.ts     # API endpoints
│   │   │
│   │   ├── utils/
│   │   │   ├── cn.ts           # classnames utility
│   │   │   ├── money.ts         # Decimal.js Money class
│   │   │   ├── date.ts          # Date formatting
│   │   │   └── validators.ts    # Zod schemas
│   │   │
│   │   └── constants.ts         # App constants
│   │
│   ├── stores/                 # Zustand stores
│   │   ├── auth-store.ts
│   │   ├── ui-store.ts
│   │   └── notification-store.ts
│   │
│   ├── hooks/                  # Global hooks
│   │   ├── use-auth.ts
│   │   ├── use-permissions.ts
│   │   └── use-tracking.ts
│   │
│   ├── types/                  # Global types
│   │   ├── api.ts
│   │   ├── user.ts
│   │   └── ...
│   │
│   └── middleware.ts            # Next.js middleware
│
├── public/                     # Static assets
│   ├── images/
│   └── fonts/
│
├── .env.example               # Environment template
├── next.config.ts             # Next.js config
├── tailwind.config.ts         # Tailwind config
├── tsconfig.json               # TypeScript config
├── package.json
└── Dockerfile
```

---

## 4. Variables de Entorno

### Frontend (.env.local)

```bash
# API
NEXT_PUBLIC_API_URL=https://api.fondoahorro.com
NEXT_PUBLIC_APP_URL=http://localhost:3000

# Auth
NEXT_PUBLIC_REFRESH_COOKIE_NAME=refresh_token
NEXT_PUBLIC_ACCESS_COOKIE_NAME=access_token

# Feature Flags
NEXT_PUBLIC_ENABLE_ANALYTICS=false
NEXT_PUBLIC_ENABLE_DARK_MODE=false
```

### Backend (application.yml)

```yaml
# Server
server:
  port: 8080

# Database
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fondoahorro
    username: ${DB_USER}
    password: ${DB_PASSWORD}

# JWT
jwt:
  secret: ${JWT_SECRET}
  access-token:
    expiration: 900000  # 15 minutes
  refresh-token:
    expiration: 604800000  # 7 days

# CORS
cors:
  allowed-origins: ${CORS_ORIGINS:http://localhost:3000}

# Redis
spring.data.redis:
  host: localhost
  port: 6379
```

---

## 5. Scripts de Package.json

```json
{
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    "lint": "next lint",
    "lint:fix": "next lint --fix",
    "typecheck": "tsc --noEmit",
    "test": "jest",
    "test:e2e": "playwright test",
    "format": "prettier --write .",
    "storybook": "storybook dev -p 6006",
    "docker:dev": "docker compose up"
  }
}
```

---

## 6. Dependencias Detalladas

### Core
```json
{
  "next": "^14.2.0",
  "react": "^18.3.0",
  "react-dom": "^18.3.0",
  "typescript": "^5.4.0"
}
```

### UI
```json
{
  "tailwindcss": "^3.4.0",
  "@radix-ui/react-dialog": "^1.0.0",
  "@radix-ui/react-dropdown-menu": "^2.0.0",
  "@radix-ui/react-select": "^2.0.0",
  "@radix-ui/react-tabs": "^1.0.0",
  "class-variance-authority": "^0.7.0",
  "clsx": "^2.1.0",
  "tailwind-merge": "^2.3.0",
  "lucide-react": "^0.400.0"
}
```

### State Management
```json
{
  "@tanstack/react-query": "^5.28.0",
  "zustand": "^4.5.0",
  "react-hook-form": "^7.51.0",
  "@hookform/resolvers": "^3.3.0",
  "zod": "^3.22.0"
}
```

### API & Data
```json
{
  "axios": "^1.6.0",
  "decimal.js": "^10.4.0",
  "date-fns": "^3.6.0"
}
```

### Notifications & Animations
```json
{
  "sonner": "^1.4.0",
  "framer-motion": "^11.0.0",
  "recharts": "^2.12.0"
}
```

---

## 7. Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-21 | @product-manager | Creación inicial del stack |

---

## 8. Referencias

- Next.js: https://nextjs.org/
- TanStack Query: https://tanstack.com/query
- Zustand: https://zustand-demo.pmnd.rs/
- shadcn/ui: https://ui.shadcn.com/
- Tailwind CSS: https://tailwindcss.com/
