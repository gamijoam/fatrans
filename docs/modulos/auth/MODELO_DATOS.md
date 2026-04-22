# Módulo AUTH - Modelo de Datos

**Proyecto:** Plataforma Fondo de Ahorro  
**Versión:** 1.0  
**Fecha:** 2026-04-14

---

## Resumen

Este documento describe el modelo de datos del módulo AUTH, incluyendo las entidades, relaciones, DDL y consideraciones de seguridad.

---

## 1. Diagrama de Entidades

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              MODELO DE DATOS AUTH                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────────┐         ┌──────────────────────┐                    │
│   │   Socio          │         │   Usuario            │                    │
│   │   (del módulo    │         │                      │                    │
│   │    SOCIOS)       │         │  - id (PK)           │                    │
│   └────────┬─────────┘         │  - nombreUsuario     │                    │
│            │                   │  - correoElectronico │                    │
│            │ 1:1               │  - passwordHash      │                    │
│            └───────────────────┤  - nombreCompleto   │                    │
│                                │  - rol               │                    │
│                                │  - socioId (FK)      │                    │
│                                │  - cuentaActiva      │                    │
│                                │  - intentosFallidos  │                    │
│                                │  - fechaBloqueo      │                    │
│                                └──────────┬───────────┘                    │
│                                           │                                 │
│                                           │ 1:N                             │
│                                           ▼                                 │
│                                ┌──────────────────────┐                     │
│                                │   Sesion            │                     │
│                                │                      │                     │
│                                │  - id (PK)          │                     │
│                                │  - usuarioId (FK)   │                     │
│                                │  - refreshTokenHash │                     │
│                                │  - accessTokenExp   │                     │
│                                │  - refreshTokenExp   │                     │
│                                │  - activo           │                     │
│                                │  - tipoToken        │                     │
│                                └──────────────────────┘                     │
│                                                                             │
│   ┌──────────────────────┐                                                   │
│   │ PasswordResetToken  │                                                   │
│   │                      │                                                   │
│   │  - id (PK)          │                                                   │
│   │  - usuarioId (FK)   │                                                   │
│   │  - tokenHash        │                                                   │
│   │  - fechaCreacion    │                                                   │
│   │  - fechaExpiracion  │                                                   │
│   │  - usado            │                                                   │
│   │  - fechaUso         │                                                   │
│   └──────────────────────┘                                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Entidades

### 2.1 Usuario

**Descripción:** Representa un usuario autenticable del sistema.

| Campo | Tipo | Nullable | Descripción |
|-------|------|----------|-------------|
| id | UUID | No | PK |
| nombre_usuario | VARCHAR(50) | No | Username único |
| correo_electronico | VARCHAR(255) | No | Email único |
| password_hash | VARCHAR(255) | No | Hash BCrypt de la contraseña |
| nombre_completo | VARCHAR(100) | No | Nombre completo |
| rol | VARCHAR(20) | No | SOCIO, ADMIN, SUPER_ADMIN |
| socio_id | UUID | Sí | FK → Socio.id (referencia al módulo SOCIOS) |
| cuenta_activa | BOOLEAN | No | Estado de la cuenta |
| intentos_fallidos | INT | No | Contador de intentos fallidos |
| fecha_bloqueo | TIMESTAMP | Sí | Fecha de bloqueo (si existe) |
| fecha_creacion | TIMESTAMP | No | Fecha de creación |
| ultima_modificacion | TIMESTAMP | No | Última modificación |

**Índices:**
- `idx_usuario_nombre_usuario` ON (nombre_usuario) UNIQUE
- `idx_usuario_correo` ON (correo_electronico) UNIQUE
- `idx_usuario_socio_id` ON (socio_id) UNIQUE (WHERE socio_id IS NOT NULL)
- `idx_usuario_rol` ON (rol)

