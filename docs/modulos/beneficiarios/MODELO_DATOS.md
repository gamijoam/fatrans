# Módulo BENEFICIARIOS - Modelo de Datos

**Proyecto:** Plataforma Fondo de Ahorro  
**Versión:** 1.0  
**Fecha:** 2026-04-19

---

## Resumen

Este documento describe el modelo de datos del módulo BENEFICIARIOS, incluyendo las entidades, relaciones, DDL y consideraciones de seguridad.

---

## 1. Diagrama de Entidades

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           MODELO DE DATOS BENEFICIARIOS                     │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌──────────────────┐         ┌──────────────────────┐                    │
│   │     Socio        │         │   Beneficiario       │                    │
│   │   (módulo SOCIOS) │         │                       │                    │
│   │                   │         │  - id (PK)           │                    │
│   │  - id (PK)        │────────►│  - socioId (FK)      │                    │
│   │  - numeroSocio    │   1:N   │  - nombreCompleto     │                    │
│   │  - numeroDocumento │         │  - numeroDocumento   │                    │
│   │  - estado         │         │  - tipoDocumento     │                    │
│   │                   │         │  - parentesco        │                    │
│   └──────────────────┘         │  - porcentaje        │                    │
│                                │  - telefono          │                    │
│                                │  - activo            │                    │
│                                │  - fechaRegistro     │                    │
│                                │  - fechaActualizacion│                    │
│                                └──────────────────────┘                    │
│                                                                             │
│   ┌──────────────────────────┐                                               │
│   │ BeneficiarioAudit        │                                               │
│   │  (Shadow Table)          │                                               │
│   │                          │                                               │
│   │  - id (PK)               │                                               │
│   │  - entidadTipo           │                                               │
│   │  - entidadId             │                                               │
│   │  - accion                │                                               │
│   │  - usuarioId             │                                               │
│   │  - rolUsuario             │                                               │
│   │  - ipCliente             │                                               │
│   │  - datosAnteriores (JSONB)│                                              │
│   │  - datosNuevos (JSONB)   │                                               │
│   │  - fechaEvento           │                                               │
│   └──────────────────────────┘                                               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Entidades

### 2.1 Beneficiario

**Descripción:** Representa un beneficiario designado por un socio del Fondo de Ahorro.

| Campo | Tipo | Nullable | Descripción |
|-------|------|----------|-------------|
| id | UUID | No | PK |
| socio_id | UUID | No | FK hacia socios.id |
| nombre_completo | VARCHAR(200) | No | Nombre completo del beneficiario |
| numero_documento | VARCHAR(20) | No | Número de documento |
| tipo_documento | VARCHAR(30) | No | CEDULA_IDENTIDAD, RIF, PASAPORTE, CEDULA_EXTRANJERO |
| parentesco | VARCHAR(20) | No | CONYUGE, HIJO, PADRE, MADRE, HERMANO, ABUELO, NIETO, SOBRINO, TIO, OTRO |
| porcentaje | DECIMAL(5,2) | No | Porcentaje de asignación (0.01 - 100.00) |
| telefono | VARCHAR(20) | Sí | Teléfono de contacto |
| activo | BOOLEAN | No | Soft delete flag (default true) |
| fecha_registro | TIMESTAMP | No | Fecha de registro |
| fecha_actualizacion | TIMESTAMP | No | Última actualización |

**Índices:**
- `idx_beneficiario_socio_id` ON (socio_id)
- `idx_beneficiario_socio_activo` ON (socio_id, activo)
- `idx_beneficiario_numero_documento` ON (numero_documento)
- `idx_beneficiario_activo` ON (activo) WHERE activo = true

**Restricciones:**
- `chk_tipo_documento` CHECK (tipo_documento IN ('CEDULA_IDENTIDAD','RIF','PASAPORTE','CEDULA_EXTRANJERO'))
- `chk_parentesco` CHECK (parentesco IN ('CONYUGE','HIJO','PADRE','MADRE','HERMANO','ABUELO','NIETO','SOBRINO','TIO','OTRO'))
- `chk_porcentaje_rango` CHECK (porcentaje >= 0.01 AND porcentaje <= 100.00)

**Uniques:**
- `uq_beneficiario_documento_activo` ON (socio_id, tipo_documento, numero_documento) WHERE activo = true

---

## 3. Tablas de Auditoría

### 3.1 Tabla beneficiaries_audit (Shadow Table)

**Descripción:** Tabla de auditoría para compliance y tracking de cambios.

