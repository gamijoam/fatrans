# Módulo DOCUMENTOS PDF - Modelo de Datos

**Proyecto:** Plataforma Fondo de Ahorro
**Versión:** 1.0
**Fecha:** 2026-04-19

---

## Resumen

Este documento describe el modelo de datos del módulo DOCUMENTOS PDF, incluyendo las entidades, relaciones, DDL, buckets MinIO y consideraciones de seguridad.

---

## 1. Diagrama de Entidades

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        MODELO DE DATOS DOCUMENTOS PDF                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────────┐         ┌──────────────────────┐                      │
│   │     Socio        │         │     Documento         │                      │
│   │   (módulo SOCIOS) │         │                       │                      │
│   │                   │         │  - id (PK)           │                      │
│   │  - id (PK)        │────────►│  - socioId (FK)      │                      │
│   │  - numeroSocio    │   1:N   │  - tipo (enum)       │                      │
│   │  - estado         │         │  - estado (enum)     │                      │
│   │                   │         │  - nombreArchivo      │                      │
│   └──────────────────┘         │  - rutaAlmacenamiento │                      │
│                                │  - hashArchivo        │                      │
│                                │  - firmaDigital       │                      │
│                                │  - tamanoBytes        │                      │
│                                │  - fechaGeneracion    │                      │
│                                │  - fechaExpiracion    │                      │
│                                │  - generadoPor        │                      │
│                                │  - clasificacion      │                      │
│                                └──────────────────────┘                      │
│                                                                             │
│   ┌──────────────────────────┐                                               │
│   │  DocumentoAudit           │                                               │
│   │  (Shadow Table)          │                                               │
│   │                          │                                               │
│   │  - id (PK)               │                                               │
│   │  - documentoId (FK)     │                                               │
│   │  - accion                │                                               │
│   │  - usuarioId             │                                               │
│   │  - usuarioRol            │                                               │
│   │  - ipCliente             │                                               │
│   │  - documentoHash         │                                               │
│   │  - resultado              │                                               │
│   │  - fechaEvento           │                                               │
│   └──────────────────────────┘                                               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Entidades

### 2.1 Documento

**Descripción:** Representa un documento PDF generado por el sistema.

| Campo | Tipo | Nullable | Descripción |
|-------|------|----------|-------------|
| id | UUID | No | PK |
| socio_id | UUID | No | FK hacia socios.id |
| tipo | VARCHAR(30) | No | ESTADO_CUENTA, CONSTANCIA_AFILIACION, CONTRATO_ADHESION, PAGARE, TABLA_AMORTIZACION, CARTA_BENEFICIARIOS |
| estado | VARCHAR(20) | No | GENERADO, ALMACENADO, EXPIRADO, REVOCADO |
| nombre_archivo | VARCHAR(255) | No | Nombre del archivo PDF |
| ruta_almacenamiento | TEXT | No | Path en MinIO |
| hash_archivo | VARCHAR(64) | No | SHA-256 del PDF |
| firma_digital | TEXT | Sí | Firma RSA SHA-256 (para CONTRATO y PAGARE) |
| tamano_bytes | BIGINT | No | Tamaño del archivo |
| fecha_generacion | TIMESTAMP | No | Fecha de generación |
| fecha_expiracion | TIMESTAMP | Sí | Fecha de expiración (null = nunca expira) |
| generado_por | VARCHAR(100) | No | USER_ID o SYSTEM |
| clasificacion | VARCHAR(20) | No | CONFIDENCIAL, RESTRINGIDO, PUBLICO |
| created_at | TIMESTAMP | No | Timestamp de creación |
| updated_at | TIMESTAMP | No | Timestamp de actualización |

**Índices:**
- `idx_documento_socio_id` ON (socio_id)
- `idx_documento_tipo` ON (tipo)
- `idx_documento_estado` ON (estado)
- `idx_documento_fecha_generacion` ON (fecha_generacion DESC)
- `idx_documento_socio_tipo` ON (socio_id, tipo)
- `idx_documento_hash` ON (hash_archivo)

**Restricciones:**
- `chk_tipo` CHECK (tipo IN ('ESTADO_CUENTA','CONSTANCIA_AFILIACION','CONTRATO_ADHESION','PAGARE','TABLA_AMORTIZACION','CARTA_BENEFICIARIOS'))
- `chk_estado` CHECK (estado IN ('GENERADO','ALMACENADO','EXPIRADO','REVOCADO'))
- `chk_clasificacion` CHECK (clasificacion IN ('CONFIDENCIAL','RESTRINGIDO','PUBLICO'))

---

## 3. Tablas de Auditoría

### 3.1 Tabla documentos_pdf_audit (Shadow Table)

**Descripción:** Tabla de auditoría para compliance y tracking de operaciones.

