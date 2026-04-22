# Frontend Web - Documentación

## Tabla de Contenidos

1. [Visión General](./VISION.md)
2. [Estructura del Proyecto](./STRUCTURE.md)
3. [Guía de Desarrollo](./DEVELOPMENT.md)
4. [API Client](./API_CLIENT.md)
5. [Autenticación](./AUTH.md)
6. [Módulos](./MODULES.md)

---

## Stack Tecnológico

| Paquete | Versión | Propósito |
|---------|---------|-----------|
| `next` | `14.2.0` | Framework - App Router |
| `react` | `^18.3.0` | UI Library |
| `@tanstack/react-query` | `^5.28.0` | Data Fetching |
| `axios` | `^1.6.8` | HTTP Client |
| `zustand` | `^4.5.2` | State Management |
| `zod` | `^3.22.4` | Validation |
| `react-hook-form` | `^7.51.2` | Forms |
| `decimal.js` | `^10.4.3` | Precisión monetaria |
| `tailwindcss` | `^3.4.3` | CSS |

## Requisitos

- Node.js 18+
- npm o yarn
- Backend ejecutándose en `localhost:8080`

## Setup Inicial

```bash
cd frontend-web
npm install
cp .env.example .env.local
npm run dev
```

La aplicación estará disponible en `http://localhost:3000`.

## Scripts Disponibles

```bash
npm run dev          # Desarrollo
npm run build        # Producción
npm run start        # Iniciar producción
npm run lint         # ESLint
npm run format       # Prettier
npm run typecheck    # TypeScript check
npm test             # Tests
```
