# Módulo KYC Simplificado - Modelo de Datos

## Resumen

Este documento describe el modelo de datos del módulo KYC Simplificado, incluyendo las entidades, relaciones, DDL y consideraciones de seguridad. El modelo está diseñado para ser extensible hacia futuras integraciones con SAIME/SENIAT.

---

## 1. Diagrama de Entidades

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              MODELO DE DATOS KYC                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────────┐         ┌──────────────────────┐                    │
│   │   Socio          │         │   VerificacionKYC    │                    │
│   │   (del módulo    │         │                      │                    │
│   │    Socios)       │         │  - id (PK)           │                    │
│   └────────┬─────────┘         │  - socioId (FK)       │──────┐              │
│            │                   │  - nivel              │      │              │
│            │ 1:1               │  - estado             │      │              │
│            └───────────────────┤  - fechaInicio        │      │              │
│                                │  - fechaCompletado   │      │ 1:N          │
│                                │  - fechaExpiracion    │      │              │
│                                │  - datosVerifAuto     │      ▼              │
│                                │  - revisadoPor        │ ┌────────────────┐│
│                                │  - fechaRevision      │ │ Documento      ││
│                                │  - comentariosRevision │ │ Identidad      ││
│                                └──────────────────────┘ │                ││
│                                                         │ - id (PK)      ││
│                                                         │ - verificacionId││
│                                                         │ - socioId       ││
│                                                         │ - tipoDocumento ││
│                                                         │ - urlStorage    ││
│                                                         │ - hashArchivo   ││
│                                                         │ - estado        ││
│                                                         │ - fechaSubida   ││
│                                                         │ - fechaExpDoc   ││
│                                                         └────────────────┘│
│                                                                             │
│   ┌──────────────────────┐                                                   │
│   │ ConsentimientoKYC    │                                                   │
│   │                      │                                                   │
│   │ - id (PK)            │                                                   │
│   │ - socioId (FK)       │                                                   │
│   │ - tipoConsentimiento │                                                   │
│   │ - aceptado           │                                                   │
│   │ - fechaConsentimiento│                                                   │
│   │ - ipCliente          │                                                   │
│   │ - userAgent          │                                                   │
│   │ - versionPolitica    │                                                   │
│   └──────────────────────┘                                                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Entidades

### 2.1 VerificacionKYC

**Descripción:** Representa el proceso de verificación KYC de un socio.

| Campo | Tipo | Nullable | Descripción |
|-------|------|----------|-------------|
| id | UUID | No | PK |
| socio_id | UUID | No | FK → Socio.id |
| nivel | VARCHAR(20) | No | BASICO, MEDIO, COMPLETO |
| estado | VARCHAR(20) | No | PENDIENTE, EN_REVISION, APROBADO, RECHAZADO, REENVIADO, EXPIRADO, CANCELADO |
| fecha_inicio | TIMESTAMP | No | Fecha que inició el proceso |
| fecha_completado | TIMESTAMP | Sí | Fecha que completó (aprobado/rechazado) |
| fecha_expiracion | TIMESTAMP | Sí | Fecha que expira el KYC |
| datos_verificacion_automatica | TEXT | Sí | JSON con resultados de verificación automática |
| revisado_por | VARCHAR(100) | Sí | ID del analista que revisó |
| fecha_revision | TIMESTAMP | Sí | Fecha de revisión |
| comentarios_revision | TEXT | Sí | Comentarios del analista |
| motivo_rechazo | TEXT | Sí | Motivo del rechazo |
| created_at | TIMESTAMP | No | |
| updated_at | TIMESTAMP | No | |
| **version** | INT | No | **IMPLEMENTADO:** Optimistic locking (@Version) |

**Índices:**
- `idx_verificacion_socio_id` ON (socio_id)
- `idx_verificacion_estado` ON (estado)
- `idx_verificacion_fecha_inicio` ON (fecha_inicio)

**Restricciones:**
- `chk_verificacion_estado` CHECK (estado IN ('PENDIENTE','EN_REVISION','APROBADO','RECHAZADO','REENVIADO','EXPIRADO','CANCELADO'))
- `chk_verificacion_nivel` CHECK (nivel IN ('BASICO','MEDIO','COMPLETO'))

---

### 2.2 DocumentoIdentidad

**Descripción:** Representa un documento de identidad subido por el socio.

