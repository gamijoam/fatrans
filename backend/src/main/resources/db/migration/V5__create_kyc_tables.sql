-- ================================================================
-- KYC Simplificado - DDL para PostgreSQL
-- Versión: 1.0
-- Fecha: 2026-04-14
-- Módulo: KYC
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
-- TABLA: audit_kyc (Shadow Table para compliance LOPDP/SUDEBAN)
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
    fecha_evento TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_entidad_tipo CHECK (entidad_tipo IN ('VERIFICACION', 'DOCUMENTO', 'CONSENTIMIENTO')),
    CONSTRAINT chk_accion CHECK (accion IN ('CREATE', 'UPDATE', 'DELETE', 'CONSULT', 'LOGIN', 'LOGOUT'))
);

CREATE INDEX idx_audit_entidad ON audit_kyc (entidad_tipo, entidad_id);
CREATE INDEX idx_audit_usuario ON audit_kyc (usuario_id, fecha_evento DESC);
CREATE INDEX idx_audit_fecha ON audit_kyc (fecha_evento DESC);

-- ================================================================
-- TRIGGER: updated_at automático para verificacion_kyc
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

-- ================================================================
-- Índices adicionales para performance
-- ================================================================

-- Búsqueda de verificaciones pendientes de revisión (FIFO)
CREATE INDEX idx_cola_revision_fifo
ON verificacion_kyc (estado, fecha_inicio ASC)
WHERE estado = 'EN_REVISION';

-- Búsqueda de KYC próximos a expirar
CREATE INDEX idx_kyc_por_expirar
ON verificacion_kyc (fecha_expiracion)
WHERE estado = 'APROBADO'
AND fecha_expiracion BETWEEN NOW() AND NOW() + INTERVAL '30 days';

-- Solo una verificación activa por socio
CREATE UNIQUE INDEX idx_socio_kyc_activo
ON verificacion_kyc (socio_id)
WHERE estado IN ('PENDIENTE', 'EN_REVISION', 'APROBADO');
