# Módulo SOCIOS - Modelo de Datos

**Proyecto:** Plataforma Fondo de Ahorro  
**Versión:** 1.0  
**Fecha:** 2026-04-14

---

## Resumen

Este documento describe el modelo de datos del módulo SOCIOS, incluyendo las entidades, relaciones, DDL y consideraciones de seguridad.

---

## 1. Diagrama de Entidades

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              MODELO DE DATOS SOCIOS                         │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────────┐         ┌──────────────────────┐                    │
│   │   Usuario        │         │   Socio              │                    │
│   │   (del módulo    │         │                      │                    │
│   │    AUTH)         │         │  - id (PK)           │                    │
│   └────────┬─────────┘         │  - numeroSocio       │                    │
│            │                   │  - primerNombre      │                    │
│            │ 1:1               │  - segundoNombre     │                    │
│            └───────────────────┤  - primerApellido    │                    │
│                                │  - segundoApellido   │                    │
│                                │  - tipoDocumento      │                    │
│                                │  - numeroDocumento   │                    │
│                                │  - fechaNacimiento   │                    │
│                                │  - genero             │                    │
│                                │  - estadoCivil        │                    │
│                                │  - nacionalidad      │                    │
│                                │  - direccion (VO)     │                    │
│                                │  - correoElectronico  │                    │
│                                │  - telefonoPrincipal │                    │
│                                │  - telefonoSecundario │                    │
│                                │  - contactoEmergencia │                    │
│                                │  - empresa            │                    │
│                                │  - departamento      │                    │
│                                │  - cargo              │                    │
│                                │  - tipoContrato       │                    │
│                                │  - salario           │                    │
│                                │  - banco              │                    │
│                                │  - numeroCuenta      │                    │
│                                │  - estado             │                    │
│                                │  - nivelKYC          │                    │
│                                └──────────────────────┘                    │
│                                                                             │
│   ┌──────────────────────┐         ┌──────────────────────┐               │
│   │ SolicitudRegistro   │         │   PasswordResetToken │               │
│   │                      │         │   (del módulo AUTH)  │               │
│   │ - id (PK)           │         └──────────┬───────────┘               │
│   │ - primerNombre      │                    │                          │
│   │ - segundoNombre     │                    │                          │
│   │ - primerApellido     │                    │                          │
│   │ - tipoDocumento      │                    │                          │
│   │ - numeroDocumento    │                    │                          │
│   │ - correoElectronico  │                    │                          │
│   │ - estado             │◄───────────────────┘                          │
│   │ - motivoRechazo      │    Genera Usuario cuando                       │
│   │ - fechaSolicitud    │    es APROBADA                                │
│   │ - fechaProcesamiento│                                              │
│   └──────────────────────┘                                               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Entidades

### 2.1 Socio

**Descripción:** Representa un socio del Fondo de Ahorro.

