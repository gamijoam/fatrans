# Autenticación

## Flujo de Autenticación

```
┌──────────────────────────────────────────────────────────────────────┐
│                    FLUJO DE AUTENTICACIÓN JWT                        │
├──────────────────────────────────────────────────────────────────────┤
│  USUARIO → Login Page → POST /api/v1/auth/login                     │
│                              ↓                                        │
│                     BACKEND (Spring Security)                        │
│                              ↓                                        │
│            Response con HttpOnly Cookies:                            │
│            - accessToken (15 min)                                     │
│            - refreshToken (7 días)                                    │
│            - usuario (JSON)                                           │
│                              ↓                                        │
│                     Browser Cookie Store                             │
│                              ↓                                        │
│                     Auth Store (Zustand)                             │
│                              ↓                                        │
│            API Client usa cookies automáticamente                     │
└──────────────────────────────────────────────────────────────────────┘
```

## Almacenamiento de Tokens

| Storage | Acceso JS | XSS | CSRF | Uso |
|---------|----------|-----|------|-----|
| **HttpOnly Cookies** | ❌ No | ✅ Seguro | ⚠️ Requiere token CSRF | ✅ Tokens |
| localStorage | ✅ Sí | ❌ Vulnerable | ✅ No afectado | ❌ NO usar |

## Auth Store

```typescript
// src/stores/auth-store.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setUser: (user: User | null) => void;
  setLoading: (loading: boolean) => void;
  logout: () => Promise<void>;
}
```

## Middleware de Autenticación

El middleware en `src/middleware.ts` protege rutas automáticamente:

```typescript
// Rutas públicas (no requieren auth)
const publicRoutes = ['/', '/login', '/registro', '/recuperar-password'];

// Rutas admin requieren rol específico
if (pathname.startsWith('/admin')) {
  const allowedRoles = ['ADMIN', 'ADMINISTRADOR', 'GESTOR'];
  // Verifica rol del usuario
}
```

## Protected Route Component

```typescript
// src/components/shared/protected-route.tsx
'use client';

export function ProtectedRoute({ children, allowedRoles }: ProtectedRouteProps) {
  const { user, isAuthenticated, isLoading } = useAuthStore();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isLoading, isAuthenticated, router]);

  // Renderiza children solo si está autenticado y tiene el rol correcto
  if (isLoading || !isAuthenticated) return <LoadingSpinner />;
  return <>{children}</>;
}
```

## Funciones de Auth

```typescript
import { login, logout, refreshAccessToken } from '@/lib/api/client';

// Login
const response = await login({ identificador, password });

// Logout
await logout();

// Refresh (automático en interceptor)
const newToken = await refreshAccessToken();
```

## Roles de Usuario

| Rol | Descripción | Acceso |
|-----|-------------|--------|
| `ADMIN` | Administrador del sistema | Todo |
| `ADMINISTRADOR` | Administrador de banco | Todo |
| `GESTOR` | Gestor de cuentas | Todo (limitado) |
| `SOCIO` | Usuario final | Solo su información |

## Reset Password Flow

```
1. Usuario solicita recuperación → POST /v1/auth/recuperar-password
2. Backend envía email con token temporal
3. Usuario ingresa token + nueva password → POST /v1/auth/reset-password
4. Backend valida token y actualiza password
```

## Validaciones

- Identificador: mínimo 3 caracteres
- Password: mínimo 8 caracteres
- Refresh token: válido por 7 días
- Access token: válido por 15 minutos
- CSRF token: generado en cada login, válido por sesión