| Campo | Tipo | Nullable | Descripción |
|-------|------|----------|-------------|
| id | UUID | No | PK |
| entidad_tipo | VARCHAR(50) | No | Tipo de entidad ('BENEFICIARIO') |
| entidad_id | UUID | No | ID de la entidad afectada |
| accion | VARCHAR(30) | No | CREATE, UPDATE, DELETE |
| usuario_id | VARCHAR(100) | No | ID del usuario que realizó la acción |
| rol_usuario | VARCHAR(30) | No | Rol del usuario (SOCIO, ADMIN) |
| ip_cliente | VARCHAR(45) | No | IP del cliente |
| datos_anteriores | JSONB | Sí | Estado anterior del registro |
| datos_nuevos | JSONB | Sí | Estado nuevo del registro |
| fecha_evento | TIMESTAMP | No | Fecha y hora del evento |

**Índices:**
- `idx_audit_entidad` ON (entidad_tipo, entidad_id)
- `idx_audit_usuario` ON (usuario_id, fecha_evento DESC)
- `idx_audit_fecha` ON (fecha_evento DESC)

**Restricciones:**
- `chk_entidad_tipo` CHECK (entidad_tipo IN ('BENEFICIARIO'))
- `chk_accion` CHECK (accion IN ('CREATE', 'UPDATE', 'DELETE'))

---

## 4. DDL Completo

```sql
-- ================================================================
-- BENEFICIARIES Module - DDL para PostgreSQL
-- Versión: 1.0
-- Fecha: 2026-04-19
-- ================================================================

-- Extensión para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ================================================================
-- TABLA: beneficiaries
-- ================================================================
CREATE TABLE beneficiaries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    socio_id UUID NOT NULL,
    nombre_completo VARCHAR(200) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL,
    tipo_documento VARCHAR(30) NOT NULL,
    parentesco VARCHAR(20) NOT NULL,
    porcentaje DECIMAL(5,2) NOT NULL,
    telefono VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT chk_tipo_documento CHECK (tipo_documento IN ('CEDULA_IDENTIDAD', 'RIF', 'PASAPORTE', 'CEDULA_EXTRANJERO')),
    CONSTRAINT chk_parentesco CHECK (parentesco IN ('CONYUGE', 'HIJO', 'PADRE', 'MADRE', 'HERMANO', 'ABUELO', 'NIETO', 'SOBRINO', 'TIO', 'OTRO')),
    CONSTRAINT chk_porcentaje_rango CHECK (porcentaje >= 0.01 AND porcentaje <= 100.00),
    
    -- Foreign Key
    CONSTRAINT fk_beneficiaries_socio FOREIGN KEY (socio_id) 
        REFERENCES socios(id) 
        ON DELETE RESTRICT
);

-- Índices
CREATE INDEX idx_beneficiaries_socio_id ON beneficiaries (socio_id);
CREATE INDEX idx_beneficiaries_socio_activo ON beneficiaries (socio_id, activo);
CREATE INDEX idx_beneficiaries_numero_documento ON beneficiaries (numero_documento);
CREATE INDEX idx_beneficiaries_activo ON beneficiaries (activo) WHERE activo = true;

-- Unique constraint: No puede haber dos beneficiarios activos con el mismo documento
CREATE UNIQUE INDEX idx_beneficiaries_documento_activo 
ON beneficiaries (socio_id, tipo_documento, numero_documento) 
WHERE activo = true;

-- ================================================================
-- TABLA: beneficiaries_audit
-- ================================================================
CREATE TABLE beneficiaries_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entidad_tipo VARCHAR(50) NOT NULL,
    entidad_id UUID NOT NULL,
    accion VARCHAR(30) NOT NULL,
    usuario_id VARCHAR(100) NOT NULL,
    rol_usuario VARCHAR(30) NOT NULL,
    ip_cliente VARCHAR(45) NOT NULL,
    datos_anteriores JSONB,
    datos_nuevos JSONB,
    fecha_evento TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_entidad_tipo_audit CHECK (entidad_tipo IN ('BENEFICIARIO')),
    CONSTRAINT chk_accion_audit CHECK (accion IN ('CREATE', 'UPDATE', 'DELETE'))
);

-- Índices de auditoría
CREATE INDEX idx_audit_entidad_beneficiaries ON beneficiaries_audit (entidad_tipo, entidad_id);
CREATE INDEX idx_audit_usuario_beneficiaries ON beneficiaries_audit (usuario_id, fecha_evento DESC);
CREATE INDEX idx_audit_fecha_beneficiaries ON beneficiaries_audit (fecha_evento DESC);

-- ================================================================
-- TRIGGER: updated_at para beneficiaries
-- ================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_beneficiaries_updated_at
    BEFORE UPDATE ON beneficiaries
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ================================================================
-- FUNCIÓN: Validar suma de porcentajes = 100%
-- ================================================================
CREATE OR REPLACE FUNCTION validate_porcentajes_sum(p_socio_id UUID)
RETURNS BOOLEAN AS $$
DECLARE
    v_sum DECIMAL(5,2);
BEGIN
    SELECT COALESCE(SUM(porcentaje), 0) INTO v_sum
    FROM beneficiaries
    WHERE socio_id = p_socio_id AND activo = true;
    
    RETURN v_sum = 100.00;
END;
$$ LANGUAGE plpgsql;

-- ================================================================
-- FUNCIÓN: Contar beneficiarios activos
-- ================================================================
CREATE OR REPLACE FUNCTION count_active_beneficiaries(p_socio_id UUID)
RETURNS INTEGER AS $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM beneficiaries
    WHERE socio_id = p_socio_id AND activo = true;
    
    RETURN v_count;
END;
$$ LANGUAGE plpgsql;
```