| Campo | Tipo | Nullable | Descripción |
|-------|------|----------|-------------|
| id | UUID | No | PK |
| numero_socio | VARCHAR(20) | No | Número único de socio (FA-YYYY-NNNNNN) |
| primer_nombre | VARCHAR(50) | No | Primer nombre |
| segundo_nombre | VARCHAR(50) | Sí | Segundo nombre |
| primer_apellido | VARCHAR(50) | No | Primer apellido |
| segundo_apellido | VARCHAR(50) | Sí | Segundo apellido |
| tipo_documento | VARCHAR(30) | No | CEDULA_IDENTIDAD, PASAPORTE, CEDULA_EXTRANJERA |
| numero_documento | VARCHAR(20) | No | Único por tipo de documento |
| fecha_nacimiento | DATE | No | Fecha de nacimiento |
| genero | VARCHAR(20) | No | MASCULINO, FEMENINO, OTRO |
| estado_civil | VARCHAR(20) | No | SOLTERO, CASADO, UNION_LIBRE, DIVORCIADO, VIUDO |
| nacionalidad | VARCHAR(50) | No | Nacionalidad |
| calle | VARCHAR(100) | No | Calle de dirección |
| ciudad | VARCHAR(100) | No | Ciudad |
| estado | VARCHAR(100) | No | Estado/Región |
| codigo_postal | VARCHAR(10) | No | Código postal (4-10 chars) |
| pais | VARCHAR(100) | No | País |
| correo_electronico | VARCHAR(255) | No | Email único |
| telefono_principal | VARCHAR(20) | No | Teléfono principal |
| telefono_secundario | VARCHAR(20) | Sí | Teléfono secundario |
| contacto_nombre | VARCHAR(100) | No | Nombre contacto de emergencia |
| contacto_telefono | VARCHAR(20) | No | Teléfono contacto de emergencia |
| contacto_parentesco | VARCHAR(50) | No | Parentesco |
| empresa | VARCHAR(100) | No | Empresa donde trabaja |
| departamento | VARCHAR(100) | Sí | Departamento |
| cargo | VARCHAR(100) | Sí | Cargo |
| tipo_contrato | VARCHAR(30) | No | PERMANENTE, TEMPORAL, PRESTACION_SERVICIOS, PASANTE |
| salario | DECIMAL(12,2) | No | Salario |
| banco | VARCHAR(50) | No | Banco |
| numero_cuenta | VARCHAR(20) | No | Número de cuenta |
| estado | VARCHAR(30) | No | PENDIENTE_APROBACION, ACTIVO, INACTIVO, ELIMINADO |
| nivel_kyc | INT | No | Nivel KYC del socio |
| fecha_registro | TIMESTAMP | No | Fecha de registro |
| fecha_actualizacion | TIMESTAMP | No | Última actualización |

**Índices:**
- `idx_socio_numero_socio` ON (numero_socio) UNIQUE
- `idx_socio_correo` ON (correo_electronico) UNIQUE
- `idx_socio_documento` ON (tipo_documento, numero_documento) UNIQUE
- `idx_socio_estado` ON (estado)
- `idx_socio_empresa` ON (empresa)

**Restricciones:**
- `chk_socio_estado` CHECK (estado IN ('PENDIENTE_APROBACION','ACTIVO','INACTIVO','ELIMINADO'))
- `chk_socio_tipo_documento` CHECK (tipo_documento IN ('CEDULA_IDENTIDAD','PASAPORTE','CEDULA_EXTRANJERA'))
- `chk_socio_genero` CHECK (genero IN ('MASCULINO','FEMENINO','OTRO'))
- `chk_socio_estado_civil` CHECK (estado_civil IN ('SOLTERO','CASADO','UNION_LIBRE','DIVORCIADO','VIUDO'))

---

### 2.2 SolicitudRegistro

**Descripción:** Gestiona las solicitudes de registro públicas.

| Campo | Tipo | Nullable | Descripción |
|-------|------|----------|-------------|
| id | UUID | No | PK |
| primer_nombre | VARCHAR(50) | No | Primer nombre |
| segundo_nombre | VARCHAR(50) | Sí | Segundo nombre |
| primer_apellido | VARCHAR(50) | No | Primer apellido |
| segundo_apellido | VARCHAR(50) | Sí | Segundo apellido |
| tipo_documento | VARCHAR(30) | No | Tipo de documento |
| numero_documento | VARCHAR(20) | No | Número de documento |
| fecha_nacimiento | DATE | No | Fecha de nacimiento |
| genero | VARCHAR(20) | No | Género |
| estado_civil | VARCHAR(20) | Sí | Estado civil |
| nacionalidad | VARCHAR(50) | Sí | Nacionalidad |
| correo_electronico | VARCHAR(255) | No | Email |
| telefono_principal | VARCHAR(20) | No | Teléfono |
| telefono_secundario | VARCHAR(20) | Sí | Teléfono secundario |
| empresa | VARCHAR(100) | No | Empresa |
| departamento | VARCHAR(100) | Sí | Departamento |
| cargo | VARCHAR(100) | Sí | Cargo |
| tipo_contrato | VARCHAR(30) | Sí | Tipo de contrato |
| salario | VARCHAR(50) | Sí | Salario (como string en tabla) |
| banco | VARCHAR(50) | Sí | Banco |
| numero_cuenta | VARCHAR(20) | Sí | Número de cuenta |
| estado | VARCHAR(20) | No | PENDIENTE, APROBADA, RECHAZADA |
| motivo_rechazo | TEXT | Sí | Motivo del rechazo |
| fecha_solicitud | TIMESTAMP | No | Fecha de solicitud |
| fecha_procesamiento | TIMESTAMP | Sí | Fecha de procesamiento |