| Campo | Tipo | Nullable | Descripción |
|-------|------|----------|-------------|
| id | UUID | No | PK |
| documento_id | UUID | No | FK hacia documentos_pdf.id |
| entidad_tipo | VARCHAR(50) | No | Tipo de entidad ('DOCUMENTO') |
| accion | VARCHAR(30) | No | GENERAR, DESCARGAR, REVOCAR, EXPIRAR |
| usuario_id | VARCHAR(100) | No | ID del usuario que realizó la acción |
| usuario_rol | VARCHAR(30) | No | Rol del usuario |
| ip_cliente | VARCHAR(45) | No | IP del cliente |
| documento_hash | VARCHAR(64) | No | Hash SHA-256 del documento |
| resultado | VARCHAR(20) | No | EXITOSO, FALLIDO |
| razon_fallo | TEXT | Sí | Razón del fallo (si aplica) |
| fecha_evento | TIMESTAMP | No | Fecha y hora del evento |

**Índices:**
- `idx_doc_audit_documento_id` ON (documento_id)
- `idx_doc_audit_usuario_id` ON (usuario_id, fecha_evento DESC)
- `idx_doc_audit_fecha` ON (fecha_evento DESC)

**Restricciones:**
- `chk_entidad_tipo_doc` CHECK (entidad_tipo IN ('DOCUMENTO'))
- `chk_accion_doc` CHECK (accion IN ('GENERAR','DESCARGAR','REVOCAR','EXPIRAR'))
- `chk_resultado_doc` CHECK (resultado IN ('EXITOSO','FALLIDO'))

---

## 4. DDL Completo

```sql
-- ================================================================
-- DOCUMENTOS PDF Module - DDL para PostgreSQL
-- Versión: 1.0
-- Fecha: 2026-04-19
-- ================================================================

-- Extensión para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================================================================
-- TABLA: documentos_pdf
-- ================================================================
CREATE TABLE documentos_pdf (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    socio_id UUID NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'GENERADO',
    nombre_archivo VARCHAR(255) NOT NULL,
    ruta_almacenamiento TEXT NOT NULL,
    hash_archivo VARCHAR(64) NOT NULL,
    firma_digital TEXT,
    tamano_bytes BIGINT NOT NULL,
    fecha_generacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_expiracion TIMESTAMP,
    generado_por VARCHAR(100) NOT NULL,
    clasificacion VARCHAR(20) NOT NULL DEFAULT 'CONFIDENCIAL',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT chk_tipo CHECK (tipo IN (
        'ESTADO_CUENTA',
        'CONSTANCIA_AFILIACION',
        'CONTRATO_ADHESION',
        'PAGARE',
        'TABLA_AMORTIZACION',
        'CARTA_BENEFICIARIOS'
    )),
    CONSTRAINT chk_estado CHECK (estado IN ('GENERADO','ALMACENADO','EXPIRADO','REVOCADO')),
    CONSTRAINT chk_clasificacion CHECK (clasificacion IN ('CONFIDENCIAL','RESTRINGIDO','PUBLICO')),
    
    -- Foreign Key
    CONSTRAINT fk_documentos_socio FOREIGN KEY (socio_id) 
        REFERENCES socios(id) 
        ON DELETE RESTRICT
);

-- Índices
CREATE INDEX idx_documentos_socio_id ON documentos_pdf (socio_id);
CREATE INDEX idx_documentos_tipo ON documentos_pdf (tipo);
CREATE INDEX idx_documentos_estado ON documentos_pdf (estado);
CREATE INDEX idx_documentos_fecha_gen ON documentos_pdf (fecha_generacion DESC);
CREATE INDEX idx_documentos_socio_tipo ON documentos_pdf (socio_id, tipo);
CREATE INDEX idx_documentos_hash ON documentos_pdf (hash_archivo);

-- ================================================================
-- TABLA: documentos_pdf_audit
-- ================================================================
CREATE TABLE documentos_pdf_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    documento_id UUID NOT NULL,
    entidad_tipo VARCHAR(50) NOT NULL DEFAULT 'DOCUMENTO',
    accion VARCHAR(30) NOT NULL,
    usuario_id VARCHAR(100) NOT NULL,
    usuario_rol VARCHAR(30) NOT NULL,
    ip_cliente VARCHAR(45) NOT NULL,
    documento_hash VARCHAR(64) NOT NULL,
    resultado VARCHAR(20) NOT NULL,
    razon_fallo TEXT,
    fecha_evento TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_entidad_tipo_audit CHECK (entidad_tipo IN ('DOCUMENTO')),
    CONSTRAINT chk_accion_audit CHECK (accion IN ('GENERAR','DESCARGAR','REVOCAR','EXPIRAR')),
    CONSTRAINT chk_resultado_audit CHECK (resultado IN ('EXITOSO','FALLIDO'))
);

-- Índices de auditoría
CREATE INDEX idx_doc_audit_documento_id ON documentos_pdf_audit (documento_id);
CREATE INDEX idx_doc_audit_usuario_id ON documentos_pdf_audit (usuario_id, fecha_evento DESC);
CREATE INDEX idx_doc_audit_fecha ON documentos_pdf_audit (fecha_evento DESC);

-- ================================================================
-- TRIGGER: updated_at para documentos_pdf
-- ================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_documentos_pdf_updated_at
    BEFORE UPDATE ON documentos_pdf
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ================================================================
-- FUNCIÓN: Marcar documentos expirados
-- ================================================================
CREATE OR REPLACE FUNCTION marcar_documentos_expirados()
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER;
BEGIN
    UPDATE documentos_pdf 
    SET estado = 'EXPIRADO', updated_at = NOW()
    WHERE estado = 'ALMACENADO' 
      AND fecha_expiracion IS NOT NULL 
      AND fecha_expiracion < NOW();
    
    GET DIAGNOSTICS v_count = ROW_COUNT;
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;

-- ================================================================
-- FUNCIÓN: Validar que documento pertenece a socio
-- ================================================================
CREATE OR REPLACE FUNCTION validar_documento_socio(p_documento_id UUID, p_socio_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    v_socio_id UUID;
BEGIN
    SELECT socio_id INTO v_socio_id
    FROM documentos_pdf
    WHERE id = p_documento_id;
    
    RETURN v_socio_id = p_socio_id;
END;
$$ LANGUAGE plpgsql;
```