---

## 5. Constraints de Seguridad

### 5.1 Validación de Porcentaje

```sql
-- El porcentaje debe estar entre 0.01 y 100.00
ALTER TABLE beneficiaries 
ADD CONSTRAINT chk_porcentaje_rango
CHECK (porcentaje >= 0.01 AND porcentaje <= 100.00);

-- Validación de formato de porcentaje (exactamente 2 decimales)
ALTER TABLE beneficiaries 
ADD CONSTRAINT chk_porcentaje_formato
CHECK (porcentaje = ROUND(porcentaje, 2));
```

### 5.2 Validación de Documento Único por Socio

```sql
-- No puede haber dos beneficiarios activos con el mismo tipo y número de documento
CREATE UNIQUE INDEX idx_unique_documento_activo
ON beneficiaries (socio_id, tipo_documento, numero_documento)
WHERE activo = true;
```

### 5.3 Validación de Documento Diferente al Titular

Esta validación se realiza a nivel de aplicación (no en BD) para poder consultar el documento del socio titular:

```java
// En CreateBeneficiarioUseCase
String numeroDocumentoSocio = socioQueryPort.getNumeroDocumentoById(socioId);
if (beneficiario.getNumeroDocumento().equals(numeroDocumentoSocio)) {
    throw new DocumentoIgualAlTitularException();
}
```

---

## 6. Políticas de Retención

| Tipo | Duración | Razón |
|------|----------|-------|
| Beneficiarios eliminados | 7 años después del soft delete | Regulación financiera |
| Logs de auditoría | 7 años | Compliance |
| Backups | 7 años (cold storage) | Recovery |

---

## 7. Notas de Seguridad

| Código | Descripción |
|--------|-------------|
| S-1 | Los logs de auditoría deben mantenerse por 7 años |
| S-2 | Validación IDOR enforced en todos los endpoints |
| S-3 | Rate limiting: 10 req/min por socio |
| S-4 | Soft delete obligatorio - nunca DELETE físico |

---

## 8. Reglas de Negocio Implementadas en Base de Datos

### 8.1 Trigger para Validar Suma de Porcentajes

```sql
CREATE OR REPLACE FUNCTION check_porcentaje_sum()
RETURNS TRIGGER AS $$
DECLARE
    v_sum DECIMAL(5,2);
    v_exclude_id UUID;
BEGIN
    -- Si es un UPDATE, excluimos el registro actual
    v_exclude_id := COALESCE(NEW.id, OLD.id);
    
    SELECT COALESCE(SUM(porcentaje), 0) INTO v_sum
    FROM beneficiaries
    WHERE socio_id = NEW.socio_id 
      AND activo = true
      AND id != v_exclude_id;
    
    -- Para INSERT, verificamos que la suma + nuevo porcentaje no exceda 100
    IF TG_OP = 'INSERT' AND v_sum + NEW.porcentaje > 100.00 THEN
        RAISE EXCEPTION 'La suma de porcentajes excedería 100%%. Suma actual: %', v_sum;
    END IF;
    
    -- Para UPDATE, verificamos que la nueva suma no exceda 100
    IF TG_OP = 'UPDATE' AND v_sum + NEW.porcentaje > 100.00 THEN
        RAISE EXCEPTION 'La suma de porcentajes excedería 100%%. Suma actual (sin este): %', v_sum;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_porcentaje_sum
BEFORE INSERT OR UPDATE ON beneficiaries
FOR EACH ROW
WHEN (NEW.activo = true)
EXECUTE FUNCTION check_porcentaje_sum();
```

---

## 9. Historial de Versiones

| Versión | Fecha | Autor | Descripción |
|---------|-------|-------|-------------|
| 1.0 | 2026-04-19 | @product-manager | Creación inicial del modelo de datos |
| 1.0 | 2026-04-19 | @documentador | Documentación formal del modelo |

---

## 10. Referencias

- Especificación técnica: `/docs/modulos/beneficiarios/SPEC.md`
- API: `/docs/modulos/beneficiarios/API.md`
- Módulo Socios: `/docs/modulos/socios/MODELO_DATOS.md`