**Índices:**
- `idx_solicitud_estado` ON (estado)
- `idx_solicitud_fecha` ON (fecha_solicitud DESC)
- `idx_solicitud_documento` ON (tipo_documento, numero_documento)

**Restricciones:**
- `chk_solicitud_estado` CHECK (estado IN ('PENDIENTE','APROBADA','RECHAZADA'))

---

## 3. Tablas de Auditoría

### 3.1 Tabla audit_socios (Shadow Table)

```sql
-- Tabla de auditoría para compliance
CREATE TABLE audit_socios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad_tipo VARCHAR(50) NOT NULL,           -- 'SOCIO', 'SOLICITUD'
    entidad_id UUID NOT NULL,                    -- ID de la entidad afectada
    accion VARCHAR(30) NOT NULL,                 -- 'CREATE', 'UPDATE', 'DELETE'
    usuario_id VARCHAR(100) NOT NULL,             -- ID del usuario que realizó la acción (o 'SYSTEM')
    rol_usuario VARCHAR(30) NOT NULL,             -- Rol del usuario
    ip_cliente VARCHAR(45) NOT NULL,              -- IP del cliente
    datos_anteriores JSONB,                      -- Estado anterior
    datos_nuevos JSONB,                          -- Estado nuevo
    fecha_evento TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_entidad_tipo CHECK (entidad_tipo IN ('SOCIO', 'SOLICITUD', 'CAMBIO_ESTADO')),
    CONSTRAINT chk_accion CHECK (accion IN ('CREATE', 'UPDATE', 'DELETE', 'CAMBIO_ESTADO'))
);

CREATE INDEX idx_audit_entidad ON audit_socios (entidad_tipo, entidad_id);
CREATE INDEX idx_audit_usuario ON audit_socios (usuario_id, fecha_evento DESC);
CREATE INDEX idx_audit_fecha ON audit_socios (fecha_evento DESC);
```

---

## 4. DDL Completo