**Restricciones:**
- `chk_usuario_rol` CHECK (rol IN ('SOCIO', 'ADMIN', 'SUPER_ADMIN'))
- `chk_intentos_fallidos` CHECK (intentos_fallidos >= 0 AND intentos_fallidos <= 10)

---

### 2.2 Sesion

**Descripción:** Representa una sesión activa con tokens JWT.

| Campo | Tipo | Nullable | Descripción |
|-------|------|----------|-------------|
| id | UUID | No | PK |
| usuario_id | UUID | No | FK → Usuario.id |
| refresh_token_hash | VARCHAR(255) | No | Hash del refresh token (único) |
| access_token_expiracion | TIMESTAMP | No | Expiración del access token |
| refresh_token_expiracion | TIMESTAMP | No | Expiración del refresh token |
| activo | BOOLEAN | No | Estado de la sesión |
| tipo_token | VARCHAR(20) | No | ACCESS_TOKEN o REFRESH_TOKEN |
| fecha_creacion | TIMESTAMP | No | Fecha de creación |
| ultima_actividad | TIMESTAMP | No | Última actividad |

**Índices:**
- `idx_sesion_usuario_id` ON (usuario_id)
- `idx_sesion_refresh_token_hash` ON (refresh_token_hash) UNIQUE
- `idx_sesion_activo` ON (activo) WHERE activo = true
- `idx_sesion_expiracion` ON (refresh_token_expiracion)

**Restricciones:**
- `chk_sesion_tipo_token` CHECK (tipo_token IN ('ACCESS_TOKEN', 'REFRESH_TOKEN'))

---

### 2.3 PasswordResetToken

**Descripción:** Token para recuperación de contraseña.

| Campo | Tipo | Nullable | Descripción |
|-------|------|----------|-------------|
| id | UUID | No | PK |
| usuario_id | UUID | No | FK → Usuario.id |
| token_hash | VARCHAR(255) | No | Hash del token (único) |
| fecha_creacion | TIMESTAMP | No | Cuándo se creó |
| fecha_expiracion | TIMESTAMP | No | Cuándo expira (1 hora) |
| usado | BOOLEAN | No | Si ya fue utilizado |
| fecha_uso | TIMESTAMP | Sí | Cuándo fue usado |

**Índices:**
- `idx_password_reset_token_hash` ON (token_hash) UNIQUE
- `idx_password_reset_usuario_id` ON (usuario_id)
- `idx_password_reset_expiracion` ON (fecha_expiracion)

---

## 3. Tablas de Auditoría

### 3.1 Tabla audit_auth (Shadow Table)

```sql
-- Tabla de auditoría para compliance
CREATE TABLE audit_auth (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad_tipo VARCHAR(50) NOT NULL,           -- 'USUARIO', 'SESION', 'LOGIN', 'LOGOUT'
    entidad_id UUID,                              -- ID de la entidad afectada (puede ser null para LOGIN)
    accion VARCHAR(30) NOT NULL,                 -- 'LOGIN_SUCCESS', 'LOGIN_FAILED', 'LOGOUT', 'CREATE', 'UPDATE', 'PASSWORD_RESET'
    usuario_id VARCHAR(100),                      -- ID del usuario que realizó la acción (o 'ANONIMO')
    ip_cliente VARCHAR(45) NOT NULL,              -- IP del cliente
    user_agent VARCHAR(500),                      -- User agent del navegador
    datos_adicionales JSONB,                      -- Datos adicionales (ej: motivo de fallo)
    fecha_evento TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_entidad_tipo CHECK (entidad_tipo IN ('USUARIO', 'SESION', 'LOGIN', 'LOGOUT', 'PASSWORD_RESET')),
    CONSTRAINT chk_accion CHECK (accion IN ('LOGIN_SUCCESS', 'LOGIN_FAILED', 'LOGOUT', 'CREATE', 'UPDATE', 'DELETE', 'PASSWORD_RESET', 'TOKEN_REFRESH'))
);

CREATE INDEX idx_audit_auth_entidad ON audit_auth (entidad_tipo, entidad_id);
CREATE INDEX idx_audit_auth_usuario ON audit_auth (usuario_id, fecha_evento DESC);
CREATE INDEX idx_audit_auth_fecha ON audit_auth (fecha_evento DESC);
CREATE INDEX idx_audit_auth_ip ON audit_auth (ip_cliente, fecha_evento DESC);
```

