# 🏦 TuFondo: Plataforma Financiera para el Sector Transporte

Bienvenido a **TuFondo**, el sistema financiero diseñado específicamente para el transportista venezolano. Esta plataforma integra una API robusta (Spring Boot), un Portal Web de alto rendimiento (Next.js 14) y una arquitectura orientada a la seguridad bancaria.

---

## 🎯 El Norte del Proyecto

Este repositorio se rige por el [**PROYECTO_MAESTRO_TUFONDO.md**](./PROYECTO_MAESTRO_TUFONDO.md), que define los 6 pilares estratégicos:
1. **Identidad y Confianza** (KYC + Scoring)
2. **Ahorro Adaptado** (Metas Visuales + Microahorro)
3. **Crédito Express** (Aprobación en 24h)
4. **Gestión de Transporte** (Perfil de Unidad + Recaudos)
5. **Protección y Bienestar** (Beneficiarios + Solidaridad)
6. **Administración y Control** (KPIs + Riesgos)

---

## 🚀 Inicio Rápido (Workflow de Desarrollo)

Para un desarrollo ágil con **Hot Reload**, recomendamos levantar la infraestructura en Docker y el Frontend localmente.

### 1. Preparar Variables de Entorno
Crea un archivo `.env` en la raíz del proyecto o exporta las variables:
```bash
export DB_PASS=pass
export REDIS_PASS=pass
export MINIO_SECRET_KEY=secret
# El secreto JWT debe tener al menos 32 caracteres (256 bits)
export JWT_SECRET=estoesunsecretomuyseguroylargoparapoderpasarlavalidaciondejtw2026
```

### 2. Levantar Infraestructura y Backend (Docker)
```bash
cd infrastructure
docker compose up -d postgres redis minio backend
```
*Si realizas cambios en el código Java:* `docker compose up -d --build backend`

### 3. Levantar Frontend Localmente
```bash
cd frontend-web
npm install
# Asegúrate de que .env.local apunte al backend de Docker (puerto 18080)
echo "NEXT_PUBLIC_API_URL=http://localhost:18080/api" > .env.local
npm run dev
```

---

## 🧪 Datos de Prueba (Automatizados)

Al arrancar el sistema, las migraciones de base de datos (Flyway) crean automáticamente el siguiente entorno de pruebas:

| Rol | Usuario | Password | Descripción |
|-----|---------|----------|-------------|
| **Admin** | `admin` | `Admin123!` | Acceso total al sistema. |
| **Socio** | `socio_prueba` | `Admin123!` | Socio con unidad registrada y saldo dual. |

### Estado del Socio de Prueba:
- **Nombre:** Carlos Pérez (V-20123456).
- **Unidad:** Encava Ent-610, Placa `20A11BB` (Alerta de SOAT activa).
- **Cuentas:** Saldo en VES y USD disponible.
- **Créditos:** Un crédito de repuestos ya desembolsado.

---

## 🛠️ Accesos y Herramientas

| Servicio | URL | Uso |
|----------|-----|-------------|
| **Frontend** | `http://localhost:3000` | Interfaz de Socio y Admin. |
| **Backend API** | `http://localhost:18080` | Endpoint base de la API. |
| **Documentación API** | `http://localhost:18080/swagger-ui.html` | Probar endpoints (Swagger). |
| **MinIO Console** | `http://localhost:19001` | Gestión de archivos (KYC/PDFs). |

---

## 🏗️ Stack Tecnológico

### Backend
- **Core:** Spring Boot 3.2.4 (Java 21)
- **Base de Datos:** PostgreSQL 15 + Flyway (Migraciones automáticas)
- **Seguridad:** JWT (HS384) + Refresh Tokens
- **Mensajería:** RabbitMQ (Próximamente)

### Frontend Web
- **Framework:** Next.js 14.2.0 (App Router)
- **Diseño:** Tailwind CSS + Shadcn/UI
- **Iconografía:** Lucide React
- **Estado:** Zustand + TanStack Query

---

## 📁 Estructura de Módulos (Backend)
- `com.tufondo.auth`: Seguridad y sesiones.
- `com.tufondo.socios`: Datos maestros de afiliados.
- `com.tufondo.ahorros`: Gestión multi-moneda.
- `com.tufondo.creditos`: Motor de préstamos.
- `com.tufondo.transporte`: **(NUEVO)** Gestión de unidades y rutas.
- `com.tufondo.kyc`: Verificación de identidad.
- `com.tufondo.documentospdf`: Generador de certificados.

---
*TuFondo: El respaldo que los bancos no te dan.*
