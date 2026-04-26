-- V9__add_registro_venezuela_fields.sql
-- Agrega campos adicionales para registro completo Venezuela

ALTER TABLE solicitud_registro ADD COLUMN tipo_documento VARCHAR(20) NOT NULL DEFAULT 'CEDULA';
ALTER TABLE solicitud_registro ADD COLUMN fecha_nacimiento DATE NOT NULL;
ALTER TABLE solicitud_registro ADD COLUMN genero VARCHAR(20) NOT NULL;
ALTER TABLE solicitud_registro ADD COLUMN estado_civil VARCHAR(20) NOT NULL;
ALTER TABLE solicitud_registro ADD COLUMN rif_empresa VARCHAR(20);
ALTER TABLE solicitud_registro ADD COLUMN departamento VARCHAR(100);
ALTER TABLE solicitud_registro ADD COLUMN cargo VARCHAR(100);
ALTER TABLE solicitud_registro ADD COLUMN salario DECIMAL(18,2);
ALTER TABLE solicitud_registro ADD COLUMN direccion_estado VARCHAR(100);
ALTER TABLE solicitud_registro ADD COLUMN direccion_ciudad VARCHAR(100);
ALTER TABLE solicitud_registro ADD COLUMN direccion_municipio VARCHAR(100);
ALTER TABLE solicitud_registro ADD COLUMN direccion_calle VARCHAR(255);
ALTER TABLE solicitud_registro ADD COLUMN emergencia_nombre VARCHAR(200);
ALTER TABLE solicitud_registro ADD COLUMN emergencia_telefono VARCHAR(20);
ALTER TABLE solicitud_registro ADD COLUMN emergencia_parentesco VARCHAR(50);
ALTER TABLE solicitud_registro ADD COLUMN acepta_terminos BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE solicitud_registro ADD COLUMN acepta_lopdp BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_solicitud_tipo_documento ON solicitud_registro(tipo_documento);
CREATE INDEX idx_solicitud_fecha_nacimiento ON solicitud_registro(fecha_nacimiento);
CREATE INDEX idx_solicitud_genero ON solicitud_registro(genero);
CREATE INDEX idx_solicitud_rif_empresa ON solicitud_registro(rif_empresa);