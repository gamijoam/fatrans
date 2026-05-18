# Guía de Despliegue - Fatrans VPS

**Fecha:** 29 Abril 2026
**Versión:** 1.0
**Servidor:** VPS Ubuntu 22.04+

---

## Tabla de Contenidos

1. [Arquitectura General](#1-arquitectura-general)
2. [Pre-requisitos](#2-pre-requisitos)
3. [Fase 1: Seguridad del Servidor](#3-fase-1-seguridad-del-servidor)
4. [Fase 2: Instalación de Software Base](#4-fase-2-instalación-de-software-base)
5. [Fase 3: Docker y Servicios](#5-fase-3-docker-y-servicios)
6. [Fase 4: Backend Spring Boot](#6-fase-4-backend-spring-boot)
7. [Fase 5: Frontend Next.js](#7-fase-5-frontend-nextjs)
8. [Fase 6: Nginx Configuration](#8-fase-6-nginx-configuration)
9. [Fase 7: SSL y Certificados](#9-fase-7-ssl-y-certificados)
10. [Fase 8: Scripts de Operaciones](#10-fase-8-scripts-de-operaciones)
11. [Fase 9: Verificación](#11-fase-9-verificación)
12. [Apéndice: Comandos Rápidos](#12-apéndice-comandos-rápidos)

---

## 1. Arquitectura General

```
┌─────────────────────────────────────────────────────────────────┐
│                        INTERNET                                  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    NGINX (:80/:443)                              │
│         Reverse Proxy + SSL + Security Headers                  │
└─────────────────────────────────────────────────────────────────┘
            │                    │                    │
            │ /www, /auth        │ /app, /admin        │ /api/*
            ▼                    ▼                    ▼
┌───────────────────────┐ ┌───────────────────────┐ ┌───────────────────────────────────┐
│  Next.js PUBLICO      │ │  Next.js PROTEGIDO   │ │     Backend Spring Boot (:8080)   │
│      (:3000)           │ │      (:3001)          │ │                                    │
│                       │ │                       │ │  Endpoints:                        │
│  Route Groups:        │ │  Route Groups:        │ │  - /api/v1/auth/*                  │
│  - (public)/          │ │  - (dashboard)/       │ │  - /api/v1/socios/*                 │
│  - (auth)/           │ │  - (admin)/           │ │  - /api/v1/creditos/*               │
│                       │ │                       │ │  - /api/v1/admin/*                   │
└───────────────────────┘ └───────────────────────┘ └───────────────────────────────────┘
                                                              │
                    ┌──────────────────────────────────────────┼──────────────────────────┐
                    │                           │                           │
                    ▼                           ▼                           ▼
            ┌───────────────┐         ┌───────────────┐         ┌───────────────┐
            │  PostgreSQL   │         │     Redis     │         │     MinIO     │
            │    (:5432)    │         │   (:6379)     │         │   (:9000)     │
            └───────────────┘         └───────────────┘         └───────────────┘
```

### Aislamiento Bancario

Esta arquitectura implementa **aislamiento por nivel de confianza**:

| Instancia | Subdominios | Nivel | Descripción |
|-----------|-------------|-------|-------------|
| **Frontend-Public** | www, auth | Público | Landing y login - expuesto a internet |
| **Frontend-Protected** | app, admin | Protegido | Dashboard y admin - requiere autenticación |
| **Backend API** | api | Interno | Solo comunicación interna via nginx |

**Si comprometen el landing (www):** El login (auth) y la app de socios (app) siguen funcionando.
**Si comprometen la app (app):** El admin está en instancia separada.
**El backend (api):** Solo accesible via nginx - nunca directamente.

### Subdominios

| Subdominio | Propósito | Routing Nginx | Frontend Instance |
|------------|-----------|---------------|-------------------|
| www.fatrans.com | Landing pública | `/` → Next.js | :3000 (Public) |
| auth.fatrans.com | Portal login | `/` → Next.js | :3000 (Public) |
| app.fatrans.com | Dashboard Socio | `/` → Next.js | :3001 (Protected) |
| admin.fatrans.com | Panel Admin | `/` → Next.js | :3001 (Protected) |
| api.fatrans.com | Backend API | `/` → Backend | N/A |

---

## 2. Pre-requisitos

### 2.1 Servidor

- **OS:** Ubuntu 22.04 LTS (64-bit)
- **RAM:** Mínimo 4GB (recomendado 8GB)
- **CPU:** 2 vCPU (recomendado 4)
- **Disco:** 50GB SSD mínimo
- **Acceso:** Root o sudo user

### 2.2 Software Local (para deployment)

```bash
# Verificar que tienes las herramientas necesarias
ssh -V
docker --version
docker-compose --version
git --version
```

### 2.3 Dominios Configurados

Antes de comenzar, asegurar que los DNS apuntan al servidor:

```
A 记录  auth.fatrans.com    → IP_SERVIDOR
A 记录  app.fatrans.com     → IP_SERVIDOR
A 记录  admin.fatrans.com   → IP_SERVIDOR
A 记录  www.fatrans.com     → IP_SERVIDOR
A 记录  api.fatrans.com     → IP_SERVIDOR
```

**Verificar:**
```bash
dig auth.fatrans.com +short
dig app.fatrans.com +short
```

---

## 3. Fase 1: Seguridad del Servidor

### 3.1 Crear Usuario para Deploy

```bash
# Conectar como root
ssh root@IP_SERVIDOR

# Crear usuario deploy
adduser deploy
usermod -aG sudo deploy

# Configurar sudo sin password (opcional, más seguro con password)
# Editar: visudo
# deploy ALL=(ALL) PASSWD: ALL
```

### 3.2 SSH Keys - Autenticación Segura

**En tu máquina local:**

```bash
# Generar par de claves SSH
ssh-keygen -t ed25519 -C "deploy@fatrans" -f ~/.ssh/fatrans_deploy

# Copiar clave pública al servidor
ssh-copy-id -i ~/.ssh/fatrans_deploy.pub deploy@IP_SERVIDOR

# Testear conexión
ssh -i ~/.ssh/fatrans_deploy deploy@IP_SERVIDOR
```

**En el servidor - Configurar SSH:**

```bash
sudo nano /etc/ssh/sshd_config
```

```conf
# Cambiar puerto default (22 → 2222)
Port 2222

# Deshabilitar login root
PermitRootLogin no

# Deshabilitar autenticación por password
PasswordAuthentication no
PubkeyAuthentication yes

# Lista blanca de usuarios
AllowUsers deploy
```

```bash
# Reiniciar SSH
sudo systemctl restart sshd

# IMPORTANTE: Mantener sesión ssh activa mientras se prueba la nueva conexión
# En otra terminal, testear:
ssh -i ~/.ssh/fatrans_deploy -p 2222 deploy@IP_SERVIDOR
```

### 3.3 Firewall UFW

```bash
# Instalar UFW
sudo apt update
sudo apt install ufw

# Configurar reglas base
sudo ufw default deny incoming
sudo ufw default allow outgoing

# Permitir SSH en puerto no estándar
sudo ufw allow 2222/tcp

# Permitir HTTP y HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Habilitar firewall
sudo ufw enable

# Verificar estado
sudo ufw status verbose
```

### 3.4 Fail2Ban - Protección contra Brute Force

```bash
# Instalar
sudo apt install fail2ban

# Configurar
sudo nano /etc/fail2ban/jail.local
```

```ini
[sshd]
enabled = true
port = 2222
filter = sshd
logpath = /var/log/auth.log
maxretry = 5
bantime = 3600
findtime = 600

[nginx-http-auth]
enabled = true
filter = nginx-http-auth
port = http,https
logpath = /var/log/nginx/error.log
maxretry = 5
```

```bash
# Reiniciar
sudo systemctl restart fail2ban

# Verificar estado
sudo fail2ban-client status
sudo fail2ban-client status sshd
```

### 3.5 Updates Automáticos

```bash
# Instalar
sudo apt install unattended-upgrades

# Configurar
sudo nano /etc/apt/apt.conf.d/50unattended-upgrades
```

```conf
Unattended-Upgrade::Allowed-Origins {
    "${distro_id}:${distro_codename}-security";
    "${distro_id}:${distro_codename}-updates";
};

Unattended-Upgrade::Automatic-Reboot "true";
Unattended-Upgrade::Automatic-Reboot-Time "03:00";
```

```bash
# Habilitar actualizaciones automáticas
sudo dpkg-reconfigure -plow unattended-upgrades
```

### 3.6 Hardening Adicional

```bash
# Instalar y configurar fail2ban (ya hecho arriba)

# Configurar sysctl para seguridad de red
sudo nano /etc/sysctl.d/99-security.conf
```

```conf
# Prevenir IP spoofing
net.ipv4.conf.all.rp_filter = 1
net.ipv4.conf.default.rp_filter = 1

# Prevenir clickjacking
net.ipv4.conf.all.accept_source_route = 0
net.ipv4.conf.default.accept_source_route = 0

# Habilitar TCP SYN cookies
net.ipv4.tcp_syncookies = 1

# Deshabilitar ICMP redirect
net.ipv4.conf.all.accept_redirects = 0
net.ipv4.conf.default.accept_redirects = 0
net.ipv6.conf.all.accept_redirects = 0
net.ipv6.conf.default.accept_redirects = 0
```

```bash
# Aplicar cambios
sudo sysctl --system
```

---

## 4. Fase 2: Instalación de Software Base

### 4.1 Docker

```bash
# Actualizar sistema
sudo apt update && sudo apt upgrade -y

# Instalar dependencias
sudo apt install -y ca-certificates curl gnupg lsb-release

# Añadir Docker GPG key
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# Añadir repositorio
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Instalar Docker
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Añadir usuario deploy al grupo docker
sudo usermod -aG docker deploy

# Habilitar Docker
sudo systemctl enable docker
sudo systemctl start docker

# Verificar instalación
docker --version
docker compose version
```

### 4.2 Git

```bash
sudo apt install -y git
git --version
```

### 4.3 Estructura de Directorios

```bash
# Crear estructura
sudo mkdir -p /opt/fatrans/{backend,frontend,data/{postgres,minio},scripts,logs,backups}
sudo chown -R deploy:deploy /opt/fatrans

# Crear enlaces simbólicos (opcional)
sudo ln -s /opt/fatrans /home/deploy/fatrans
```

**Nota:** Tanto `frontend_public` como `frontend_protected` usan el **mismo código fuente** en `/opt/fatrans/frontend`. La diferencia es el puerto en que corren y qué subdominios nginx les rutea.

---

## 5. Fase 3: Docker y Servicios

### 5.1 Variables de Entorno - Producción

**CREAR ARCHIVO /opt/fatrans/.env - NUNCA COMMITEAR**

```bash
sudo nano /opt/fatrans/.env
```

```env
# ================================
# FATRANS - PRODUCTION ENVIRONMENT
# ================================

# Base de datos - GENERAR CONTRASEÑAS FUERTES
DB_PASS=Qx9#mP2$kL7@nQ4!vR8
DB_URL=jdbc:postgresql://postgres:5432/fondo

# Redis
REDIS_PASS=Zx3@nB5%jM9&pW2!qT6$

# JWT - GENERAR CON: openssl rand -base64 64
JWT_SECRET=<OUTPUT_OPENSSL_RAND_64_CHARS>
JWT_ISSUER=fondo-ahorro-platform
JWT_ACCESS_EXPIRATION_MINUTES=15
JWT_REFRESH_EXPIRATION_DAYS=7

# MinIO - GENERAR KEYS UNICOS
MINIO_ACCESS_KEY=fatrans_minio_admin
MINIO_SECRET_KEY=<GENERAR_32_CHARS_MINIMO>

# CORS - SOLO DOMINIOS DE PRODUCCION
CORS_ORIGINS=https://auth.fatrans.com,https://app.fatrans.com,https://admin.fatrans.com,https://www.fatrans.com

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

```bash
# Permisos correctos
sudo chmod 600 /opt/fatrans/.env
sudo chown deploy:deploy /opt/fatrans/.env
```

### 5.2 Docker Compose - Producción

```bash
sudo nano /opt/fatrans/docker-compose.prod.yml
```

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: fatrans_postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: fondo
      POSTGRES_USER: app
      POSTGRES_PASSWORD: ${DB_PASS}
    volumes:
      - pg_data:/var/lib/postgresql/data
      - ./data/postgres/init:/docker-entrypoint-initdb.d:ro
    networks:
      - fatrans_network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U app -d fondo"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: fatrans_redis
    restart: unless-stopped
    command: redis-server --requirepass ${REDIS_PASS} --appendonly yes
    volumes:
      - redis_data:/data
    networks:
      - fatrans_network
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASS}", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  minio:
    image: minio/minio:latest
    container_name: fatrans_minio
    restart: unless-stopped
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_ACCESS_KEY}
      MINIO_ROOT_PASSWORD: ${MINIO_SECRET_KEY}
    volumes:
      - minio_data:/data
    networks:
      - fatrans_network
    ports:
      - "127.0.0.1:9000:9000"
      - "127.0.0.1:9001:9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3

  backend:
    build:
      context: /opt/fatrans/backend
      dockerfile: Dockerfile.prod
    container_name: fatrans_backend
    restart: unless-stopped
    ports:
      - "127.0.0.1:18080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_URL: jdbc:postgresql://postgres:5432/fondo
      DB_USER: app
      DB_PASS: ${DB_PASS}
      REDIS_HOST: redis
      REDIS_PORT: 6379
      REDIS_PASSWORD: ${REDIS_PASS}
      JWT_SECRET: ${JWT_SECRET}
      JWT_ISSUER: ${JWT_ISSUER}
      JWT_ACCESS_EXPIRATION_MINUTES: ${JWT_ACCESS_EXPIRATION_MINUTES}
      JWT_REFRESH_EXPIRATION_DAYS: ${JWT_REFRESH_EXPIRATION_DAYS}
      MINIO_ENDPOINT: http://minio:9000
      MINIO_ACCESS_KEY: ${MINIO_ACCESS_KEY}
      MINIO_SECRET_KEY: ${MINIO_SECRET_KEY}
      CORS_ORIGINS: ${CORS_ORIGINS}
    volumes:
      - ./logs/backend:/app/logs
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
      minio:
        condition: service_healthy
    networks:
      - fatrans_network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  minio_init:
    image: minio/mc:latest
    container_name: fatrans_minio_init
    depends_on:
      minio:
        condition: service_healthy
    entrypoint: /bin/sh
    command: >
      -c "sleep 10 &&
          mc alias set local http://minio:9000 $${MINIO_ACCESS_KEY} $${MINIO_SECRET_KEY} &&
          mc mb local/fondodocs --ignore-existing &&
          mc mb local/bucket-kyc --ignore-existing &&
          mc mb local/bucket-documentos --ignore-existing &&
          mc mb local/bucket-contratos --ignore-existing &&
          mc mb local/bucket-creditos --ignore-existing &&
          mc anonymous set public local/fondodocs &&
          exit 0"
    environment:
      MINIO_ACCESS_KEY: ${MINIO_ACCESS_KEY}
      MINIO_SECRET_KEY: ${MINIO_SECRET_KEY}
    networks:
      - fatrans_network
    restart: "no"

  # ===========================================
  # FRONTEND PUBLICO - Landing + Auth
  # Puertos: 3000
  # Subdominios: www.fatrans.com, auth.fatrans.com
  # ===========================================
  frontend_public:
    build:
      context: /opt/fatrans/frontend
      dockerfile: Dockerfile.prod
      args:
        NEXT_PUBLIC_API_URL: https://api.fatrans.com
        NEXT_PUBLIC_APP_URL: https://app.fatrans.com
        FRONTEND_MODE: public
    container_name: fatrans_frontend_public
    restart: unless-stopped
    ports:
      - "127.0.0.1:3000:3000"
    environment:
      NODE_ENV: production
      NEXT_PUBLIC_API_URL: https://api.fatrans.com
      NEXT_PUBLIC_APP_URL: https://app.fatrans.com
    networks:
      - fatrans_network
    healthcheck:
      test: ["CMD", "wget", "--spider", "-q", "http://localhost:3000"]
      interval: 30s
      timeout: 10s
      retries: 3
    volumes:
      - frontend_public_logs:/app/.next

  # ===========================================
  # FRONTEND PROTEGIDO - App + Admin
  # Puertos: 3001
  # Subdominios: app.fatrans.com, admin.fatrans.com
  # ===========================================
  frontend_protected:
    build:
      context: /opt/fatrans/frontend
      dockerfile: Dockerfile.prod
      args:
        NEXT_PUBLIC_API_URL: https://api.fatrans.com
        NEXT_PUBLIC_APP_URL: https://app.fatrans.com
        FRONTEND_MODE: protected
    container_name: fatrans_frontend_protected
    restart: unless-stopped
    ports:
      - "127.0.0.1:3001:3000"
    environment:
      NODE_ENV: production
      NEXT_PUBLIC_API_URL: https://api.fatrans.com
      NEXT_PUBLIC_APP_URL: https://app.fatrans.com
    networks:
      - fatrans_network
    healthcheck:
      test: ["CMD", "wget", "--spider", "-q", "http://localhost:3000"]
      interval: 30s
      timeout: 10s
      retries: 3
    volumes:
      - frontend_protected_logs:/app/.next

networks:
  fatrans_network:
    driver: bridge

volumes:
  pg_data:
    driver: local
  redis_data:
    driver: local
  minio_data:
    driver: local
  frontend_public_logs:
    driver: local
  frontend_protected_logs:
    driver: local
```

### 5.3 Iniciar Servicios Base

```bash
cd /opt/fatrans

# Iniciar solo servicios base (postgres, redis, minio)
docker compose -f docker-compose.prod.yml up -d postgres redis minio minio_init

# Verificar que están corriendo
docker compose -f docker-compose.prod.yml ps

# Ver logs
docker compose -f docker-compose.prod.yml logs -f postgres
```

---

## 6. Fase 4: Backend Spring Boot

### 6.1 Preparar Código Backend

```bash
# En el servidor o usando Git
cd /opt/fatrans
git clone <REPO_URL> backend_temp || cp -r /path/to/local/backend ./backend

# Si clonaste, seleccionar branch producción
cd backend
git checkout main  # o la branch de producción
```

### 6.2 Cambios Requeridos en Código

**ANTES DE COMPILAR, aplicar estos cambios:**

#### 6.2.1 SecurityConfig.java - Cookies Seguras

```bash
nano /opt/fatrans/backend/src/main/java/com/tufondo/auth/infrastructure/security/SecurityConfig.java
```

**Cambiar la configuración del chain de seguridad, agregar:**

```java
// En el método securityFilterChain, después de .cors()
.cookie(cookie -> cookie
    .httpOnly(true)
    .secure(true)
    .sameSite("Strict")
)
```

#### 6.2.2 application.yml - Production Profile

```bash
nano /opt/fatrans/backend/src/main/resources/application-prod.yml
```

```yaml
spring:
  config:
    activate:
      on-profile: prod

  jpa:
    hibernate:
      ddl-auto: validate  # CRITICO: No usar update en producción

  flyway:
    enabled: true
    baseline-on-migrate: true

# Deshabilitar actuator info en producción
management:
  endpoints:
    web:
      exposure:
        exclude: info,env,beans
```

#### 6.2.3 Remover defaults de MinIO

En `application.yml` principal, cambiar:

```yaml
# ANTES (con defaults inseguros)
minio:
  access-key: ${MINIO_ACCESS_KEY:minioadmin}
  secret-key: ${MINIO_SECRET_KEY:minioadmin123}

# DESPUES (sin defaults)
minio:
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
```

### 6.3 Dockerfile para Producción

```bash
nano /opt/fatrans/backend/Dockerfile.prod
```

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -Pprod && \
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /app/target/dependency ./dependency/
COPY --from=builder /app/target/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
```

### 6.4 Construir e Iniciar Backend

```bash
cd /opt/fatrans

# Construir imagen
docker build -t fatrans/backend:latest -f backend/Dockerfile.prod backend/

# Taggear para producción
docker tag fatrans/backend:latest fatrans/backend:$(git rev-parse --short HEAD)

# Actualizar compose para usar la nueva imagen
# (En docker-compose.prod.yml, ya está configurado para construir)

# Iniciar backend
docker compose -f docker-compose.prod.yml up -d backend

# Ver logs
docker compose -f docker-compose.prod.yml logs -f backend
```

---

## 7. Fase 5: Frontend Next.js

### 7.1 next.config.js - Producción

El mismo `next.config.js` sirve para ambas instancias. Los route groups de Next.js manejan qué páginas se sirven según la URL.

```bash
nano /opt/fatrans/frontend/next.config.js
```

```javascript
/** @type {import('next').NextConfig} */
const nextConfig = {
  // Modo producción
  reactStrictMode: true,

  // Compilación optimizada
  swcMinify: true,

  // Remover console logs en producción
  compiler: {
    removeConsole: process.env.NODE_ENV === 'production',
  },

  // Configuración de imágenes
  images: {
    domains: ['fatrans.com', 'minio.fatrans.com'],
    formats: ['image/webp', 'image/avif'],
  },

  // Headers de seguridad
  async headers() {
    return [
      {
        source: '/:path*',
        headers: [
          {
            key: 'X-DNS-Prefetch-Control',
            value: 'on',
          },
          {
            key: 'X-Content-Type-Options',
            value: 'nosniff',
          },
          {
            key: 'X-Frame-Options',
            value: 'DENY',
          },
          {
            key: 'Referrer-Policy',
            value: 'strict-origin-when-cross-origin',
          },
          {
            key: 'Permissions-Policy',
            value: 'camera=(), microphone=(), geolocation=(), payment=()',
          },
        ],
      },
      {
        source: '/app/:path*',
        headers: [
          {
            key: 'X-Robots-Tag',
            value: 'noindex, nofollow',
          },
          {
            key: 'Strict-Transport-Security',
            value: 'max-age=63072000; includeSubDomains; preload',
          },
        ],
      },
      {
        source: '/admin/:path*',
        headers: [
          {
            key: 'X-Robots-Tag',
            value: 'noindex, nofollow',
          },
          {
            key: 'Strict-Transport-Security',
            value: 'max-age=63072000; includeSubDomains; preload',
          },
        ],
      },
      {
        // Public pages get HSTS too but no robots tag
        source: '/:path*',
        headers: [
          {
            key: 'Strict-Transport-Security',
            value: 'max-age=63072000; includeSubDomains; preload',
          },
        ],
      },
    ];
  },

  // Redirecciones para subdominios
  async redirects() {
    return [
      {
        source: '/login',
        destination: '/auth/login',
        permanent: false,
      },
      {
        source: '/dashboard',
        destination: '/app',
        permanent: false,
      },
    ];
  },

  // Rewrites para API - redirige al backend
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:18080/api/:path*',
      },
    ];
  },

  // Optimizaciones adicionales
  experimental: {
    optimizeCss: true,
  },

  // Logging condicional
  env: {
    NEXT_PUBLIC_APP_URL: process.env.NEXT_PUBLIC_APP_URL,
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
  },
};

module.exports = nextConfig;
```

### 7.2 Dockerfile Frontend (Universal)

El mismo Dockerfile sirve para ambas instancias. El modo se configura en el build args.

```bash
nano /opt/fatrans/frontend/Dockerfile.prod
```

```dockerfile
FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci

COPY . .

ARG NEXT_PUBLIC_API_URL
ARG NEXT_PUBLIC_APP_URL
ARG FRONTEND_MODE=public

ENV NEXT_PUBLIC_API_URL=$NEXT_PUBLIC_API_URL
ENV NEXT_PUBLIC_APP_URL=$NEXT_PUBLIC_APP_URL
ENV FRONTEND_MODE=$FRONTEND_MODE
ENV NODE_ENV=production

RUN npm run build

FROM node:20-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder --chown=nextjs:nodejs /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

CMD ["node", "server.js"]
```

**Importante:** Agregar `"standalone": true` al `package.json` del frontend:

```json
{
  "name": "frontend-web",
  "version": "1.0.0",
  "private": true,
  "standalone": true,
  "scripts": {
    "dev": "next dev",
    "build": "next build",
    "start": "next start",
    ...
  }
}
```

### 7.3 Construir e Iniciar Frontends

```bash
cd /opt/fatrans

# Construir imagen base (una vez)
docker build -t fatrans/frontend:latest \
  -f frontend/Dockerfile.prod \
  --build-arg NEXT_PUBLIC_API_URL=https://api.fatrans.com \
  --build-arg NEXT_PUBLIC_APP_URL=https://app.fatrans.com \
  --build-arg FRONTEND_MODE=public \
  frontend/

# O construir directamente con docker-compose (recomendado)
cd /opt/fatrans
docker compose -f docker-compose.prod.yml up -d frontend_public frontend_protected

# Ver logs
docker compose -f docker-compose.prod.yml logs -f frontend_public
docker compose -f docker-compose.prod.yml logs -f frontend_protected
```

### 7.4 Verificación de Frontends

```bash
# Verificar que ambos están corriendo
docker ps | grep fatrans_frontend

# Testear acceso local
curl -s -o /dev/null -w "%{http_code}" http://localhost:3000  # Public
curl -s -o /dev/null -w "%{http_code}" http://localhost:3001  # Protected

# Verificar route groups activos
curl -s http://localhost:3000 | grep -o '<title>.*</title>'    # Landing
curl -s http://localhost:3000/auth/login | grep -o '<title>.*</title>'  # Login
curl -s http://localhost:3001/app | grep -o '<title>.*</title>'  # Dashboard
```

---

## 8. Fase 6: Nginx Configuration

### 8.1 Instalar Nginx

```bash
sudo apt install -y nginx
```

### 8.2 Configuración Principal

```bash
sudo nano /etc/nginx/nginx.conf
```

```nginx
user www-data;
worker_processes auto;
pid /run/nginx.pid;
error_log /var/log/nginx/error.log warn;

events {
    worker_connections 2048;
    use epoll;
    multi_accept on;
}

http {
    # basic settings
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # logging
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for" '
                    'rt=$request_time uct="$upstream_connect_time" '
                    'uht="$upstream_header_time" urt="$upstream_response_time"';

    access_log /var/log/nginx/access.log main buffer=16k;

    # performance
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_comp_level 6;
    gzip_types text/plain text/css text/xml application/json application/javascript
               application/xml application/xml+rss text/javascript application/x-javascript;

    # Security headers
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    # Rate limiting zones
    limit_req_zone $binary_remote_addr zone=login_limit:10m rate=5r/m;
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=30r/m;
    limit_req_zone $binary_remote_addr zone=general:10m rate=50r/s;

    # IP whitelist para admin (opcional)
    geo $whitelist {
        default 0;
        # 192.168.1.0/24 1;
        # 10.0.0.0/8 1;
    }

    include /etc/nginx/conf.d/*.conf;
    include /etc/nginx/sites-enabled/*;
}
```

### 8.3 Sites Configuration - Main Domain

```bash
sudo nano /etc/nginx/sites-available/fatrans_main
```

```nginx
# www.fatrans.com - Landing page principal
server {
    listen 80;
    listen [::]:80;
    server_name www.fatrans.com fatrans.com;

    # Redirect to HTTPS
    return 301 https://www.fatrans.com$request_uri;
}

# auth.fatrans.com
server {
    listen 80;
    listen [::]:80;
    server_name auth.fatrans.com;

    return 301 https://auth.fatrans.com$request_uri;
}

# app.fatrans.com
server {
    listen 80;
    listen [::]:80;
    server_name app.fatrans.com;

    return 301 https://app.fatrans.com$request_uri;
}

# admin.fatrans.com
server {
    listen 80;
    listen [::]:80;
    server_name admin.fatrans.com;

    return 301 https://admin.fatrans.com$request_uri;
}

# api.fatrans.com
server {
    listen 80;
    listen [::]:80;
    server_name api.fatrans.com;

    return 301 https://api.fatrans.com$request_uri;
}
```

### 8.4 HTTPS Configuration

```bash
sudo nano /etc/nginx/sites-available/fatrans_https
```

```nginx
# ===============================
# WWW.FATRANS.COM - Landing
# Routing: FRONTEND PUBLICO (:3000)
# ===============================
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name www.fatrans.com;

    # SSL Configuration
    ssl_certificate /etc/letsencrypt/live/fatrans.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/fatrans.com/privkey.pem;
    ssl_trusted_certificate /etc/letsencrypt/live/fatrans.com/chain.pem;

    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_session_tickets off;

    # Modern TLS
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    # HSTS
    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;

    # OCSP Stapling
    ssl_stapling on;
    ssl_stapling_verify on;
    resolver 8.8.8.8 8.8.4.4 valid=300s;

    location / {
        # Routing a FRONTEND PUBLICO (:3000)
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;

        limit_req zone=general burst=20 nodelay;
    }

    access_log /var/log/nginx/www_access.log;
    error_log /var/log/nginx/www_error.log;
}

# ===============================
# AUTH.FATRANS.COM - Login Portal
# Routing: FRONTEND PUBLICO (:3000)
# ===============================
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name auth.fatrans.com;

    # SSL (same cert for all subdomains if wildcard)
    ssl_certificate /etc/letsencrypt/live/fatrans.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/fatrans.com/privkey.pem;

    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;

    location / {
        # Routing a FRONTEND PUBLICO (:3000)
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;

        # Rate limiting específico para auth
        limit_req zone=login_limit burst=5 nodelay;
    }

    access_log /var/log/nginx/auth_access.log;
    error_log /var/log/nginx/auth_error.log;
}

# ===============================
# APP.FATRANS.COM - User Dashboard
# Routing: FRONTEND PROTEGIDO (:3001)
# ===============================
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name app.fatrans.com;

    ssl_certificate /etc/letsencrypt/live/fatrans.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/fatrans.com/privkey.pem;

    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
    add_header X-Robots-Tag "noindex, nofollow" always;

    location / {
        # Routing a FRONTEND PROTEGIDO (:3001)
        proxy_pass http://127.0.0.1:3001;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;

        limit_req zone=general burst=50 nodelay;
    }

    access_log /var/log/nginx/app_access.log;
    error_log /var/log/nginx/app_error.log;
}

# ===============================
# ADMIN.FATRANS.COM - Admin Panel
# Routing: FRONTEND PROTEGIDO (:3001)
# ===============================
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name admin.fatrans.com;

    ssl_certificate /etc/letsencrypt/live/fatrans.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/fatrans.com/privkey.pem;

    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
    add_header X-Robots-Tag "noindex, nofollow" always;

    # IP Whitelist opcional (descomentar si se requiere)
    # satisfies any;
    # if ($whitelist = 0) {
    #     return 403;
    # }

    location / {
        # Routing a FRONTEND PROTEGIDO (:3001)
        proxy_pass http://127.0.0.1:3001;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;

        limit_req zone=api_limit burst=30 nodelay;
    }

    access_log /var/log/nginx/admin_access.log;
    error_log /var/log/nginx/admin_error.log;
}

# ===============================
# API.FATRANS.COM - Backend API
# Routing: BACKEND SPRING BOOT (:8080)
# ===============================
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name api.fatrans.com;

    ssl_certificate /etc/letsencrypt/live/fatrans.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/fatrans.com/privkey.pem;

    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;

    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
    add_header X-Robots-Tag "noindex, nofollow" always;

    # CORS Preflight - importante para API
    location /api/v1/auth/login {
        proxy_pass http://127.0.0.1:18080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        limit_req zone=login_limit burst=5 nodelay;
    }

    location / {
        proxy_pass http://127.0.0.1:18080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        limit_req zone=api_limit burst=30 nodelay;
    }

    access_log /var/log/nginx/api_access.log;
    error_log /var/log/nginx/api_error.log;
}
```

### 8.5 Habilitar Sites

```bash
# Habilitar configuración
sudo ln -s /etc/nginx/sites-available/fatrans_main /etc/nginx/sites-enabled/
sudo ln -s /etc/nginx/sites-available/fatrans_https /etc/nginx/sites-enabled/

# Deshabilitar default
sudo rm /etc/nginx/sites-enabled/default

# Test configuración
sudo nginx -t

# Recargar nginx
sudo systemctl reload nginx
```

---

## 9. Fase 7: SSL y Certificados

### 9.1 Instalar Certbot

```bash
sudo apt update
sudo apt install -y certbot python3-certbot-nginx
```

### 9.2 Obtener Certificados

```bash
# Certificado wildcard (requiere verificación DNS)
sudo certbot certonly --manual --preferred-challenges dns --server 'https://acme-v02.api.letsencrypt.org/directory' -d "fatrans.com" -d "*.fatrans.com"

# O certificados individuales (más simple)
sudo certbot --nginx -d www.fatrans.com -d auth.fatrans.com -d app.fatrans.com -d admin.fatrans.com -d api.fatrans.com
```

### 9.3 Auto-renewal

```bash
# Test renewal
sudo certbot renew --dry-run

# El servicio systemd de certbot ya se crea automáticamente
# Verificar:
systemctl status certbot.timer
```

### 9.4 Scripts de Renew Automático

```bash
sudo nano /etc/letsencrypt/renewal-hooks/post/reload_nginx.sh
```

```bash
#!/bin/bash
systemctl reload nginx
docker exec fatrans_backend /opt/java/app/actuator/refresh || true
```

```bash
sudo chmod +x /etc/letsencrypt/renewal-hooks/post/reload_nginx.sh
```

---

## 10. Fase 8: Scripts de Operaciones

### 10.1 Backup PostgreSQL

```bash
sudo nano /opt/fatrans/scripts/backup-postgres.sh
```

```bash
#!/bin/bash
# ===================================
# Backup PostgreSQL - Fatrans
# ===================================

set -e

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/fatrans/backups/postgres"
RETENTION_DAYS=30

# Crear directorio si no existe
mkdir -p $BACKUP_DIR

# Nombre del archivo
FILENAME="fatrans_backup_${DATE}.sql.gz"

# Ejecutar backup
docker exec fatrans_postgres pg_dump -U app fondo | gzip > ${BACKUP_DIR}/${FILENAME}

# Verificar que el backup no está vacío
if [ -s "${BACKUP_DIR}/${FILENAME}" ]; then
    echo "[$(date)] Backup creado: ${FILENAME}"
else
    echo "[$(date)] ERROR: Backup está vacío"
    exit 1
fi

# Limpiar backups antiguos
find $BACKUP_DIR -name "fatrans_backup_*.sql.gz" -mtime +$RETENTION_DAYS -delete

# Mostrar tamaño
du -h ${BACKUP_DIR}/${FILENAME}

echo "[$(date)] Backup completado exitosamente"
```

```bash
sudo chmod +x /opt/fatrans/scripts/backup-postgres.sh
```

### 10.2 Cron Jobs

```bash
# Editar crontab
sudo crontab -e
```

```cron
# Backup PostgreSQL diario a las 3:00 AM
0 3 * * * /opt/fatrans/scripts/backup-postgres.sh >> /var/log/fatrans/backup.log 2>&1

# Verificar SSL cada semana
0 0 * * 0 certbot renew --dry-run >> /var/log/fatrans/ssl_check.log 2>&1
```

### 10.3 Health Check

```bash
sudo nano /opt/fatrans/scripts/health-check.sh
```

```bash
#!/bin/bash
# ===================================
# Health Check - Fatrans
# ===================================

ERRORS=0

# Check Docker containers (incluye ambos frontends)
for container in fatrans_postgres fatrans_redis fatrans_minio fatrans_backend fatrans_frontend_public fatrans_frontend_protected; do
    if ! docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
        echo "[ERROR] Container ${container} no está corriendo"
        ERRORS=$((ERRORS+1))
    fi
done

# Check servicios web
for url in "https://www.fatrans.com" "https://auth.fatrans.com" "https://app.fatrans.com" "https://admin.fatrans.com" "https://api.fatrans.com/actuator/health"; do
    if ! curl -sf -o /dev/null -w "%{http_code}" $url | grep -q "200"; then
        echo "[ERROR] ${url} no responde"
        ERRORS=$((ERRORS+1))
    fi
done

# Check frontends directamente en puertos internos
for port in 3000 3001; do
    if ! curl -sf -o /dev/null -w "%{http_code}" http://localhost:${port} | grep -q "200"; then
        echo "[ERROR] Frontend en puerto ${port} no responde"
        ERRORS=$((ERRORS+1))
    fi
done

# Check SSL
if ! certbot certificates --domains fatrans.com | grep -q "Expiry Date"; then
    echo "[ERROR] Certificado SSL próximo a expirar"
    ERRORS=$((ERRORS+1))
fi

# Check disco
USAGE=$(df / | awk 'NR==2 {print $5}' | sed 's/%//')
if [ $USAGE -gt 85 ]; then
    echo "[WARNING] Disco al ${USAGE}%"
fi

exit $ERRORS
```

```bash
sudo chmod +x /opt/fatrans/scripts/health-check.sh
```

---

## 11. Fase 9: Verificación

### 11.1 Checklist de Verificación

```bash
# 1. Servicios Docker (verificar 2 frontends)
docker ps
# Debe mostrar: postgres, redis, minio, backend, frontend_public, frontend_protected

# 2. Endpoints de salud
curl -s https://api.fatrans.com/actuator/health | jq .

# 3. Verificar ambos frontends
curl -s -o /dev/null -w "%{http_code}" http://localhost:3000  # Public
curl -s -o /dev/null -w "%{http_code}" http://localhost:3001  # Protected

# 4. Login flow
curl -s -X POST https://api.fatrans.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@fatrans.com","password":"test123"}' | jq .

# 5. Rate limiting
for i in {1..10}; do
  curl -s -o /dev/null -w "%{http_code}\n" \
    -X POST https://api.fatrans.com/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"wrong"}'
done
# Los últimos deberían retornar 429

# 6. Headers de seguridad
curl -sI https://api.fatrans.com | grep -iE "(strict-transport|x-frame|x-content-type)"

# 7. Verificar aislamiento - detener un frontend
docker stop fatrans_frontend_public
curl -s -o /dev/null -w "%{http_code}" https://www.fatrans.com   # Debería fallar (404/502)
curl -s -o /dev/null -w "%{http_code}" https://app.fatrans.com   # Debería funcionar (aislado)
docker start fatrans_frontend_public

# 8. SSL Certificate
echo | openssl s_client -connect api.fatrans.com:443 -servername api.fatrans.com 2>/dev/null | openssl x509 -noout -dates

# 9. Logs de errores
docker compose -f /opt/fatrans/docker-compose.prod.yml logs --tail=100 | grep -i error
```

### 11.2 Test de Login Completo

1. Abrir https://auth.fatrans.com
2. Ingresar credenciales
3. Verificar redirect a https://app.fatrans.com
4. Abrir DevTools → Network
5. Verificar que cookies tienen flags HttpOnly y Secure

### 11.3 Test de Rate Limiting

```bash
# Login attempts rápidos
for i in {1..10}; do
  echo "Attempt $i:"
  curl -s -w "\n" -X POST https://api.fatrans.com/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@test.com","password":"wrong"}'
  sleep 0.2
done
```

Los intentos 6+ deberían retornar 429 Too Many Requests.

---

## 12. Apéndice: Comandos Rápidos

### Gestión de Servicios

```bash
# Iniciar todo
cd /opt/fatrans && docker compose -f docker-compose.prod.yml up -d

# Detener todo
cd /opt/fatrans && docker compose -f docker-compose.prod.yml down

# Ver logs de un servicio específico
docker compose -f docker-compose.prod.yml logs -f frontend_public
docker compose -f docker-compose.prod.yml logs -f frontend_protected
docker compose -f docker-compose.prod.yml logs -f backend

# Reiniciar frontend específico
docker restart fatrans_frontend_public
docker restart fatrans_frontend_protected

# Reiniciar backend
docker exec fatrans_backend /opt/java/app/actuator/restart || docker restart fatrans_backend

# Ver uso de recursos
docker stats

# Test de aislamiento - detener public
docker stop fatrans_frontend_public
# www.fatrans.com y auth.fatrans.com fallan
# app.fatrans.com y admin.fatrans.com siguen funcionando
docker start fatrans_frontend_public
```

### Logs

```bash
# Logs nginx
tail -f /var/log/nginx/*_access.log

# Logs frontend public
docker logs -f fatrans_frontend_public --tail=100

# Logs frontend protected
docker logs -f fatrans_frontend_protected --tail=100

# Logs backend
docker logs -f fatrans_backend --tail=100

# Todos los logs
cd /opt/fatrans && docker compose -f docker-compose.prod.yml logs --tail=50 -f
```

### Backup Manual

```bash
# Backup ahora
/opt/fatrans/scripts/backup-postgres.sh

# Restaurar backup
gunzip < /opt/fatrans/backups/postgres/fatrans_backup_20260429_030000.sql.gz | docker exec -i fatrans_postgres psql -U app fondo
```

### SSL

```bash
# Ver certificados
sudo certbot certificates

# Renovar manualmente
sudo certbot renew --force-renewal

# Verificar nginx después de cambios
sudo nginx -t && sudo systemctl reload nginx
```

### Seguridad

```bash
# Ver intentos de login fallidos
sudo fail2ban-client status sshd

# Ver reglas UFW
sudo ufw status numbered

# Ver conexiones activas
ss -tunap | grep :80
```

---

## Checklist Final de Deploy

```markdown
## Pre-Deploy
- [ ] DNS configurado para todos los subdominios
- [ ] SSH keys configuradas y probadas
- [ ] UFW firewall activo
- [ ] Fail2Ban configurado
- [ ] Variables de entorno en /opt/fatrans/.env

## Infraestructura
- [ ] Docker instalado y funcionando
- [ ] PostgreSQL corriendo y healthy
- [ ] Redis corriendo y healthy
- [ ] MinIO corriendo y buckets creados

## Backend
- [ ] SecurityConfig.java actualizado (Secure + HttpOnly cookies)
- [ ] application.yml ddl-auto: validate
- [ ] Credenciales MinIO sin defaults
- [ ] Docker image construida
- [ ] /actuator/health responde 200

## Frontend (2 instancias aisladas)
- [ ] next.config.js con removeConsole
- [ ] Headers de seguridad configurados
- [ ] frontend_public Docker image construida y corriendo en :3000
- [ ] frontend_protected Docker image construida y corriendo en :3001
- [ ] www.fatrans.com → :3000 funcionando
- [ ] auth.fatrans.com → :3000 funcionando
- [ ] app.fatrans.com → :3001 funcionando
- [ ] admin.fatrans.com → :3001 funcionando

## Nginx
- [ ] SSL certificates instalados
- [ ] Headers HSTS, CSP, etc. configurados
- [ ] Rate limiting activo
- [ ] Proxy a backend funcionando en :18080
- [ ] Routing correcto a frontends (public :3000, protected :3001)

## Verificación de Aislamiento
- [ ] Si cae frontend_public, www y auth no funcionan (aislado)
- [ ] Si cae frontend_protected, app y admin no funcionan (aislado)
- [ ] Backend (api) sigue funcionando si cualquier frontend cae
- [ ] Login funciona end-to-end
- [ ] Rate limiting bloquea después del límite
- [ ] Cookies tienen HttpOnly y Secure
- [ ] Backup automático configurado
```

---

*Guía de despliegue generada para Fatrans - Fondo de Ahorro Platform*
*Versión 1.0 - 29 Abril 2026*
