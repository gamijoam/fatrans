-- V7__create_unidades_transporte.sql

CREATE TABLE unidades_transporte (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    socio_id UUID NOT NULL,
    placa VARCHAR(20) UNIQUE NOT NULL,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    ano_vehiculo INT NOT NULL,
    tipo_unidad VARCHAR(20) NOT NULL,
    capacidad_pasajeros INT,
    soat_vencimiento DATE,
    seguro_vencimiento DATE,
    revision_tecnica_vencimiento DATE,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    CONSTRAINT fk_unidad_socio FOREIGN KEY (socio_id) REFERENCES socios(id) ON DELETE CASCADE
);

CREATE INDEX idx_unidades_transporte_socio_id ON unidades_transporte(socio_id);
CREATE INDEX idx_unidades_transporte_placa ON unidades_transporte(placa);
