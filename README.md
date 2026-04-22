# 🏦 Plataforma Digital - Fondo de Ahorro

Bienvenido al repositorio central de la plataforma integral para la gestión de fondos de ahorro. Este sistema incluye la API Core (Spring Boot) y el Portal Web (Next.js 14+).

---

## 🚀 Requisitos Previos

Para levantar todo el entorno localmente de forma automatizada, solo necesitas tener instalado:
- **Docker** y **Docker Compose**
- **Git**

*(Nota: No necesitas instalar Java, Node, Postgres ni Flutter en tu máquina si solo vas a consumir los servicios o levantar el entorno, Docker se encarga de todo).*

---

## 🛠️ Cómo Iniciar el Proyecto Localmente

### Levantar todo el entorno (Backend + Frontend + Database)

```bash
cd infrastructure
docker compose up -d --build
```

Esto levanta:
- **PostgreSQL** en puerto `5432` (local: `15432`)
- **Redis** en puerto `6379` (local: `16379`)
- **MinIO** en puerto `9000` (local: `19000`)
- **Backend (Spring Boot)** en puerto `8080` (local: `18080`)
- **Frontend (Next.js)** en puerto `3000` (local: `13000`)

### Verificar que funciona

```bash
docker ps
```

### Accesos

| Servicio | URL | Credenciales |
|----------|-----|-------------|
| Frontend | http://localhost:13000 | - |
| Backend API | http://localhost:18080 | - |
| Swagger UI | http://localhost:18080/v3/api-docs | - |
| MinIO Console | http://localhost:19001 | minioadmin / minioadmin123 |

---

## 📁 Estructura del Proyecto

```
fondo-ahorro-platform/
├── backend/                    # API Spring Boot (Java 21)
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── frontend-web/               # Portal Web (Next.js 14+)
│   ├── src/
│   │   ├── app/               # App Router
│   │   ├── components/        # Componentes UI
│   │   ├── features/          # Módulos feature-based
│   │   ├── lib/              # Utils (API client, money, etc.)
│   │   ├── stores/           # Zustand stores
│   │   └── types/            # Tipos TypeScript
│   ├── Dockerfile
│   └── package.json
├── infrastructure/
│   ├── docker-compose.yml     # Orquestación completa
│   ├── postgres-init.sql
│   └── seed_admin.sql
├── docs/                      # Documentación
│   ├── frontend/              # Docs frontend
│   ├── auditorias/
│   └── modulos/
├── blueprint.md               # Blueprint técnico frontend
└── README.md
```

---

## 📚 Documentación Importante

Para entender cómo aportar al proyecto:

- 👉 [Flujo de Trabajo y Reglas Atómicas (Git Flow)](docs/DEVELOPER_WORKFLOW.md)
- 👉 [Informe General y Funcional del Proyecto](docs/informe_general_proyecto.md)
- 👉 [Guía de Frontend Web](docs/frontend/README.md) - Stack, setup, arquitectura
- 👉 [Blueprint Técnico Frontend](blueprint.md) - Especificaciones completas

---

## 🖥️ Desarrollo Local (Frontend)

### Requisitos locales
- Node.js 18+
- npm o yarn

### Setup

```bash
cd frontend-web
npm install
cp .env.example .env.local
npm run dev
```

### Scripts disponibles

```bash
npm run dev          # Desarrollo (http://localhost:3000)
npm run build        # Producción
npm run lint         # ESLint
npm run format       # Prettier
npm run typecheck    # TypeScript check
```

---

## 🔐 Variables de Entorno

### Backend (en infrastructure o .env)

```env
DB_PASS=tu_password_db
REDIS_PASS=tu_password_redis
JWT_SECRET=tu_jwt_secret_muy_largo_y_seguro
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123
```

### Frontend

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_APP_NAME=FATRANS
```

---

## 🏗️ Arquitectura

### Backend
- **Framework:** Spring Boot 3.2 (Java 21)
- **Arquitectura:** Clean Architecture + DDD
- **Base de datos:** PostgreSQL 15
- **Caché:** Redis 7
- **Object Storage:** MinIO
- **Seguridad:** JWT con refresh tokens

### Frontend Web
- **Framework:** Next.js 14 (App Router)
- **UI:** React 18 + Tailwind CSS
- **Estado:** Zustand + TanStack Query
- **Validación:** Zod + React Hook Form
- **Dinero:** Decimal.js (precisión financiera)

---

## 📦 Modules

| Módulo | Descripción | Endpoints |
|--------|-------------|-----------|
| **Auth** | Autenticación JWT | 9 |
| **Socios** | Gestión de socios | 13 |
| **Cuentas** | Cuentas de ahorro | 12 |
| **Créditos** | Sistema de créditos | 14 |
| **KYC** | Verificación de identidad | 12 |
| **Beneficiarios** | Designación beneficiarios | 4 |
| **Documentos** | Generación PDF | 9 |