| Campo | Tipo | Nullable | Descripción |
|-------|------|----------|-------------|
| id | UUID | No | PK |
| verificacion_id | UUID | No | FK → VerificacionKYC.id |
| socio_id | UUID | No | FK → Socio.id |
| tipo_documento | VARCHAR(30) | No | CEDULA_ANVERSO, CEDULA_REVERSO, SELFIE_CEDULA, etc. |
| url_almacenamiento | VARCHAR(500) | No | URL al object storage |
| nombre_original | VARCHAR(255) | No | Nombre original del archivo |
| tamano_bytes | BIGINT | No | Tamaño del archivo |
| mime_type | VARCHAR(50) | No | image/jpeg, image/png, application/pdf |
| hash_archivo | VARCHAR(64) | No | SHA-256 del archivo |
| fecha_subida | TIMESTAMP | No | Fecha de subida |
| fecha_expiracion_documento | DATE | Sí | Fecha de expiración del documento |
| estado | VARCHAR(20) | No | PENDIENTE, VALIDADO, RECHAZADO, EXPIRADO |
| motivo_rechazo | TEXT | Sí | Motivo si fue rechazado |
| metadatos_validacion | TEXT | Sí | JSON con resultados de validación |
| observaciones | TEXT | Sí | Observaciones adicionales |
| created_at | TIMESTAMP | No | |
| updated_at | TIMESTAMP | No | |

**Índices:**
- `idx_documento_verificacion_id` ON (verificacion_id)
- `idx_documento_socio_id` ON (socio_id)
- `idx_documento_tipo` ON (tipo_documento)
- `idx_documento_estado` ON (estado)

**Restricciones:**
- `chk_documento_estado` CHECK (estado IN ('PENDIENTE','VALIDADO','RECHAZADO','EXPIRADO'))
- `chk_documento_tamano` CHECK (tamano_bytes <= 10485760)  -- 10MB

---

### 2.3 ConsentimientoKYC

**Descripción:** Almacena el consentimiento del usuario para el tratamiento de sus datos (LOPDP).

> ⚠️ **IMPLEMENTACIÓN IMPORTANTE:** Al revocar consentimiento (LOPDP Art. 7), se crea un **nuevo registro** con `aceptado=false`. No se modifican registros existentes para mantener trazabilidad de auditoría.

| Campo | Tipo | Nullable | Descripción |
|-------|------|----------|-------------|
| id | UUID | No | PK |
| socio_id | UUID | No | FK → Socio.id |
| tipo_consentimiento | VARCHAR(30) | No | KYC_BASICO, KYC_MEDIO, KYC_COMPLETO |
| aceptado | BOOLEAN | No | true si aceptó, false si revocó |
| fecha_consentimiento | TIMESTAMP | No | Fecha y hora del consentimiento |
| ip_cliente | VARCHAR(45) | No | IP del cliente (IPv4 o IPv6) |
| user_agent | VARCHAR(500) | Sí | User agent del navegador |
| version_politica | VARCHAR(20) | No | Versión de la política (lista blanca: "1.0", "2.0", "2.1") |

**Índices:**
- `idx_consentimiento_socio_id` ON (socio_id)
- `idx_consentimiento_fecha` ON (fecha_consentimiento DESC)

---

## 3. Tabla Audit KYC (Shadow Table - IMPLEMENTADA)

Esta tabla registra todos los eventos de auditoría para cumplir con LOPDP y SUDEBAN. El servicio `KYCAuditService` gestiona automáticamente el tracking de accesos.

```sql
-- Tabla de auditoría para compliance (IMPLEMENTADA)
CREATE TABLE audit_kyc (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad_tipo VARCHAR(50) NOT NULL,           -- 'VERIFICACION', 'DOCUMENTO', 'CONSENTIMIENTO'
    entidad_id UUID NOT NULL,                    -- ID de la entidad afectada
    accion VARCHAR(30) NOT NULL,                 -- 'CREATE', 'UPDATE', 'DELETE', 'CONSULT'
    usuario_id VARCHAR(100) NOT NULL,             -- ID del usuario que realizó la acción
    rol_usuario VARCHAR(30) NOT NULL,             -- Rol del usuario
    ip_cliente VARCHAR(45) NOT NULL,              -- IP del cliente
    datos_anteriores JSONB,                      -- Estado anterior (para updates)
    datos_nuevos JSONB,                           -- Estado nuevo
    fecha_evento TIMESTAMP NOT NULL DEFAULT NOW(),

    -- Índices para auditoría
    CONSTRAINT chk_entidad_tipo CHECK (entidad_tipo IN ('VERIFICACION', 'DOCUMENTO', 'CONSENTIMIENTO')),
    CONSTRAINT chk_accion CHECK (accion IN ('CREATE', 'UPDATE', 'DELETE', 'CONSULT', 'LOGIN', 'LOGOUT'))
);

CREATE INDEX idx_audit_entidad ON audit_kyc (entidad_tipo, entidad_id);
CREATE INDEX idx_audit_usuario ON audit_kyc (usuario_id, fecha_evento DESC);
CREATE INDEX idx_audit_fecha ON audit_kyc (fecha_evento DESC);
```

