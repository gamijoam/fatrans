# Visión General - Frontend Web

## Propósito

Frontend web moderno para la plataforma de Fondo de Ahorro, construido con Next.js 14+ (App Router) siguiendo arquitectura feature-based.

## Objetivos

| Objetivo | Descripción | Prioridad |
|----------|-------------|-----------|
| **Seguridad** | Manejo seguro de tokens con HttpOnly cookies | 🔴 Crítica |
| **UX FinTech** | Dashboard financiero con métricas | 🔴 Alta |
| **Mantenibilidad** | Arquitectura extensible y testeable | 🔴 Alta |
| **Rendimiento** | Carga rápida con SSR y caching | 🟡 Media |
| **SEO** | Landing pages indexables | 🟢 Baja |

## Alcance

```
FRONTEND WEB (Next.js 14+)
├── Landing Page pública (SEO)
├── Portal Socio (autenticado)
│   ├── Dashboard
│   ├── Cuentas de Ahorro
│   ├── Créditos
│   ├── KYC
│   ├── Beneficiarios
│   └── Documentos PDF
├── Portal Admin
│   ├── Dashboard stats
│   ├── Gestión Socios
│   ├── Gestión Créditos
│   └── Reportes
└── Auth (login, registro, recuperación)
```

## Principios de Arquitectura

1. **Feature-Based Architecture**: Cada módulo tiene su propia carpeta con API, components, hooks, schemas y types.
2. **Separación de Responsabilidades**: UI components son reutilizables, feature components son específicos.
3. **Type Safety**: TypeScript + Zod para validación en tiempo de desarrollo y runtime.
4. **Seguridad First**: HttpOnly cookies + CSRF, nunca localStorage para tokens.
5. **Performance**: React Query para cache inteligente, SSR para contenido público.

## Decisiones Clave

| Decisión | Razón |
|----------|-------|
| **Next.js 14 App Router** | SSR/SSG, middleware integrado, API routes |
| **TanStack Query** | Cache inteligente, deduplicación, loading states |
| **Zustand** | Boilerplate mínimo vs Redux, middleware simple |
| **React Hook Form + Zod** | Validación type-safe, performance superior |
| **Decimal.js** | Evita floating point errors en cálculos financieros |
| **HttpOnly Cookies** | Protege contra XSS, sincronización automática |
