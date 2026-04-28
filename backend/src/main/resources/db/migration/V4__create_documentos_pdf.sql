-- V4__create_documentos_pdf.sql

CREATE TABLE documentos_pdf (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    socio_id UUID NOT NULL REFERENCES socios(id),
    tipo_documento VARCHAR(50) NOT NULL,
    numero_documento VARCHAR(50) UNIQUE NOT NULL,
    url_documento VARCHAR(500),
    bucket VARCHAR(100),
    object_key VARCHAR(255),
    hash_sha256 VARCHAR(64),
    firma_digital BOOLEAN DEFAULT FALSE,
    fecha_firma TIMESTAMP,
    firmado_por UUID,
    estado VARCHAR(20) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE documentos_pdf_audit (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    documento_id UUID NOT NULL REFERENCES documentos_pdf(id),
    accion VARCHAR(20) NOT NULL,
    realizado_por UUID,
    fecha_accion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_origen VARCHAR(45)
);