---

## 5. Buckets MinIO

### 5.1 Arquitectura de Buckets Segregados

| Bucket | Propósito | Retención | Acceso |
|--------|-----------|-----------|--------|
| `bucket-documentos` | Estados de cuenta, constancias | 7 años | Pre-signed URL |
| `bucket-contratos` | Contratos de adhesión | Permanente | Pre-signed URL |
| `bucket-pagares` | Pagarés de crédito | 10 años | Pre-signed URL |
| `bucket-creditos` | Tablas de amortización | 10 años | Pre-signed URL |
| `bucket-temporal` | PDFs pendientes de escaneo | 24 horas | Interno |

### 5.2 Configuración de Buckets

```yaml
# application.yml
minio:
  buckets:
    documentos:
      name: bucket-documentos
      retention-years: 7
    contratos:
      name: bucket-contratos
      retention-years: permanent
    pagares:
      name: bucket-pagares
      retention-years: 10
    creditos:
      name: bucket-creditos
      retention-years: 10
    temporal:
      name: bucket-temporal
      retention-hours: 24

storage:
  presigned-url:
    expiration-minutes: 15
```

---

## 6. Políticas de Retención

| Tipo de Documento | Duración | Razón Legal |
|------------------|----------|--------------|
| Estados de cuenta | 7 años | SUDEBAN |
| Constancias de afiliación | 7 años | SUDEBAN |
| Contratos de adhesión | Permanente | Valor legal |
| Pagarés | 10 años | SUDEBAN |
| Tablas de amortización | 10 años | SUDEBAN |
| Carta de beneficiarios | 7 años post-eliminación socio | Regulación |
| Logs de auditoría | 7 años | Compliance |

---

## 7. Notas de Seguridad

| Código | Descripción |
|--------|-------------|
| S-1 | Hash SHA-256 calculado sobre el PDF final (post-watermark) |
| S-2 | Pre-signed URLs con TTL máximo de 15 minutos |
| S-3 | Firma digital RSA SHA-256 para contratos y pagarés |
| S-4 | ClamAV escanea antes de almacenamiento |
| S-5 | Buckets segregados por tipo de documento |
| S-6 | Auditoría inmutable (INSERT only) |

---

## 8. Validaciones a Nivel de Base de Datos

### 8.1 Hash SHA-256

```sql
-- Validar formato de hash
ALTER TABLE documentos_pdf 
ADD CONSTRAINT chk_hash_formato
CHECK (hash_archivo ~ '^SHA-256:[a-f0-9]{64}$');
```

### 8.2 Firma Digital para Contratos y Pagarés

```sql
-- La firma digital es obligatoria para CONTRATO y PAGARE
CREATE OR REPLACE FUNCTION validar_firma_digital()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.tipo IN ('CONTRATO_ADHESION', 'PAGARE') AND NEW.firma_digital IS NULL THEN
        RAISE EXCEPTION 'La firma digital es obligatoria para %', NEW.tipo;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_firma_digital
BEFORE INSERT OR UPDATE ON documentos_pdf
FOR EACH ROW
EXECUTE FUNCTION validar_firma_digital();
```

---

## 9. Migración Flyway

**Archivo:** `V2__create_documentos_pdf.sql`

**Ubicación:** `/backend/src/main/resources/db/migration/`

---

## 10. Historial de Versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-19 | @product-manager | Creación inicial del modelo de datos |
| 1.0 | 2026-04-19 | @auditoria | Revisión de seguridad |
| 1.0 | 2026-04-19 | @documentador | Documentación formal del modelo |

---

## 11. Referencias

- Especificación técnica: `/docs/modulos/documentospdf/SPEC.md`
- API: `/docs/modulos/documentospdf/API.md`
- Módulo KYC: `/docs/modulos/kyc/MODELO_DATOS.md`
- Módulo Socios: `/docs/modulos/socios/MODELO_DATOS.md`