```sql
-- ================================================================
-- SOCIOS Module - DDL para PostgreSQL
-- Versión: 1.0
-- Fecha: 2026-04-14
-- ================================================================

-- Extensión para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================================================================
-- TABLA: socios
-- ================================================================
CREATE TABLE socios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_socio VARCHAR(20) NOT NULL,
    primer_nombre VARCHAR(50) NOT NULL,
    segundo_nombre VARCHAR(50),
    primer_apellido VARCHAR(50) NOT NULL,
    segundo_apellido VARCHAR(50),
    tipo_documento VARCHAR(30) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    genero VARCHAR(20) NOT NULL,
    estado_civil VARCHAR(20) NOT NULL,
    nacionalidad VARCHAR(50) NOT NULL,
    
    -- Direccion (desnormalizada)
    calle VARCHAR(100) NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    estado VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(10) NOT NULL,
    pais VARCHAR(100) NOT NULL,
    
    correo_electronico VARCHAR(255) NOT NULL,
    telefono_principal VARCHAR(20) NOT NULL,
    telefono_secundario VARCHAR(20),
    
    -- Contacto de emergencia (desnormalizado)
    contacto_nombre VARCHAR(100) NOT NULL,
    contacto_telefono VARCHAR(20) NOT NULL,
    contacto_parentesco VARCHAR(50) NOT NULL,
    
    empresa VARCHAR(100) NOT NULL,
    departamento VARCHAR(100),
    cargo VARCHAR(100),
    tipo_contrato VARCHAR(30) NOT NULL,
    salario DECIMAL(12,2) NOT NULL,
    banco VARCHAR(50) NOT NULL,
    numero_cuenta VARCHAR(20) NOT NULL,
    
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_APROBACION',
    nivel_kyc INT NOT NULL DEFAULT 0,
    fecha_registro TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT chk_socio_estado CHECK (estado IN ('PENDIENTE_APROBACION', 'ACTIVO', 'INACTIVO', 'ELIMINADO')),
    CONSTRAINT chk_socio_tipo_documento CHECK (tipo_documento IN ('CEDULA_IDENTIDAD', 'PASAPORTE', 'CEDULA_EXTRANJERA')),
    CONSTRAINT chk_socio_genero CHECK (genero IN ('MASCULINO', 'FEMENINO', 'OTRO')),
    CONSTRAINT chk_socio_estado_civil CHECK (estado_civil IN ('SOLTERO', 'CASADO', 'UNION_LIBRE', 'DIVORCIADO', 'VIUDO')),
    CONSTRAINT chk_socio_tipo_contrato CHECK (tipo_contrato IN ('PERMANENTE', 'TEMPORAL', 'PRESTACION_SERVICIOS', 'PASANTE')),
    CONSTRAINT chk_socio_numero_documento CHECK (numero_documento ~ '^[VE][0-9]{7,8}$'),
    CONSTRAINT chk_socio_codigo_postal CHECK (LENGTH(codigo_postal) BETWEEN 4 AND 10),
    
    -- Uniques
    CONSTRAINT uq_socio_numero_socio UNIQUE (numero_socio),
    CONSTRAINT uq_socio_correo UNIQUE (correo_electronico),
    CONSTRAINT uq_socio_documento UNIQUE (tipo_documento, numero_documento)
);

-- Índices
CREATE INDEX idx_socio_numero_socio ON socios (numero_socio);
CREATE INDEX idx_socio_correo ON socios (correo_electronico);
CREATE INDEX idx_socio_documento ON socios (tipo_documento, numero_documento);
CREATE INDEX idx_socio_estado ON socios (estado);
CREATE INDEX idx_socio_empresa ON socios (empresa);
CREATE INDEX idx_socio_nombre ON socios (primer_nombre, primer_apellido);

-- ================================================================
-- TABLA: solicitud_registro
-- ================================================================
CREATE TABLE solicitud_registro (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    primer_nombre VARCHAR(50) NOT NULL,
    segundo_nombre VARCHAR(50),
    primer_apellido VARCHAR(50) NOT NULL,
    segundo_apellido VARCHAR(50),
    tipo_documento VARCHAR(30) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    genero VARCHAR(20) NOT NULL,
    estado_civil VARCHAR(20),
    nacionalidad VARCHAR(50),
    correo_electronico VARCHAR(255) NOT NULL,
    telefono_principal VARCHAR(20) NOT NULL,
    telefono_secundario VARCHAR(20),
    empresa VARCHAR(100) NOT NULL,
    departamento VARCHAR(100),
    cargo VARCHAR(100),
    tipo_contrato VARCHAR(30),
    salario VARCHAR(50),
    banco VARCHAR(50),
    numero_cuenta VARCHAR(20),
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    motivo_rechazo TEXT,
    fecha_solicitud TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_procesamiento TIMESTAMP,
    
    CONSTRAINT chk_solicitud_estado CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA'))
);

CREATE INDEX idx_solicitud_estado ON solicitud_registro (estado);
CREATE INDEX idx_solicitud_fecha ON solicitud_registro (fecha_solicitud DESC);
CREATE INDEX idx_solicitud_documento ON solicitud_registro (tipo_documento, numero_documento);
CREATE INDEX idx_solicitud_correo ON solicitud_registro (correo_electronico);

-- ================================================================
-- TABLA: audit_socios
-- ================================================================
CREATE TABLE audit_socios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad_tipo VARCHAR(50) NOT NULL,
    entidad_id UUID NOT NULL,
    accion VARCHAR(30) NOT NULL,
    usuario_id VARCHAR(100) NOT NULL,
    rol_usuario VARCHAR(30) NOT NULL,
    ip_cliente VARCHAR(45) NOT NULL,
    datos_anteriores JSONB,
    datos_nuevos JSONB,
    fecha_evento TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_entidad ON audit_socios (entidad_tipo, entidad_id);
CREATE INDEX idx_audit_usuario ON audit_socios (usuario_id, fecha_evento DESC);
CREATE INDEX idx_audit_fecha ON audit_socios (fecha_evento DESC);

-- ================================================================
-- TRIGGER: updated_at para socios
-- ================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_socios_updated_at
    BEFORE UPDATE ON socios
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

## 5. Índices de Rendimiento

### 5.1 Índices para Búsqueda

```sql
-- Búsqueda por número de socio (búsqueda exacta)
CREATE INDEX idx_socio_numero_socio_buscar ON socios (numero_socio);