---

## 4. DDL Completo

```sql
-- ================================================================
-- AUTH Module - DDL para PostgreSQL
-- Versión: 1.0
-- Fecha: 2026-04-14
-- ================================================================

-- Extensión para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================================================================
-- TABLA: usuarios
-- ================================================================
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nombre_usuario VARCHAR(50) NOT NULL,
    correo_electronico VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'SOCIO',
    socio_id UUID,
    cuenta_activa BOOLEAN NOT NULL DEFAULT true,
    intentos_fallidos INT NOT NULL DEFAULT 0,
    fecha_bloqueo TIMESTAMP,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    ultima_modificacion TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT chk_usuario_rol CHECK (rol IN ('SOCIO', 'ADMIN', 'SUPER_ADMIN')),
    CONSTRAINT chk_intentos_fallidos CHECK (intentos_fallidos >= 0 AND intentos_fallidos <= 10),
    
    -- Uniques
    CONSTRAINT uq_usuario_nombre_usuario UNIQUE (nombre_usuario),
    CONSTRAINT uq_usuario_correo UNIQUE (correo_electronico)
);

-- Índices
CREATE INDEX idx_usuario_nombre_usuario ON usuarios (nombre_usuario);
CREATE INDEX idx_usuario_correo ON usuarios (correo_electronico);
CREATE UNIQUE INDEX idx_usuario_socio_id ON usuarios (socio_id) WHERE socio_id IS NOT NULL;
CREATE INDEX idx_usuario_rol ON usuarios (rol);
CREATE INDEX idx_usuario_activo ON usuarios (cuenta_activa) WHERE cuenta_activa = true;

-- ================================================================
-- TABLA: sesiones
-- ================================================================
CREATE TABLE sesiones (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    access_token_expiracion TIMESTAMP NOT NULL,
    refresh_token_expiracion TIMESTAMP NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    tipo_token VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    ultima_actividad TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_sesion_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_sesion_tipo_token CHECK (tipo_token IN ('ACCESS_TOKEN', 'REFRESH_TOKEN'))
);

-- Índices
CREATE INDEX idx_sesion_usuario_id ON sesiones (usuario_id);
CREATE UNIQUE INDEX idx_sesion_refresh_token_hash ON sesiones (refresh_token_hash);
CREATE INDEX idx_sesion_activo ON sesiones (activo) WHERE activo = true;
CREATE INDEX idx_sesion_expiracion ON sesiones (refresh_token_expiracion);

-- ================================================================
-- TABLA: password_reset_token
-- ================================================================
CREATE TABLE password_reset_token (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_expiracion TIMESTAMP NOT NULL,
    usado BOOLEAN NOT NULL DEFAULT false,
    fecha_uso TIMESTAMP,
    
    CONSTRAINT fk_password_reset_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) 
        ON DELETE CASCADE,
    CONSTRAINT uq_password_reset_token UNIQUE (token_hash)
);

-- Índices
CREATE UNIQUE INDEX idx_password_reset_token_hash ON password_reset_token (token_hash);
CREATE INDEX idx_password_reset_usuario_id ON password_reset_token (usuario_id);
CREATE INDEX idx_password_reset_expiracion ON password_reset_token (fecha_expiracion);

-- ================================================================
-- TABLA: audit_auth
-- ================================================================
CREATE TABLE audit_auth (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad_tipo VARCHAR(50) NOT NULL,
    entidad_id UUID,
    accion VARCHAR(30) NOT NULL,
    usuario_id VARCHAR(100),
    ip_cliente VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    datos_adicionales JSONB,
    fecha_evento TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_auth_entidad ON audit_auth (entidad_tipo, entidad_id);
CREATE INDEX idx_audit_auth_usuario ON audit_auth (usuario_id, fecha_evento DESC);
CREATE INDEX idx_audit_auth_fecha ON audit_auth (fecha_evento DESC);
CREATE INDEX idx_audit_auth_ip ON audit_auth (ip_cliente, fecha_evento DESC);

-- ================================================================
-- TRIGGER: updated_at para usuarios
-- ================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.ultima_modificacion = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_usuarios_updated_at
    BEFORE UPDATE ON usuarios
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sesiones_ultima_actividad
    BEFORE UPDATE ON sesiones
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

## 5. Índices de Rendimiento

### 5.1 Índices para Búsqueda

```sql
-- Búsqueda de sesión activa por refresh token
CREATE INDEX idx_sesion_activa_refresh 
ON sesiones (refresh_token_hash, activo) 
WHERE activo = true;