### Servicio de Auditoría

El `KYCAuditService` implementa:
- Logueo automático de CREATE, UPDATE, DELETE, CONSULT
- Captura de datos anteriores/nuevos en JSONB
- Registro de IP del cliente y user agent
- Retención de 7 años para compliance SUDEBAN

---

## 4. DDL Completo

```sql
-- ================================================================
-- KYC Simplificado - DDL para PostgreSQL
-- Versión: 1.0
-- Fecha: 2026-04-14
-- ================================================================

-- Extensión para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================================================================
-- TABLA: verificacion_kyc
-- ================================================================
CREATE TABLE verificacion_kyc (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    socio_id UUID NOT NULL,
    nivel VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_inicio TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_completado TIMESTAMP,
    fecha_expiracion TIMESTAMP,
    datos_verificacion_automatica TEXT,
    revisado_por VARCHAR(100),
    fecha_revision TIMESTAMP,
    comentarios_revision TEXT,
    motivo_rechazo TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_verificacion_nivel CHECK (nivel IN ('BASICO', 'MEDIO', 'COMPLETO')),
    CONSTRAINT chk_verificacion_estado CHECK (estado IN ('PENDIENTE', 'EN_REVISION', 'APROBADO', 'RECHAZADO', 'REENVIADO', 'EXPIRADO', 'CANCELADO'))
);

CREATE INDEX idx_verificacion_socio_id ON verificacion_kyc (socio_id);
CREATE INDEX idx_verificacion_estado ON verificacion_kyc (estado);
CREATE INDEX idx_verificacion_fecha_inicio ON verificacion_kyc (fecha_inicio DESC);

-- ================================================================
-- TABLA: documento_identidad
-- ================================================================
CREATE TABLE documento_identidad (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    verificacion_id UUID NOT NULL,
    socio_id UUID NOT NULL,
    tipo_documento VARCHAR(30) NOT NULL,
    url_almacenamiento VARCHAR(500) NOT NULL,
    nombre_original VARCHAR(255) NOT NULL,
    tamano_bytes BIGINT NOT NULL,
    mime_type VARCHAR(50) NOT NULL,
    hash_archivo VARCHAR(64) NOT NULL,
    fecha_subida TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_expiracion_documento DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    motivo_rechazo TEXT,
    metadatos_validacion TEXT,
    observaciones TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_documento_verificacion 
        FOREIGN KEY (verificacion_id) 
        REFERENCES verificacion_kyc(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_documento_estado CHECK (estado IN ('PENDIENTE', 'VALIDADO', 'RECHAZADO', 'EXPIRADO')),
    CONSTRAINT chk_documento_tamano CHECK (tamano_bytes <= 10485760),
    -- CORRECCIÓN A1: Constraint UNIQUE para evitar documentos duplicados del mismo tipo
    CONSTRAINT uq_documento_tipo_verificacion UNIQUE (verificacion_id, tipo_documento)
);

CREATE INDEX idx_documento_verificacion_id ON documento_identidad (verificacion_id);
CREATE INDEX idx_documento_socio_id ON documento_identidad (socio_id);
CREATE INDEX idx_documento_tipo ON documento_identidad (tipo_documento);
CREATE INDEX idx_documento_estado ON documento_identidad (estado);

-- ================================================================
-- TABLA: consentimiento_kyc
-- ================================================================
CREATE TABLE consentimiento_kyc (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    socio_id UUID NOT NULL,
    tipo_consentimiento VARCHAR(30) NOT NULL,
    aceptado BOOLEAN NOT NULL,
    fecha_consentimiento TIMESTAMP NOT NULL DEFAULT NOW(),
    ip_cliente VARCHAR(45) NOT NULL,
    user_agent VARCHAR(500),
    version_politica VARCHAR(20) NOT NULL
);

CREATE INDEX idx_consentimiento_socio_id ON consentimiento_kyc (socio_id);
CREATE INDEX idx_consentimiento_fecha ON consentimiento_kyc (fecha_consentimiento DESC);

-- ================================================================
-- TABLA: audit_kyc (Shadow Table para compliance)
-- ================================================================
CREATE TABLE audit_kyc (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
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

CREATE INDEX idx_audit_entidad ON audit_kyc (entidad_tipo, entidad_id);
CREATE INDEX idx_audit_usuario ON audit_kyc (usuario_id, fecha_evento DESC);
CREATE INDEX idx_audit_fecha ON audit_kyc (fecha_evento DESC);

-- ================================================================
-- TRIGGER: updated_at para verificacion_kyc
-- ================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_verificacion_kyc_updated_at
    BEFORE UPDATE ON verificacion_kyc
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_documento_identidad_updated_at
    BEFORE UPDATE ON documento_identidad
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

## 5. Relaciones con Otros Módulos

### 5.1 Relación con Módulo Socios

```sql
-- La tabla verificacion_kyc tiene FK a socio (del módulo Socios)
-- Esto requiere que el módulo Socios ya tenga la tabla creada

