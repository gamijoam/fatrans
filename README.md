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

### 1. Crear archivo de variables de entorno

```bash
cd infrastructure
cp .env.example .env
# Editar .env y completar los valores
```

### 2. Levantar todo el entorno (Backend + Frontend + Database)

```bash
docker compose up -d --build
```

Esto levanta:
- **PostgreSQL** en puerto `5432` (local: `15432`)
- **Redis** en puerto `6379` (local: `16379`)
- **MinIO** en puerto `9000` (local: `19000`)
- **Backend (Spring Boot)** en puerto `8080` (local: `18080`)
- **Frontend (Next.js)** en puerto `3000` (local: `13000`)

### 3. Crear usuario Admin inicial

**Solo la primera vez**, después de que el backend esté corriendo:

```bash
# Ver instrucciones en:
cat infrastructure/SEED_ADMIN.md
```

### 4. Verificar que funciona

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
├── frontend-web/               # Portal Web (Next.js 14+)
├── infrastructure/             # Docker Compose y scripts
│   ├── docker-compose.yml
│   ├── .env.example           # Template de variables
│   ├── seed_admin.sql         # Script para crear admin (manual)
│   └── SEED_ADMIN.md           # Instrucciones del script
├── docs/                      # Documentación
│   ├── informacion/            # Guías de cambios y fundamentos
│   ├── frontend/               # Docs del frontend
│   └── auditorias/             # Auditorías de código
└── CONTRIBUTING.md            # Reglas del equipo
```

---

## 📚 Documentación para Desarrolladores

### 🚀 Guía de Inicio Rápido
Ver sección **[Cómo Iniciar el Proyecto](#-cómo-iniciar-el-proyecto-localmente)** arriba.

### 📖 Guías de Cambios y Fundamentos
Ubicación: `docs/informacion/`

| Guía | Descripción |
|------|-------------|
| **[GUIA_TIPOS_GENERADOS.md](docs/informacion/GUIA_TIPOS_GENERADOS.md)** | Cómo usar los 94 tipos TypeScript generados desde OpenAPI |

### ⚙️ Scripts de Infrastructure

| Script | Cuándo usarlo | Ubicación |
|--------|--------------|-----------|
| **Crear Admin Inicial** | Solo la primera vez que levantas el proyecto | [SEED_ADMIN.md](infrastructure/SEED_ADMIN.md) |
| **Regenerar Tipos API** | Cuando el backend cambia | `npm run generate:types` (en frontend-web) |

### 📋 Reglas del Equipo
**[CONTRIBUTING.md](CONTRIBUTING.md)** - Commits, PRs, Code Review, Branch Protection

---

## 📋 Proyecto GitHub - Tablero Kanban

Gestión del desarrollo frontend con GitHub Projects:

🔗 **https://github.com/users/gamijoam/projects/2**

### Cómo usar el tablero:

| Paso | Acción |
|------|--------|
| 1 | Ir a https://github.com/users/gamijoam/projects/2 |
| 2 | Agregar issues al tablero arrastrándolas |
| 3 | Cambiar estado: Por Hacer → En Progreso → Hecho |
| 4 | Asignar Sprint y Prioridad a cada card |

### Estructura del equipo:

- **Columnas:** To Do | In Progress | Done
- **Campos:** Sprint, Prioridad
- **Vinculado a:** 40 issues organizadas por milestone

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
