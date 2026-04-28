-- V3__create_beneficiaries.sql

CREATE TABLE beneficiaries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    socio_id UUID NOT NULL REFERENCES socios(id),
    nombre_completo VARCHAR(200) NOT NULL,
    tipo_documento VARCHAR(30) NOT NULL,
    numero_documento VARCHAR(50) NOT NULL,
    parentesco VARCHAR(50) NOT NULL,
    porcentaje DECIMAL(5,2) NOT NULL,
    telefono VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    UNIQUE(socio_id, tipo_documento, numero_documento)
);

CREATE TABLE beneficiaries_audit (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entidad_tipo VARCHAR(50) NOT NULL,
    entidad_id UUID NOT NULL,
    accion VARCHAR(30) NOT NULL,
    usuario_id VARCHAR(100) NOT NULL,
    rol_usuario VARCHAR(30) NOT NULL,
    ip_cliente VARCHAR(45) NOT NULL,
    datos_anteriores JSONB,
    datos_nuevos JSONB,
    fecha_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_entidad_beneficiaries ON beneficiaries_audit(entidad_tipo, entidad_id);
CREATE INDEX idx_audit_usuario_beneficiaries ON beneficiaries_audit(usuario_id, fecha_evento);
CREATE INDEX idx_audit_fecha_beneficiaries ON beneficiaries_audit(fecha_evento);
