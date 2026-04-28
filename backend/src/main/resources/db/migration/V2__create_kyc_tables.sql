-- V2__create_kyc_tables.sql

CREATE TABLE verificacion_kyc (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    socio_id UUID NOT NULL REFERENCES socios(id),
    nivel_verificacion VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    score_confianza DECIMAL(5,2),
    fecha_inicio TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_finalizacion TIMESTAMP,
    revisado_por UUID,
    motivo_rechazo TEXT,
    fecha_expiracion DATE
);

CREATE TABLE documento_identidad (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    verificacion_id UUID NOT NULL REFERENCES verificacion_kyc(id),
    tipo_documento VARCHAR(50) NOT NULL,
    url_documento VARCHAR(500) NOT NULL,
    estado_validacion VARCHAR(20) NOT NULL,
    fecha_subida TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    metadata JSONB
);

CREATE TABLE consentimiento_kyc (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    socio_id UUID NOT NULL REFERENCES socios(id),
    tipo_consentimiento VARCHAR(100) NOT NULL,
    otorgado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_consentimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT
);

CREATE TABLE audit_kyc (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tipo_evento VARCHAR(50) NOT NULL,
    socio_id UUID NOT NULL,
    usuario_id VARCHAR(100) NOT NULL,
    rol_usuario VARCHAR(30),
    endpoint VARCHAR(500),
    metodo_http VARCHAR(10),
    ip_cliente VARCHAR(45),
    user_agent VARCHAR(500),
    verificacion_id UUID,
    documento_id UUID,
    descripcion VARCHAR(1000),
    datos_adicionales TEXT,
    estado_anterior VARCHAR(20),
    estado_nuevo VARCHAR(20),
    exitoso BOOLEAN NOT NULL,
    codigo_error VARCHAR(20),
    fecha_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_fecha ON audit_kyc(fecha_evento);
CREATE INDEX idx_audit_socio_id ON audit_kyc(socio_id);
CREATE INDEX idx_audit_tipo_evento ON audit_kyc(tipo_evento);
CREATE INDEX idx_audit_usuario ON audit_kyc(usuario_id);
