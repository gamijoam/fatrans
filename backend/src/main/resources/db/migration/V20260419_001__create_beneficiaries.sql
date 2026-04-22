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
-- COMENTARIOS
-- ================================================================
COMMENT ON TABLE beneficiaries IS 'Tabla de beneficiarios de socios del Fondo de Ahorro';
COMMENT ON TABLE beneficiaries_audit IS 'Tabla de auditoría para tracking de cambios en beneficiarios';
COMMENT ON COLUMN beneficiaries.socio_id IS 'FK hacia socios.id';
COMMENT ON COLUMN beneficiaries.porcentaje IS 'Porcentaje de asignación (0.01 - 100.00)';
COMMENT ON COLUMN beneficiaries.activo IS 'Flag para soft delete';
COMMENT ON COLUMN beneficiaries.telefono IS 'Teléfono de contacto (opcional)';