-- Extensión futura: Agregar campos a la tabla Socio
-- ALTER TABLE socio ADD COLUMN nivel_verificacion_kyc VARCHAR(20);
-- ALTER TABLE socio ADD COLUMN fecha_ultimo_kyc TIMESTAMP;
```

### 5.2 Integración Futura con SAIME/SENIAT

```
┌─────────────────────────────────────────────────────────────┐
│           ARQUITECTURA DE EXTENSIBILIDAD                   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   Domain Layer                                              │
│   ┌─────────────────────────────────────────────────────┐  │
│   │ IdentidadVerificatorPort (interface)                │  │
│   │   - verificar(DatosVerificacion): Resultado         │  │
│   └─────────────────────────────────────────────────────┘  │
│                          │                                  │
│                          ▼                                  │
│   ┌───────────────────┐    ┌────────────────────────────┐  │
│   │ Local Adapter     │    │ SaimeAdapter (FUTURO)     │  │
│   │ (implementación   │    │                           │  │
│   │  actual)          │    │ // Llama a API SAIME      │  │
│   └───────────────────┘    └────────────────────────────┘  │
│                                                             │
│   RifVerificatorPort (interface)                            │
│                          │                                  │
│                          ▼                                  │
│   ┌───────────────────┐    ┌────────────────────────────┐  │
│   │ Local Adapter     │    │ SeniatAdapter (FUTURO)    │  │
│   │ (implementación   │    │                           │  │
│   │  actual)          │    │ // Llama a API SENIAT     │  │
│   └───────────────────┘    └────────────────────────────┘  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. Constraints de Seguridad

### 6.1 Constraints de Validación

```sql
-- Constraint: Verificar que el número de cédula tenga formato válido
-- Formato venezolano: V-12345678, E-12345678, etc.
ALTER TABLE documento_identidad 
ADD CONSTRAINT chk_formato_cedula_venezuela
CHECK (tipo_documento != 'CEDULA_ANVERSO' OR 
       metadatos_validacion::json->>'numero_cedula' ~ '^[VE][0-9]{7,8}$');

-- Constraint: No permitir documentos expirados
ALTER TABLE documento_identidad 
ADD CONSTRAINT chk_documento_no_expirado
CHECK (fecha_expiracion_documento IS NULL OR 
       fecha_expiracion_documento > CURRENT_DATE);

-- Constraint: Solo una verificación activa por socio
CREATE UNIQUE INDEX idx_socio_kyc_activo 
ON verificacion_kyc (socio_id) 
WHERE estado IN ('PENDIENTE', 'EN_REVISION', 'APROBADO');
```

### 6.2 Notas de Auditoría (V-I)

| Código | Descripción |
|--------|-------------|
| V-I1 | Todos los accesos a documentos deben quedar registrados en audit_kyc |
| V-I2 | Los hash de archivos permiten verificar integridad (SHA-256) |
| V-I3 | Tokens de acceso a storage expiran después de 15 minutos |
| V-I4 | Las URLs de documentos no deben contener información sensible |
| V-I5 | Los logs de auditoría deben mantenerse por 7 años (regulación) |

---

## 7. Recomendaciones de Índices

### 7.1 Índices para Performance

