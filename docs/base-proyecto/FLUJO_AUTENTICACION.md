# Flujo de Autenticación - Fondo de Ahorro

**Proyecto:** FATRANS
**Versión:** 1.1
**Fecha:** 2026-04-22

---

> **NOTA IMPORTANTE:** Este documento usa los endpoints `*-web` del backend que manejan cookies httpOnly específicamente para web. Verificar que el backend tenga implementados estos endpoints antes de proceder.

---

## 1. Arquitectura de Autenticación

### 1.1 Diagrama General BFF

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ARQUITECTURA DE AUTENTICACIÓN BFF                        │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                              BROWSER                                         │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                        Cookie Jar                                       │ │
│  │  ┌─────────────────────┐    ┌─────────────────────┐                   │ │
│  │  │   access_token      │    │   refresh_token     │                   │ │
│  │  │   HttpOnly          │    │   HttpOnly           │                   │ │
│  │  │   Secure            │    │   Secure             │                   │ │
│  │  │   SameSite=Strict   │    │   SameSite=Strict    │                   │ │
│  │  │   Path=/            │    │   Path=/api/v1/auth/│                   │ │
│  │  │   Max-Age=900       │    │   refresh-web        │                   │ │
│  │  │                     │    │   Max-Age=604800    │                   │ │
│  │  └─────────────────────┘    └─────────────────────┘                   │ │
│  │                                                                           │ │
│  │  ⚠️ JavaScript NO puede acceder a estas cookies (XSS mitigated)          │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                     React Application                                    │ │
│  │                                                                        │ │
│  │  ┌─────────────────┐         ┌─────────────────┐                      │ │
│  │  │  Auth Store     │         │  TanStack Query │                      │ │
│  │  │  (Zustand)     │         │  (API Cache)    │                      │ │
│  │  │                 │         │                 │                      │ │
│  │  │  • user         │◀───────▶│  • useLogin()   │                      │ │
│  │  │  • isAuth       │         │  • useCuentas() │                      │ │
│  │  │  • role         │         │  • useCreditos() │                      │ │
│  │  │  • socioId      │         │                 │                      │ │
│  │  └─────────────────┘         └─────────────────┘                      │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS (Cookie: access_token)
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           NEXT.JS BFF                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                      API Routes / Server Actions                        │ │
│  │                                                                        │ │
│  │  POST /api/auth/login                                                  │ │
│  │    └─▶ Axios POST to Spring Boot: /api/v1/auth/login-web             │ │
│  │    └─▶ Backend sets httpOnly cookies directly                           │ │
│  │    └─▶ Extract user from response body/headers                         │ │
│  │    └─▶ Return user data to client                                      │ │
│  │                                                                        │ │
│  │  POST /api/auth/refresh                                                 │ │
│  │    └─▶ Read refresh_token from cookie                                  │ │
│  │    └─▶ Axios POST to Spring Boot: /api/v1/auth/refresh-web            │ │
│  │    └─▶ Backend sets new httpOnly cookies                               │ │
│  │    └─▶ Return user data to client                                      │ │
│  │                                                                        │ │
│  │  POST /api/auth/logout                                                 │ │
│  │    └─▶ Axios POST to Spring Boot: /api/v1/auth/logout-web             │ │
│  │    └─▶ Backend clears httpOnly cookies                                 │ │
│  │    └─▶ Return success to client                                        │ │
│  │                                                                        │ │
│  │  API Proxy (all other endpoints)                                        │ │
│  │    └─▶ Read access_token from cookie                                   │ │
│  │    └─▶ Axios GET/POST to Spring Boot (with Authorization header)      │ │
│  │    └─▶ Return response to client                                        │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                    Middleware (Next.js Edge)                            │ │
│  │                                                                        │ │
│  │  • Route protection (dashboard/*, admin/*)                             │ │
│  │  • Role-based redirects                                                 │ │
│  │  • Token validation (lightweight)                                        │ │
│  │  • CSRF token handling                                                 │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS (Authorization: Bearer <jwt>)
                                    │ or Cookie for /auth/* endpoints
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SPRING BOOT API                                      │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                    Security Configuration                                │ │
│  │                                                                        │ │
│  │  • JWT Filter (extracts token from header OR cookie)                   │ │
│  │  • CORS Configuration (allows only Next.js origin)                      │ │
│  │  • Role-based authorization                                             │ │
│  │  • Rate limiting (Bucket4j)                                             │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │               Endpoints Web (con cookies httpOnly)                     │ │
│  │                                                                        │ │
│  │  POST /api/v1/auth/login-web     → Creates JWT, sets httpOnly cookies│ │
│  │  POST /api/v1/auth/refresh-web    → Validates refresh, sets new cookies │ │
│  │  POST /api/v1/auth/logout-web   → Invalidates session, clears cookies │ │
│  │                                                                        │ │
│  │               Endpoints Estándar (para mobile/etc)                     │ │
│  │                                                                        │ │
│  │  POST /api/v1/auth/login        → Returns tokens in body (no cookies)  │ │
│  │  POST /api/v1/auth/refresh     → Returns tokens in body (no cookies)  │ │
│  │  POST /api/v1/auth/logout      → Invalidates session                   │ │
│  │  GET  /api/v1/auth/me         → Returns user from JWT                 │ │
│  │  ... (all other protected endpoints)                                   │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Endpoints del Backend

| Endpoint | Uso | Cookies |
|----------|-----|---------|
| `POST /api/v1/auth/login-web` | Login web | ✅ httpOnly cookies |
| `POST /api/v1/auth/refresh-web` | Refresh web | ✅ httpOnly cookies |
| `POST /api/v1/auth/logout-web` | Logout web | ✅ httpOnly cookies |
| `POST /api/v1/auth/login` | Login mobile | ❌ Tokens en body |
| `POST /api/v1/auth/refresh` | Refresh mobile | ❌ Tokens en body |
┌─────────────────────────────────────────────────────────────────────────────┐
│                    ARQUITECTURA DE AUTENTICACIÓN BFF                        │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                              BROWSER                                         │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                        Cookie Jar                                       │ │
│  │  ┌─────────────────────┐    ┌─────────────────────┐                   │ │
│  │  │   access_token      │    │   refresh_token     │                   │ │
│  │  │   HttpOnly          │    │   HttpOnly           │                   │ │
│  │  │   Secure            │    │   Secure             │                   │ │
│  │  │   SameSite=Strict   │    │   SameSite=Strict    │                   │ │
│  │  │   Path=/            │    │   Path=/api/auth/    │                   │ │
│  │  │   Max-Age=900       │    │   refresh-web        │                   │ │
│  │  │                     │    │   Max-Age=604800    │                   │ │
│  │  └─────────────────────┘    └─────────────────────┘                   │ │
│  │                                                                           │ │
│  │  ⚠️ JavaScript NO puede acceder a estas cookies (XSS mitigated)          │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                     React Application                                    │ │
│  │                                                                        │ │
│  │  ┌─────────────────┐         ┌─────────────────┐                      │ │
│  │  │  Auth Store     │         │  TanStack Query │                      │ │
│  │  │  (Zustand)     │         │  (API Cache)    │                      │ │
│  │  │                 │         │                 │                      │ │
│  │  │  • user         │◀───────▶│  • useLogin()   │                      │ │
│  │  │  • isAuth       │         │  • useCuentas() │                      │ │
│  │  │  • role         │         │  • useCreditos()│                      │ │
│  │  │  • socioId      │         │                 │                      │ │
│  │  └─────────────────┘         └─────────────────┘                      │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS (Cookie: access_token)
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           NEXT.JS BFF                                        │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                      API Routes / Server Actions                        │ │
│  │                                                                        │ │
│  │  POST /api/auth/login                                                  │ │
│  │    └─▶ Axios POST to Spring Boot                                       │ │
│  │    └─▶ Extract tokens from response body                               │ │
│  │    └─▶ Set-Cookie: access_token (httpOnly)                            │ │
│  │    └─▶ Set-Cookie: refresh_token (httpOnly)                            │ │
│  │    └─▶ Return user data to client                                      │ │
│  │                                                                        │ │
│  │  POST /api/auth/refresh                                                 │ │
│  │    └─▶ Read refresh_token from cookie                                  │ │
│  │    └─▶ Axios POST to Spring Boot                                       │ │
│  │    └─▶ Extract new tokens from response body                           │ │
│  │    └─▶ Set-Cookie: access_token (new)                                  │ │
│  │    └─▶ Set-Cookie: refresh_token (new, rotation)                      │ │
│  │    └─▶ Return user data to client                                      │ │
│  │                                                                        │ │
│  │  API Proxy (all other endpoints)                                        │ │
│  │    └─▶ Read access_token from cookie                                    │ │
│  │    └─▶ Axios GET/POST to Spring Boot (with Authorization header)       │ │
│  │    └─▶ Return response to client                                       │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                    Middleware (Next.js Edge)                            │ │
│  │                                                                        │ │
│  │  • Route protection (dashboard/*, admin/*)                              │ │
│  │  • Role-based redirects                                                 │ │
│  │  • Token validation (lightweight)                                      │ │
│  │  • CSRF token handling                                                 │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTPS (Authorization: Bearer <jwt>)
                                    │ or Cookie for /auth/* endpoints
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SPRING BOOT API                                      │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                    Security Configuration                                │ │
│  │                                                                        │ │
│  │  • JWT Filter (extracts token from header OR cookie)                    │ │
│  │  • CORS Configuration (allows only Next.js origin)                     │ │
│  │  • Role-based authorization                                             │ │
│  │  • Rate limiting (Bucket4j)                                             │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐ │
│  │                    Endpoints                                             │ │
│  │                                                                        │ │
│  │  POST /api/v1/auth/login        → Creates JWT, returns in body         │ │
│  │  POST /api/v1/auth/refresh     → Validates refresh, creates new JWTs  │ │
│  │  POST /api/v1/auth/logout      → Invalidates session                  │ │
│  │  GET  /api/v1/auth/me          → Returns user from JWT                │ │
│  │  ... (all other protected endpoints)                                   │ │
│  └───────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Flujos Detallados

### 2.1 Login Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              LOGIN FLOW                                     │
└─────────────────────────────────────────────────────────────────────────────┘

1. USER OPENS LOGIN PAGE
═══════════════════════════════
   Browser                    Next.js                   Spring Boot
       │                          │                          │
       │  GET /auth/login         │                          │
       │─────────────────────────▶│                          │
       │                          │                          │
       │  Render LoginPage         │                          │
       │◀─────────────────────────│                          │
       │                          │                          │

2. USER SUBMITS CREDENTIALS
════════════════════════════════
   Browser                    Next.js                   Spring Boot
       │                          │                          │
       │  POST /api/auth/login     │                          │
       │  {identificador, password}│                          │
       │─────────────────────────▶│                          │
       │                          │  POST /api/v1/auth/login-web
       │                          │  {identificador, password}
       │                          │─────────────────────────▶│
       │                          │                          │
       │                          │  Validate credentials     │
       │                          │  Generate JWTs           │
       │                          │  Set-Cookie: access_token│
       │                          │  Set-Cookie: refresh_token
       │                          │                          │
       │                          │◀──────────────────────────│
       │                          │  {usuario} (from body)   │
       │  Cookies set by backend: │                         │
       │  access_token &          │                         │
       │  refresh_token            │                         │
       │◀─────────────────────────│                          │
       │                          │                          │
       │  Update AuthStore:       │                          │
       │  { user, isAuth: true } │                          │
       │                          │                          │
       │  Redirect to /dashboard   │                          │
       │─────────────────────────▶│                          │
       │                          │  POST /api/v1/auth/login │
       │                          │  {identificador, password}
       │                          │─────────────────────────▶│
       │                          │                          │
       │                          │  Validate credentials     │
       │                          │  Generate JWTs           │
       │                          │                          │
       │                          │◀──────────────────────────│
       │                          │  {accessToken,          │
       │                          │   refreshToken,         │
       │                          │   usuario}               │
       │                          │                          │
       │  Set-Cookie: access_token │                         │
       │  (httpOnly, secure,       │                         │
       │   sameSite=strict)       │                         │
       │◀─────────────────────────│                          │
       │                          │                          │
       │  Set-Cookie: refresh_token│                         │
       │  (httpOnly, secure,      │                         │
       │   sameSite=strict,       │                         │
       │   Path=/api/auth/       │                         │
       │   refresh-web)          │                         │
       │◀─────────────────────────│                          │
       │                          │                          │
       │  Update AuthStore:       │                          │
       │  { user, isAuth: true }  │                          │
       │                          │                          │
       │  Redirect to /dashboard  │                          │
       │─────────────────────────▶│                          │
       │                          │                          │
```

---

### 2.2 Authenticated Request Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        AUTHENTICATED REQUEST FLOW                           │
└─────────────────────────────────────────────────────────────────────────────┘

1. COMPONENT MAKES API CALL
═══════════════════════════════════
   Browser                    Next.js                   Spring Boot
       │                          │                          │
       │  useCuentas() query      │                          │
       │                          │  GET /api/cuentas/socio/123
       │                          │  Cookie: access_token
       │                          │─────────────────────────▶│
       │                          │                          │
       │                          │  JwtFilter extracts token │
       │                          │  from cookie             │
       │                          │                          │
       │                          │  Validate JWT            │
       │                          │  Extract usuarioId, rol  │
       │                          │                          │
       │                          │  Execute query           │
       │                          │                          │
       │                          │◀──────────────────────────│
       │                          │  200 OK                  │
       │                          │  {cuentas: [...]}        │
       │                          │                          │
       │◀─────────────────────────│                          │
       │  {cuentas: [...]}        │                          │
       │                          │                          │
```

---

### 2.3 Token Refresh Flow (401 Interceptor)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         TOKEN REFRESH FLOW (401)                              │
└─────────────────────────────────────────────────────────────────────────────┘

1. API CALL RETURNS 401
════════════════════════════
   Browser                    Next.js                   Spring Boot
       │                          │                          │
       │  GET /api/cuentas/...   │                          │
       │─────────────────────────▶│                          │
       │                          │  GET /api/v1/cuentas/... │
       │                          │  (with access_token cookie)│
       │                          │─────────────────────────▶│
       │                          │                          │
       │                          │◀──────────────────────────│
       │                          │  401 Unauthorized        │
       │                          │  (token expired)         │
       │                          │                          │
       │  401 detected            │                          │
       │  (axios interceptor)     │                          │
       │                          │                          │
       │  POST /api/auth/refresh  │                          │
       │  Cookie: refresh_token   │                          │
       │─────────────────────────▶│                          │
       │                          │  POST /api/v1/auth/      │
       │                          │           refresh-web   │
       │                          │  Cookie: refresh_token  │
       │                          │─────────────────────────▶│
       │                          │                          │
       │                          │  Validate refresh token  │
       │                          │  Invalidate old session   │
       │                          │  Generate new tokens     │
       │                          │  Set-Cookie: NEW tokens │
       │                          │                          │
       │                          │◀──────────────────────────│
       │                          │  200 OK + new cookies   │
       │                          │                          │
       │  Cookies updated by backend│                         │
       │  (access_token &         │                         │
       │   refresh_token)         │                         │
       │◀─────────────────────────│                          │
       │                          │                          │
       │  Retry original request   │                         │
       │  GET /api/cuentas/...   │                          │
       │  Cookie: NEW access_token│                         │
       │─────────────────────────▶│                          │
       │                          │                          │
       │◀─────────────────────────│                          │
       │  200 OK {cuentas: [...]} │                          │
       │                          │                          │
       │  GET /api/cuentas/...    │                          │
       │─────────────────────────▶│                          │
       │                          │  GET /api/v1/cuentas/...  │
       │                          │─────────────────────────▶│
       │                          │                          │
       │                          │◀──────────────────────────│
       │                          │  401 Unauthorized        │
       │                          │  (token expired)         │
       │                          │                          │
       │  401 detected            │                          │
       │  (axios interceptor)     │                          │
       │                          │                          │
       │  POST /api/auth/refresh  │                          │
       │  Cookie: refresh_token    │                          │
       │─────────────────────────▶│                          │
       │                          │  POST /api/v1/auth/       │
       │                          │           refresh-web    │
       │                          │  Cookie: refresh_token   │
       │                          │─────────────────────────▶│
       │                          │                          │
       │                          │  Validate refresh token  │
       │                          │  Invalidate old session  │
       │                          │  Generate new tokens     │
       │                          │                          │
       │                          │◀──────────────────────────│
       │                          │  200 OK + new tokens     │
       │                          │                          │
       │  Set-Cookie: access_token │                         │
       │  (NEW, httpOnly, ...)    │                         │
       │◀─────────────────────────│                          │
       │                          │                          │
       │  Set-Cookie: refresh_token│                         │
       │  (NEW, httpOnly, ...,    │                         │
       │   rotation)             │                         │
       │◀─────────────────────────│                          │
       │                          │                          │
       │  Retry original request   │                         │
       │  GET /api/cuentas/...    │                          │
       │  Cookie: NEW access_token │                         │
       │─────────────────────────▶│                          │
       │                          │                          │
       │◀─────────────────────────│                          │
       │  200 OK {cuentas: [...]}  │                          │
       │                          │                          │
```

---

### 2.4 Logout Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              LOGOUT FLOW                                     │
└─────────────────────────────────────────────────────────────────────────────┘

   Browser                    Next.js                   Spring Boot
       │                          │                          │
       │  Click "Logout"          │                          │
       │                          │  POST /api/auth/logout    │
       │                          │  Cookie: access_token    │
       │─────────────────────────▶│                          │
       │                          │  POST /api/v1/auth/      │
       │                          │           logout-web     │
       │                          │  Cookie: access_token    │
       │                          │─────────────────────────▶│
       │                          │                          │
       │                          │  Invalidate session       │
       │                          │  (all sessions)          │
       │                          │  Clear cookies           │
       │                          │                          │
       │                          │◀──────────────────────────│
       │                          │  200 OK                  │
       │                          │                          │
       │  Cookies cleared by backend│                        │
       │  (Max-Age=0 on both)    │                         │
       │◀─────────────────────────│                          │
       │                          │                          │
       │  Clear AuthStore:        │                          │
       │  { user: null,         │                          │
       │    isAuth: false }      │                          │
       │                          │                          │
       │  Redirect to /auth/login │
       │─────────────────────────▶│                          │
       │                          │  POST /api/v1/auth/logout │
       │                          │  Cookie: access_token     │
       │                          │─────────────────────────▶│
       │                          │                          │
       │                          │  Invalidate session      │
       │                          │  (all sessions)          │
       │                          │                          │
       │                          │◀──────────────────────────│
       │                          │  200 OK                  │
       │                          │                          │
       │  Set-Cookie: access_token│                         │
       │  (Max-Age=0, delete)     │                         │
       │◀─────────────────────────│                          │
       │                          │                          │
       │  Set-Cookie: refresh_token│                         │
       │  (Max-Age=0, delete)     │                         │
       │◀─────────────────────────│                          │
       │                          │                          │
       │  Clear AuthStore:        │                          │
       │  { user: null,           │                          │
       │    isAuth: false }      │                          │
       │                          │                          │
       │  Redirect to /auth/login │                          │
       │─────────────────────────▶│                          │
       │                          │                          │
```

---

## 3. Implementación Frontend

### 3.1 Axios Client (client.ts)

```typescript
// src/lib/api/client.ts
import axios from 'axios';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  withCredentials: true, // Include cookies in cross-origin requests
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: Token is automatically sent via cookie by the browser
// No need to manually add Authorization header for SameSite=Strict cookies
apiClient.interceptors.request.use(async (config) => {
  return config;
});

// Response interceptor: Handle 401 (token refresh)
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // Avoid infinite loops - only retry once
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Call refresh endpoint via Next.js API route (BFF pattern)
        await axios.post(
          '/api/auth/refresh',
          {},
          { withCredentials: true }
        );

        // Retry original request after successful refresh
        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh failed, redirect to login
        window.location.href = '/auth/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default apiClient;
```

### 3.2 Auth Store (auth-store.ts)

```typescript
// src/stores/auth-store.ts
import { create } from 'zustand';

interface User {
  id: string;
  nombreUsuario: string;
  correoElectronico: string;
  nombreCompleto: string;
  rol: 'SOCIO' | 'ADMIN' | 'SUPER_ADMIN';
  socioId?: string;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  setUser: (user: User | null) => void;
  setAuthenticated: (isAuth: boolean) => void;
  setLoading: (isLoading: boolean) => void;
  logout: () => Promise<void>;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: true,

  setUser: (user) => set({ user }),

  setAuthenticated: (isAuthenticated) => set({ isAuthenticated }),

  setLoading: (isLoading) => set({ isLoading }),

  logout: async () => {
    try {
      // Call logout endpoint to invalidate session on backend
      await fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include',
      });
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Clear state - NOT persisted to localStorage for security
      set({ user: null, isAuthenticated: false });
    }
  },
}));
```

> **⚠️ IMPORTANTE:** El auth store NO debe persistir datos de usuario en localStorage. El token de autenticación vive en cookies httpOnly y el usuario se valida contra el backend en cada request. Persistir el usuario en localStorage expondría datos sensibles.

### 3.3 Middleware (middleware.ts)

```typescript
// src/middleware.ts
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Public routes (no auth required)
  const publicRoutes = ['/', '/auth/login', '/auth/registro', '/auth/recuperar-password', '/auth/reset-password'];
  const isPublicRoute = publicRoutes.some(route => pathname.startsWith(route));

  // Get auth cookie
  const accessToken = request.cookies.get('access_token')?.value;

  // If trying to access protected route without auth
  if (!isPublicRoute && !accessToken) {
    const loginUrl = new URL('/auth/login', request.url);
    loginUrl.searchParams.set('redirect', pathname);
    return NextResponse.redirect(loginUrl);
  }

  // If authenticated and trying to access login page
  if (accessToken && pathname.startsWith('/auth/login')) {
    return NextResponse.redirect(new URL('/dashboard', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    '/((?!api|_next/static|_next/image|favicon.ico).*)',
  ],
};
```

### 3.4 Login API Route

```typescript
// src/app/api/auth/login/route.ts
import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { identificador, password } = body;

    // Call backend with credentials to receive httpOnly cookies
    const backendResponse = await fetch(
      `${process.env.BACKEND_API_URL}/api/v1/auth/login-web`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ identificador, password }),
        credentials: 'include', // IMPORTANT: Receive and forward cookies
      }
    );

    if (!backendResponse.ok) {
      const errorData = await backendResponse.json();
      return NextResponse.json(
        { error: errorData.codigo || 'LOGIN_FAILED' },
        { status: backendResponse.status }
      );
    }

    const usuario = await backendResponse.json();

    // Create response - cookies are set by the backend directly
    // We just need to forward user data
    const nextResponse = NextResponse.json(usuario);

    // Copy cookies from backend response to client response
    // This is critical for BFF pattern - cookies must flow through
    const setCookieHeaders = backendResponse.headers.getSetCookie?.() || [];
    for (const cookieHeader of setCookieHeaders) {
      nextResponse.headers.append('Set-Cookie', cookieHeader);
    }

    return nextResponse;
  } catch (error: any) {
    console.error('Login error:', error);
    return NextResponse.json(
      { error: 'LOGIN_FAILED' },
      { status: 500 }
    );
  }
}
```

### 3.5 Refresh API Route

```typescript
// src/app/api/auth/refresh/route.ts
import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
  try {
    // Call backend with current refresh token cookie
    // Credentials: 'include' is critical - sends cookies to backend
    const backendResponse = await fetch(
      `${process.env.BACKEND_API_URL}/api/v1/auth/refresh-web`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include', // Include refresh_token cookie
      }
    );

    if (!backendResponse.ok) {
      // Refresh failed - clear cookies and redirect to login
      const nextResponse = NextResponse.json(
        { error: 'REFRESH_FAILED' },
        { status: 401 }
      );

      // Clear cookies on failure
      nextResponse.cookies.set('access_token', '', {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'strict',
        path: '/',
        maxAge: 0,
      });
      nextResponse.cookies.set('refresh_token', '', {
        httpOnly: true,
        secure: process.env.NODE_ENV === 'production',
        sameSite: 'strict',
        path: '/api/v1/auth/refresh-web',
        maxAge: 0,
      });

      return nextResponse;
    }

    const usuario = await backendResponse.json();

    // Create response
    const nextResponse = NextResponse.json(usuario);

    // Copy cookies from backend response (includes new tokens via rotation)
    const setCookieHeaders = backendResponse.headers.getSetCookie?.() || [];
    for (const cookieHeader of setCookieHeaders) {
      nextResponse.headers.append('Set-Cookie', cookieHeader);
    }

    return nextResponse;
  } catch (error: any) {
    console.error('Refresh error:', error);
    return NextResponse.json(
      { error: 'REFRESH_FAILED' },
      { status: 500 }
    );
  }
}
```

---

## 4. Seguridad

### 4.1 Protecciones Implementadas

| Protección | Implementación |
|------------|----------------|
| XSS | Tokens en cookies httpOnly (JS no puede acceder) |
| CSRF | SameSite=Strict previene requests cross-site |
| Token Theft | httpOnly previene robo por XSS |
| Token Rotation | Cada refresh invalida el anterior |
| Session Fixation | Nuevos tokens en cada login/refresh |
| Rate Limiting | Bucket4j en backend (5 login/min por IP) |
| Account Lockout | 5 intentos fallidos = 30 min bloqueo |

### 4.2 Cookie Security Flags

```javascript
{
  httpOnly: true,     // JavaScript cannot access
  secure: true,        // Only sent over HTTPS
  sameSite: 'strict',  // Only sent to same origin
  path: '/',            // Scope of the cookie
  maxAge: 900          // 15 minutes for access token
}
```

---

## 5. Historial de Cambios

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-21 | @product-manager | Creación inicial del flujo de autenticación |
| 1.1 | 2026-04-22 | @auditoria | Corrección crítica: usar endpoints `*-web`, paths de cookies correctos,移除 persistencia de usuario en localStorage |

---

## 6. Referencias

- Issue #47 - Cookies httpOnly: `/docs/modulos/auth/ISSUE_47_COOKIES_HTTPONLY.md`
- Auth API: `/docs/modulos/auth/API.md`
- Auth Spec: `/docs/modulos/auth/SPEC.md`
