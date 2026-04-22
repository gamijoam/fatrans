-- ================================================================
-- V6__create_documentos_pdf.sql
-- Módulo de Documentos PDF - DDL para PostgreSQL
-- Versión: 1.0
-- Fecha: 2026-04-19
-- Autor: Documentos PDF Module Team
-- ================================================================

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
    hash_archivo VARCHAR(71) NOT NULL,
    firma_digital TEXT,
    tamano_bytes BIGINT NOT NULL,
    fecha_generacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_expiracion TIMESTAMP,
    generado_por VARCHAR(100) NOT NULL,
    clasificacion VARCHAR(20) NOT NULL DEFAULT 'CONFIDENCIAL',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    -- Constraints de validación
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

    -- Foreign Key hacia socios
    CONSTRAINT fk_documentos_socio FOREIGN KEY (socio_id)
        REFERENCES socios(id)
        ON DELETE RESTRICT
);

-- ================================================================
-- TABLA: documentos_pdf_audit (Shadow Table)
-- ================================================================
CREATE TABLE documentos_pdf_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    documento_id UUID NOT NULL,
    entidad_tipo VARCHAR(50) NOT NULL DEFAULT 'DOCUMENTO',
    accion VARCHAR(30) NOT NULL,
    usuario_id VARCHAR(100) NOT NULL,
    usuario_rol VARCHAR(30) NOT NULL,
    ip_cliente VARCHAR(45) NOT NULL,
    documento_hash VARCHAR(71) NOT NULL,
    resultado VARCHAR(20) NOT NULL,
    razon_fallo TEXT,
    fecha_evento TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_entidad_tipo_audit CHECK (entidad_tipo IN ('DOCUMENTO')),
    CONSTRAINT chk_accion_audit CHECK (accion IN ('GENERAR','DESCARGAR','REVOCAR','EXPIRAR')),
    CONSTRAINT chk_resultado_audit CHECK (resultado IN ('EXITOSO','FALLIDO')),

    -- Foreign Key hacia documentos_pdf
    CONSTRAINT fk_doc_audit_documento FOREIGN KEY (documento_id)
        REFERENCES documentos_pdf(id)
        ON DELETE RESTRICT
);

-- ================================================================
-- ÍNDICES para documentos_pdf
-- ================================================================
CREATE INDEX idx_documentos_socio_id ON documentos_pdf (socio_id);
CREATE INDEX idx_documentos_tipo ON documentos_pdf (tipo);
CREATE INDEX idx_documentos_estado ON documentos_pdf (estado);
CREATE INDEX idx_documentos_fecha_gen ON documentos_pdf (fecha_generacion DESC);
CREATE INDEX idx_documentos_socio_tipo ON documentos_pdf (socio_id, tipo);
CREATE INDEX idx_documentos_hash ON documentos_pdf (hash_archivo);

-- ================================================================
-- ÍNDICES para documentos_pdf_audit
-- ================================================================
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
-- FUNCIÓN: Validar firma digital para CONTRATO y PAGARE
-- ================================================================
CREATE OR REPLACE FUNCTION validar_firma_digital_documento()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.tipo IN ('CONTRATO_ADHESION', 'PAGARE') AND (NEW.firma_digital IS NULL OR NEW.firma_digital = '') THEN
        RAISE EXCEPTION 'La firma digital es obligatoria para %', NEW.tipo;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_firma_digital_documento
    BEFORE INSERT OR UPDATE ON documentos_pdf
    FOR EACH ROW
    EXECUTE FUNCTION validar_firma_digital_documento();

-- ================================================================
-- FUNCIÓN: Validar que documento pertenece a socio (IDOR)
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

-- ================================================================
-- COMENTARIOS
-- ================================================================
COMMENT ON TABLE documentos_pdf IS 'Almacena documentos PDF generados por el sistema';
COMMENT ON TABLE documentos_pdf_audit IS 'Auditoría de operaciones sobre documentos PDF';
COMMENT ON COLUMN documentos_pdf.hash_archivo IS 'SHA-256 del archivo PDF en formato SHA-256:xxxxx';
COMMENT ON COLUMN documentos_pdf.firma_digital IS 'Firma digital RSA-SHA256 para contratos y pagarés';
COMMENT ON COLUMN documentos_pdf.clasificacion IS 'Nivel de confidencialidad: CONFIDENCIAL, RESTRINGIDO, PUBLICO';