-- Búsqueda de socios activos por empresa
CREATE INDEX idx_socio_activos_empresa 
ON socios (empresa) 
WHERE estado = 'ACTIVO';

-- Búsqueda de socios pendientes de aprobación
CREATE INDEX idx_socio_pendientes 
ON socios (fecha_registro DESC) 
WHERE estado = 'PENDIENTE_APROBACION';

-- Búsqueda de solicitudes pendientes (FIFO)
CREATE INDEX idx_solicitud_pendientes_fifo 
ON solicitud_registro (estado, fecha_solicitud ASC) 
WHERE estado = 'PENDIENTE';
```

---

## 6. Constraints de Seguridad

### 6.1 Validación de Datos

```sql
-- Formato de número de documento venezolano
ALTER TABLE socios 
ADD CONSTRAINT chk_formato_documento_venezuela
CHECK (tipo_documento != 'CEDULA_IDENTIDAD' OR 
       numero_documento ~ '^[VE][0-9]{7,8}$');

-- Longitud del código postal
ALTER TABLE socios 
ADD CONSTRAINT chk_codigo_postal_longitud
CHECK (LENGTH(codigo_postal) BETWEEN 4 AND 10);

-- Salario positivo
ALTER TABLE socios 
ADD CONSTRAINT chk_salario_positivo
CHECK (salario > 0);
```

---

## 7. Políticas de Retención

| Tipo | Duración | Razón |
|------|----------|-------|
| Socios eliminados | 7 años después del soft delete | Regulación financiera |
| Solicitudes rechazadas | 2 años | Auditoría |
| Logs de auditoría | 7 años | Compliance |
| Backups | 7 años (cold storage) | Recovery |

---

## 8. Notas de Seguridad

| Código | Descripción |
|--------|-------------|
| S-1 | Los campos `salario` y `numero_cuenta` deben estar encriptados en la base de datos (AES-256) |
| S-2 | Las contraseñas nunca se almacenan en la tabla socios |
| S-3 | El número de socio se genera usando hash SHA-256 |
| S-4 | Los logs de auditoría deben mantenerse por 7 años |

---

## 9. Historial de Versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-14 | @documentador | Creación inicial - Reorganización de documentación |

---

## 10. Referencias

- Especificación técnica: `/docs/modulos/socios/SPEC.md`
- API: `/docs/modulos/socios/API.md`
- Módulo AUTH: `/docs/modulos/auth/MODELO_DATOS.md`