-- Búsqueda de sesiones activas por usuario
CREATE INDEX idx_sesiones_activas_usuario 
ON sesiones (usuario_id) 
WHERE activo = true;

-- Búsqueda de tokens de recuperación válidos
CREATE INDEX idx_password_reset_validos 
ON password_reset_token (token_hash, usado, fecha_expiracion) 
WHERE usado = false AND fecha_expiracion > NOW();

-- Búsqueda de intentos fallidos recientes por IP
CREATE INDEX idx_audit_auth_intentos_fallidos 
ON audit_auth (ip_cliente, fecha_evento DESC) 
WHERE accion = 'LOGIN_FAILED';
```

---

## 6. Constraints de Seguridad

### 6.1 Validación de Password

```sql
-- Aunque la validación se hace en aplicación, se puede agregar constraint:
-- NOTA: Las constraints de CHECK en PostgreSQL no soportan regex directamente
-- La validación de formato de password se hace en la capa de aplicación
```

### 6.2 Notas de Auditoría

| Código | Descripción |
|--------|-------------|
| A-1 | Todos los eventos de login (éxito y fallo) deben registrarse |
| A-2 | Los intentos fallidos de login deben incluir IP y timestamp |
| A-3 | Los refresh tokens deben ser hasheados con PBKDF2 antes de almacenarse |
| A-4 | Los tokens de recuperación de contraseña deben tener expiración de 1 hora |

---

## 7. Políticas de Retención

| Tipo | Duración | Razón |
|------|----------|-------|
| Sesiones activas | 7 días (refresh token) | Duración del token |
| Sesiones expiradas | 30 días para cleanup | Retención mínima |
| Tokens de recuperación usados | 24 horas | Cleanup rápido |
| Logs de auditoría | 7 años | Compliance regulatorio |
| Backups | 7 años (cold storage) | Recovery |

---

## 8. Notas de Seguridad

| Código | Descripción |
|--------|-------------|
| S-1 | Las contraseñas nunca se almacenan en texto plano (usar BCrypt) |
| S-2 | Los refresh tokens se almacenan hasheados con PBKDF2 (100,000 iteraciones) |
| S-3 | Los tokens de recuperación de contraseña se almacenan hasheados |
| S-4 | La IP y user agent se registran en cada evento de login |
| S-5 | Los intentos de login fallidos se bloquean después de 5 intentos |
| S-6 | El bloqueo de cuenta dura 30 minutos |

---

## 9. Historial de Versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-14 | @documentador | Creación inicial - Reorganización de documentación |

---

## 10. Referencias

- Especificación técnica: `/docs/modulos/auth/SPEC.md`
- API: `/docs/modulos/auth/API.md`
- Módulo SOCIOS: `/docs/modulos/socios/MODELO_DATOS.md`