```sql
-- Búsqueda de verificaciones pendientes de revisión (FIFO)
CREATE INDEX idx_cola_revision_fifo 
ON verificacion_kyc (estado, fecha_inicio ASC) 
WHERE estado = 'EN_REVISION';

-- Búsqueda de KYC próximos a expirar
CREATE INDEX idx_kyc_por_expirar 
ON verificacion_kyc (fecha_expiracion) 
WHERE estado = 'APROBADO' 
AND fecha_expiracion BETWEEN NOW() AND NOW() + INTERVAL '30 days';

-- Búsqueda de documentos por tipo y estado
CREATE INDEX idx_documentos_pendientes 
ON documento_identidad (verificacion_id, tipo_documento, estado) 
WHERE estado = 'PENDIENTE';
```

### 7.2 Índices para Auditoría

```sql
-- Búsqueda de actividad por usuario
CREATE INDEX idx_audit_actividad_usuario 
ON audit_kyc (usuario_id, fecha_evento DESC);

-- Búsqueda de cambios en documentos específicos
CREATE INDEX idx_audit_documento 
ON audit_kyc (entidad_tipo, entidad_id, fecha_evento DESC);
```

---

## 8. Modelo de Datos para Almacenamiento de Documentos

### 8.1 Estructura de Object Storage (MinIO/S3)

```
bucket-kyc/
├── {socioId}/
│   ├── {verificacionId}/
│   │   ├── documentos/
│   │   │   ├── CEDULA_ANVERSO_20260414_103000.jpg
│   │   │   ├── CEDULA_REVERSO_20260414_103005.jpg
│   │   │   ├── SELFIE_CEDULA_20260414_103010.jpg
│   │   │   └── COMPROBANTE_DOMICILIO_20260414_103015.pdf
│   │   └── thumbnails/
│   │       ├── CEDULA_ANVERSO_thumb.jpg
│   │       └── ...
│   └── backups/
│       └── (documentos de verificaciones anteriores)
```

### 8.2 Políticas de Retención

| Tipo | Duración | Razón |
|------|----------|-------|
| Documentos activos | 7 años después del cierre de cuenta | Regulación financiera |
| Documentos eliminados | 90 días en trash | Recuperación ante errores |
| Thumbnails | 2 años | Accesibilidad |
| Logs de acceso | 5 años | Auditoría |
| Backups | 7 años (cold storage) | Compliance |

---

## 8.1 Seguridad del Storage (Correcciones de Auditoría)

> ⚠️ **NOTA:** Las siguientes correcciones son OBLIGATORIAS antes de producción.

### Correcciones de Seguridad

| ID | Hallazgo | Corrección |
|----|----------|------------|
| **C1** | Documentos sin encriptación | Usar SSE-C (Server-Side Encryption with Customer keys) en MinIO/S3 |
| **A2** | Nombres de archivo predecibles | Generar nombres aleatorios con UUID, no usar nombre original |
| **A7** | Sin backup georeplicado | Configurar replicación Cross-Region en S3/MinIO |

### Estructura de Storage CORREGIDA

```
bucket-kyc/
├── {socioId}/
│   ├── {verificacionId}/
│   │   ├── documentos/
│   │   │   ├── {UUID}.jpg      # Nombre aleatorio, no original
│   │   │   ├── {UUID}.jpg
│   │   │   └── {UUID}.pdf
│   │   └── metadata/
│   │       └── {uuid}_meta.json  # Metadatos del documento
│   └── backups/
│       └── (replicación cross-region)
```

### Notas de Implementación

1. **Encriptación AES-256:** Configurar en el bucket o por objeto con SSE-KMS
2. **Nombres de archivo:** Siempre usar `UUID.randomUUID() + extension` 
3. **Path traversal:** Validar que el path no contenga `..` antes de guardar
4. **Acceso a documentos:** Solo mediante pre-signed URLs con expiración de 15 minutos

---

## 9. Historial de Versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-14 | @product-manager | Creación inicial - KYC Simplificado |
| 1.1 | 2026-04-14 | @auditoria | Actualización modelo datos post-implementación: tabla audit_kyc con KYCAuditService, optimistic locking @Version, versionPolitica con lista blanca |

---

## 10. Referencias

- Especificación técnica: `/docs/modulos/kyc/SPEC.md`
- API: `/docs/modulos/kyc/API.md`
- Auditoría de seguridad: `/docs/auditorias/ULTIMA_AUDITORIA.md`